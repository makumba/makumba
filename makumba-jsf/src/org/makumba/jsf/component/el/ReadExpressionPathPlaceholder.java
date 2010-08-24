package org.makumba.jsf.component.el;

import org.makumba.Pointer;

public class ReadExpressionPathPlaceholder extends ExpressionPathPlaceholder {

    private Pointer basePointer;

    public Pointer getPointer() {
        return this.basePointer;
    }

    public ReadExpressionPathPlaceholder(ReadExpressionPathPlaceholder expr, String field) {
        super(expr, field);
        this.basePointer = expr.basePointer;
    }

    public ReadExpressionPathPlaceholder(Pointer p, String label) {
        super(label);
        if (p == null) {
        	// TODO issue a warning in the logs
            // throw new NullPointerException();
        }
        this.basePointer = p;
    }

    @Override
    public String toString() {
        return basePointer + " " + getProjectionPath();
    }

}
