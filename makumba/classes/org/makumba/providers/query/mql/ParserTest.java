package org.makumba.providers.query.mql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.hql.antlr.HqlTokenTypes;
import org.hibernate.hql.ast.HqlParser;
import org.hibernate.hql.ast.tree.Node;
import org.hibernate.hql.ast.util.ASTPrinter;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NameResolver;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.query.hql.HQLQueryAnalysisProvider;
import org.makumba.providers.query.hql.HqlAnalyzeWalker;
import org.makumba.providers.query.hql.MddObjectType;
import org.makumba.providers.query.oql.OQLQueryAnalysisProvider;
import org.makumba.providers.query.oql.QueryAST;

import antlr.collections.AST;

//import antlr.debug.misc.ASTFrame;

public class ParserTest {

    private static QueryAnalysisProvider qap = new HQLQueryAnalysisProvider();

    private static PrintWriter pw = new PrintWriter(System.out);

    private static ASTPrinter printerHql = new ASTPrinter(HqlTokenTypes.class);

    private static ASTPrinter printerHqlSql = new ASTPrinter(HqlSqlTokenTypes.class);

    private static NameResolver nr;
    static {
        String databaseProperties = "test/localhost_mysql_makumba.properties";

        Properties p = new Properties();
        try {
            p.load(org.makumba.commons.ClassResource.get(databaseProperties).openStream());
        } catch (Exception e) {
            throw new org.makumba.ConfigFileError(databaseProperties);
        }

        nr = new NameResolver(p);
    }

    public static void main(String[] argv) {

        int line = 1;
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader((InputStream) ClassResource.get(
                "org/makumba/providers/query/mql/queries.txt").getContent()));
            String query = null;
            while ((query = rd.readLine()) != null) {
                if (!query.trim().startsWith("#")) {
                    query = preProcess(query);
                    analyseQuery(line, query);
                }
                line++;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("analyzed " + line + " queries");

    }

    private static void transformOQL(AST a) {
        if (a == null)
            return;
        if (a.getType() == HqlTokenTypes.IDENT && a.getText().startsWith("$")) {
            a.setType(HqlTokenTypes.COLON);
            AST para = new Node();
            para.setType(HqlTokenTypes.IDENT);
            para.setText("makumbaParam" + a.getText().substring(1));
            a.setFirstChild(para);
            a.setText(":");
        } else 
            if (a.getType() == HqlTokenTypes.EQ || a.getType() == HqlTokenTypes.NE) {
                if (isNil(a.getFirstChild())) {
                    setNullTest(a);
                    a.setFirstChild(a.getFirstChild().getNextSibling());
                } else if (isNil(a.getFirstChild().getNextSibling())) {
                    setNullTest(a);
                    a.getFirstChild().setNextSibling(null);
                }

            }
       else
            if(a.getType()== HqlTokenTypes.AGGREGATE && a.getText().toLowerCase().equals("avg")){
                AST plus= new Node();
                plus.setType(HqlTokenTypes.PLUS);
                plus.setText("+");
                AST zero= new Node();
                zero.setType(HqlTokenTypes.NUM_DOUBLE);
                zero.setText("0.0");
                plus.setFirstChild(zero);
                zero.setNextSibling(a.getFirstChild());
                a.setFirstChild(plus);
            }
        
        transformOQL(a.getFirstChild());
        transformOQL(a.getNextSibling());
    }

    private static void setNullTest(AST a) {
        if (a.getType() == HqlTokenTypes.EQ) {
            a.setType(HqlTokenTypes.IS_NULL);
            a.setText("is null");
        } else {
            a.setType(HqlTokenTypes.IS_NOT_NULL);
            a.setText("is not null");
        }
    }

    private static boolean isNil(AST a) {
        return a.getType() == HqlTokenTypes.IDENT && a.getText().toUpperCase().equals("NIL");
    }

    static String cleanUp(String s, String toRemove) {
        StringBuffer ret = new StringBuffer();
        char lastNonSpace = 0;
        boolean prevSpace = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (toRemove.indexOf(c) == -1) {
                if (prevSpace) {
                    if (Character.isJavaIdentifierStart(c) && Character.isJavaIdentifierPart(lastNonSpace)) {
                        ret.append(' ');
                    }
                }
                lastNonSpace = c;
                ret.append(c);
                prevSpace = false;
                continue;
            }
            prevSpace = true;
        }
        return ret.toString();
    }

    private static void analyseQuery(int line, String query) {
        AST hql = null;
        AST hql_sql = null;
        boolean passedMql = false;
        MqlSqlWalker m = new MqlSqlWalker(nr);
        MqlSqlGenerator mg = new MqlSqlGenerator();
        Throwable thr=null;
        boolean printedError;
        String oql_sql=null;
        try {
            HqlParser parser = HqlParser.getInstance(query);
            parser.statement();
            if (parser.getParseErrorHandler().getErrorCount() > 0)
                parser.getParseErrorHandler().throwQueryException();
            hql = parser.getAST();

            transformOQL(hql);
            
            m.statement(hql);
            if (m.error == null){
                hql_sql = m.getAST();
                mg.statement(hql_sql);
            }
            oql_sql = ((QueryAST) OQLQueryAnalysisProvider.parseQueryFundamental(query)).writeInSQLQuery(nr).toLowerCase();
            if(m.error==null && mg.error==null){
                String mql_sql = mg.toString().toLowerCase();

                oql_sql = cleanUp(oql_sql, " ").replace('\"', '\'');
                mql_sql = cleanUp(mql_sql, " ");

                if (!oql_sql.equals(mql_sql) && !cleanUp(oql_sql, "()").equals(cleanUp(mql_sql, "()"))) {
                    System.out.println(line + ": " + query + "\n\t" + mql_sql + "\n\t" + oql_sql);
                }
            }
            HqlAnalyzeWalker walker = new HqlAnalyzeWalker();
            walker.setAllowLogicalExprInSelect(true);
            walker.setTypeComputer(new MddObjectType());
            walker.setDebug(query);
            walker.statement(hql);
        } catch (Throwable t) {
            if(m.error!=null || mg.error!=null){
                if(m.error!=null)
                    thr=m.error;
                if(mg.error!=null)
                    thr=mg.error;
                System.err.println(line + ": " + thr.getMessage() + " " + query);
                if(t!=null)
                    System.err.println(line + ": " + t.getMessage() + " " + query);
                if(oql_sql!=null)
                    System.out.println("OQL SQL: "+oql_sql);
                return;                
            }

            if ("survey".equals(t.getMessage()) // HQL analyzer fails stuff like FROM T t, t n
                    ||
            t.toString().indexOf("FROM expected") != -1 // FROM-less queries can't pass
                    ||
            t.toString().indexOf("In operand") != -1 // OQL has issues with set operands
                ||
           t.toString().indexOf("defined twice") != -1 // HQLAnalyzer is really strict
                ){
                System.err.println(line + ": " + t.getMessage() + " " + query);
                               
                return;
            }

            thr=t;
            System.err.println("Only outside MQL");
        }
        if(m.error!=null)
            thr=m.error;
        if(mg.error!=null)
            thr=mg.error;
        if(thr!=null){
            System.err.println(line + ": " + thr.getMessage() + " " + query);
            if(oql_sql!=null)
                System.out.println("OQL SQL: "+oql_sql);
            
            if (hql != null && hql_sql == null) {
                printerHql.showAst(hql, pw);
            }
            if (hql_sql != null) {
                printerHqlSql.showAst(hql_sql, pw);
            }

            thr.printStackTrace();
        }
    }

    // {new org.hibernate.hql.ast.util.ASTPrinter(HqlSqlTokenTypes.class).showAst(#f, new
    // java.io.PrintWriter(System.out)); }

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
