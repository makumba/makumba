package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.providers.datadefinition.mdd.FieldNode;
import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;

import antlr.collections.AST;

public class RangeValidationRule extends ValidationRuleNode {

    public RangeValidationRule(MDDNode mdd, AST originAST, FieldNode field) {
        super(mdd, originAST, field);
    }

    private static final long serialVersionUID = 1L;

}
