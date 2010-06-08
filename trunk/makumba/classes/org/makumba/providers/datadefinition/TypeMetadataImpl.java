package org.makumba.providers.datadefinition;

import org.makumba.DataDefinition;
import org.makumba.MakumbaError;
import org.makumba.TypeMetadata;
import org.makumba.providers.DataDefinitionProvider;

public class TypeMetadataImpl implements TypeMetadata {

    private final DataDefinitionProvider ddp;

    private Class<?> clazz;

    private DataDefinition dd;

    public Class<?> getClazz() {
        return this.clazz;
    }

    public DataDefinition getDataDefinition() {
        return this.dd;
    }

    public TypeMetadataImpl(String typeName) {
        ddp = DataDefinitionProvider.getInstance();
        this.dd = ddp.getDataDefinition(typeName);
        try {
            this.clazz = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new MakumbaError("Could not find class of type '" + typeName + "'");
        }

    }

}
