package org.makumba.providers.datadefinition.makumba.validation;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ValidationDefinitionParseError;
import org.makumba.commons.RegExpUtils;

/**
 * This class provides basic support for rules that operate on a single field, e.g. range checks or regular expressions.
 * 
 * @author Rudolf Mayer
 * @version $Id: AbstractValidationRule.java,v 1.1 Sep 6, 2007 1:28:56 AM rudi Exp $
 */
public abstract class SingleFieldValidationRule extends BasicValidationRule {

    private static final long serialVersionUID = 1L;

    protected static String compileRule(String operator, String definition) {
         return "(" + RegExpUtils.fieldName + ")" + RegExpUtils.LineWhitespaces + "%" + RegExpUtils.LineWhitespaces
         + "(" + operator + ")" + RegExpUtils.LineWhitespaces + "=" + RegExpUtils.LineWhitespaces + definition;
//        return "(" + RegExpUtils.fieldName + ")" + "%" + "(" + operator + ")" + "=" + definition;
    }

    protected SingleFieldValidationRule() {
    }

    protected SingleFieldValidationRule(FieldDefinition fd, String fieldName, String errorMessage, String ruleName,
            int[] allowedTypes) {
        super(fd, fieldName, errorMessage, ruleName, allowedTypes);
        checkApplicability();
    }

    /** Checks whether the rule is applicable for the given field types. */
    public boolean checkApplicability() throws ValidationDefinitionParseError {
        for (int i = 0; i < allowedTypes.length; i++) {
            if (allowedTypes[i] == fd.getIntegerType()) {
                return true;
            }
        }
        throw new ValidationDefinitionParseError(fd.getName(), "Rule '" + ruleSyntax + "' is only applicable for "
                + getAllowedTypes() + " types, &lt;" + fd.getName() + "&gt; is of type '" + fd.getType() + "'!", "");
    }

    /** Throw a default exception. */
    protected void throwException() throws InvalidValueException {
        throw new InvalidValueException(getFieldName(), getErrorMessage());
    }

}