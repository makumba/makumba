package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.makumba.DataDefinition;
import org.makumba.ValidationRule;

import antlr.CommonAST;

/**
 * AST node that collects information for building a {@link DataDefinition}
 * 
 * @author Manuel Gay
 * @version $Id: MDDNode.java,v 1.1 May 3, 2009 8:01:07 PM manu Exp $
 */
public class MDDNode extends CommonAST {
    
    final static String createName = "TS_create";

    final static String modifyName = "TS_modify";
        
    /** name of the data definition **/
    protected String name = "";
    
    /** name of the index field **/
    protected String indexName = "";
        
    /** the title field **/
    protected TitleFieldNode titleField = new TitleFieldNode();
    
    /** origin of the data definition **/
    protected URL origin;

    /** name of the parent MDD of the subfield **/
    protected String parent;
    
    /** name of the field in the parent MDD, if this is a ptrOne or setComplex **/
    protected String fieldNameInParent = "";

    /** indicator if this is MDD is a file subfield **/
    protected boolean isFileSubfield = false;
    
    protected LinkedHashMap<String, FieldNode> fields = new LinkedHashMap<String, FieldNode>();
    
    protected LinkedHashMap<String, ValidationRule> validationRules = new LinkedHashMap<String, ValidationRule>();
    
    public MDDNode(String name, URL origin) {
        this.name = name;
        this.origin = origin;
    }
    
    /** constructor for the creation of subfields **/
    public MDDNode(MDDNode parent, String subFieldName) {
        this.name = parent.name + "->" + subFieldName;
        this.origin = parent.origin;
        this.parent = parent.name;
        this.fieldNameInParent = subFieldName;
    }

    
    protected void setTitleField(TitleFieldNode title) {
        titleField = title;
        titleField.mdd = this;
    }
    
    public void addField(FieldNode fi) {
        fields.put(fi.name, fi);
    }
    

    public void removeField(String name) {
        if(fields.get(name) != null) {
            fields.remove(name);
        }
    }
    
    public void addValidationRule(ValidationRuleNode vn) {
        validationRules.put(vn.getRuleName(), vn);
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Type name: " + name + "\n");
        sb.append("Type origin: " + origin + "\n");
        if(parent !=null)
            sb.append("Type parent: " + parent + "\n");
        if(titleField != null) {
            sb.append("Title field: " + titleField.getText());
        }
        sb.append("\nFields:" + "\n");
        for(Iterator<String> i = fields.keySet().iterator(); i.hasNext();) {
            sb.append(fields.get(i.next()).toString() + "\n");
        }
        
        sb.append("\nValidation rules:" + "\n");
        for(Iterator<String> i = validationRules.keySet().iterator(); i.hasNext();) {
            sb.append(validationRules.get(i.next()).toString() + "\n");
        }
        
       
        
        return sb.toString();
        
    }


}
