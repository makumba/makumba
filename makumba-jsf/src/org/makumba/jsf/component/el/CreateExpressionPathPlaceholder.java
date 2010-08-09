package org.makumba.el;

import org.makumba.DataDefinition;

public class CreateExpressionPathPlaceholder extends ExpressionPathPlaceholder {

    private DataDefinition type;

    private boolean pointer;

    public boolean isPointer() {
        return pointer;
    }

    public void setPointer(boolean pointer) {
        this.pointer = pointer;
    }

    public DataDefinition getType() {
        return type;
    }

    public void setType(DataDefinition type) {
        this.type = type;
    }

    public CreateExpressionPathPlaceholder(CreateExpressionPathPlaceholder expr, String field) {
        super(expr, field);
    }

    public CreateExpressionPathPlaceholder(DataDefinition type, String label) {
        super(label);
        this.type = type;
        this.pointer = true;
    }

    @Override
    public String toString() {
        return "[CreateExpressionPathPlaceholder label:" + label + " type:" + type + " isPointer:" + pointer + " path:"
                + getProjectionPath() + "]";
    }

}
