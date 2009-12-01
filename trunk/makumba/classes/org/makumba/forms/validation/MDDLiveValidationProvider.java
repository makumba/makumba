package org.makumba.forms.validation;

import java.util.Collection;

import org.makumba.ValidationRule;
import org.makumba.providers.datadefinition.mdd.MDDExpressionParser;
import org.makumba.providers.datadefinition.mdd.MDDTokenTypes;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;

/**
 * New LiveValidationProvider for the new MDD parser. Once we switch to the new parser, merge it with the
 * {@link LiveValidationProvider}
 * 
 * @author Manuel Gay
 * @version $Id: MDDLiveValidationProvider.java,v 1.1 Jul 13, 2009 12:20:12 PM manu Exp $
 */
public class MDDLiveValidationProvider extends LiveValidationProvider {

    private static final long serialVersionUID = -1887449782752264509L;

    @Override
    protected void addValidationRules(String inputName, String formIdentifier,
            Collection<ValidationRule> validationRules, StringBuffer validations, String inputVarName) {

        for (ValidationRule validationRule : validationRules) {
            ValidationRuleNode rule = (ValidationRuleNode) validationRule;

            switch (rule.getValidationType()) {

                case LENGTH:
                    validations.append(getValidationLine(inputVarName, "Validate.Length", rule, getRangeLimits(
                        rule.getLowerBound(), rule.getUpperBound())));
                    break;
                case RANGE:
                    validations.append(getValidationLine(inputVarName, "Validate.Numericality", rule, getRangeLimits(
                        rule.getLowerBound(), rule.getUpperBound())));
                    break;
                case REGEX:
                    validations.append(getValidationLine(inputVarName, "Validate.Format", rule, "pattern: /^"
                            + rule.getExpression() + "$/i, "));
                    break;
                case COMPARISON:

                    ComparisonValidationRule c = (ComparisonValidationRule) rule;
                    String lhs = c.getComparisonExpression().getLhs();
                    String rhs = c.getComparisonExpression().getRhs();
                    String operator = c.getComparisonExpression().getOperator();

                    // For comparison rules, if we do a greater / less than comparison, we have to check whether we are
                    // adding the live validation for the first or the second argument in the rule
                    // Depending on this, we have to invert the rule for the live validation provider by swapping the
                    // variable names & inverting the comparison operator
                    if (!(lhs + formIdentifier).equals(inputName) && !c.getComparisonExpression().isEqualityOperator()) {
                        String temp = lhs;
                        lhs = rhs;
                        rhs = temp;
                        operator = c.getComparisonExpression().getInvertedOperator();
                    }

                    String arguments = "element1: \"" + lhs + formIdentifier + "\", element2: \"" + rhs
                            + formIdentifier + "\", comparisonOperator: \"" + operator + "\", ";

                    switch (c.getComparisonExpression().getComparisonType()) {

                        // FIXME: need to implement date comparisons
                        case DATE:
                            continue;

                        case NUMBER:
                            validations.append(getValidationLine(inputVarName, "MakumbaValidate.NumberComparison",
                                rule, arguments));
                            break;

                        case STRING:
                            if (c.getComparisonExpression().getLhs_type() == MDDTokenTypes.UPPER
                                    || c.getComparisonExpression().getLhs_type() == MDDTokenTypes.LOWER) {
                                arguments += "functionToApply: \""
                                        + MDDExpressionParser._tokenNames[c.getComparisonExpression().getLhs_type()].toLowerCase()
                                        + "\", ";
                                validations.append(getValidationLine(inputVarName, "MakumbaValidate.StringComparison",
                                    rule, arguments));
                            }

                            break;
                    }

                    break;
            }
        }
    }

}
