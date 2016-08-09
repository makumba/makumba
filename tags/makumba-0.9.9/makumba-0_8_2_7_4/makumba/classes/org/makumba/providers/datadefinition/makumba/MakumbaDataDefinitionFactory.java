package org.makumba.providers.datadefinition.makumba;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.providers.DataDefinitionProvider;

/**
 * This class is the Makumba implementation of a data definition provider, based on MDD files.<br>
 * TODO refactor together with RecordInfo to build objects (and not use static methods)
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class MakumbaDataDefinitionFactory extends DataDefinitionProvider {

    public DataDefinition getDataDefinition(String typeName) {
        return RecordInfo.getRecordInfo(typeName.replaceAll("__", "->"));
    }

    public DataDefinition getVirtualDataDefinition(String name) {
        return new RecordInfo(name.replaceAll("__", "->"));
    }

    public FieldDefinition makeFieldDefinition(String name, String definition) {
        return FieldInfo.getFieldInfo(name.replaceAll("__", "->"), definition.replaceAll("__", "->"), true);
    }

    public FieldDefinition makeFieldOfType(String name, String type) {
        return FieldInfo.getFieldInfo(name.replaceAll("__", "->"), type.replaceAll("__", "->"), false);
    }

    public FieldDefinition makeFieldOfType(String name, String type, String description) {
        return FieldInfo.getFieldInfo(name.replaceAll("__", "->"), type.replaceAll("__", "->"), false, description);
    }

    public FieldDefinition makeFieldWithName(String name, FieldDefinition type) {
        return FieldInfo.getFieldInfo(name, type, false);
    }

    public FieldDefinition makeFieldWithName(String name, FieldDefinition type, String description) {
        return FieldInfo.getFieldInfo(name, type, false, description);
    }

    private static class SingletonHolder implements org.makumba.commons.SingletonHolder {
        private static DataDefinitionProvider singleton = new MakumbaDataDefinitionFactory();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    public static DataDefinitionProvider getInstance() {
        return SingletonHolder.singleton;
    }
    
    private MakumbaDataDefinitionFactory() {
        
    }

}
