package org.makumba.providers.datadefinition.mdd;

public class ValidationNode extends MDDAST {
    
    /** name of the rule **/
    private String name;
    
    /** type of the rule **/
    private ValidationType type;
    
    /** field the rule applies to **/
    private String field;
    
    /** the parent MDD **/
    private MDDNode mdd;
    
    public ValidationNode(MDDNode mdd) {
        this.mdd = mdd;
    }
    

}
