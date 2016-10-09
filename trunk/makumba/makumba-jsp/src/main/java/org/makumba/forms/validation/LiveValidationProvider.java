package org.makumba.forms.validation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.makumba.FieldDefinition;
import org.makumba.FieldDefinition.FieldErrorMessageType;
import org.makumba.ValidationRule;
import org.makumba.commons.StringUtils;
import org.makumba.forms.html.FieldEditor;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.mdd.MDDExpressionBaseParser;
import org.makumba.providers.datadefinition.mdd.MDDExpressionBaseParserTokenTypes;
import org.makumba.providers.datadefinition.mdd.MDDTokenTypes;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;

/**
 * This class implements java-script based client side validation
 * 
 * @author Rudolf Mayer
 * @author Oscar Marginean
 * @version $Id: LiveValidationProvider.java,v 1.1 15.09.2007 13:32:07 Rudolf
 *          Mayer Exp $
 */
public class LiveValidationProvider implements ClientsideValidationProvider, Serializable {

    private static final long serialVersionUID = 1L;

    private transient DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

	/**
	 * classes to be appended to the form class attribute
	 */
	private Set<String> formValidationClasses = new LinkedHashSet<String>(Arrays.asList("mak-live-validation"));

	@Override
	public String getFieldValidationRules(String inputName, String formIdentifier, FieldDefinition fieldDefinition,
			boolean validateLive) {
		inputName = inputName + formIdentifier;
		Set<String> rules = new LinkedHashSet<String>();
		Collection<ValidationRule> validationRules = ddp.getValidationRules(fieldDefinition);

		if (fieldDefinition == null) {
			System.out.println("Null field definition for " + inputName);
		}

		// add validation rules automatically inferred from field modifiers -
		// not null, not empty, numerically
		if (fieldDefinition.isNotNull() && !fieldDefinition.isDateType()) {
			String failureMessage = fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_NULL) != null
					? fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_NULL)
					: FieldDefinition.ERROR_NOT_NULL;

			rules.add(getValidationRulesJSON("presence", failureMessage));
		} else if (fieldDefinition.isNotEmpty()) { // add a length validation,
			String failureMessage = fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_EMPTY) != null
					? fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_EMPTY)
					: FieldDefinition.ERROR_NOT_EMPTY;
			// minimum length 1
			rules.add(getValidationRulesJSON("length", failureMessage, getRangeLimits("1", "?")));
		}

		if (fieldDefinition.isIntegerType()) {
			String failureMessage = fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_INT) != null
					? fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_INT) : FieldEditor.ERROR_NO_INT;
			rules.add(getValidationRulesJSON("numericality", failureMessage, "\"onlyInteger\": true,"));

		} else if (fieldDefinition.isRealType()) {
			String failureMessage = fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_REAL) != null
					? fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_REAL) : FieldEditor.ERROR_NO_REAL;
			rules.add(getValidationRulesJSON("numericality", failureMessage));
		}

		if (fieldDefinition.isUnique() && !fieldDefinition.isDateType()) {
			String failureMessage = fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_UNIQUE) != null
					? fieldDefinition.getErrorMessage(FieldErrorMessageType.NOT_UNIQUE)
					: FieldDefinition.ERROR_NOT_UNIQUE;
			rules.add(
					getValidationRulesJSON("uniqueness", failureMessage,
							String.format("\"table\": \"%s\", \"field\":\"%s\",",
							fieldDefinition.getDataDefinition().getName(), fieldDefinition.getName())));
		}

		if (validationRules != null) {
			rules.addAll(getExtraValidationRules(inputName, formIdentifier, validationRules));
		}

		return rules.size() > 0 ? String.format("{%s}", StringUtils.concatAsString(rules, ",")).replace("\\", "\\\\")
				: null;
	}

	/**
	 * Get custom validation rules defined in the MDD
	 * 
	 * @param inputName
	 *            the name of the input being processed
	 * @param formIdentifier
	 *            the identifier of the form
	 * @param validationRules
	 *            custom validation rules
	 * @return a set of custom validation rules
	 */
	protected Set<String> getExtraValidationRules(String inputName, String formIdentifier,
			Collection<ValidationRule> validationRules) {

		Set<String> rules = new LinkedHashSet<String>();

		for (ValidationRule validationRule : validationRules) {
			ValidationRuleNode rule = (ValidationRuleNode) validationRule;

			switch (rule.getValidationType()) {

			case LENGTH:
				rules.add(getValidationRulesJSON("length", rule.getErrorMessage(),
						getRangeLimits(rule.getLowerBound(), rule.getUpperBound())));
				break;
			case RANGE:
				rules.add(getValidationRulesJSON("numericality", rule.getErrorMessage(),
						getRangeLimits(rule.getLowerBound(), rule.getUpperBound())));
				break;
			case REGEX:
				rules.add(getValidationRulesJSON("format", rule.getErrorMessage(),
						"\"pattern\": " + formatRegularExpression(rule.getExpression()) + ","));
				break;
				
			// case UNIQUENESS:
			// rules.add(getValidationRulesJSON("uniqueness",
			// rule.getErrorMessage(),
			// "\"fields\": [\"" +
			// StringUtils.concatAsString(rule.getValidationRuleArguments(),
			// "\", \"")
			// + "\"],"));
			// break;

			case COMPARISON:

				ComparisonValidationRule c = (ComparisonValidationRule) rule;
				String lhs = c.getComparisonExpression().getLhs();
				String rhs = c.getComparisonExpression().getRhs();
				Integer rhs_type = c.getComparisonExpression().getRhs_type();
				// getLhs_type
				String operator = c.getComparisonExpression().getOperator();

				// For comparison rules, if we do a greater / less than
				// comparison, we have to check whether we are
				// adding the live validation for the first or the second
				// argument in the rule
				// Depending on this, we have to invert the rule for the live
				// validation provider by swapping the
				// variable names & inverting the comparison operator
				if (!(lhs + formIdentifier).equals(inputName) && !c.getComparisonExpression().isEqualityOperator()) {
					lhs = c.getComparisonExpression().getRhs();
					rhs = c.getComparisonExpression().getLhs();
					rhs_type = c.getComparisonExpression().getLhs_type();
					operator = c.getComparisonExpression().getInvertedOperator();
				}

				String arguments = "";
				if (rhs_type == MDDExpressionBaseParserTokenTypes.POSITIVE_INTEGER
						|| rhs_type == MDDExpressionBaseParserTokenTypes.NEGATIVE_INTEGER) {
					arguments = "\"value\": " + rhs;
				} else {
					arguments = "\"field\": \"" + rhs + "\"";
				}

				arguments += ", \"comparisonOperator\": \"" + operator + "\", ";

				switch (c.getComparisonExpression().getComparisonType()) {

				// FIXME: need to implement date comparisons
				case DATE:
					continue;

				case NUMBER:
					rules.add(getValidationRulesJSON("numberComparison", rule.getErrorMessage(), arguments));
					break;

				case STRING:
					if (c.getComparisonExpression().getLhs_type() == MDDTokenTypes.UPPER
							|| c.getComparisonExpression().getLhs_type() == MDDTokenTypes.LOWER) {
						arguments += "\"functionToApply\": \""
								+ MDDExpressionBaseParser._tokenNames[c.getComparisonExpression().getLhs_type()]
										.toLowerCase()
								+ "\", ";

						rules.add(getValidationRulesJSON("stringComparison", rule.getErrorMessage(), arguments));
					}

					break;
				default:
				}

				break;
			default:
			}
		}
		
		return rules;
	}

    /**
     * Formats a regular expression to be used in JavaScript.<br/>
     * The transformations are:
     * <ul>
     * <li>JavaScript regexp patterns are enclosed in / /</li>
     * <li>the regexp is augmented by a ^ prefix and $ suffix, to match line begin and end, respectively.</li>
     * <li>i is added in the end, to match case-insensitive</li>
     * </ul>
     * 
     * @return the given expression, with a prefix /^ and a suffix $/i
     */
    @Override
    public String formatRegularExpression(String expression) {
		return "\"/^" + expression + "$/i\"";
    }

    @Override
    public String[] getNeededJavaScriptFileNames() {
        // the order gets reversed for some reason... therefore we reverse it here as well
		return new String[] { "makumba-livevalidation.js" };
    }

	protected String getValidationRulesJSON(String validationType, String failureMessage) {
		return getValidationRulesJSON(validationType, failureMessage, "");
	}

	protected String getValidationRulesJSON(String validationType, String failureMessage, String arguments) {
		return String.format("\"%s\": {%s \"failureMessage\":\"%s\"}", validationType, arguments, failureMessage);
	}

    protected String getRangeLimits(String lower, String upper) {
        String s = "";
        if (!lower.equals("?")) {
			s += "\"minimum\": " + lower;
        }
        if (!upper.equals("?")) {
            if (s.length() > 0) {
                s += ", ";
            }
			s += "\"maximum\": " + upper;
        }
        if (s.length() > 0) {
            s += ", ";
        }
        return s;
    }

	@Override
	public String getFormValidationClasses() {
		return StringUtils.concatAsString(formValidationClasses, " ");
	}

}
