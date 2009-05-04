package org.makumba.providers.datadefinition.mdd;

import java.net.URL;
import java.util.Iterator;

import org.apache.commons.collections.map.LinkedMap;
import org.makumba.DataDefinition;

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
    
    
    
    protected LinkedMap fields = new LinkedMap();
    
    
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
    
    void addStandardFields(String name) {
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
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Type name: " + name + "\n");
        sb.append("Type origin: " + origin + "\n");
        if(parent !=null)
            sb.append("Type parent: " + parent.name + "\n");
        sb.append("Fields:" + "\n");
        for(Iterator<String> i = fields.asList().iterator(); i.hasNext();) {
            sb.append(fields.get(i.next()).toString() + "\n");
        }
        
        return sb.toString();
        
    }

}
