package org.makumba.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.LogicException;

/**
 * This provider makes it possible to run queries against a data source.
 * 
 * @author Manuel Gay
 * @version $Id: QueryExecutionProvider.java,v 1.1 17.09.2007 15:16:57 Manuel Exp $
 */
public abstract class QueryExecutionProvider {

    private static String[] queryProviders = { "oql", "org.makumba.providers.query.oql.OQLQueryExecutionProvider",
            "hql", "org.makumba.providers.query.hql.HQLQueryExecutionProvider" };

    static final Map<String, Class> providerClasses = new HashMap();

    /**
     * Puts the QueryEnvironmentExecutionProviders into a Map
     */
    static {
        for (int i = 0; i < queryProviders.length; i += 2)
            try {
                providerClasses.put(queryProviders[i], Class.forName(queryProviders[i + 1]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
    }

    /**
     * Initalises the provider with the datasource
     * 
     * @param dataSource
     *            the source on which the query should be run
     */
    public abstract void init(String dataSource);

    /**
     * Executes a query with a given set of parameters
     * 
     * @param query
     *            the query to execute
     * @param args
     *            the arguments of this query
     * @param offset
     *            from which record should results be returned
     * @param limit
     *            until which record should results be returned
     * @return a Vector holding Dictionaries corresponding to a result
     * @throws LogicException
     *             because we use Attributes TODO implement a bridge from Attributes to Map and don't use Attributes
     *             here anymore
     */
    public abstract Vector execute(String query, Attributes args, int offset, int limit) throws LogicException;

    /**
     * Closes the environment, when all queries were executed
     */
    public abstract void close();

    /**
     * Provides the query execution environment corresponding to the query language
     * 
     * @param dataSource
     *            the source on which the query should be run
     * @param name
     *            the name of the query execution provider (oql, hql, castorOql, ...)
     * @return
     */
    public static QueryExecutionProvider makeQueryRunner(String dataSource, String name) {

        QueryExecutionProvider qeep = null;
        try {
            qeep = (QueryExecutionProvider) providerClasses.get(name).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        qeep.init(dataSource);

        return qeep;
    }

}
