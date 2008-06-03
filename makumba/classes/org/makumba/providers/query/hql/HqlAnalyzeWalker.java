package org.makumba.providers.query.hql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.makumba.DataDefinitionNotFoundError;
import org.makumba.commons.RuntimeWrappedException;

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
    private String query;

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

        return new ParamTypeAST(ExprTypeAST.PARAMETER, pe.getText());
    }
    
    AST deriveLogicalExpr(AST le) {
        // we assume that the two sides of the logical operation have the same type, which helps us to determine the parameter type if one side is a parameter

        // FIXME grammar should be adjusted to build ExprTypeASTs whenever possible so the if() below passes, one way to work on that is to uncomment the else
        // FIXME :param in (subquery) should use the subquery type analysis, currently it doesn't pass this if()
        // FIXME :param in (comstant list) should infer the type from the constants
        // FIXME exists() doesn't pass the if, but it maybe isn't useful for parameter type inference as it's a unary operator
        
        if(le.getFirstChild() instanceof ExprTypeAST && le.getFirstChild().getNextSibling() instanceof ExprTypeAST){
                        
            ExprTypeAST firstValue = null;
            ExprTypeAST secondValue = null;
  
            firstValue = (ExprTypeAST) le.getFirstChild();
            secondValue = (ExprTypeAST) firstValue.getNextSibling();
            
            inferParam(firstValue, secondValue);
        }
        //else{ System.out.println(le+" "+le.getFirstChild()+" "+le.getFirstChild().getNextSibling()); }
        
        //FIXME check if the IN operands are of the same type
        return new ExprTypeAST(ExprTypeAST.INT);
    }
    
    private void inferParam(ExprTypeAST firstValue, ExprTypeAST secondValue) {
        // FIXME look at OQL analyzer, this is done there
        inferParam1(firstValue, secondValue);
        inferParam1(secondValue, firstValue);
    }

    private void inferParam1(ExprTypeAST firstValue, ExprTypeAST secondValue) {
        if(firstValue instanceof ParamTypeAST && secondValue!=null){
            // FIXME: check whether we already had a type for this parameter, and if the two are compatible
            paramTypes.put(firstValue.getText(), secondValue);
        }
    }
    
    private static Map<String, Integer> methodTypes = new HashMap<String, Integer>();
    
    static {
        
        String[] methodReal = {"COS","COSH","EXP","LN","LOG","SIN","SINH","SQRT","TAN","TANH","ACOS","ASIN","ATAN"};
        
        String[] methodChar = {"CHR","CONCAT","INITCAP","LOWER","LPAD","LTRIM","NLS_INITCAP","NLS_LOWER","NLSSORT","NLS_UPPER",
                "RPAD","RTRIM","SOUNDEX","SUBSTR","TRANSLATE","TREAT","TRIM","UPPER"};
        
        String[] methodInt = {"ASCII","INSTR","LENGTH", "DAY", "MONTH", "YEAR"};
        
        String[] methodDate = {"ADD_MONTHS","CURRENT_DATE","CURRENT_TIMESTAMP","DBTIMEZONE","EXTRACT","FROM_TZ",
                "LAST_DAY","LOCALTIMESTAMP","MONTHS_BETWEEN","NEW_TIME","NEXT_DAY","NUMTODSINTERVAL","NUMTOYMINTERVAL",
                "ROUND","SESSIONTIMEZONE","SYS_EXTRACT_UTC","SYSDATE","SYSTIMESTAMP","TO_DSINTERVAL","TO_TIMESTAMP",
                "TO_TIMESTAMP_TZ","TO_YMINTERVAL","TRUNC","TZ_OFFSET","DAYOFWEEK","WEEKDAY","DAYOFMONTH","DAYOFYEAR",
                "MONTH","DAYNAME","MONTHNAME","QUARTER","WEEK","YEARWEEK","HOUR","MINUTE","SECOND","PERIOD_ADD","PERIOD_DIFF",
                "DATE_ADD","DATE_SUB","ADDDATE","SUBDATE","TO_DAYS","FROM_DAYS","DATE_FORMAT","TIME_FORMAT","CURDATE",
                "NOW","UNIX_TIMESTAMP","SEC_TO_TIME","TIME_TO_SEC"};
        
        for(String method : methodReal) {
            methodTypes.put(method, new Integer(ExprTypeAST.DOUBLE));
        }
        for(String method : methodChar) {
            methodTypes.put(method, new Integer(ExprTypeAST.STRING));
        }
        for(String method : methodInt) {
            methodTypes.put(method, new Integer(ExprTypeAST.INT));
        }
        for(String method : methodDate) {
            methodTypes.put(method, new Integer(ExprTypeAST.DATE));
        }

    }

    //FIXME if a function parameter is a query parameter, assign its type
    AST deriveFunctionCallExpr(AST fc, AST e) {

        String functionCall = fc.getText().toUpperCase();
        
        return new ExprTypeAST(((Integer)methodTypes.get(functionCall)).intValue());
        
    }

    AST deriveQueryExpr(AST ae) throws SemanticException {
        // the query type is the first projection type
        // FIXME maybe we should check to make sure that we only have one projection
        ae= ae.getFirstChild().getFirstChild();
        return ae;
    }
    AST deriveAggregateExpr(AST ae, AST ag) throws SemanticException {
        String agr= ag.getText();
        if("max".equals(agr) || "min".equals(agr))
            // min and max return the same type as the expr
            return ae;
        if("avg".equals(agr))
            return new ExprTypeAST(ExprTypeAST.DOUBLE);
        if("count".equals(agr))
            return new ExprTypeAST(ExprTypeAST.INT);
        
        // FIXME see if there are other aggregate functions, throw exception for unknown aggregate (though the walker should not let it pass)
        return null;
    }

    AST deriveArithmethicExpr(AST ae) throws SemanticException {
        // FIXME: add arithmetic parameter inferences, see deriveLogicalExpr
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
        if(isSubQuery()){
            HashMap aliasTypes1=(HashMap)((HashMap)aliasTypes).clone();
            stackAliases.push(aliasTypes);
            aliasTypes= aliasTypes1;
        }
    }
    
    void afterStatementCompletion(String statementName) {
        if(isSubQuery())
            aliasTypes = (HashMap) stackAliases.pop();
        super.afterStatementCompletion(statementName);
    }
    
    
    public Map<String, String> getLabelTypes() {
        return aliasTypes;
    }

    public Map getParameterTypes() {
        return paramTypes;
    }

    public List getResult() {
        return result;
    }
    
    @Override
    public void reportError(RecognitionException ex){
        throw new RuntimeWrappedException(ex);
    }

    public void setDebug(String query1) {
        query=query1;        
    }
    
    public String toString() {
        return query;
    }
}
