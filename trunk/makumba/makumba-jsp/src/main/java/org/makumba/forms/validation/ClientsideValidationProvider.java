package org.makumba.forms.validation;

import org.makumba.FieldDefinition;
import org.makumba.analyser.MakumbaJspAnalyzer;

/**
 * Provides an interface to a client-side validation mechanism. For HTML forms,
 * this can e.g. be java-script validation.
 * 
 * @author Rudolf Mayer
 * @author Oscar Marginean
 * @version $Id: ClientsideValidationProvider.java,v 1.1 15.09.2007 13:28:28
 *          Rudolf Mayer Exp $
 */
public interface ClientsideValidationProvider {
    /**
     * Return an array of file names to libraries that shall be included. Makumba could check via page analysis
     * {@link MakumbaJspAnalyzer} if the libraries are already included by the programmer, and add them if needed.
     */
    public String[] getNeededJavaScriptFileNames();

    /** Applies any formatting needed on the provided regular expression. */
    public String formatRegularExpression(String expression);

	/**
	 * Returns the validator classes to be added to the form so that it's picked
	 * up by the JS
	 * 
	 * @return validator classes separated by space
	 */
	public String getFormValidationClasses();

	/**
	 * Returns a stringified JSON object to be consumed by the validation
	 * library
	 * 
	 * @return stringified JSON object
	 */
	String getFieldValidationRules(String inputName, String formIdentifier, FieldDefinition fieldDefinition,
			boolean validateLive);
}
