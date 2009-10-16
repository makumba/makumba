package org.makumba.providers.datadefinition.mdd.validation;

import org.makumba.providers.datadefinition.mdd.FieldNode;
import org.makumba.providers.datadefinition.mdd.MDDNode;
import org.makumba.providers.datadefinition.mdd.ValidationRuleNode;
import org.makumba.providers.datadefinition.mdd.ValidationType;

import antlr.collections.AST;

/**
 * A validation rule for multi-uniqueness keys
 * @version $Id: MultiUniquenessValidationRule.java,v 1.1 29.06.2009 14:18:03 gaym Exp $
 */
public class MultiUniquenessValidationRule extends ValidationRuleNode {

    private static final long serialVersionUID = 6912344546110275697L;

    public MultiUniquenessValidationRule(MDDNode mdd, AST originAST, ValidationType type, FieldNode parentField) {
        super(mdd, originAST, type, parentField);
        this.type = ValidationType.UNIQUENESS;
    }

    @Override
    public String getRuleName() {
        return "unique() { " + multiUniquenessFields.toString() + " } : " + message  + " (line " + getLine() + ")";
    }
}
