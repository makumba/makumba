package org.makumba.providers;

import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.commons.Configuration;

/**
 * This class is a facade for creating different kinds of DataDefinitionProviders. Its constructor knows from a
 * Configuration (or in the future maybe through other means) which implementation to use, and provides this
 * implementation methods to its client, without revealing the implementation used.
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class DataDefinitionProvider implements DataDefinitionProviderInterface {

    private DataDefinitionProviderInterface dataDefinitionProviderImplementation;

    /**
     * Default constructor, using the default Configuration.
     */
    public DataDefinitionProvider() {
        Configuration c = new Configuration();
        try {
            this.dataDefinitionProviderImplementation = (DataDefinitionProviderInterface) Class.forName(
                c.getDefaultDataDefinitionProviderClass()).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public DataDefinitionProvider(Configuration c) {
        try {
            this.dataDefinitionProviderImplementation = (DataDefinitionProviderInterface) Class.forName(
                c.getDefaultDataDefinitionProviderClass()).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public DataDefinition getDataDefinition(String typeName) {
        return dataDefinitionProviderImplementation.getDataDefinition(typeName);
    }

    public Vector getDataDefinitionsInLocation(String location) {
        return dataDefinitionProviderImplementation.getDataDefinitionsInLocation(location);
    }

    public DataDefinition getVirtualDataDefinition(String name) {
        return dataDefinitionProviderImplementation.getVirtualDataDefinition(name);
    }

    public FieldDefinition makeFieldDefinition(String name, String definition) {
        return dataDefinitionProviderImplementation.makeFieldDefinition(name, definition);
    }

    public FieldDefinition makeFieldOfType(String name, String type) {
        return dataDefinitionProviderImplementation.makeFieldOfType(name, type);
    }

    public FieldDefinition makeFieldOfType(String name, String type, String description) {
        return dataDefinitionProviderImplementation.makeFieldOfType(name, type, description);
    }

    public FieldDefinition makeFieldWithName(String name, FieldDefinition type) {
        return dataDefinitionProviderImplementation.makeFieldWithName(name, type);
    }

    public FieldDefinition makeFieldWithName(String name, FieldDefinition type, String description) {
        return dataDefinitionProviderImplementation.makeFieldWithName(name, type, description);
    }

    /**
     * This method finds a field definition with the given name within the given data definition. The difference to a
     * simple {@link DataDefinition#getFieldDefinition(String)} is that the field name can be of the form
     * field.subfield.otherSubfield, over an arbitrary number of levels.
     */
    public static final FieldDefinition getFieldDefinition(DataDefinition dd, String fieldName,
            String lineWithDefinition) throws DataDefinitionParseError {
        DataDefinition checkedDataDef = dd;

        // treat sub-fields
        int indexOf = -1;
        while ((indexOf = fieldName.indexOf(".")) != -1) {
            // we have a sub-record-field
            String subFieldName = fieldName.substring(0, indexOf);
            fieldName = fieldName.substring(indexOf + 1);
            checkedDataDef = checkedDataDef.getFieldDefinition(subFieldName).getPointedType();
        }

        FieldDefinition fd = checkedDataDef.getFieldDefinition(fieldName);
        if (fd == null) {
            throw new DataDefinitionParseError(checkedDataDef.getName(), "Field '" + fieldName
                    + "' not defined in type " + dd.getName() + "!", lineWithDefinition);
        }
        return fd;
    }

}
