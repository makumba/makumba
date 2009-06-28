package org.makumba.providers.datadefinition.mdd;

import java.io.Serializable;
import java.util.ArrayList;

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
    
    public ValidationRuleNode(MDDNode mdd, AST originAST, String ruleName) {
        this(mdd, originAST);
        this.name = ruleName;
    }
    
    
    /**
     * Checks if the type of the rule is compatible with the field types it referrs to
     */
    public void checkApplicability() {
        if(!type.checkApplicability(field.makumbaType)) {
            throw new RuntimeException("A " + getValidationType().getDescription() + " can only be assigned to fields of type " + getValidationType().getApplicableTypes() + " whereas the type of field " + field.name + " is " + field.makumbaType.getTypeName());
        }
    }
    
    // TODO name should be line or so
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
        sb.append("== Validation rule: " + (name==null?"":name) + " (line "+ getLine() + ")\n");
        sb.append("== Type: " + type.getDescription() + "\n");
        if(field != null)
            sb.append("== Field: " + field.name + "\n");
        sb.append("== Message: " + message +"\n");
        return sb.toString();
    }

    
}
