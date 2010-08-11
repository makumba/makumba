package org.makumba.jsf.update;

import java.util.HashMap;
import java.util.Map;

import org.makumba.DataDefinition;
import org.makumba.Pointer;

public class ObjectInputValue {

    public enum ValueType {
        CREATE,
        UPDATE,
        ADD
    }

    private String label;

    private ValueType command;

    private Pointer pointer;

    private DataDefinition type;

    private Map<String, InputValue> fields = new HashMap<String, InputValue>();

    private ObjectInputValue addReference;

    private String addFieldPath;

    public String getAddFieldPath() {
        return addFieldPath;
    }

    public void setAddFieldPath(String fieldPath) {
        this.addFieldPath = fieldPath;
    }

    public ObjectInputValue getAddReference() {
        return addReference;
    }

    public void setAddReference(ObjectInputValue placeholder) {
        this.addReference = placeholder;
    }

    public void setFields(Map<String, InputValue> fields) {
        this.fields = fields;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ValueType getCommand() {
        return command;
    }

    public void setCommand(ValueType command) {
        this.command = command;
    }

    public Pointer getPointer() {
        return pointer;
    }

    public void setPointer(Pointer pointer) {
        this.pointer = pointer;
    }

    public DataDefinition getType() {
        return type;
    }

    public void setType(DataDefinition type) {
        this.type = type;
    }

    public void addField(String path, InputValue value) {
        this.fields.put(path, value);
    }

    public Map<String, InputValue> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return "ObjectInputValue [label=" + label + ", command=" + command + ", pointer=" + pointer + ", type=" + type
                + ", addReference=" + addReference + ", addFieldPath=" + addFieldPath + "]";
    }

}
