package org.makumba.providers.datadefinition.mdd;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.ValidationRule;

import antlr.collections.AST;


public class ValidationRuleNode extends MDDAST implements ValidationRule {
    
    /** name of the rule **/
    protected String name;
    
    /** type of the rule **/
    protected ValidationType type;
    
    /** message **/
    protected String message;
    
    /** field the rule applies to **/
    protected String field;
    
    /** the parent MDD **/
    protected MDDNode mdd;
    
    
    /** range validation limits **/
    protected String lowerBound;
    protected String upperBound;
    
    
    public ValidationRuleNode(MDDNode mdd, AST originAST) {
        initialize(originAST);
     
        // we need to overwrite the type after the initialisation
        setType(MDDTokenTypes.VALIDATION);
        
        this.mdd = mdd;
    }
    
    public ValidationRuleNode(MDDNode mdd, AST originAST, String field) {
        this(mdd, originAST);
        this.field = field;
    }
    
    
    /**
     * Checks if the type of the rule is compatible with the field types it referrs to
     */
    public void checkApplicability() {
        FieldType makType = ((FieldNode)mdd.fields.get(field)).makumbaType;
        if(!type.checkApplicability(makType)) {
            throw new RuntimeException("A " + getValidationType().getDescription() + " can only be assigned to fields of type " + getValidationType().getApplicableTypes() + " whereas the type of field " + field + " is " + makType.getTypeName());
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

    public boolean validate(Object value) throws InvalidValueException {
        // TODO Auto-generated method stub
        return false;
    }

    public int compareTo(ValidationRule o) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("== Validation rule: " + (name==null?"":name) + " (line "+ getLine() + ")\n");
        sb.append("== Type: " + type.getDescription() + "\n");
        sb.append("== Field: " + field + "\n");
        sb.append("== Message: " + message);
        return sb.toString();
    }

    
}
