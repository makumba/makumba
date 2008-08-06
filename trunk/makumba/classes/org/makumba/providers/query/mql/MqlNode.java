package org.makumba.providers.query.mql;

import org.makumba.FieldDefinition;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NameResolver.TextList;

import antlr.CommonAST;
import antlr.collections.AST;

/** 
 * The root of Mql analysis tree nodes. It performs analysis as the tree is built.
 * It should know how to determine its Makumba type, if it is not a parameter. 
 * @author Cristian Bogdan
 * @version $Id: MqlNode.java,v 1.1 Aug 5, 2008 5:38:16 PM cristi Exp $
 */

public class MqlNode extends CommonAST {
    NameResolver.TextList text;
    
    MqlSqlWalker walker;

    private MqlNode father;

    private FieldDefinition makType;

    private String originalText;

    protected MqlSqlWalker getWalker() {
        return walker;
    }

    protected void setWalker(MqlSqlWalker walker) {
        this.walker = walker;
    }

    public MqlNode() {
    }

    /** we analyze the tree as it is built */
    public void setFather(MqlNode node) {
        father = node;
    }

    /** we analyze the tree as it is built, we call oneMoreChild() to see whether we are ready for analysis */
    @Override
    public void setFirstChild(AST a) {
        super.setFirstChild(a);
        if (a != null) {
            ((MqlNode) a).setFather(this);
            oneMoreChild((MqlNode) a);
        }
    }

    /** we analyze the tree as it is built, we call oneMoreChild() of the father to see whether it is ready for analysis */
    @Override
    public void setNextSibling(AST a) {
        super.setNextSibling(a);
        if (father != null)
            father.oneMoreChild((MqlNode) a);
    }

    protected void oneMoreChild(MqlNode child) {
        FieldDefinition tp = findMakType(child);
        if (tp != null)
            makType = tp;
    }

    protected FieldDefinition findMakType(MqlNode child) {
        switch (getType()) {
            // TODO: type of functions
            // if their args are parameters, set parameter types

            // TODO: type of ANY and ALL is the type of the (only) projection of the subquery
            case HqlSqlTokenTypes.AGGREGATE:
            case HqlSqlTokenTypes.UNARY_MINUS:
            case HqlSqlTokenTypes.UNARY_PLUS:
                return child.getMakType();
            case HqlSqlTokenTypes.CASE:
                // TODO: maybe WHEN, THEN or ELSE are parameters
                // set the WHEN parameter type to boolean, and THEN has the type of ELSE
                // if both THEN and ELSE are parameters, we're in trouble
                // TODO: this is untested
                if (child.getType() == HqlSqlTokenTypes.THEN)
                    return ((MqlNode) child.getFirstChild()).getMakType();
        }
        return null;
    };

    protected void setMakType(FieldDefinition fieldDefinition) {
        makType = fieldDefinition;
    }

    protected FieldDefinition getMakType() {
        return makType;
    }

    public void setText(String text) {
        super.setText(text);
        if (originalText == null && text.length()>0)
            originalText = text;
    }

    public void setType(int type) {
        super.setType(type);
        String def = knownType();
        if (def != null)
            setMakType(walker.currentContext.ddp.makeFieldDefinition("x", def));
    }

    String knownType() {

        // FIXME: this is not actually checking for correctness: e.g. AND should have 2 boolean operators, etc.
        // to fix that, the MqlBinaryOperator can be used / subclassed
        switch (getType()) {
            case HqlSqlTokenTypes.NUM_INT:
            case HqlSqlTokenTypes.NUM_LONG:
            case HqlSqlTokenTypes.COUNT:
                return "int";
            case HqlSqlTokenTypes.NUM_FLOAT:
            case HqlSqlTokenTypes.NUM_DOUBLE:
                return "real";
            case HqlSqlTokenTypes.FALSE:
            case HqlSqlTokenTypes.TRUE:
            case HqlSqlTokenTypes.EXISTS:
            case HqlSqlTokenTypes.NOT:
            case HqlSqlTokenTypes.AND:
            case HqlSqlTokenTypes.OR:
            case HqlSqlTokenTypes.IS_NULL:
            case HqlSqlTokenTypes.IS_NOT_NULL:
            case HqlSqlTokenTypes.IN:
                return "boolean";
            case HqlSqlTokenTypes.QUOTED_STRING:
                return "char[255]";
        }
        return null;
    }

    protected void checkForOperandType(MqlNode ast) {
        if (!ast.isParam() && ast.getMakType() == null)
            throw new IllegalStateException("No makumba type computed for " + walker.printer.showAsString(ast, ""));
    }

    boolean isParam() {
        return getType() == HqlSqlTokenTypes.NAMED_PARAM || getType() == HqlSqlTokenTypes.PARAM;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void writeTo(TextList t) {
        if(text==null)
            t.append(getText());
        else
            t.append(text);
    }

    public String getText(){
        if(text!=null)
            return text.toString();
        else 
            return super.getText();
    }

    public void setTextList(TextList tl) {
        text=tl;        
    }
}
