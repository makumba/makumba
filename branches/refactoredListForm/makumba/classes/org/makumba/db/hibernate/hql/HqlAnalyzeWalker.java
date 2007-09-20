package org.makumba.db.hibernate.hql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.makumba.DataDefinitionNotFoundError;

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

    void setAliasType(AST alias, String type) throws antlr.RecognitionException{
        if (aliasTypes.get(alias.getText()) != null)
            throw new antlr.SemanticException("alias " + alias.getText() + " defined twice");

        if (typeComputer.determineType(type, null) != null)
            aliasTypes.put(alias.getText(), type);
        else {
            // we have a from section in the form a.b.c that is not a type!
            // a must be a recognizable label
            int dot = type.indexOf('.');
            if (dot == -1)
                throw new DataDefinitionNotFoundError(type);
            String label = type.substring(0, dot);
            String labelType = (String) aliasTypes.get(label);
            if (labelType == null)
                throw new SemanticException("Unknown label: "+label+"\n"+(new NoSuchFieldException(label)).getStackTrace());
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
        //FIXME put the initialization stuff in another class
        Map methodTypes = new HashMap();
        String functionCall = fc.getText().toUpperCase();
        //String arguments = e.getFirstChild().getClass().getName();
        //System.out.println("FC: "+functionCall+"args: "+arguments);
        
        /* determining the type returned by the method */
        String[] methodReal = {"COS","COSH","EXP","LN","LOG","SIN","SINH","SQRT","TAN","TANH","ACOS","ASIN","ATAN"};

        for(int i=0; i<methodReal.length;i++) {
            methodTypes.put(methodReal[i],new Integer(ExprTypeAST.DOUBLE));
        }
        
        //return new ExprTypeAST(ExprTypeAST.DOUBLE);
        
        String[] methodChar = {"CHR","CONCAT","INITCAP","LOWER","LPAD","LTRIM","NLS_INITCAP","NLS_LOWER","NLSSORT","NLS_UPPER",
                "RPAD","RTRIM","SOUNDEX","SUBSTR","TRANSLATE","TREAT","TRIM","UPPER"};
        
        for(int i=0; i<methodChar.length;i++) {
            methodTypes.put(methodChar[i],new Integer(ExprTypeAST.STRING));
        }
        
        String[] methodInt = {"ASCII","INSTR","LENGTH"};
        
        for(int i=0; i<methodInt.length;i++) {
            methodTypes.put(methodInt[i],new Integer(ExprTypeAST.INT));
        }
        
        String[] methodDate = {"ADD_MONTHS","CURRENT_DATE","CURRENT_TIMESTAMP","DBTIMEZONE","EXTRACT","FROM_TZ",
                "LAST_DAY","LOCALTIMESTAMP","MONTHS_BETWEEN","NEW_TIME","NEXT_DAY","NUMTODSINTERVAL","NUMTOYMINTERVAL",
                "ROUND","SESSIONTIMEZONE","SYS_EXTRACT_UTC","SYSDATE","SYSTIMESTAMP","TO_DSINTERVAL","TO_TIMESTAMP",
                "TO_TIMESTAMP_TZ","TO_YMINTERVAL","TRUNC","TZ_OFFSET","DAYOFWEEK","WEEKDAY","DAYOFMONTH","DAYOFYEAR",
                "MONTH","DAYNAME","MONTHNAME","QUARTER","WEEK","YEARWEEK","HOUR","MINUTE","SECOND","PERIOD_ADD","PERIOD_DIFF",
                "DATE_ADD","DATE_SUB","ADDDATE","SUBDATE","TO_DAYS","FROM_DAYS","DATE_FORMAT","TIME_FORMAT","CURDATE",
                "NOW","UNIX_TIMESTAMP","SEC_TO_TIME","TIME_TO_SEC"};
        
        for(int i=0; i<methodDate.length;i++) {
            methodTypes.put(methodDate[i],new Integer(ExprTypeAST.DATE));
        }

        return new ExprTypeAST(((Integer)methodTypes.get(functionCall)).intValue());
        
    }

    AST deriveArithmethicExpr(AST ae) throws SemanticException {
        // case-when already returns an analyzed expression
        if(ae instanceof ExprTypeAST)
            return ae;
        String operator = ae.getText();
        ExprTypeAST firstValue = null;
        ExprTypeAST secondValue = null;
        firstValue = (ExprTypeAST) ae.getFirstChild();
        secondValue = (ExprTypeAST) firstValue.getNextSibling();
        
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
        HashMap aliasTypes1=(HashMap)((HashMap)aliasTypes).clone();
        stackAliases.push(aliasTypes);
        aliasTypes= aliasTypes1;
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
