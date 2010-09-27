package org.makumba.providers;

import java.util.Map;

import org.makumba.DataDefinition;
import org.makumba.commons.NameResolver;

/**
 * Transoforms a query to ordinal parameters.
 * <ul>
 * <li>expansion of list/vector parameters</li>
 * <li>transformation of named parameters into (ordered) numbered parameters</li>
 * </ul>
 * 
 * @author Manuel Gay
 * @version $Id: SQLQueryGenerator.java,v 1.1 Mar 3, 2010 6:50:10 PM manu Exp $
 */
public interface ParameterTransformer {

    /**
     * Initialises the SQLQueryGenerator. Called at resource configuration time
     */
    public void init(Map<String, Object> arguments);

    /**
     * Provides the transformed query, with expanded and transformed parameters
     * 
     * @param nr
     *            the {@link NameResolver} used to resolve database-level table and field names
     * @return the expanded SQL query String
     */
    public String getTransformedQuery(NameResolver nr);

    /**
     * Provides the parameters in the order following the one of the query returned by
     * {@link #getTransformedQuery(NameResolver, Object)}
     * 
     * @return an object array containing the ordered parameter values
     */
    public Object[] toParameterArray(Map<String, Object> arguments);

    /**
     * Gets the types of the arguments
     * 
     * @return a DataDefinition with the types of all the arguments
     */
    public DataDefinition getTransformedParameterTypes();

    /**
     * The number of arguments of the query
     */
    public int getParameterCount();

}
