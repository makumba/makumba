package org.makumba.providers.datadefinition.mdd;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;

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
    
    private String typeName;
    
    private MDDAnalyzeWalker walker;

    public MDDBuildWalker(String typeName, MDDAnalyzeWalker analysisWalker) {
        this.typeName = typeName;
        this.walker = analysisWalker;
    }

}
