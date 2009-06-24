package org.makumba.providers.datadefinition.mdd;

import java.util.HashMap;

import org.makumba.MakumbaError;

import antlr.collections.AST;

/**
 * Build walker that glues all the contents of the MDD together.
 * 
 * @author Manuel Gay
 * @version $Id: MDDBuildWalker.java,v 1.1 May 3, 2009 10:13:05 PM manu Exp $
 */
public class MDDBuildWalker extends MDDBuildBaseWalker {
    
    private MDDFactory factory = null;
    
    private HashMap<String, MDDAST> typeShorthands;
    
    public MDDBuildWalker(String typeName, MDDNode mdd, HashMap<String, MDDAST> typeShorthands, MDDFactory factory) {
        this.typeName = typeName;
        this.mdd = mdd;
        this.typeShorthands = typeShorthands;
        this.factory = factory;
    }
    
    @Override
    protected void processUnknownType(AST field) {
        FieldNode fieldNode = (FieldNode) field;
        MDDAST type = typeShorthands.get(fieldNode.unknownType);
        if(type == null) {
            factory.doThrow(this.typeName, "Unknown field type: "+fieldNode.unknownType, field);
        } else {
            fieldNode.makumbaType = type.makumbaType;
        }
        
        field = fieldNode;
        
    }
    
    @Override
    protected void checkTitleField(AST titleField) {
        TitleFieldNode title = (TitleFieldNode) titleField;
        
        // titleField can be a field name or a function
        switch(title.titleType) {
            case MDDTokenTypes.FIELD:
                Object field = title.mdd.fields.get(title.getText());
                if(field == null) {
                    factory.doThrow(this.typeName, "Field " + title.getText() + " does not exist in type " + title.mdd.name , titleField);
                }
                break;
            default:
                throw new MakumbaError("invalid title field type: " + title.titleType);
        }
        
    }
    
    

}
