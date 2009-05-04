package org.makumba.providers.datadefinition.mdd;

import java.util.HashMap;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;

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
    
    private HashMap<String, AnalysisAST> typeShorthands;
    
    public MDDBuildWalker(String typeName, MDDNode mdd, HashMap<String, AnalysisAST> typeShorthands) {
        this.typeName = typeName;
        this.mdd = mdd;
        this.typeShorthands = typeShorthands;
    }
    
    @Override
    protected void processUnknownType(FieldNode field) {
        AnalysisAST type = typeShorthands.get(field.unknownType);
        if(type == null) {
            throw new DataDefinitionParseError("Unknown field type: "+field.unknownType);
        } else {
            field.makumbaType = type.makumbaType;
        }
        
    }

}
