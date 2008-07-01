package org.makumba.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;

/**
 * This provider makes it possible to run queries against a data source.
 * 
 * @author Manuel Gay
 * @version $Id: QueryExecutionProvider.java,v 1.1 17.09.2007 15:16:57 Manuel Exp $
 */
public abstract class QueryProvider {

    private static String[] queryProviders = { "oql", "org.makumba.db.makumba.OQLQueryProvider", "hql",
            "org.makumba.db.hibernate.HQLQueryProvider" };

    static final Map<String, Class<?>> providerClasses = new HashMap<String, Class<?>>();

    public QueryProvider() {
        try {
            if (getQueryAnalysisProviderClass() != null) {
                if (qap == null) {
                    qap = (QueryAnalysisProvider) Class.forName(getQueryAnalysisProviderClass()).newInstance();
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected String getQueryAnalysisProviderClass() {
        return null;
    }

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
     * Provides the analysis QueryProvider for a given query language.<br>
     * <br>
     * TODO this should be refactored to use the same mechanism as for the dataDefinitionProvider
     * 
     * @param name
     *            the name of the query language
     * @return the QueryProvider able of performing analysis for this language
     */
    public static QueryProvider makeQueryAnalzyer(String name) {
        try {
            return (QueryProvider) providerClasses.get(name).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Provides the query execution environment corresponding to the query language. TODO this should be refactored to
     * use the same mechanism as for the dataDefinitionProvider
     * 
     * @param dataSource
     *            the source on which the query should be run
     * @param name
     *            the name of the query execution provider (oql, hql, castorOql, ...)
     * @return
     */
    public static QueryProvider makeQueryRunner(String dataSource, String name) {
        QueryProvider qeep = makeQueryAnalzyer(name);
        qeep.init(dataSource);

        return qeep;
    }

    /**
     * Initalises the provider with the datasource
     * 
     * @param dataSource
     *            the source on which the query should be run
     */
    public void init(String dataSource) {
        this.dataSource = dataSource;
    }

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
     */
    public abstract Vector execute(String query, Map args, int offset, int limit);

    /**
     * Pre-process the query at execution time. For now does inlining of functions defined in the MDD.<br>
     * FIXME: this should not be done twice at analysis and execution time
     */
    public String preprocessMDDFunctionsAtExecute(String query) {
        return query;
    }

    /**
     * Closes the environment, when all queries were executed
     */
    public abstract void close();

    /**
     * Returns the notation of the primary key in the query language
     * 
     * @param label
     *            the label of the object
     * @return the notation for the primary key of the object
     */
    public abstract String getPrimaryKeyNotation(String label);

    /**
     * Returns the QueryAnalysis for the given query
     * 
     * @param query
     *            the query we want to analyse
     * @return the {@link QueryAnalysis} for this query and QueryProvider
     */
    public QueryAnalysis getQueryAnalysis(String query) {
        // pre-process the query
        return qap.getQueryAnalysis(preprocessMDDFunctionsAtQueryAnalysis(query));
    }

    /** Pre-process the query at analysis time. For now does inlining of functions defined in the MDD. */
    public String preprocessMDDFunctionsAtQueryAnalysis(String query) {
        return query;
    }

    private String dataSource;

    private QueryAnalysisProvider qap;

    /**
     * Gets the data source of the QueryProvider.
     * 
     * @return the data source of the provider, may be null if it just does analysis
     */
    public String getDataSource() {
        return dataSource;
    }

    public abstract boolean selectGroupOrOrderAsLabels();

    public abstract FieldDefinition getAlternativeField(DataDefinition dd, String fn);

}
