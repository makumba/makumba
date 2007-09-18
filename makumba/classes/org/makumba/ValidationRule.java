package org.makumba;

/**
 * Defines the basics of a Makumba Validation rule.
 * 
 * @author Rudolf Mayer
 * @version $Id: ValidationRule.java,v 1.1 Sep 4, 2007 2:27:33 AM rudi Exp $
 */
public interface ValidationRule extends Comparable {
    /**
     * Perform the validation process.
     * 
     * @throws InvalidValueException
     *             if the validation fails
     */
    public boolean validate(Object value) throws InvalidValueException;

    /** Gets the error message that should be shown for this rule. */
    public String getErrorMessage();

    /** Gets the name of this rule. */
    public String getRuleName();

    /** Gets the {@link FieldDefinition} this rule applies to. */
    public FieldDefinition getFieldDefinition();
}
