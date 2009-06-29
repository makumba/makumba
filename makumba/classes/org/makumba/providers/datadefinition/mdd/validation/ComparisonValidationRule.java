package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.ValidationType;

import antlr.collections.AST;

public class ComparisonValidationRule extends ValidationRuleNode {

    private static final long serialVersionUID = 1L;

    
    public ComparisonValidationRule(MDDNode mdd, AST originAST, ValidationType type) {
        super(mdd, originAST, type);
    }


}
