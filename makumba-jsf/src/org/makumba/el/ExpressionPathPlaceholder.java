package org.makumba.el;

public abstract class ExpressionPathPlaceholder {
    // everything starts from a label
    protected String label;

    // after that, comes the field.field path to the desired property
    protected String fieldDotField = "";

    public ExpressionPathPlaceholder(String label) {
        this.label = label;
    }

    public ExpressionPathPlaceholder(ExpressionPathPlaceholder expr, String field) {
        this.label = expr.label;
        this.fieldDotField = expr.fieldDotField + "." + field;
    }

    /**
     * Returns label.field.field
     */
    public String getExpressionPath() {
        return label + fieldDotField;
    }

    /**
     * Returns field.field (without label)
     */
    public String getPathForUpdate() {
        return this.fieldDotField;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return getExpressionPath();
    }

}