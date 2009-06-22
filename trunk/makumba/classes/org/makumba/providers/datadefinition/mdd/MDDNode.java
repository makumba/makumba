package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;

import org.makumba.DataDefinition;
import org.makumba.MakumbaError;


import antlr.CommonAST;

/**
 * AST node that collects information for building a {@link DataDefinition}
 * 
 * @author Manuel Gay
 * @version $Id: MDDNode.java,v 1.1 May 3, 2009 8:01:07 PM manu Exp $
 */
public class MDDNode extends CommonAST {
    
    static final String createName = "TS_create";

    static final String modifyName = "TS_modify";
    
    /** name of the data definition **/
    protected String name;
    
    /** name of the index field **/
    protected String indexName;
    
    /** the title field **/
    protected TitleFieldNode titleField;
    
    /** origin of the data definition **/
    protected URL origin;
    
    /** parent of the data definition **/
    protected MDDNode parent;
    
    protected LinkedHashMap<String, FieldNode> fields = new LinkedHashMap<String, FieldNode>();
    
    protected LinkedHashMap<String, ValidationRuleNode> singleFieldValidationRules = new LinkedHashMap<String, ValidationRuleNode>();
    
    protected LinkedHashMap<String, ValidationRuleNode> namedValidationRules = new LinkedHashMap<String, ValidationRuleNode>();
    
    public MDDNode(String name, URL origin) {
        this.name = name;
        this.origin = origin;
        addStandardFields(name);
    }
    
    /** constructor for the creation of subfields **/
    public MDDNode(MDDNode parent, String subFieldName) {
        this.name = parent.name + "->" + subFieldName;
        this.origin = parent.origin;
        this.parent = parent;
    }
    
    
    /*
       RecordInfo(RecordInfo ri, String subfield) {
        // initStandardFields(subfield);
        name = ri.name;
        origin = ri.origin;
        this.subfield = subfield;
        this.papa = ri;
        // this.templateArgumentNames= ri.templateArgumentNames;
        ptrSubfield = papa.ptrSubfield + "->" + subfield;
        subfieldPtr = papa.subfieldPtr + subfield + "->";

     */
    
    protected void setTitleField(TitleFieldNode title) {
        title.mdd = this;
        titleField = title;
    }
    
    protected void addStandardFields(String name) {
        FieldNode fi;

        indexName = name;

        fi = new FieldNode(this, indexName);
        fi.makumbaType = FieldType.PTRINDEX;
        fi.description = "Unique index";
        fi.fixed = true;
        fi.notNull = true;
        fi.unique = true;
        addField(fi);

        fi = new FieldNode(this, modifyName);
        fi.makumbaType = FieldType.DATEMODIFY;
        fi.notNull = true;
        fi.description = "Last modification date";
        addField(fi);

        fi = new FieldNode(this, createName);
        fi.makumbaType = FieldType.DATECREATE;
        fi.description = "Creation date";
        fi.fixed = true;
        fi.notNull = true;
        addField(fi);
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
        switch(vn.type) {
            case LENGTH:
            case RANGE:
                singleFieldValidationRules.put(vn.field, vn);
                break;
            default:
                throw new MakumbaError("should not be here");
        }
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Type name: " + name + "\n");
        sb.append("Type origin: " + origin + "\n");
        if(parent !=null)
            sb.append("Type parent: " + parent.name + "\n");
        if(titleField != null) {
            sb.append("Title field: " + titleField.getText());
        }
        sb.append("\nFields:" + "\n");
        for(Iterator<String> i = fields.keySet().iterator(); i.hasNext();) {
            sb.append(fields.get(i.next()).toString() + "\n");
        }
        
        sb.append("\nValidation rules:" + "\n");
        for(Iterator<String> i = singleFieldValidationRules.keySet().iterator(); i.hasNext();) {
            sb.append(singleFieldValidationRules.get(i.next()).toString() + "\n");
        }
        
        for(Iterator<String> i = namedValidationRules.keySet().iterator(); i.hasNext();) {
            sb.append(singleFieldValidationRules.get(i.next()).toString() + "\n");
        }
        
        
        return sb.toString();
        
    }


}
