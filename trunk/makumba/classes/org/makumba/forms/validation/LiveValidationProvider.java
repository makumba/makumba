package org.makumba.forms.validation;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.makumba.FieldDefinition;
import org.makumba.ValidationRule;
import org.makumba.commons.StringUtils;
import org.makumba.forms.html.FieldEditor;
import org.makumba.providers.datadefinition.makumba.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.NumberRangeValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.RangeValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.RegExpValidationRule;
import org.makumba.providers.datadefinition.makumba.validation.StringLengthValidationRule;

/**
 * This class implements java-script based client side validation using the <i>LiveValidation</i> library. For more
 * details, please refer to the webpage at <a href="http://www.livevalidation.com/">http://www.livevalidation.com/</a>.
 * 
 * @author Rudolf Mayer
 * @version $Id: LiveValidationProvider.java,v 1.1 15.09.2007 13:32:07 Rudolf Mayer Exp $
 */
public class LiveValidationProvider implements ClientsideValidationProvider, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Gathers all validation object definitions and calls to it, e.g.<br>
     * <code>
     * var ageValidation = new LiveValidation('age', { validMessage: " " });<br>
     * ageValidation.add( Validate.Numericality , { onlyInteger: true, failureMessage: "invalid integer" } );<br>
     * ageValidation.add( Validate.Numericality , { minimum: 12, failureMessage: "Age must be at least 12 years" } );<br>
     * </code>
     */
    private StringBuffer validationObjects = new StringBuffer();

    /**
     * Gathers all the names of the validation variables defined. this is needed to make mass validation in
     * {@link #getOnSubmitValidation()}.
     */
    private LinkedHashSet<String> definitionVarNames = new LinkedHashSet<String>();

    /** Initialises a field, basically does create the variables and calls for this field. */
    public void initField(String inputName, String formIdentifier, FieldDefinition fieldDefinition, boolean validateLive) {
        inputName = inputName + formIdentifier;
        Collection<ValidationRule> validationRules = fieldDefinition.getValidationRules();
        int size = validationRules != null ? validationRules.size() : 1;
        StringBuffer validations = new StringBuffer(100 + size * 50);
        String inputVarName = inputName.replaceAll("\\.", "__") + "Validation";

        if (fieldDefinition == null) {
            System.out.println("Null field definition for " + inputName);
        }

        // add validation rules automatically inferred from field modifiers - not null, not empty, numerically
        if (fieldDefinition.isNotNull() && !fieldDefinition.isDateType()) {
            // FIXME: not-null check for dates needed
            // FIXME: fix for checkboxes (and radio buttons?) needed?
            validations.append(getValidationLine(inputVarName, "Validate.Presence",
                fieldDefinition.getNotNullErrorMessage() != null ? fieldDefinition.getNotNullErrorMessage()
                        : FieldDefinition.ERROR_NOT_NULL));
        } else if (fieldDefinition.isNotEmpty()) { // add a length validation, minimum length 1
            validations.append(getValidationLine(inputVarName, "Validate.Length",
                fieldDefinition.getNotEmptyErrorMessage() != null ? fieldDefinition.getNotEmptyErrorMessage()
                        : FieldDefinition.ERROR_NOT_EMPTY, getRangeLimits("1", "?")));
        }

        if (fieldDefinition.isIntegerType()) {
            validations.append(getValidationLine(inputVarName, "Validate.Numericality",
                fieldDefinition.getNotIntErrorMessage() != null ? fieldDefinition.getNotIntErrorMessage()
                        : FieldEditor.ERROR_NO_INT, "onlyInteger: true,"));
        } else if (fieldDefinition.isRealType()) {
            validations.append(getValidationLine(inputVarName, "Validate.Numericality",
                fieldDefinition.getNotRealErrorMessage() != null ? fieldDefinition.getNotRealErrorMessage()
                        : FieldEditor.ERROR_NO_REAL));
        }

        if (validationRules != null) {
            addValidationRules(inputName, formIdentifier, validationRules, validations, inputVarName);
        }

        if (fieldDefinition.isUnique() && !fieldDefinition.isDateType()) {
            validations.append(getValidationLine(inputVarName, "MakumbaValidate.Uniqueness",
                fieldDefinition.getNotUniqueErrorMessage() != null ? fieldDefinition.getNotUniqueErrorMessage()
                        : FieldDefinition.ERROR_NOT_UNIQUE, "table: \"" + fieldDefinition.getDataDefinition().getName()
                        + "\", " + "field: \"" + fieldDefinition.getName() + "\", "));
        }

        if (validations.length() > 0) {
            definitionVarNames.add(inputVarName);
            validationObjects.append("var " + inputVarName + " = new LiveValidation('" + inputName
                    + "', { validMessage: \" \" });\n");
            validationObjects.append(validations);
            validationObjects.append("\n");
        }
    }

    protected void addValidationRules(String inputName, String formIdentifier,
            Collection<ValidationRule> validationRules, StringBuffer validations, String inputVarName) {

        for (ValidationRule validationRule : validationRules) {
            ValidationRule rule = validationRule;

            if (rule instanceof StringLengthValidationRule) {
                validations.append(getValidationLine(inputVarName, "Validate.Length", rule,
                    getRangeLimits((RangeValidationRule) rule)));
            } else if (rule instanceof NumberRangeValidationRule) {
                validations.append(getValidationLine(inputVarName, "Validate.Numericality", rule,
                    getRangeLimits((RangeValidationRule) rule)));
            } else if (rule instanceof RegExpValidationRule) {
                validations.append(getValidationLine(inputVarName, "Validate.Format", rule, "pattern: /^"
                        + ((RegExpValidationRule) rule).getRegExp() + "$/i, "));
            } else if (rule instanceof ComparisonValidationRule) {
                ComparisonValidationRule c = (ComparisonValidationRule) rule;

                // FIXME: need to implement date comparisons
                if (c.getFieldDefinition().isDateType()) {
                    continue;
                }

                String arguments = "element1: \"" + c.getFieldName() + formIdentifier + "\", element2: \""
                        + c.getOtherFieldName() + formIdentifier + "\", comparisonOperator: \""
                        + c.getCompareOperator() + "\", ";

                // FIXME: implement a modification for comparison rules with greater / less than comparison as in
                // MDDLiveValidationProvider

                if (c.getFieldDefinition().isNumberType()) {
                    validations.append(getValidationLine(inputVarName, "MakumbaValidate.NumberComparison", rule,
                        arguments));
                } else if (c.getFieldDefinition().isDateType()) {
                    // TODO: implement!
                } else if (c.getFieldDefinition().isStringType()) {
                    if (c.getFunctionName() != null && c.getFunctionName().length() > 0) {
                        arguments += "functionToApply: \"" + c.getFunctionName() + "\", ";
                        validations.append(getValidationLine(inputVarName, "MakumbaValidate.StringComparison", rule,
                            arguments));
                    }
                }
            }
        }
    }

    /** Returns the result of the initialisation, surrounded by a <code>&lt;script&gt;</code> tag. */
    public StringBuffer getClientValidation(boolean validateLive) {
        StringBuffer b = new StringBuffer();
        if (validationObjects.length() > 0) {
            b.append("<script type=\"text/javascript\">\n");
            b.append(validationObjects);

            b.append("function " + getValidationFunction() + " {\n");
            b.append("  valid = LiveValidation.massValidate( ").append(StringUtils.toString(definitionVarNames)).append(
                " );\n");
            b.append("  if (!valid) { alert('Please correct all form errors first!'); }\n");
            b.append("  return valid;\n");
            b.append("}\n");
            b.append("</script>\n");
        }

        return b;
    }

    private StringBuffer getValidationFunction() {
        return new StringBuffer("validateForm_").append(
            StringUtils.concatAsString(definitionVarNames.toArray(new Object[definitionVarNames.size()]))).append("()");
    }

    /**
     * returns the call for the onSubmit validation, e.g.:<br>
     * <code>function(e) { return LiveValidation.massValidate( [emailValidation, weightValidation, hobbiesValidation, ageValidation] );</code>
     */
    public StringBuffer getOnSubmitValidation() {
        if (definitionVarNames.size() > 0) {
            StringBuffer sb = new StringBuffer(getValidationFunction()).append(";");
            return sb;
        } else {
            return null;
        }
    }

    public String[] getNeededJavaScriptFileNames() {
        // the order gets reversed for some reason... therefore we reverse it here as well
        return new String[] { "prototype.js", "makumba-livevalidation.js", "livevalidation_1.3_standalone.js" };
    }

    protected String getValidationLine(String inputVarName, String validationType, ValidationRule rule, String arguments) {
        return getValidationLine(inputVarName, validationType, rule.getErrorMessage(), arguments);
    }

    protected String getValidationLine(String inputVarName, String validationType, String failureMessage) {
        return getValidationLine(inputVarName, validationType, failureMessage, "");
    }

    protected String getValidationLine(String inputVarName, String validationType, String failureMessage,
            String arguments) {
        return inputVarName + ".add( " + validationType + " , { " + arguments + " failureMessage: \"" + failureMessage
                + "\" } );\n";
    }

    private String getRangeLimits(RangeValidationRule rule) {
        String lower = rule.getLowerLimitString();
        String upper = rule.getUpperLimitString();
        return getRangeLimits(lower, upper);
    }

    protected String getRangeLimits(String lower, String upper) {
        String s = "";
        if (!lower.equals("?")) {
            s += "minimum: " + lower;
        }
        if (!upper.equals("?")) {
            if (s.length() > 0) {
                s += ", ";
            }
            s += "maximum: " + upper;
        }
        if (s.length() > 0) {
            s += ", ";
        }
        return s;
    }
}
