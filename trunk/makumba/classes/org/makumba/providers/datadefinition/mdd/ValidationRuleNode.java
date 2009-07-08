package org.makumba.providers.datadefinition.mdd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ValidationRule;
import org.makumba.providers.datadefinition.mdd.validation.ComparisonValidationRule;

import antlr.collections.AST;


public class ValidationRuleNode extends MDDAST implements ValidationRule, Serializable {
    
    /** name of the rule **/
    protected String name;
    
    /** type of the rule **/
    protected ValidationType type;
    
    /** message **/
    protected String message;
    
    /** field the rule applies to **/
    protected FieldNode field;
    
    /** the parent MDD **/
    protected MDDNode mdd;
    
    
    /** range validation limits **/
    protected String lowerBound;
    protected String upperBound;
    
    /** multi-uniqueness constraints **/
    protected ArrayList<String> multiUniquenessFields = new ArrayList<String>();
    
    /** arguments of the comparison rule **/
    protected ArrayList<String> arguments = new ArrayList<String>();

    /** regex expression **/
    protected String expression;
    
    /** comparison expression **/
    protected ComparisonExpressionNode comparisonExpression;
        
    public ValidationRuleNode(MDDNode mdd, AST originAST) {
        initialize(originAST);
     
        // we need to overwrite the type after the initialisation
        setType(MDDTokenTypes.VALIDATION);
        this.mdd = mdd;
    }
    
    public ValidationRuleNode(MDDNode mdd, AST originAST, FieldNode field) {
        this(mdd, originAST);
        this.field = field;
    }
    
    public ValidationRuleNode(MDDNode mdd, AST originAST, ValidationType type) {
        this(mdd, originAST);
        this.type = type;
        this.name = type.getDescription();
    }
    
    
    /**
     * Checks if the type of the rule is compatible with the field types it referrs to
     */
    public void checkApplicability() {
        if(!type.checkApplicability(field.makumbaType)) {
            throw new RuntimeException("A " + getValidationType().getDescription() + " can only be assigned to fields of type " + getValidationType().getApplicableTypes() + " whereas the type of field " + field.name + " is " + field.makumbaType.getTypeName());
        }
    }
    
    // should be overridden in subclasses to represent the rule depending on its type
    public String getRuleName() {
        return this.name;
    }
    
    public ValidationType getValidationType() {
        return type;
    }
   
    public String getErrorMessage() {
        return message;
    }

    public FieldDefinition getFieldDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    /** should be overriden in extended classes **/
    public boolean validate(Object value) throws InvalidValueException {
        return false;
    }
    
    /**
     * We order the rules such that comparison rules come last. This is important for live validation, where first the
     * validity of each field by itself should be checked.
     */
    public int compareTo(ValidationRule o) {
        if (this instanceof ComparisonValidationRule) {
            return 1;
        } else if (o instanceof ComparisonValidationRule) {
            return -1;
        } else {
            return 0;
        }
    }

    /** Throw a default exception. */
    protected void throwException() throws InvalidValueException {
        throw new InvalidValueException(field.name, getErrorMessage());
    }

    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("== Validation rule: " + getRuleName() + "\n");
        return sb.toString();
    }

    
}
