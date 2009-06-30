package org.makumba.providers.datadefinition.mdd;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.makumba.FieldInfo;

/**
 * AST node that collects information about a mdd function
 * @author Manuel Gay
 * @version $Id: FunctionNode.java,v 1.1 May 3, 2009 10:15:52 PM manu Exp $
 */
public class FunctionNode extends MDDAST {
    
    protected MDDNode mdd;
    
    protected String name;
    
    protected DataDefinition parameters;
    
    protected String queryFragment;
    
    protected String errorMessage;
    
    protected String sessionVariableName;
    
    public FunctionNode(MDDNode mdd, String name) {
        this.mdd = mdd;
        this.name = name;
        this.setType(MDDTokenTypes.FUNCTION);
        parameters = DataDefinitionProvider.getInstance().getVirtualDataDefinition(mdd.getName() + "." + name);

    }
    
    public void addParameter(String paramName, FieldType type, String pointedType) {
        if(type == FieldType.PTRREL) {
            DataDefinition pointedDD = MDDProvider.getMDD(pointedType);
            parameters.addField(DataDefinitionProvider.getInstance().makeFieldWithName(paramName, pointedDD.getFieldDefinition(pointedDD.getIndexPointerFieldName())));
        } else {
            FieldDefinition fd = DataDefinitionProvider.getInstance().makeFieldOfType(paramName, type.getTypeName());
            parameters.addField(fd);
        }
        
        
    }

}
