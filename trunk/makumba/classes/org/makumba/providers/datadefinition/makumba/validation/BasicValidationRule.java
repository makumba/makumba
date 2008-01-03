package org.makumba.providers.datadefinition.makumba.validation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.FieldDefinition;
import org.makumba.ValidationRule;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.datadefinition.makumba.FieldInfo;

/**
 * This class provides basic support for validation rules. New rules should extend this class, or
 * {@link SingleFieldValidationRule} if they are operating on one field only.
 * 
 * @author Rudolf Mayer
 * @version $Id: BasicValidationRule.java,v 1.1 Sep 17, 2007 12:26:09 AM rudi Exp $
 */
public abstract class BasicValidationRule implements ValidationRule, Serializable {
    protected static final String rangeValue = "(" + RegExpUtils.minOneDigit + "|\\?)";

    protected static final String rangeDef = "\\[" + rangeValue + "\\.\\." + rangeValue + "\\]";

    protected static final String fieldDef = RegExpUtils.fieldName + RegExpUtils.minOneLineWhitespace;

    protected static final String lowerFunction = "lower";

    protected static final String upperFunction = "upper";

    protected static final String fieldFunction = RegExpUtils.or(new String[] {
            RegExpUtils.fieldName,
            "(?:" + lowerFunction + "|" + upperFunction + ")" + RegExpUtils.LineWhitespaces + "\\("
                    + RegExpUtils.LineWhitespaces + RegExpUtils.fieldName + RegExpUtils.LineWhitespaces + "\\)" });

    /** The field definition this rule applies to. */
    protected FieldDefinition fd;

    /** The name of the field (not always the same as fd.getName() for compound forms). */
    protected String fieldName;

    /** Stores what types of fields are allowed (int, char.. see {@link FieldDefinition} . */
    protected int[] allowedTypes;

    protected String ruleSyntax;

    protected String errorMessage;

    protected String ruleName;

    /** Number types, int and real. */
    public static int[] NUMBER_TYPES = { FieldDefinition._int, FieldDefinition._real };

    /** String types, char and text */
    public static int[] STRING_TYPES = { FieldDefinition._char, FieldDefinition._text };

    public BasicValidationRule(FieldDefinition fd, String fieldName, String errorMessage, String ruleName,
            int[] allowedTypes) {
        this.allowedTypes = allowedTypes;
        this.errorMessage = errorMessage;
        this.ruleName = ruleName;
        this.fd = fd;
        this.fieldName = fieldName;
    }

    protected BasicValidationRule() {
        super();
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public FieldDefinition getFieldDefinition() {
        return fd;
    }

    /** Return a string represenation of the allowed types. */
    protected String getAllowedTypes() {
        String s = "";
        for (int i = 0; i < allowedTypes.length; i++) {
            s += "'" + FieldInfo.getStringType(allowedTypes[i]) + "'";
            if (i + 2 < allowedTypes.length) {
                s += ", ";
            } else if (i + 1 < allowedTypes.length) {
                s += " and ";
            }
        }
        return s;
    }

    /** Apply a function to the fields value. */
    protected Object applyFunction(Object o1, String functionName) {
        if (functionName.equals("lower")) {
            return ((String) o1).toLowerCase();
        } else {
            return o1;
        }
    }

    /** return the (first) field name the rule operates on. */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Extract the function argument from the rule definition statement. I.e. for 'lower(name)', 'name' will be
     * returned.
     * 
     * @return the function argument, or null if this statement does not contain a function.
     */
    public static String extractFunctionArgument(String statement) {
        int beginIndex = statement.indexOf("(");
        int endIndex = statement.lastIndexOf(")");
        if (beginIndex == -1 || endIndex == -1 || beginIndex >= endIndex) {
            return statement; // TODO: throw some exception?
        } else {
            return statement.substring(beginIndex + 1, endIndex);
        }
    }

    /** Checks if the given statement contains a syntactically correct function call. */
    public static boolean isFunctionCall(String statement) {
        int beginIndex = statement.indexOf("(");
        int endIndex = statement.lastIndexOf(")");
        return beginIndex != -1 && endIndex != -1 && beginIndex < endIndex;
    }

    /** Checks if the given statement contains a syntactically correct and <b>known</b> function call. */
    public static boolean isValidFunctionCall(String s) {
        List<String> validFunctions = Arrays.asList(new String[] { "lower" });
        return isFunctionCall(s) && validFunctions.contains(extractFunctionNameFromStatement(s));
    }

    /**
     * Extract the function name from the rule definition statement. I.e. for 'lower(name)', 'lower' will be returned.
     * 
     * @return the function name, or null if this statement does not contain a function.
     */
    public static String extractFunctionNameFromStatement(String statement) {
        if (!isFunctionCall(statement)) {
            return null;
        } else {
            return statement.substring(0, statement.indexOf("("));
        }
    }

    /** Main class for testing purposes. */
    public static void main(String[] args) {
        System.out.println(fieldFunction);
        Pattern p = Pattern.compile(fieldFunction);
        System.out.println(p.pattern());
        String[] rules = { "someField", "lower(someField)", "lower(  someField    )", "lower (  someField    )" };
        for (int i = 0; i < rules.length; i++) {
            Matcher matcher = p.matcher(rules[i]);
            System.out.println(rules[i] + ":" + matcher.matches());
        }
    }

    /**
     * We order the rules such that comparison rules come last. This is important for live validation, where first the
     * validity of each field by itself should be checked.
     */
    public int compareTo(ValidationRule o) {
        if (this instanceof ComparisonValidationRule) {
            return 1;
        } else if (o instanceof ComparisonValidationRule) {
            return -1;
        } else {
            return 0;
        }
    }

}