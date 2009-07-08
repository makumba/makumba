package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.InvalidValueException;
import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.ValidationType;

import antlr.collections.AST;

public class ComparisonValidationRule extends ValidationRuleNode {

    private static final long serialVersionUID = 1L;

    
    public ComparisonValidationRule(MDDNode mdd, AST originAST, ValidationType type) {
        super(mdd, originAST, type);
    }
    
    @Override
    public String getRuleName() {
        return "compare() { " + comparisonExpression.toString() + " } : " + message + " (line " + getLine() + ")";
    }
    
    @Override
    public boolean validate(Object value) throws InvalidValueException {
        
        // TODO implement this
        // we have all the necessary data in the comparisonExpressionNode
        // we now need to take the arguments of the validation expression and assign the values
        // so we have
        // compare(birthdate) { birthdate >= date($now, $now, $now - 105, 0, 0, 2) } : "Maximum age is 105!"
        // and we get a value, so we use that value on the operand that corresponds to the argument name
        // another possibility is that we have
        // compare() { firstSex >= birthdate } : "Can't have sex before you'are born!"
        // then we get two values, but which one is which?
        // ==> we need to pass the value and its field/expression name when calling this rule
        // in fact, when such a rule is executed, we have to retrieve the values according to the field names
        // and then we pass the values along with their names to the rule, and it can process them
        
        
        return false;
    
    }

}
