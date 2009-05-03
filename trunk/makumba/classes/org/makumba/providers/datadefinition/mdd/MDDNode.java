package org.makumba.providers.datadefinition.mdd;

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
    
    
    
    protected LinkedMap fields = new LinkedMap();
    
    
    public MDDNode(String name) {
        this.name = name;
        addStandardFields(name);
    }
    
    void addStandardFields(String name) {
        FieldNode fi;

        indexName = name;

        fi = new FieldNode(this, indexName);
        fi.makumbaType = FieldType.PTRINDEX;
        fi.description = "Unique index";
        fi.fixed = true;
        fi.notNull = true;
        fi.unique = true;
        addField1(fi);

        fi = new FieldNode(this, modifyName);
        fi.makumbaType = FieldType.DATEMODIFY;
        fi.notNull = true;
        fi.description = "Last modification date";
        addField1(fi);

        fi = new FieldNode(this, createName);
        fi.makumbaType = FieldType.DATECREATE;
        fi.description = "Creation date";
        fi.fixed = true;
        fi.notNull = true;
        addField1(fi);
    }
    
    private void addField1(FieldNode fi) {
        fields.put(fi.name, fi);
    }

    
    

    
    

}
