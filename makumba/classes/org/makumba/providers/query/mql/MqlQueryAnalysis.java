package org.makumba.providers.query.mql;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.hql.antlr.HqlTokenTypes;
import org.hibernate.hql.ast.HqlParser;
import org.hibernate.hql.ast.tree.Node;
import org.makumba.DataDefinition;
import org.makumba.commons.NameResolver;
import org.makumba.commons.RegExpUtils;
import org.makumba.commons.NameResolver.TextList;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;

import antlr.ANTLRException;
import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;

public class MqlQueryAnalysis implements QueryAnalysis {

    private static final String MAKUMBA_PARAM = "param";

    private String query;

    private List<String> parameterOrder = new ArrayList<String>();

    private DataDefinition proj;

    private boolean noFrom = false;

    private Hashtable<String, DataDefinition> labels;

    private Hashtable<String, String> aliases;

    private DataDefinition paramInfo;

    private TextList text;

    public MqlQueryAnalysis(String query) throws ANTLRException {
        this.query = query;
        query = preProcess(query);

        if (query.toLowerCase().indexOf("from") == -1) {
            noFrom = true;
            query += " FROM org.makumba.db.makumba.Catalog c";
        }

        HqlParser parser = HqlParser.getInstance(query);
        parser.statement();
        if (parser.getParseErrorHandler().getErrorCount() > 0)
            parser.getParseErrorHandler().throwQueryException();

        transformOQL(parser.getAST());

        String hqlDebug= MqlSqlWalker.printer.showAsString(parser.getAST(), "");

        MqlSqlWalker mqlAnalyzer = new MqlSqlWalker(DataDefinitionProvider.getInstance().getVirtualDataDefinition(
            "Parameters for " + query));
        try{
        mqlAnalyzer.statement(parser.getAST());
        }catch(ANTLRException e){
            doThrow(e, hqlDebug);
        }
        doThrow(mqlAnalyzer.error, hqlDebug);
        String mqlDebug= MqlSqlWalker.printer.showAsString(mqlAnalyzer.getAST(), "");
        
        labels = mqlAnalyzer.rootContext.labels;
        aliases = mqlAnalyzer.rootContext.aliases;
        paramInfo = mqlAnalyzer.paramInfo;
        proj = DataDefinitionProvider.getInstance().getVirtualDataDefinition("Projections for " + query);
        mqlAnalyzer.setProjectionTypes(proj);

        MqlSqlGenerator mg = new MqlSqlGenerator();
        try{
            mg.statement(mqlAnalyzer.getAST());
        }catch(Throwable e){
            doThrow(e, mqlDebug);
        }
        doThrow(mg.error, mqlDebug);

        text = mg.text;
    }

    private void doThrow(Throwable t, String debugTree) throws ANTLRException {
        if (t == null)
            return;
        System.err.println(query+" "+debugTree);
        if (t instanceof RuntimeException)
            throw (RuntimeException) t;
        if (t instanceof ANTLRException)
            throw (ANTLRException) t;
    }

    public String writeInSQLQuery(NameResolver nr) {
        // TODO: we can cache these SQL results by the key of the NameResolver
        // still we should first check if this is needed, maybe the generated SQL (or processing of it)
        // is cached already somewhere else
        String sql = text.toString(nr);
        if (noFrom)
            return sql.substring(0, sql.toLowerCase().indexOf("from")).trim();
        return sql;
    }

    public String getQuery() {
        return query;
    }

    public DataDefinition getLabelType(String labelName) {
        String s1 = (String) aliases.get(labelName);
        if (s1 != null)
            labelName = s1;
        return (DataDefinition) labels.get(labelName);
    }

    public Map<String, DataDefinition> getLabelTypes() {
        return labels;
    }

    public DataDefinition getParameterTypes() {
        return paramInfo;
    }

    public DataDefinition getProjectionType() {
        return proj;
    }

    public int parameterAt(int index) {
        String s = parameterOrder.get(index);
        if (!s.startsWith(MAKUMBA_PARAM))
            throw new IllegalArgumentException("parameter at " + index + " is not a $n parameter");
        s = s.substring(MAKUMBA_PARAM.length());
        return Integer.parseInt(s) + 1;
    }

    public int parameterNumber() {
        return parameterOrder.size();
    }

    void transformOQL(AST a) {
        if (a == null)
            return;
        // FIXME: we take advantage of this depth-first traversal of the HQL pass 1 tree to
        // set the parameter order. However, the HQL pass 1 tree does not respect query order because
        // the FROM section comes before the SELECT (to ease pass 2 analysis).
        // if there are parameters in the FROM section (like in subqueries or so), their order will
        // not be correctly set by this code.
        // The solution is to do a separate traversal just for parameter order, making sure SELECT is
        // traversed before FROM, and then the rest of the tree
        if (a.getType() == HqlTokenTypes.IDENT && a.getText().startsWith("$")) {
            // replacement of $n with (: makumbaParam n)
            a.setType(HqlTokenTypes.COLON);
            AST para = new Node();
            para.setType(HqlTokenTypes.IDENT);
            para.setText(MAKUMBA_PARAM + (Integer.parseInt(a.getText().substring(1)) - 1));
            parameterOrder.add(para.getText());
            a.setFirstChild(para);
            a.setText(":");
        } else if (a.getType() == HqlTokenTypes.EQ || a.getType() == HqlTokenTypes.NE) {
            // replacement of = or <> NIL with IS (NOT) NULL
            if (MqlQueryAnalysis.isNil(a.getFirstChild())) {
                MqlQueryAnalysis.setNullTest(a);
                a.setFirstChild(a.getFirstChild().getNextSibling());
            } else if (MqlQueryAnalysis.isNil(a.getFirstChild().getNextSibling())) {
                MqlQueryAnalysis.setNullTest(a);
                a.getFirstChild().setNextSibling(null);
            }

        } else if (a.getType() == HqlTokenTypes.AGGREGATE && a.getText().toLowerCase().equals("avg")) {
            // OQL puts a 0.0+ in front of any AVG() expression probably to force the result to be floating point
            AST plus = new Node();
            plus.setType(HqlTokenTypes.PLUS);
            plus.setText("+");
            AST zero = new Node();
            zero.setType(HqlTokenTypes.NUM_DOUBLE);
            zero.setText("0.0");
            plus.setFirstChild(zero);
            zero.setNextSibling(a.getFirstChild());
            a.setFirstChild(plus);
        } else if (a.getType() == HqlTokenTypes.COLON && a.getFirstChild() != null
                && a.getFirstChild().getType() == HqlTokenTypes.IDENT)
            // we also accept : params though we might not know what to do with them later
            parameterOrder.add(a.getFirstChild().getText());

        transformOQL(a.getFirstChild());
        transformOQL(a.getNextSibling());
    }

    static boolean isNil(AST a) {
        return a.getType() == HqlTokenTypes.IDENT && a.getText().toUpperCase().equals("NIL");
    }

    static void setNullTest(AST a) {
        if (a.getType() == HqlTokenTypes.EQ) {
            a.setType(HqlTokenTypes.IS_NULL);
            a.setText("is null");
        } else {
            a.setType(HqlTokenTypes.IS_NOT_NULL);
            a.setText("is not null");
        }
    }

    public static final String regExpInSET = "in" + RegExpUtils.minOneWhitespace + "set" + RegExpUtils.whitespace
            + "\\(";

    public static final Pattern patternInSet = Pattern.compile(regExpInSET);

    public static String preProcess(String query) {
        // replace -> (subset separators) with __
        query = query.replaceAll("->", "__");

        // replace IN SET with IN.
        Matcher m = patternInSet.matcher(query.toLowerCase()); // find all occurrences of lower-case "in set"
        while (m.find()) {
            int start = m.start();
            int beginSet = m.group().indexOf("set"); // find location of "set" keyword
            // System.out.println(query);
            // composing query by concatenating the part before "set", 3 space and the part after "set"
            query = query.substring(0, start + beginSet) + "   " + query.substring(start + beginSet + 3);
            // System.out.println(query);
            // System.out.println();
        }
        query = query.replaceAll("IN SET", "IN    ");
        return query;
    }
}
