package org.makumba.providers.datadefinition.mdd;

import java.util.HashMap;

import org.hibernate.property.Getter;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;

import antlr.collections.AST;

/**
 * Build walker that glues all the contents of the MDD together. It walks over a simple tree of the kind
 * <pre>
 * MDDNode
 *   TitleFieldNode
 *   FieldNode
 *   FieldNode
 *   ...
 *   FieldNode
 *   ...
 *   ValidationNode
 *   ValidationNode
 *   ...
 *   FunctionNode
 *   FunctionNode
 * </pre>
 * 
 * and builds the {@link DataDefinition} and {@link FieldDefinition} objects.
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
            factory.doThrow("Unknown field type: "+fieldNode.unknownType, field);
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
                    factory.doThrow("Field " + title.getText() + " does not exist in type " + title.mdd.name , titleField);
                }
                break;
            default:
                throw new MakumbaError("should not be here");
        }
        
    }
    
    

}
