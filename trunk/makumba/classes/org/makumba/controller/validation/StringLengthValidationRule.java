package org.makumba.controller.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.Text;
import org.makumba.commons.RegExpUtils;

/**
 * This validation rule implements string-length checks, using the syntax &lt;fieldname&gt; <i>length in</i>
 * [&lt;lowerCValue&gt;..&lt;upperValue&gt;]. ? is allowed as identifier for unlimted ranges in either range end. This
 * rule can be used both for char and text types.
 * 
 * @author Rudolf Mayer
 * @version $Id: StringLengthValidationRule.java,v 1.1 Sep 17, 2007 12:18:42 AM rudi Exp $
 */
public class StringLengthValidationRule extends RangeValidationRule {

    private static final String operator = "length in";

    private static final Pattern pattern = Pattern.compile(getAcceptedRules());

    private static final String rule = "(" + RegExpUtils.fieldName + ")" + RegExpUtils.LineWhitespaces + operator
            + RegExpUtils.minOneLineWhitespace + "" + rangeDef + "";

    private static final long serialVersionUID = 1L;

    public static String getOperator() {
        return operator;
    }

    public StringLengthValidationRule(FieldDefinition fd, String fieldName, String ruleName, String errorMessage,
            String lowerLimitString, String upperLimitString) {
        super(fd, fieldName, errorMessage, ruleName, STRING_TYPES, lowerLimitString, upperLimitString);

        if (lowerLimitString.equals("?")) {
            lowerLimit = new Integer(0);
        } else {
            lowerLimit = Integer.valueOf(lowerLimitString);
        }
        if (upperLimitString.equals("?")) {
            upperLimit = new Integer(Integer.MAX_VALUE); // FIXME: use the max value makumba can handle
        } else {
            upperLimit = Integer.valueOf(upperLimitString);
        }
    }

    private StringLengthValidationRule() {
    }

    public boolean validate(Object value) throws InvalidValueException {
        if (!(value instanceof String || value instanceof Text)) {
            return false;// TODO: think of throwing some "cannot validate exception"
        }
        String s = value.toString();
        if (lowerLimit.intValue() <= s.length() && s.length() <= upperLimit.intValue()) {
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
        Pattern p = Pattern.compile(StringLengthValidationRule.getAcceptedRules());
        String[] rules = { "someField length [1..20]", "someField length [?..500]", "someField llngth [?..500]" };
        RegExpUtils.evaluate(p, rules);
    }

}
