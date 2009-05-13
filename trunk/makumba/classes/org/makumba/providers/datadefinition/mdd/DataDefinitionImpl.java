package org.makumba.providers.datadefinition.mdd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Vector;

import org.apache.commons.collections.map.ListOrderedMap;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.ValidationDefinition;

public class DataDefinitionImpl implements DataDefinition {
    
    private ListOrderedMap fields = new ListOrderedMap();

    public void addField(FieldDefinition fd) {
        // TODO Auto-generated method stub
            

    }

    public void addFunction(String name, QueryFragmentFunction function) {
        // TODO Auto-generated method stub

    }

    public void addMultiUniqueKey(MultipleUniqueKeyDefinition definition) {
        // TODO Auto-generated method stub

    }

    public void checkFieldNames(Dictionary<String, Object> d) {
        // TODO Auto-generated method stub

    }

    public void checkUpdate(String fieldName, Dictionary<String, Object> d) {
        // TODO Auto-generated method stub

    }

    public Collection<QueryFragmentFunction> getActorFunctions() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getCreationDateFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDefinition getFieldDefinition(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDefinition getFieldDefinition(int n) {
        // TODO Auto-generated method stub
        return null;
    }

    public Vector<String> getFieldNames() {
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDefinition getFieldOrPointedFieldDefinition(String nm) {
        // TODO Auto-generated method stub
        return null;
    }

    public QueryFragmentFunction getFunction(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<QueryFragmentFunction> getFunctions() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getIndexPointerFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getLastModificationDateFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public MultipleUniqueKeyDefinition[] getMultiFieldUniqueKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

    public FieldDefinition getParentField() {
        // TODO Auto-generated method stub
        return null;
    }

    public ArrayList<FieldDefinition> getReferenceFields() {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<QueryFragmentFunction> getSessionFunctions() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSetMemberFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getSetOwnerFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getTitleFieldName() {
        // TODO Auto-generated method stub
        return null;
    }

    public ArrayList<FieldDefinition> getUniqueFields() {
        // TODO Auto-generated method stub
        return null;
    }

    public ValidationDefinition getValidationDefinition() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean hasMultiUniqueKey(String[] fieldNames) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isTemporary() {
        // TODO Auto-generated method stub
        return false;
    }

    public long lastModified() {
        // TODO Auto-generated method stub
        return 0;
    }

}
