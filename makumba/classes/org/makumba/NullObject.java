package org.makumba;

import java.io.Serializable;

public class NullObject implements Serializable {
    private static final long serialVersionUID = 1L;

    String s;

    NullObject(String s) {
        this.s = s;
    }

    public String toString() {
        return s;
    }
}
