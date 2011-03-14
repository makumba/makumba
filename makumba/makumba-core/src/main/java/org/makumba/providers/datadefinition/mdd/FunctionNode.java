package org.makumba.providers.datadefinition.mdd;

import java.util.HashMap;
import java.util.Map;

import antlr.collections.AST;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.QueryFragmentFunction;
import org.makumba.providers.DataDefinitionProvider;

/**
 * AST node that collects information about a mdd function
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: FunctionNode.java,v 1.1 May 3, 2009 10:15:52 PM manu Exp $
 */
public class FunctionNode extends MDDAST {

    private static final long serialVersionUID = -7546695379163902054L;

    protected MDDNode mdd;

    protected String name;

    protected DataDefinition parameters;

    protected Map<Integer, String> deferredParameters;

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
        deferredParameters = new HashMap<Integer, String>();
    }

    public void addParameter(String paramName, FieldType type, String pointedType) {
        if (type == FieldType.PTR) {
            if(mdd.getName().equals(pointedType)) {
                deferredParameters.put(parameters.getFieldNames().size(), pointedType + "###" + paramName);
            } else {
                addPointerParam(paramName, pointedType, parameters);
            }
        } else {
            FieldDefinition fd = DataDefinitionProvider.getInstance().makeFieldOfType(paramName, type.getTypeName());
            parameters.addField(fd);
        }
    }

    private void addPointerParam(String paramName, String pointedType, DataDefinition parameters) {
        DataDefinition pointedDD = DataDefinitionProvider.getMDD(pointedType);
        parameters.addField(DataDefinitionProvider.getInstance().makeFieldWithName(paramName,
            pointedDD.getFieldDefinition(pointedDD.getIndexPointerFieldName())));
    }

    public boolean isDeferred() {
        return deferredParameters.size() > 0;
    }

    public void compileDeferred() {
        DataDefinition newParams = DataDefinitionProvider.getInstance().getVirtualDataDefinition(mdd.getName() + "." + name);
        for(int i = 0; i < parameters.getFieldNames().size() + deferredParameters.size(); i++) {
            if(deferredParameters.containsKey(i)) {
                String typeAndName = deferredParameters.get(i);
                String pointedType = typeAndName.substring(0, typeAndName.indexOf("###"));
                String paramName = typeAndName.substring(typeAndName.indexOf("###") + 3, typeAndName.length());
                addPointerParam(paramName, pointedType, newParams);
            } else {
                newParams.addField(parameters.getFieldDefinition(i));
            }
        }
        this.parameters = newParams;
    }

}
