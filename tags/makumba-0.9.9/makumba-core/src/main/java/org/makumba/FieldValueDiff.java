package org.makumba;

import java.util.Dictionary;

/**
 * This class represents a changed of a value in an MDD field. It stores the field name as in the mak:form (or the
 * {@link Dictionary} via the {@link Transaction} API) it was submitted, the {@link FieldDefinition} it refers to, as
 * well as the old and new values.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class FieldValueDiff implements Comparable<FieldValueDiff> {
    private FieldDefinition fieldDefinition;

    private String fieldName;

    private Object oldValue;

    private Object newValue;

    public FieldValueDiff(String fieldName, FieldDefinition fieldDefinition, Object oldValue, Object newValue) {
        this.fieldDefinition = fieldDefinition;
        this.fieldName = fieldName;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public FieldDefinition getFieldDefinition() {
        return fieldDefinition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getNewValue() {
        return newValue;
    }

    public Object getOldValue() {
        return oldValue;
    }

    @Override
    public String toString() {
        return fieldName + ": '" + oldValue + "' => '" + newValue + "'";
    }

    public int compareTo(FieldValueDiff o) {
        return getFieldName().compareTo(o.getFieldName());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FieldValueDiff) {
            FieldValueDiff dc = (FieldValueDiff) obj;
            return getFieldDefinition().equals(dc.getFieldDefinition()) && getFieldName().equals(dc.getFieldName())
                    && getOldValue().equals(dc.getOldValue()) && getNewValue().equals(dc.getNewValue());
        } else {
            return false;
        }
    }
}
