package org.makumba.el;

import java.io.Serializable;

import org.makumba.DataDefinition;

public class CreateValue implements Serializable {

    private static final long serialVersionUID = 1L;

    private DataDefinition type;

    private String path;

    private Object value;

    public CreateValue(DataDefinition type, String path, Object value) {
        super();
        this.type = type;
        this.path = path;
        this.value = value;
    }

    public DataDefinition getType() {
        return type;
    }

    public void setType(DataDefinition type) {
        this.type = type;
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

}
