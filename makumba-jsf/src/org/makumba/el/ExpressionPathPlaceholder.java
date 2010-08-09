package org.makumba.jsf.component.el;

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
    public String getProjectionPath() {
        return label + fieldDotField;
    }

    /**
     * Returns field.field.property or property if field.field is empty
     */
    public String getPath(String property) {
        // FIXME: if property is id and fieldDotField is empty, we have an exception because we're editing a primary key
        // this should be detected earlier in the page analysis
        if (this.fieldDotField.length() == 0) {
            return property;
        }
        String dotProp = "." + property;
        if ("id".equals(property)) {
            dotProp = "";
        }

        return this.fieldDotField.substring(1) + dotProp;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public String toString() {
        return getProjectionPath();
    }

}