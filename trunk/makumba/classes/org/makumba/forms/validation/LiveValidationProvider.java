package org.makumba.forms.validation;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
     * {@link #getOnSubmitValidation(boolean)}.
     */
    private HashSet<String> definitionVarNames = new HashSet<String>();

    /** initialises a field, basically does create the variables and calls for this field. */
    public void initField(String inputName, FieldDefinition fieldDefinition, boolean validateLive) {
        Collection validationRules = fieldDefinition.getValidationRules();// def.getValidationRules(inputName);
        int size = validationRules != null ? validationRules.size() : 1;
        StringBuffer validations = new StringBuffer(100 + size * 50);
        String inputVarName = inputName.replaceAll("\\.", "__") + "Validation";

        if (fieldDefinition == null) {
            System.out.println("null def for " + inputName);
        }
        
        if (fieldDefinition.isNotNull()) {
            validations.append(getValidationLine(inputVarName, "Validate.Presence", FieldDefinition.ERROR_NOT_NULL));
        }
        if (fieldDefinition.isIntegerType()) {
            validations.append(getValidationLine(inputVarName, "Validate.Numericality", FieldEditor.ERROR_NO_INT,
                "onlyInteger: true,"));
        } else if (fieldDefinition.isRealType()) {
            validations.append(getValidationLine(inputVarName, "Validate.Numericality", FieldEditor.ERROR_NO_INT));
        }
        if (validationRules != null) {
            for (Iterator iter = validationRules.iterator(); iter.hasNext();) {
                ValidationRule rule = (ValidationRule) iter.next();
                if (rule instanceof StringLengthValidationRule) {
                    validations.append(getValidationLine(inputVarName, "Validate.Length", rule, getRangeLimits(rule)));
                } else if (rule instanceof NumberRangeValidationRule) {
                    validations.append(getValidationLine(inputVarName, "Validate.Numericality", rule,
                        getRangeLimits(rule)));
                } else if (rule instanceof RegExpValidationRule) {
                    validations.append(getValidationLine(inputVarName, "Validate.Format", rule, "pattern: /^"
                            + ((RegExpValidationRule) rule).getRegExp() + "$/i, "));
                } else if (rule instanceof ComparisonValidationRule) {
                    ComparisonValidationRule c = (ComparisonValidationRule) rule;

                    // FIXME: need to implement date comparisions
                    if (c.getFieldDefinition().isDateType()) {
                        continue;
                    }

                    String arguments = "element1: \"" + c.getFieldName() + "\", element2: \"" + c.getOtherFieldName()
                            + "\", comparisonOperator: \"" + c.getCompareOperator() + "\", ";

                    if (c.getFieldDefinition().isNumberType()) {
                        validations.append(getValidationLine(inputVarName, "MakumbaValidate.NumberComparison", rule,
                            arguments));
                    } else if (c.getFieldDefinition().isDateType()) {
                        // todo: implement!
                    } else if (c.getFieldDefinition().isStringType()) {
                        if (c.getFunctionName() != null && c.getFunctionName().length() > 0) {
                            arguments += "functionToApply: \"" + c.getFunctionName() + "\", ";
                            validations.append(getValidationLine(inputVarName, "MakumbaValidate.StringComparison",
                                rule, arguments));
                        }
                    }
                }
            }
        }
        if (fieldDefinition.isUnique() && !fieldDefinition.isDateType( )) {
            validations.append(getValidationLine(inputVarName, "MakumbaValidate.Uniqueness", FieldDefinition.ERROR_NOT_UNIQUE,
                    "table: \""+fieldDefinition.getDataDefinition().getName()+"\", "+"field: \""+fieldDefinition.getName()+"\", "));
        }
        
        if (validations.length() > 0) {
            definitionVarNames.add(inputVarName);
            validationObjects.append("var " + inputVarName + " = new LiveValidation('" + inputName
                    + "', { validMessage: \" \" });\n");
            validationObjects.append(validations);
            validationObjects.append("\n");
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
            b.append("  if (!valid) {");
            b.append("    alert('Please correct all form errors first!');");
            b.append("  }\n");
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
    public StringBuffer getOnSubmitValidation(boolean validateLive) {
        if (definitionVarNames.size() > 0) {
            StringBuffer sb = new StringBuffer(getValidationFunction()).append(";");
            return sb;
        } else {
            return null;
        }
    }

    public String[] getNeededJavaScriptFileNames() {
        // the order gets reversed for some reason... therefore we reverse it here as well
        return new String[] { "makumba-livevalidation.js", "livevalidation_1.2_standalone.js" };
    }

    private String getValidationLine(String inputVarName, String validationType, ValidationRule rule, String arguments) {
        return getValidationLine(inputVarName, validationType, rule.getErrorMessage(), arguments);
    }

    private String getValidationLine(String inputVarName, String validationType, String failureMessage) {
        return getValidationLine(inputVarName, validationType, failureMessage, "");
    }

    private String getValidationLine(String inputVarName, String validationType, String failureMessage, String arguments) {
        return inputVarName + ".add( " + validationType + " , { " + arguments + " failureMessage: \"" + failureMessage
                + "\" } );\n";
    }

    private String getRangeLimits(ValidationRule rule) {
        String lower = ((RangeValidationRule) rule).getLowerLimitString();
        String upper = ((RangeValidationRule) rule).getUpperLimitString();
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
