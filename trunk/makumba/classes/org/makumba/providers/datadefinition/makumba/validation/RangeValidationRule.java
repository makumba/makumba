package org.makumba.providers.datadefinition.makumba.validation;

import org.makumba.FieldDefinition;

public abstract class RangeValidationRule extends SingleFieldValidationRule {

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