package org.makumba.el;

import java.io.Serializable;

import org.makumba.Pointer;

public class UpdateValue implements Serializable {

    private static final long serialVersionUID = 2051359295024396260L;

    private Pointer pointer;

    private String path;

    private Object value;

    public Pointer getPointer() {
        return pointer;
    }

    public void setPointer(Pointer pointer) {
        this.pointer = pointer;
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

    public UpdateValue(Pointer pointer, String path, Object value) {
        super();
        this.pointer = pointer;
        this.path = path;
        this.value = value;
    }

}
