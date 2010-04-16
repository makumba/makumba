///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: FieldCursor.java 1707 2007-09-28 15:35:48Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.providers.query.mql;

import java.util.ArrayList;

import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NameResolver.TextList;
import org.makumba.providers.DataDefinitionProvider;

import antlr.CommonAST;
import antlr.SemanticException;
import antlr.collections.AST;

/**
 * The root of Mql analysis tree nodes. It performs analysis as the tree is built. It should know how to determine its
 * Makumba type, if it is not a parameter.
 * 
 * @author Cristian Bogdan
 * @version $Id: MqlNode.java,v 1.1 Aug 5, 2008 5:38:16 PM cristi Exp $
 */

public class MqlNode extends CommonAST {
    private static final long serialVersionUID = 1L;

    private NameResolver.TextList textList;

    MqlSqlWalker walker;

    private MqlNode father;

    private FieldDefinition makType;

    private String originalText;

    protected ArrayList<String> checkAsIds;

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

    public MqlNode getFather() {
        return father;
    }

    /** we analyze the tree as it is built, we call oneMoreChild() to see whether we are ready for analysis */
    @Override
    public void setFirstChild(AST a) {
        super.setFirstChild(a);
        if (a != null) {
            ((MqlNode) a).setFather(this);
            addCheckedIds((MqlNode) a);
            oneMoreChild((MqlNode) a);
        }
    }

    /** we analyze the tree as it is built, we call oneMoreChild() of the father to see whether it is ready for analysis */
    @Override
    public void setNextSibling(AST a) {
        super.setNextSibling(a);
        if (father != null) {
            father.addCheckedIds((MqlNode) a);
            father.oneMoreChild((MqlNode) a);
        }
    }

    private void addCheckedIds(MqlNode child) {
        if (child != null && child.checkAsIds != null) {
            if (getType() == HqlSqlTokenTypes.WHERE) {
                // there are AS labels used in the WHERE section, we cannot allow that
                walker.error = new SemanticException("cannot use AS identifiers in WHERE: " + child.checkAsIds, "",
                        getLine(), getColumn());
            } else if (checkAsIds != null) {
                checkAsIds.addAll(child.checkAsIds);
            } else {
                checkAsIds = child.checkAsIds;
            }
        }
    }

    protected void oneMoreChild(MqlNode child) {
        if (walker.error != null || child == null) {
            return;
        }
        FieldDefinition tp = findMakType(child);
        if (tp != null) {
            makType = tp;
        }
        if (getType() == HqlSqlTokenTypes.NOT && child.isParam()) {
            child.setMakType(makeBooleanFieldDefinition());
        }
        if ((getType() == HqlSqlTokenTypes.IN || getType() == HqlSqlTokenTypes.NOT_IN)
                && child.getType() == HqlSqlTokenTypes.IN_LIST) {
            // getFirstChild() is the left side of the IN expression, child.getFirstChild() is the right side, where we
            // expect a parameter list
            MqlNode leftHandSide = (MqlNode) getFirstChild();
            MqlNode inListMember = (MqlNode) child.getFirstChild();

            // for processing the left operand, we assume either that the list (right operand)
            // has one member or that all members are of the same type
            boolean leftParam = checkParam(inListMember, leftHandSide);
            if (!leftParam) {
                try {
                    checkAndRewriteOperand(inListMember, leftHandSide);
                } catch (SemanticException e) {
                    walker.error = e;
                    return;
                }
            }
            // processing of the right operand, we look for parameters or strings to rewrite
            do {
                if (checkParam(leftHandSide, inListMember)) {
                    if (leftParam) {
                        walker.error = new SemanticException("cannot have paramters on both sides of IN", "",
                                getLine(), getColumn());
                        return;
                    } else {
                        try {
                            checkAndRewriteOperand(leftHandSide, inListMember);
                        } catch (SemanticException e) {
                            walker.error = e;
                            return;
                        }
                    }
                }
                inListMember = (MqlNode) inListMember.getNextSibling();
            } while (inListMember != null);
        }
    }

    protected FieldDefinition findMakType(MqlNode child) {
        switch (getType()) {
            // TODO: type of functions
            // if their args are parameters, set parameter types

            // type of ANY, ALL, SOME is the type of the (only) projection of the subquery
            case HqlSqlTokenTypes.ANY:
            case HqlSqlTokenTypes.ALL:
            case HqlSqlTokenTypes.SOME:
                return child.getMakType();
            case HqlSqlTokenTypes.SELECT:
                if (child.getType() == HqlSqlTokenTypes.SELECT_CLAUSE) {
                    return child.getMakType();
                }
                return null;
            case HqlSqlTokenTypes.SELECT_CLAUSE:
                if (makType != null) {
                    // if SELECT_CLAUSE already has children, we have more projections, so for now we set the type back
                    // to null
                    return makType;
                }
                // otherwise this child is the first projection and we set the type to it
                return child.getMakType();
            case HqlSqlTokenTypes.METHOD_CALL:
                return getFunctionType(child);
            case HqlSqlTokenTypes.AGGREGATE:
            case HqlSqlTokenTypes.UNARY_MINUS:
            case HqlSqlTokenTypes.UNARY_PLUS:
                if (child.isParam()) {
                    child.setMakType(DataDefinitionProvider.getInstance().makeFieldDefinition("x", "int"));
                }
                return child.getMakType();
            case HqlSqlTokenTypes.WHEN:
                // TODO: maybe WHEN, THEN or ELSE are parameters
                // set the WHEN parameter type to boolean, and THEN has the type of ELSE
                // if both THEN and ELSE are parameters, we're in trouble
                if (getFirstChild().getNextSibling() != null) {
                    if (father != null) {
                        father.makType = child.makType; // propagate the type of the child (the THEN) to the CASE node
                        return child.makType; 
                    }
                }
                break;

        }
        return null;
    };

    /**
     * Computes the type of function, based on their path. Note that MDD functions are inlined so their type doesn't
     * need to be computed. For actor functions, computation happens after the complete sub-tree is built, in the
     * grammar.
     */
    private FieldDefinition getFunctionType(MqlNode child) {
        String type = null;
        String name = child.getText();
        MQLFunctionDefinition functionDef = MQLFunctionRegistry.findMQLFunction(name);
        if (functionDef != null) {
            type = functionDef.getReturnType();
        }

        if (type != null) {
            child.setType(HqlSqlTokenTypes.METHOD_NAME);
            return DataDefinitionProvider.getInstance().makeFieldDefinition("x", type);
        }
        return null;
    }

    public void setMakType(FieldDefinition fd) {
        if (fd.getType().equals("ptrIndex")) {
            fd = DataDefinitionProvider.getInstance().makeFieldDefinition("x", "ptr " + fd.getPointedType().getName());
        }
        makType = fd;
    }

    public FieldDefinition getMakType() {
        return makType;
    }

    @Override
    public void setText(String text) {
        super.setText(text);
        if (originalText == null && text.length() > 0) {
            originalText = text;
        }
    }

    @Override
    public void setType(int type) {
        super.setType(type);
        String def = knownType();
        if (def != null) {
            setMakType(DataDefinitionProvider.getInstance().makeFieldDefinition("x", def));
        }
    }

    @Override
    public void initialize(AST t) {
        super.initialize(t);
        if (t instanceof MqlNode) {
            MqlNode n = (MqlNode) t;
            makType = n.makType;
            father = n.father;
            originalText = n.originalText;
            textList = n.textList;
            checkAsIds = n.checkAsIds;
            walker = n.walker;
        }
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
            case HqlSqlTokenTypes.IS_NULL:
            case HqlSqlTokenTypes.IS_NOT_NULL:
            case HqlSqlTokenTypes.IN:
            case HqlSqlTokenTypes.NOT_IN:
                return "boolean";
            case HqlSqlTokenTypes.QUOTED_STRING:
                return "char[255]";
        }
        return null;
    }

    protected void checkForOperandType(MqlNode ast) {
        if (!ast.isParam() && ast.getMakType() == null) {
            throw new IllegalStateException("No makumba type computed for " + MqlQueryAnalysis.showAst(ast));
        }
    }

    boolean isParam() {
        return getType() == HqlSqlTokenTypes.NAMED_PARAM || getType() == HqlSqlTokenTypes.PARAM;
    }

    /**
     * whether this is a custom MDD function call
     */
    boolean isFunctionCall() {
        return getType() == HqlSqlTokenTypes.METHOD_CALL && getText().startsWith("methodCallPlaceholder_");
    }

    void checkOperandTypes(MqlNode left, MqlNode right) throws SemanticException {
        checkForOperandType(left);
        checkForOperandType(right);

        if (!(left.isParam() && left.getMakType() == null) // 
                && !right.getMakType().isAssignableFrom(left.getMakType()) //
                && !(right.getMakType().isNumberType() && left.getMakType().isNumberType()) //
                && !(right.getMakType().isDateType() && left.getMakType().isDateType())) {
            throw new SemanticException("incompatible operands " + left.getText() + "("
                    + toStringType(left.getMakType()) + ") and " + right.getText() + " ("
                    + toStringType(right.getMakType()) + ")", "", getLine(), getColumn());
        }
    }

    private static String toStringType(FieldDefinition makType) {
        String s = makType.toString();
        if (!s.equals("ptr")) {
            return s;
        }
        return s + " " + makType.getPointedType().getName();
    }

    public String getOriginalText() {
        return originalText;
    }

    public void writeTo(TextList t) {
        if (textList == null) {
            t.append(getText());
        } else {
            t.append(textList);
        }
    }

    @Override
    public String toString() {
        if (textList != null) {
            return textList.toString();
        } else {
            return super.getText();
        }
    }

    public void setTextList(TextList tl) {
        textList = tl;
    }

    /** Symmetrically check both sides for operands that need to be rewritten */
    protected boolean checkAndRewriteOperand(MqlNode left, MqlNode right) throws SemanticException {
        if (right.getType() == HqlSqlTokenTypes.QUOTED_STRING && !left.isParam()) {
            return checkAndRewrite(left, right);
        } else if (left.getType() == HqlSqlTokenTypes.QUOTED_STRING && !right.isParam()) {
            return checkAndRewrite(right, left);
        } else {
            checkOperandTypes(left, right);
            return false;
        }
    }

    /** this method does the actual operand rewriting, called by {@link #checkAndRewriteOperand(MqlNode, MqlNode)} */
    private boolean checkAndRewrite(MqlNode left, MqlNode right) throws SemanticException {
        if (right.getText().startsWith("methodCallPlaceholder_")) {
            return true;
        }

        String s = right.getText();
        String arg1 = s.substring(1, s.length() - 1);
        Object o = null;
        try {
            o = (left.getMakType()).checkValue(arg1);
        } catch (org.makumba.InvalidValueException e) {
            // walker.printer.showAst(right, walker.pw);
            throw new SemanticException(e.getMessage(), "", getLine(), getColumn());
        }
        if (o instanceof Pointer) {
            o = new Long(((Pointer) o).longValue());
        }
        if (o instanceof Number) {
            right.setText(o.toString());
            // FIXME: also adapt the type of the node to identifier? seems to be needed on repeated calls
            // otherwise, we get a StringIndexOutOfBoundsException on:
            // String arg1 = s.substring(1, s.length() - 1);
            right.setType(HqlSqlTokenTypes.IDENT);
        } else {
            right.setText("\'" + o + "\'");
        }
        return true;
    }

    protected boolean checkParam(MqlNode left, MqlNode right) {
        if (right.isParam()) {
            if (left.isParam()) {
                walker.error = new SemanticException("can't have two parameters in a non-logical binary operator", "",
                        getLine(), getColumn());
                return true;
            } else {
                walker.setParameterType(right, left.getMakType());
                return true;
            }
        }
        return false;
    }

    protected FieldDefinition makeBooleanFieldDefinition() {
        return DataDefinitionProvider.getInstance().makeFieldDefinition("x", "boolean");
    }

}
