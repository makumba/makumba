/*
 * Created on Apr 3, 2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.providers;

import java.util.List;

import org.makumba.DataDefinition;

/**
 * Query parameter information: name and type for each position, possibility for multiple values.
 * 
 * @author cristi
 */
public interface QueryParameters {
    /**
     * Gets the types of the query parameters, as resulted from the query analysis.
     * 
     * @return A DataDefinition containing in the first field the type of the QL parameter mentioned first in the query.
     *         Each new mentioning of a parameter will get a new field in this DataDefinition!
     */
    DataDefinition getParameterTypes();

    /**
     * Names of parameters
     * 
     * @return
     */
    List<String> getParameterOrder();

    /**
     * Whether the parameter is multi-value or not
     * 
     * @param position
     * @return
     */
    boolean isMultiValue(int position);
}
