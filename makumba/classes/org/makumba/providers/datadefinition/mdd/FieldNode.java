package org.makumba.providers.datadefinition.mdd;

import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.makumba.DataDefinition;

import antlr.CommonAST;

/**
 * AST node that collects information about a MDD field
 * 
 * @author Manuel Gay
 * @version $Id: FieldNode.java,v 1.1 May 3, 2009 6:14:27 PM manu Exp $
 */
public class FieldNode extends CommonAST {
    
    public FieldNode(MDDNode mdd, String name) {
        this.mdd = mdd;
        this.name = name;
    }
    
    // basic field info
    protected MDDNode mdd;
    
    protected String name;

    protected FieldType makumbaType;
    
    protected String description;

    // modifiers
    protected boolean fixed;

    protected boolean notNull;

    protected boolean notEmpty;

    protected boolean unique;

    
    // intEnum
    private DualHashBidiMap intEnumValues = new DualHashBidiMap();

    private DualHashBidiMap intEnumValuesDeprecated = new DualHashBidiMap();
    
    public void addIntEnumValue(int index, String text) {
        intEnumValues.put(index, text);
    }

    public void addIntEnumValueDeprecated(int index, String text) {
        intEnumValuesDeprecated.put(index, text);
    }
    
    // subfield - ptrOne, setComplex
    protected DataDefinition subfield;
    

}
