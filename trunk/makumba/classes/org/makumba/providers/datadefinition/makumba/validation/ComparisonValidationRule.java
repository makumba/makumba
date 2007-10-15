package org.makumba.providers.datadefinition.makumba.validation;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ValidationDefinitionParseError;
import org.makumba.commons.RegExpUtils;
import org.makumba.forms.html.dateEditor;

/**
 * This validation rule implements comparison validations between two fields, using the syntax &lt;fieldname&gt;
 * <i>comparison operatror</i> &lt;otherFieldname&gt;. Valid operators are {@value #comparisonOperators}. This rule
 * can be used for either two date, number or string types.
 * 
 * @author Rudolf Mayer
 * @version $Id: NumberRangeValidationRule.java,v 1.1 Sep 6, 2007 1:31:27 AM rudi Exp $
 */
public class ComparisonValidationRule extends BasicValidationRule {

    private static final String operator = "compare";

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd. MMM yyyy HH:mm:ss");

    private static final long serialVersionUID = 1L;

    public static final String now = "$now";

    public static final String today = "$today";

    public static final String dateFunction = "date(";

    public static final String dateFunctionParamExpression = RegExpUtils.whitespace + "(\\$now"
            + RegExpUtils.whitespace + "(\\+" + RegExpUtils.whitespace + "\\d+|-" + RegExpUtils.whitespace
            + "\\d+)?|\\d+)" + RegExpUtils.whitespace;

    /** the full date function {@value #dateFunctionExpression}. */
    public static final String dateFunctionExpression = "date\\((" + dateFunctionParamExpression + ",){0,5}("
            + dateFunctionParamExpression + ")?\\)";

    /** The compare-to part, either another field, or a date fucntion: {@value #compTo} */
    public static final String compareTo = RegExpUtils.or(new String[] { RegExpUtils.fieldName, "\\" + now,
            "\\" + today, dateFunctionExpression });

    public static final String dateExpression = RegExpUtils.or(new String[] { "\\" + now, "\\" + today,
            dateFunctionExpression });

    /** Possible comparison operators: {@value #comparisonOperators} */
    public static final String comparisonOperators = "(<|>|=|>=|<=|!=)";

    public static final String rule = "(" + RegExpUtils.fieldName + "|lower\\(" + RegExpUtils.fieldName + "\\))"
            + RegExpUtils.LineWhitespaces + comparisonOperators + RegExpUtils.LineWhitespaces + compareTo;

    private static final Pattern pattern = Pattern.compile(rule);

    private static final Pattern dateExpressionPattern = Pattern.compile(dateExpression);

    private FieldDefinition otherFd;

    private String otherFieldName;

    private String compareOperator;

    private String compareToExpression;

    private String functionName;

    public ComparisonValidationRule(FieldDefinition fd, String fieldName, String functionName, FieldDefinition otherFd,
            String otherFieldName, String ruleName, String errorMessage, String compareOperator)
            throws ValidationDefinitionParseError {
        super(fd, fieldName, errorMessage, ruleName, NUMBER_TYPES);
        this.otherFd = otherFd;
        this.otherFieldName = otherFieldName;
        this.compareOperator = compareOperator;
        this.functionName = functionName;
    }

    public ComparisonValidationRule(FieldDefinition fd, String fieldName, String compareToExpression, String ruleName,
            String errorMessage, String compareOperator) {
        super(fd, fieldName, errorMessage, ruleName, NUMBER_TYPES);
        this.compareOperator = compareOperator;
        this.compareToExpression = compareToExpression;
    }

    private ComparisonValidationRule() {
    }

    public boolean validate(Object value) throws InvalidValueException {
        boolean validateAgainstExpression = value instanceof Date && compareToExpression != null;
        boolean validateAgaintsField = value instanceof Object[] && ((Object[]) value).length == 2;
        if (!(validateAgainstExpression || validateAgaintsField)) {
            return false; // TODO: think of throwing some "cannot validate exception"
        }
        Object o1;
        Object o2;
        if (validateAgainstExpression) {
            o1 = value;
            o2 = evaluateExpression();
        } else {
            o1 = ((Object[]) value)[0];
            o2 = ((Object[]) value)[1];
        }
        if (functionName != null) {
            o1 = applyFunction(o1, functionName);
        }
        boolean numberComparison = (o1 instanceof Number && o2 instanceof Number);
        boolean dateComparison = (o1 instanceof Date && o2 instanceof Date);
        boolean stringComparison = (o1 instanceof String && o2 instanceof String);
        if (!numberComparison && !dateComparison && !stringComparison) {
            return false; // TODO: think of throwing some "cannot validate exception"
        }
        int compare;
        if (numberComparison) {
            compare = Double.compare(((Number) o1).doubleValue(), ((Number) o2).doubleValue());
        } else if (stringComparison) {
            compare = ((String) o1).compareTo((String) o2);
        } else {
            compare = ((Date) o1).compareTo(((Date) o2));
        }
        if (compareOperator.equals("<")) {
            return throwException(compare < 0);
        } else if (compareOperator.equals("<=")) {
            return throwException(compare < 0 || compare == 0);
        } else if (compareOperator.equals("=")) {
            return throwException(compare == 0);
        } else if (compareOperator.equals(">")) {
            return throwException(compare > 0);
        } else if (compareOperator.equals(">=")) {
            return throwException(compare > 0 || compare == 0);
        } else if (compareOperator.equals("!=")) {
            return throwException(compare != 0);
        }
        return false; // TODO: think of throwing some "cannot validate exception"
    }

    public String toString() {
        String to;
        if (compareToExpression != null) {
            to = compareToExpression + " [ = " + dateFormat.format(evaluateExpression()) + "]";
        } else {
            to = otherFieldName;
        }
        String field;
        if (functionName != null) {
            field = functionName + "(" + fieldName + ")";
        } else {
            field = fieldName;
        }

        return field + " " + compareOperator + " " + to;
    }

    public String getOtherFieldName() {
        return otherFieldName;
    }

    public FieldDefinition getOtherFd() {
        return otherFd;
    }

    public String getCompareOperator() {
        return compareOperator;
    }

    public String getFunctionName() {
        return functionName;
    }

    protected boolean throwException(boolean b) throws InvalidValueException {
        if (!b) {
            throw new InvalidValueException(fieldName, getErrorMessage());
        } else {
            return b;
        }
    }

    public boolean isCompareToExpression() {
        return compareToExpression != null;
    }

    public Date evaluateExpression() throws ValidationDefinitionParseError {
        // we have a comparison to a date constant
        GregorianCalendar c = new GregorianCalendar();
        c.set(Calendar.MILLISECOND, 0);
        String expr = compareToExpression.replaceAll("\\s", "");
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

    public static boolean matches(String rule) {
        return getMatcher(rule).matches();
    }

    public static Matcher getMatcher(String rule) {
        return pattern.matcher(rule);
    }

    public static Matcher getDateExpressionMatcher(String rule) {
        return dateExpressionPattern.matcher(rule);
    }

    public static boolean matchesDateExpression(String rule) {
        return getDateExpressionMatcher(rule).matches();
    }

    public static List getOperators() {
        return Arrays.asList(new String[] { "<", "=", ">", "!=", ">=", "<=" });
    }

    /** Do some pattern matching tests. */
    public static void main(String[] args) {
        String[] rules = { "someField > $now", "someField = $today", "someField > someOtherField", "someField = $now",
                "someField >= other", "lower(x)=y", "someField<=$today",
                "birthdate <= date($now, $now, $now + 105, 0, 0, 0)",
                "birthdate <= date($now, $now, $now + 105, 0, 0, 0)", " beginDate >= date($now,$now,$now - 5)",
                "beginDate >= date($now-5,$now,$now - 5)", "birthdate >= date($now, $now, $now - 15, 0, 0, 0)",
                "lower(indiv.name) != indiv.name" };
        rules = new String[]{"birthdate <= date($now, $now, $now - 15, 0, 0, 0)"};

        RegExpUtils.evaluate(pattern, rules, false);
    }

    public static Object getOperator() {
        return operator;
    }

}
