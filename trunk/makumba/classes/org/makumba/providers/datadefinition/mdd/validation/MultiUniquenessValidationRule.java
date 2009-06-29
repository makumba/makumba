package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.ValidationType;

import antlr.collections.AST;

/**
 * A validation rule for multi-uniqueness keys
 * @version $Id: MultiUniquenessValidationRule.java,v 1.1 29.06.2009 14:18:03 gaym Exp $
 */
public class MultiUniquenessValidationRule extends ValidationRuleNode {

    public MultiUniquenessValidationRule(MDDNode mdd, AST originAST, ValidationType type) {
        super(mdd, originAST, type);
    }

    @Override
    public String getRuleName() {
        return "unique() { " + multiUniquenessFields.toString() + " } : " + message  + " (line " + getLine() + ")";
    }
}
