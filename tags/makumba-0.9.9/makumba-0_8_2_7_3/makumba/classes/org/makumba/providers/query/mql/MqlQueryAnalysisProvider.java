package org.makumba.providers.query.mql;

import java.util.List;

import org.hibernate.hql.antlr.HqlTokenTypes;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;

import antlr.collections.AST;

public class MqlQueryAnalysisProvider extends QueryAnalysisProvider {
    public static int parsedQueries = NamedResources.makeStaticCache("MQL parsed queries", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return new MqlQueryAnalysis((String) nm, true, true);
        }
    }, true);

    @Override
    public QueryAnalysis getRawQueryAnalysis(String query) {
        return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(query);
    }

    @Override
    public QueryAnalysis getRawQueryAnalysis(String query, String insertIn) {
        return (QueryAnalysis) NamedResources.getStaticCache(parsedQueries).getResource(MqlQueryAnalysis.formatQueryAndInsert(query, insertIn));
    }

    
    @Override
    public boolean selectGroupOrOrderAsLabels() {
        return false;
    }

    @Override
    public FieldDefinition getAlternativeField(DataDefinition dd, String fn) {
        if (fn.equals("id"))
            return dd.getFieldDefinition(dd.getIndexPointerFieldName());
        return null;
    }

    @Override
    public String getPrimaryKeyNotation(String label) {
        return label;
    }

    @Override
    public String getParameterSyntax() {
        return "$";
    }
    
    /** Transform OQL $x into :parameters, and record the parameter order */
    public static void transformOQLParameters(AST a, List<String> parameterOrder) {
        if (a == null)
            return;
        // MQL allows $some.param
        if(a.getType() == HqlTokenTypes.DOT && a.getFirstChild().getText().startsWith("$")) {
            a.setType(HqlTokenTypes.IDENT);
            a.setText(a.getFirstChild().getText() + "." + a.getFirstChild().getNextSibling().getText());
            a.setFirstChild(null);
        }
        if (a.getType() == HqlTokenTypes.IDENT && a.getText().startsWith("$") && a.getText().indexOf("###") < 0) {
            // replacement of $n with (: makumbaParam n)
            a.setType(HqlTokenTypes.COLON);
            AST para = new Node();
            para.setType(HqlTokenTypes.IDENT);
            try {
                para.setText(MqlQueryAnalysis.MAKUMBA_PARAM + (Integer.parseInt(a.getText().substring(1)) - 1));
            } catch (NumberFormatException e) {
                // we probably are in some query analysis, so we ignore
                para.setText(a.getText().substring(1));
            }
            parameterOrder.add(para.getText());

            // we append in the tree to the parameter name the parameter position, 
            // to be able to retrieve the position, and thus identify the parameter at type analysis
            para.setText(para.getText() + "###" + (parameterOrder.size() - 1));
            a.setFirstChild(para);
            a.setText(":");
        }else if (a.getType() == HqlTokenTypes.COLON && a.getFirstChild() != null
                && a.getFirstChild().getType() == HqlTokenTypes.IDENT){
            // we also accept : params though we might not know what to do with them later
            parameterOrder.add(a.getFirstChild().getText());

            // we append in the tree to the parameter name the parameter position, 
            // to be able to retrieve the position, and thus identify the parameter at type analysis
            // unless this is a "valid" : param (result of function inlining)
            if(a.getFirstChild().getText().indexOf("###") < 0 && !a.getFirstChild().getText().startsWith(MqlQueryAnalysis.MAKUMBA_PARAM)) {
                a.getFirstChild().setText(a.getFirstChild().getText() + "###" + (parameterOrder.size() - 1));
            }
        }
        if(a.getType()==HqlTokenTypes.SELECT_FROM){
           // first the SELECT part
           transformOQLParameters(a.getFirstChild().getNextSibling(), parameterOrder);
           // then the FROM part
           transformOQLParameters(a.getFirstChild(), parameterOrder);           
           // then the rest
           transformOQLParameters(a.getNextSibling(), parameterOrder);            

        }else{
            transformOQLParameters(a.getFirstChild(), parameterOrder);
            // we make sure we don't do "SELECT" again
            if(a.getType()!=HqlTokenTypes.FROM)
                transformOQLParameters(a.getNextSibling(), parameterOrder);
       }
    }

    /** Transform the tree so that various OQL notations are still accepted
     * replacement of = or <> NIL with IS (NOT) NULL
     * OQL puts a 0.0+ in front of any AVG() expression 
     * 
     * This method also does various subquery transformations which are not OQL-specific, to support:
     * size(), elements(), firstElement()... 
     * */
    public static void transformOQL(AST a) {
        if (a == null)
            return;
        if (a.getType() == HqlTokenTypes.EQ || a.getType() == HqlTokenTypes.NE) {
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
        } 
        else if (a.getType() == HqlTokenTypes.ELEMENTS) {
            makeSubquery(a, a.getFirstChild());
        } else if (a.getType() == HqlTokenTypes.METHOD_CALL && a.getFirstChild().getText().toLowerCase().equals("size")) {
            makeSelect(a, HqlTokenTypes.COUNT, "count");
        } else if (a.getType() == HqlTokenTypes.METHOD_CALL
                && a.getFirstChild().getText().toLowerCase().endsWith("element")) {
            makeSelect(a, HqlTokenTypes.AGGREGATE, a.getFirstChild().getText().substring(0, 3));
        }

        transformOQL(a.getFirstChild());
        transformOQL(a.getNextSibling());
    }

    private static void makeSelect(AST a, int type, String text) {
        makeSubquery(a, a.getFirstChild().getNextSibling().getFirstChild());
        AST from = a.getFirstChild().getFirstChild();
        from.setNextSibling(ASTUtil.makeNode(HqlTokenTypes.SELECT, "select"));
        from.getNextSibling().setFirstChild(ASTUtil.makeNode(type, text));
        from.getNextSibling().getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.IDENT, "makElementsLabel"));
    }
    
    private static void makeSubquery(AST a, AST type) {
        a.setType(HqlTokenTypes.QUERY);
        a.setFirstChild(ASTUtil.makeNode(HqlTokenTypes.SELECT_FROM, "select"));
        a.getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.FROM, "from"));
        a.getFirstChild().getFirstChild().setFirstChild(ASTUtil.makeNode(HqlTokenTypes.RANGE, "range"));
        a.getFirstChild().getFirstChild().getFirstChild().setFirstChild(type);
        type.setNextSibling(ASTUtil.makeNode(HqlTokenTypes.ALIAS, "makElementsLabel"));
    }



}
