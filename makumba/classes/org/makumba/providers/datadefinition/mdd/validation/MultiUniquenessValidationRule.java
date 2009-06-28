package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;

import antlr.collections.AST;

public class MultiUniquenessValidationRule extends ValidationRuleNode {

    public MultiUniquenessValidationRule(MDDNode mdd, AST originAST, String ruleName) {
        super(mdd, originAST, ruleName);
    }

}
