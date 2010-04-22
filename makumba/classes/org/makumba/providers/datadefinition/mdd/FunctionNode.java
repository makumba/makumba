package org.makumba.providers.datadefinition.mdd;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.providers.DataDefinitionProvider;

import antlr.collections.AST;

/**
 * AST node that collects information about a mdd function
 * @author Manuel Gay
 * @version $Id: FunctionNode.java,v 1.1 May 3, 2009 10:15:52 PM manu Exp $
 */
public class FunctionNode extends MDDAST {
    
    private static final long serialVersionUID = -7546695379163902054L;

    protected MDDNode mdd;
    
    protected String name;
    
    protected DataDefinition parameters;
    
    protected String queryFragment;
    
    protected String errorMessage;
    
    protected String sessionVariableName;

    protected QueryFragmentFunction function;
    
    public FunctionNode(MDDNode mdd, AST name) {
        initialize(name);
        
        // we need to overwrite the type after the initialisation
        this.setText(name.getText());
        this.mdd = mdd;
        this.name = name.getText();
        this.setType(MDDTokenTypes.FUNCTION);
        parameters = DataDefinitionProvider.getInstance().getVirtualDataDefinition(mdd.getName() + "." + name);

    }
    
    public void addParameter(String paramName, FieldType type, String pointedType) {
        if(type == FieldType.PTR) {
            DataDefinition pointedDD = MDDProvider.getMDD(pointedType);
            parameters.addField(DataDefinitionProvider.getInstance().makeFieldWithName(paramName, pointedDD.getFieldDefinition(pointedDD.getIndexPointerFieldName())));
        } else {
            FieldDefinition fd = DataDefinitionProvider.getInstance().makeFieldOfType(paramName, type.getTypeName());
            parameters.addField(fd);
        }
        
        
    }

}
