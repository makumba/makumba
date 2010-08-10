package org.makumba.jsf.update;

import java.util.ArrayList;
import java.util.List;

import org.makumba.DataDefinition;
import org.makumba.Pointer;

public class ObjectInputValue {

    public enum ValueType {
        CREATE,
        UPDATE
    }

    private ValueType command;

    private Pointer pointer;

    private String clientId;

    private DataDefinition type;

    private List<InputValue> inputValues = new ArrayList<InputValue>();

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

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public DataDefinition getType() {
        return type;
    }

    public void setType(DataDefinition type) {
        this.type = type;
    }

    public void addInputValue(InputValue val) {
        this.inputValues.add(val);
    }

    public List<InputValue> getInputValues() {
        return this.inputValues;
    }

}
