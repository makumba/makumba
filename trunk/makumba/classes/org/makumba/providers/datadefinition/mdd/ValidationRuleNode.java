package org.makumba.providers.datadefinition.mdd;

import antlr.collections.AST;


public class ValidationRuleNode extends MDDAST {
    
    /** name of the rule **/
    protected String name;
    
    /** type of the rule **/
    protected ValidationType type;
    
    public ValidationType getValidationType() {
        return type;
    }

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
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("== Validation rule: " + (name==null?"":name) + " (line "+ getLine() + ")\n");
        sb.append("== Type: " + type.getDescription() + "\n");
        sb.append("== Field: " + field);
        return sb.toString();
    }

}
