package org.makumba.providers.datadefinition.makumba.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ValidationDefinitionParseError;
import org.makumba.commons.RegExpUtils;

/**
 * This validation rule implements number range checks, using the syntax &lt;fieldname&gt; <i>in range</i>
 * [&lt;lowerCValue&gt;..&lt;upperValue&gt;]. ? is allowed as identifier for unlimted ranges in either range end. This
 * rule can be used both for int and for real data types.
 * 
 * @author Rudolf Mayer
 * @version $Id: NumberRangeValidationRule.java,v 1.1 Sep 6, 2007 1:31:27 AM rudi Exp $
 */
public class NumberRangeValidationRule extends RangeValidationRule {

    private static final String operator = "in range";

    private static final Pattern pattern = Pattern.compile(getAcceptedRules());

    private static final String rule = "(" + RegExpUtils.fieldName + ")" + RegExpUtils.LineWhitespaces + operator
            + RegExpUtils.minOneLineWhitespace + rangeDef;

    private static final long serialVersionUID = 1L;

    public static String getOperator() {
        return operator;
    }

    public NumberRangeValidationRule(FieldDefinition fd, String fieldName, String ruleName, String errorMessage,
            String lowerLimitString, String upperLimitString) throws ValidationDefinitionParseError {
        super(fd, fieldName, errorMessage, ruleName, NUMBER_TYPES, lowerLimitString, upperLimitString);

        if (lowerLimitString.equals("?")) {
            lowerLimit = new Double(Double.MIN_VALUE); // FIXME: use the min value makumba can handle
        } else {
            lowerLimit = Double.valueOf(lowerLimitString);
        }
        if (upperLimitString.equals("?")) {
            upperLimit = new Double(Double.MAX_VALUE); // FIXME: use the max value makumba can handle
        } else {
            upperLimit = Double.valueOf(upperLimitString);
        }
    }

    private NumberRangeValidationRule() {
    }

    public boolean validate(Object value) throws InvalidValueException {
        if (!(value instanceof Number)) {
            return false;// TODO: think of throwing some "cannot validate exception"
        }
        if ((lowerLimit.doubleValue() <= ((Number) value).doubleValue() && ((Number) value).doubleValue() <= upperLimit.doubleValue())) {
            return true;
        } else {
            throwException();
            return false;
        }
    }

    public String toString() {
        return "" + fieldName + " " + getOperator() + " [" + lowerLimitString + ".." + upperLimitString + "]";
    }

    public static String getAcceptedRules() {
        return rule;
    }

    public static boolean matches(String rule) {
        return getMatcher(rule).matches();
    }

    public static Matcher getMatcher(String rule) {
        return pattern.matcher(rule);
    }

    /** Do some pattern matching tests. */
    public static void main(String[] args) {
        Pattern p = Pattern.compile(NumberRangeValidationRule.getAcceptedRules());
        String[] rules = { "someField range [1..20]", "someField range [?..500]", "someField rrnge [?..500]" };
        RegExpUtils.evaluate(p, rules);
    }

}
