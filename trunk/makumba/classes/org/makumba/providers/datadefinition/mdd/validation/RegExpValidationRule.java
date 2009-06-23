package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;

import antlr.collections.AST;

public class RegExpValidationRule extends ValidationRuleNode {

    private static final long serialVersionUID = 1L;

    public RegExpValidationRule(MDDNode mdd, AST originAST, String field) {
        super(mdd, originAST, field);
        // TODO Auto-generated constructor stub
    }

}
