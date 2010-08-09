package org.makumba.el;

import java.io.Serializable;

import org.makumba.DataDefinition;
import org.makumba.Pointer;

/**
 * This class represents a value set in an input field through an UEL expression and relating to a specific makumba type
 * or record.
 * 
 * @author manu
 */
public class InputValue implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum ValueType {
        CREATE,
        UPDATE
    }

    protected ValueType command;

    protected String path;

    protected Object value;

    protected Pointer pointer;

    protected String clientId;

    protected DataDefinition type;

    public InputValue(String path, Object value, String clientId) {
        this.path = path;
        this.value = value;
    }

    public InputValue(DataDefinition type, String path, Object value, String clientId) {
        this(path, value, clientId);
        this.type = type;
        this.command = ValueType.CREATE;
    }

    public InputValue(Pointer pointer, String clientId, String path, Object value) {
        this(path, value, clientId);
        this.pointer = pointer;
        this.command = ValueType.UPDATE;
    }

    public ValueType getCommand() {
        return command;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Pointer getPointer() {
        return pointer;
    }

    public void setPointer(Pointer pointer) {
        this.pointer = pointer;
    }

    /**
     * @return the id of the UIComponent (EditableValueHolder) that provoked this change
     */
    public String getId() {
        return clientId;
    }

    public DataDefinition getType() {
        return type;
    }

    public void setType(DataDefinition type) {
        this.type = type;
    }

}