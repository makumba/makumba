package org.makumba.forms.validation;

import java.util.Collection;

import org.makumba.ValidationRule;
import org.makumba.providers.datadefinition.mdd.MDDExpressionParser;
import org.makumba.providers.datadefinition.mdd.MDDTokenTypes;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;

/**
 * New LiveValidationProvider for the new MDD parser. Once we switch to the new parser, merge it with the {@link LiveValidationProvider}
 * @author Manuel Gay
 * @version $Id: MDDLiveValidationProvider.java,v 1.1 Jul 13, 2009 12:20:12 PM manu Exp $
 */
public class MDDLiveValidationProvider extends LiveValidationProvider {
    
    private static final long serialVersionUID = -1887449782752264509L;

    @Override
    protected void addValidationRules(String formIdentifier, Collection<ValidationRule> validationRules,
            StringBuffer validations, String inputVarName) {
        
        for (ValidationRule validationRule : validationRules) {
            ValidationRuleNode rule = (ValidationRuleNode) validationRule;
            
            switch(rule.getValidationType()) {
                
                case LENGTH:
                    validations.append(getValidationLine(inputVarName, "Validate.Length", rule,
                        getRangeLimits(rule.getLowerBound(), rule.getUpperBound())));
                    break;
                case RANGE:
                validations.append(getValidationLine(inputVarName, "Validate.Numericality", rule,
                    getRangeLimits(rule.getLowerBound(), rule.getUpperBound())));
                    break;
                case REGEX:
                    validations.append(getValidationLine(inputVarName, "Validate.Format", rule, "pattern: /^"
                        + rule.getExpression() + "$/i, "));
                    break;
                case COMPARISON:
                    
                    ComparisonValidationRule c = (ComparisonValidationRule) rule;

                    String arguments = "element1: \"" + c.getComparisonExpression().getLhs() + formIdentifier + "\", element2: \""
                    + c.getComparisonExpression().getRhs() + formIdentifier + "\", comparisonOperator: \""
                    + c.getComparisonExpression().getOperator() + "\", ";
                    
                    switch(c.getComparisonExpression().getComparisonType()) {
                        
                        // FIXME: need to implement date comparisons
                        case DATE:
                        continue;
                        
                        case NUMBER:
                            validations.append(getValidationLine(inputVarName, "MakumbaValidate.NumberComparison", rule,
                                arguments));
                            break;
                            
                        case STRING:
                            if (c.getComparisonExpression().getLhs_type() == MDDTokenTypes.UPPER || c.getComparisonExpression().getLhs_type() == MDDTokenTypes.LOWER) {
                                arguments += "functionToApply: \"" + MDDExpressionParser._tokenNames[c.getComparisonExpression().getLhs_type()].toLowerCase() + "\", ";
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
