package org.makumba.db.hibernate.hql;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import antlr.RecognitionException;
import antlr.SemanticException;
import antlr.collections.AST;
/**
 * @author Christian Bogdan
 * @author Manuel Gay
 * @version $id
 */

public class HqlAnalyzeWalker extends HqlAnalyzeBaseWalker {

    private List result;

    void setAliasType(AST alias, String type) throws antlr.RecognitionException {
        if (aliasTypes.get(alias.getText()) != null)
            throw new antlr.SemanticException("alias " + alias.getText() + " defined twice");

        if (typeComputer.determineType(type, null) != null)
            aliasTypes.put(alias.getText(), type);
        else {
            // we have a from section in the form a.b.c that is not a type!
            // a must be a recognizable label
            int dot = type.indexOf('.');
            if (dot == -1)
                throw new SemanticException("unknown OQL type: " + type);
            String label = type.substring(0, dot);
            String labelType = (String) aliasTypes.get(label);
            if (labelType == null)
                throw new SemanticException("unknown OQL label/alias: " + label);
            while (true) {
                int dot1 = type.indexOf('.', dot + 1);
                String field;
                if (dot1 == -1)
                    field = type.substring(dot + 1);
                else
                    field = type.substring(dot + 1, dot1);
                // System.out.println(field);
                Object tp = typeComputer.determineType(labelType, field);
                if (!(tp instanceof String))
                    throw new SemanticException("composite type expected in FROM expression " + type + ". " + field
                            + " is a non-composite field of type " + labelType);
                labelType = (String) tp;
                dot = dot1;
                if (dot1 == -1)
                    break;
            }
            aliasTypes.put(alias.getText(), labelType);
        }
        // System.out.println(alias.getText()+" "+aliasTypes.get(alias.getText()));
    }

    AST deriveParamExpr(AST pe) throws SemanticException {

        return new ParamTypeAST(ExprTypeAST.PARAMETER);
    }

    AST deriveArithmethicExpr(AST ae) throws SemanticException {
        String operator = ae.getText();
        ExprTypeAST firstValue = null;
        ExprTypeAST secondValue = null;
        try {
            firstValue = (ExprTypeAST) ae.getFirstChild();
            secondValue = (ExprTypeAST) firstValue.getNextSibling();
        } catch (ClassCastException e) {
            // this may occur if the expression is neither of type ExprTypeAST nor ObjectTypeAST (ie, it is a CommonAST)
            throw new SemanticException("Cannot perform operation " + operator + " on elements "
                    + ae.getFirstChild().getType() + " and " + ae.getFirstChild().getNextSibling().getType());
        }

        // if the expr are not litterals
        if (firstValue.getObjectType() != null || secondValue.getObjectType() != null) {
            throw new SemanticException("Cannot perform operation " + operator + " on elements " + firstValue.getText()
                    + " and " + secondValue.getText());
        }
        if (firstValue.getDataType() == ExprTypeAST.PARAMETER || secondValue.getDataType() == ExprTypeAST.PARAMETER)
            return new ExprTypeAST(ExprTypeAST.PARAMETER);

        return new ExprTypeAST(firstValue.getDataType() > secondValue.getDataType() ? firstValue.getDataType()
                : secondValue.getDataType());
    }

    protected void setAlias(AST se, AST i) {
        if (se instanceof ExprTypeAST) {
            ((ExprTypeAST) se).setIdentifier(i.getText());
        }
    }

    /** this method is called when the SELECT section (projections) is parsed */
    void getReturnTypes(AST r) throws RecognitionException {
        result = new ArrayList();
        for (AST a = r.getFirstChild(); a != null; a = a.getNextSibling()) {
            result.add(a);
        }
    }

    public Map getLabelTypes() {
        return aliasTypes;
    }

    public List getResult() {
        return result;
    }
}
