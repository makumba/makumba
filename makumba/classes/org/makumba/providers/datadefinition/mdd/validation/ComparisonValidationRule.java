package org.makumba.providers.datadefinition.mdd.validation;

import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.InvalidValueException;
import org.makumba.ValidationDefinitionParseError;
import org.makumba.commons.RegExpUtils;
import org.makumba.forms.html.dateEditor;
import org.makumba.providers.Configuration;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.ValidationType;

import antlr.collections.AST;

public class ComparisonValidationRule extends ValidationRuleNode {

    private static final long serialVersionUID = 1L;

    
    public ComparisonValidationRule(MDDNode mdd, AST originAST, ValidationType type) {
        super(mdd, originAST, type);
    }
    
    @Override
    public String getRuleName() {
        return "compare() { " + expression + " } : " + message + " (line " + getLine() + ")";
    }
    
    
    private static final String now = "$now";

    private static final String today = "$today";

    private static final String dateFunction = "date(";
    
    private static final String dateFunctionParamExpression = RegExpUtils.whitespace + "(\\$now"
    + RegExpUtils.whitespace + "(\\+" + RegExpUtils.whitespace + "\\d+|-" + RegExpUtils.whitespace
    + "\\d+)?|\\d+)" + RegExpUtils.whitespace;

    private static final String dateFunctionExpression = "date\\((" + dateFunctionParamExpression + ",){0,5}("
        + dateFunctionParamExpression + ")?\\)";
    
    private static final String dateExpression = RegExpUtils.or(new String[] { "\\" + now, "\\" + today,
            dateFunctionExpression });
    
    private Pattern datePattern;
    
    private String query;
    
    private HashMap<String, Object> arguments = new HashMap<String, Object>();
    
    private int argumentIndex = 0;
    
    @Override
    public boolean validate(Object value) throws InvalidValueException {
        
        if(query == null) {
            
            // pre-process the expression, i.e. replace constants and virtual date function with arguments
            preProcess();
            
            // evaluation query
            query = "SELECT (" + expression + ") as expression FROM " + this.mdd.getName() + " t";
            
        }
        
        // TODO analyze this first, catch analysis problems
        
        System.out.println("RUNNING QUERY " + query);

        QueryProvider query = QueryProvider.makeQueryRunner(Configuration.getDefaultDataSourceName(), "oql");
        Vector<Dictionary<String, Object>> res = query.execute("SELECT (lower(t.name) = t.name) as expression FROM ParserTest t", arguments, 0, -1);
        
        // TODO treat cases where there's no result
        
        boolean result = (Boolean) res.get(0).get("expression");
        
        return result;
    }
    
    
    private void preProcess() {
        
        if(datePattern == null) {
            datePattern = Pattern.compile(dateExpression);
        }
        
        Matcher matcher = datePattern.matcher(expression);
        while(matcher.matches()) {
            String dateExpr = expression.substring(matcher.start(), matcher.end());
            arguments.put("arg"+argumentIndex, evaluateExpression(dateExpr));
            matcher.replaceFirst("$arg"+argumentIndex);
            argumentIndex++;
        }
    }
    

    
    private Date evaluateExpression(String expression) throws ValidationDefinitionParseError {
        // we have a comparison to a date constant
        GregorianCalendar c = new GregorianCalendar();
        c.set(Calendar.MILLISECOND, 0);
        String expr = expression.replaceAll("\\s", "");
        if (expr.equals(now)) {
            // we will just use the calendar
        } else if (expr.equals(today)) {
            // we use today, i.e. this dates, but 00:00.00 for the time
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
        } else if (expr.startsWith(dateFunction)) {
            // this can be done with a regexp too

            // we strip of the parantheses
            expr = extractFunctionArgument(expr);

            String[] split = expr.split(",");
            for (int i = 0; i < split.length; i++) {
                if (split[i].equals(now)) {
                    // we take current time, nothing to do :-)
                } else if (split[i].startsWith(now)) {
                    // we take current value, +/- the specified value
                    String operator = split[i].substring(now.length(), now.length() + 1);
                    int summand = Integer.parseInt(split[i].substring(now.length() + 1));
                    int val = c.get(dateEditor.components[i]);
                    if (operator.trim().equals("-")) {
                        c.set(dateEditor.components[i], val - summand);
                    } else if (operator.trim().equals("+")) {
                        c.set(dateEditor.components[i], val + summand);
                    }
                } else {// we assume a number
                    c.set(dateEditor.components[i], Integer.parseInt(split[i]));
                }
            }

        }
        return c.getTime();
    }
    
    private String extractFunctionArgument(String statement) {
        int beginIndex = statement.indexOf("(");
        int endIndex = statement.lastIndexOf(")");
        if (beginIndex == -1 || endIndex == -1 || beginIndex >= endIndex) {
            return statement; // TODO: throw some exception?
        } else {
            return statement.substring(beginIndex + 1, endIndex);
        }
    }

}
