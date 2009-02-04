package org.makumba;

/**
 * Not-unique is a special case of an {@link InvalidValueException} - the value is syntactically correct, but is
 * restricted to only one usage. This exception can be used for single-field and multi-field uniqueness with the
 * respective constructors.
 * 
 * @author Rudolf Mayer
 * @version $Id: NotUniqueException.java,v 1.1 Sep 19, 2007 9:18:42 PM rudi Exp $
 */
public class NotUniqueException extends InvalidValueException {
    private static final long serialVersionUID = 1L;

    public NotUniqueException(String primaryField, String message) {
        super(message);
        this.field = primaryField;
    }

    /** Uniqueness violation for a single field. */
    public NotUniqueException(FieldDefinition fd, Object value) {
        super(fd, "Allows only unique values - an entry with the value " + getValueForMessage(fd, value)
                + " already exists!");
    }

    private static Object getValueForMessage(FieldDefinition fd, Object value) {
        if (value != null && value.equals("")) {
            return "empty";
        }
        return "'" + value + "'";
    }

}
