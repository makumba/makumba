package org.makumba.providers.datadefinition.mdd;

import org.apache.commons.collections.bidimap.DualHashBidiMap;

import antlr.collections.AST;

/**
 * AST node that collects information about a MDD field
 * 
 * @author Manuel Gay
 * @version $Id: FieldNode.java,v 1.1 May 3, 2009 6:14:27 PM manu Exp $
 */
public class FieldNode extends MDDAST {
    
    private static final long serialVersionUID = 1L;

    // basic field info
    protected MDDNode mdd;
    
    protected String name;

    protected FieldType makumbaType;
    
    protected String description;
    
    // for unknown mak type, probably macro type
    protected String unknownType;

    // modifiers
    protected boolean fixed;

    protected boolean notNull;

    protected boolean notEmpty;

    protected boolean unique;

    
    // intEnum
    private DualHashBidiMap intEnumValues = new DualHashBidiMap();

    private DualHashBidiMap intEnumValuesDeprecated = new DualHashBidiMap();
    
    // char length
    protected int charLength;
    
    // pointed type
    protected String pointedType;
    
    // subfield - ptrOne, setComplex
    protected MDDNode subfield;

    public FieldNode(MDDNode mdd, String name) {
        
        // AST
        setText(name);
        setType(MDDTokenTypes.FIELD);
        
        this.mdd = mdd;
        this.name = name;
        
    }
    
    /**
     * Constructor for FieldNode, originAST being an AST used for giving context to errors
     */
    public FieldNode(MDDNode mdd, String name, AST originAST) {
        initialize(originAST);
        
        // we need to overwrite the type after the initialisation
        setText(name);
        setType(MDDTokenTypes.FIELD);
        
        this.mdd = mdd;
        this.name = name;
        
    }
    
    
    public void addIntEnumValue(int index, String text) {
        intEnumValues.put(index, text);
    }

    public void addIntEnumValueDeprecated(int index, String text) {
        intEnumValuesDeprecated.put(index, text);
    }
        
    public MDDNode initSubfield() {
        if(this.subfield == null) {
            this.subfield = new MDDNode(mdd, this.name);
        }
        return this.subfield;
    }
    
    public void addSubfield(FieldNode subfield) {
        this.subfield.addField(subfield);
    }   
    
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("== Field name: " + name + " (line "+ getLine() + ")\n");
        if(makumbaType != null) {
            sb.append("== Field type: " + makumbaType.getTypeName() + "\n");
        } else {
            sb.append("== Unknown field type: " + unknownType + "\n");
        }
        sb.append("== Modifiers: " + (fixed? "fixed ":"") + (unique? "unique ":"") + (notNull? "not null ":"") + (notEmpty? "not empty ":"")  + "\n");
        if(description != null) sb.append("== Description: "+ description + "\n");
        if(subfield != null) {
            sb.append("\n== Subfield detail" + "\n\n");
            sb.append(subfield.toString() + "\n");
        }
        return sb.toString();
    }

}
