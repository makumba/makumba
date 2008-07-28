package org.makumba.providers.datadefinition.makumba.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.commons.RegExpUtils;

/**
 * This validation rule implements pattern matching for string types, using the syntax &lt;fieldname&gt; <i>matches</i>
 * &lt;regular expression&lt;. This rule can be used both for char and text types.
 * 
 * @author Rudolf Mayer
 * @version $Id: RegExpValidationRule.java,v 1.1 Sep 4, 2007 2:57:22 AM rudi Exp $
 */
public class RegExpValidationRule extends SingleFieldValidationRule {
    private static final String operator = "matches";

    private static final String rule = compileRule(operator, "(" + RegExpUtils.nonWhitespaces + ")");

    private static final Pattern pattern = Pattern.compile(getAcceptedRules());

    private static final long serialVersionUID = 1L;

    private String regExp;

    private Pattern regExpPattern;

    public static String getOperator() {
        return operator;
    }

    public RegExpValidationRule(FieldDefinition fd, String fieldName, String ruleName, String errorMessage,
            String regExp) {
        super(fd, fieldName, errorMessage, ruleName, STRING_TYPES);

        this.regExp = regExp;
        regExpPattern = Pattern.compile(regExp);
        if (!(fd.getIntegerType() == FieldDefinition._char || fd.getIntegerType() == FieldDefinition._text)) {
        }
    }

    public boolean validate(Object value) throws InvalidValueException {
        if (!(value instanceof String)) {
            return false;// TODO: think of throwing some "cannot validate exception"
        }
        Matcher matcher = regExpPattern.matcher((String) value);
        if (!matcher.matches()) {
            // throw new InvalidValueException(fieldName, "does not match regular expression '" + regExp + "'");
            throwException();
            return false;
        } else {
            return true;
        }
    }

    public String toString() {
        return fieldName + " " + getOperator() + " " + regExp;
    }

    public static String getAcceptedRules() {
        return rule;
    }

    public static Matcher getMatcher(String rule) {
        return pattern.matcher(rule);
    }

    public static boolean matches(String rule) {
        return getMatcher(rule).matches();
    }

    /** Do some pattern matching tests. */
    public static void main(String[] args) {
        System.out.println(getAcceptedRules());
        Pattern p = Pattern.compile(RegExpValidationRule.getAcceptedRules());
        String[] rules = { "someField%matches=abc", "someField%matches=abc", "someField%matches=fsgd" };
        RegExpUtils.evaluate(p, rules);
    }

    public String getRegExp() {
        return regExp;
    }

}
