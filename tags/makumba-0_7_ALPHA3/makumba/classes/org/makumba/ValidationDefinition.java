package org.makumba;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Information about a makumba validation definition. Implementations of this interface can obtain the validation
 * definition e.g. by parsing a validation definition file, or other means.
 * 
 * @author Rudolf Mayer
 * @version $Id: ValidationDefinition.java,v 1.1 Sep 4, 2007 2:25:01 AM rudi Exp $
 */
public interface ValidationDefinition {
    /** Name of this validation definition, normally the same */
    public String getName();

    /** Get all validation rules for the given field name. */
    public Collection<ValidationRule> getValidationRules(String fieldName);

    /** Get the validation rule with the given rule name. */
    public ValidationRule getValidationRule(String ruledName);

    /** Add a new rule for the given field. */
    public void addRule(String fieldName, ValidationRule rule);

    /** Add several rules for the given field. */
    public void addRule(String fieldName, Collection<ValidationRule> rules);

    /** Get the data definition associated with this validation definition. */
    public DataDefinition getDataDefinition();

    /** Get the operator names of known rules. FIXME: this should probably be a static method somewhere else. */
    public ArrayList getRulesSyntax();

    public boolean hasValidationRules();
}
