package org.makumba.db.hibernate.hql;

import java.util.ArrayList;
import java.util.HashMap;
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
    
    AST deriveLogicalExpr(AST le) {
        //FIXME check if the IN operands are of the same type
        return new ExprTypeAST(ExprTypeAST.INT);
    }
    
    AST deriveFunctionCallExpr(AST fc, AST e) {
        
        String functionCall = fc.getText().toUpperCase();
        //String arguments = e.getFirstChild().getClass().getName();
        //System.out.println("FC: "+functionCall+"args: "+arguments);
        
        /* determining the type returned by the method */
        if(functionCall.equals("COS")
                || functionCall.equals("COSH")
                || functionCall.equals("EXP")
                || functionCall.equals("LN")
                || functionCall.equals("LOG")
                || functionCall.equals("SIN")
                || functionCall.equals("SINH")
                || functionCall.equals("SQRT")
                || functionCall.equals("TAN")
                || functionCall.equals("TANH")
                || functionCall.equals("ACOS")
                || functionCall.equals("ASIN")
                || functionCall.equals("ATAN")) {
            return new ExprTypeAST(ExprTypeAST.DOUBLE);
        }
        if(functionCall.equals("CHR")
                || functionCall.equals("CONCAT")
                || functionCall.equals("INITCAP")
                || functionCall.equals("LOWER")
                || functionCall.equals("LPAD")
                || functionCall.equals("LTRIM")
                || functionCall.equals("NLS_INITCAP")
                || functionCall.equals("NLS_LOWER")
                || functionCall.equals("NLSSORT")
                || functionCall.equals("NLS_UPPER")
                || functionCall.equals("RPAD")
                || functionCall.equals("RTRIM")
                || functionCall.equals("SOUNDEX")
                || functionCall.equals("SUBSTR")
                || functionCall.equals("TRANSLATE")
                || functionCall.equals("TREAT")
                || functionCall.equals("TRIM")
                || functionCall.equals("UPPER")) {
            return new ExprTypeAST(ExprTypeAST.STRING);
        }
        if(functionCall.equals("ASCII")
                || functionCall.equals("INSTR")
                || functionCall.equals("LENGTH")) {
            return new ExprTypeAST(ExprTypeAST.INT);
        }
        
        if(functionCall.equals("ADD_MONTHS")
                || functionCall.equals("ADD_MONTHS")
                || functionCall.equals("CURRENT_DATE")
                || functionCall.equals("CURRENT_TIMESTAMP")
                || functionCall.equals("DBTIMEZONE")
                || functionCall.equals("EXTRACT")
                || functionCall.equals("FROM_TZ")
                || functionCall.equals("LAST_DAY")
                || functionCall.equals("LOCALTIMESTAMP")
                || functionCall.equals("MONTHS_BETWEEN")
                || functionCall.equals("NEW_TIME")
                || functionCall.equals("NEXT_DAY")
                || functionCall.equals("NUMTODSINTERVAL")
                || functionCall.equals("NUMTOYMINTERVAL")
                || functionCall.equals("ROUND")
                || functionCall.equals("SESSIONTIMEZONE")
                || functionCall.equals("SYS_EXTRACT_UTC")
                || functionCall.equals("SYSDATE")
                || functionCall.equals("SYSTIMESTAMP")
                || functionCall.equals("TO_DSINTERVAL")
                || functionCall.equals("TO_TIMESTAMP")
                || functionCall.equals("TO_TIMESTAMP_TZ")
                || functionCall.equals("TO_YMINTERVAL")
                || functionCall.equals("TRUNC")
                || functionCall.equals("TZ_OFFSET")
                || functionCall.equals("DAYOFWEEK")
                || functionCall.equals("WEEKDAY")
                || functionCall.equals("DAYOFMONTH")
                || functionCall.equals("DAYOFYEAR")
                || functionCall.equals("MONTH")
                || functionCall.equals("DAYNAME")
                || functionCall.equals("MONTHNAME")
                || functionCall.equals("QUARTER")
                || functionCall.equals("WEEK")
                || functionCall.equals("YEARWEEK")
                || functionCall.equals("HOUR")
                || functionCall.equals("MINUTE")
                || functionCall.equals("SECOND")
                || functionCall.equals("PERIOD_ADD")
                || functionCall.equals("PERIOD_DIFF")
                || functionCall.equals("DATE_ADD")
                || functionCall.equals("DATE_SUB")
                || functionCall.equals("ADDDATE")
                || functionCall.equals("SUBDATE")
                || functionCall.equals("TO_DAYS")
                || functionCall.equals("FROM_DAYS")
                || functionCall.equals("DATE_FORMAT")
                || functionCall.equals("TIME_FORMAT")
                || functionCall.equals("CURDATE")
                || functionCall.equals("NOW")
                || functionCall.equals("UNIX_TIMESTAMP")
                || functionCall.equals("SEC_TO_TIME")
                || functionCall.equals("TIME_TO_SEC")) {
            return new ExprTypeAST(ExprTypeAST.DATE);
        }
                
        return fc;
        
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
    void getReturnTypes(AST r, java.util.Stack stackAliases) throws RecognitionException {
        if(isSubQuery())
            return;
        result = new ArrayList();
        for (AST a = r.getFirstChild(); a != null; a = a.getNextSibling()) {
            result.add(a);
        }
    }
    
    void beforeStatement(String statementName, int statementType) {
        super.beforeStatement(statementName, statementType);
        stackAliases.push(aliasTypes);
        aliasTypes= new java.util.HashMap();
    }
    
    void afterStatementCompletion(String statementName) {
        super.afterStatementCompletion(statementName);
        aliasTypes = (HashMap) stackAliases.pop();
    }
    
    
    public Map getLabelTypes() {
        return aliasTypes;
    }

    public List getResult() {
        return result;
    }
}
