package org.makumba.providers.datadefinition.makumba.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.FieldDefinition;

public abstract class RangeValidationRule extends SingleFieldValidationRule {

    protected static final String rule = rangeDef;

    protected static final Pattern pattern = Pattern.compile(getAcceptedRules());
    
    public static String getAcceptedRules() {
        return rule;
    }

    public static Matcher getMatcher(String rule) {
        return pattern.matcher(rule);
    }

    public static boolean matches(String rule) {
        return getMatcher(rule).matches();
    }

    protected String lowerLimitString;

    protected String upperLimitString;

    protected Number lowerLimit;

    protected Number upperLimit;
    
    protected RangeValidationRule() {
        super();
    }

    public RangeValidationRule(FieldDefinition fd, String fieldName, String errorMessage, String ruleName,
            int[] allowedTypes, String lowerLimitString, String upperLimitString) {
        super(fd, fieldName, errorMessage, ruleName, allowedTypes);
        this.lowerLimitString = lowerLimitString.trim();
        this.upperLimitString = upperLimitString.trim();
    }

    public String getLowerLimitString() {
        return lowerLimitString;
    }

    public String getUpperLimitString() {
        return upperLimitString;
    }

}