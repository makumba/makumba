package org.makumba.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;

/**
 * This provider makes it possible to run queries against a data source.
 * 
 * @author Manuel Gay
 * @version $Id: QueryExecutionProvider.java,v 1.1 17.09.2007 15:16:57 Manuel Exp $
 */
public abstract class QueryProvider {

    private static Map<String, QueryProvider> analysisQueryProviders = new HashMap<String, QueryProvider>();

    private static String[] queryProviders = {"oql", "org.makumba.providers.query.oql.OQLQueryProvider", "hql", "org.makumba.providers.query.hql.HQLQueryProvider" };

    static final Map<String, Class> providerClasses = new HashMap<String, Class>();

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
     * Provides the analysis QueryProvider for a given query language.
     * 
     * TODO this should be refactored to use the same mechanism as for the dataDefinitionProvider
     * 
     * @param name
     *            the name of the query language
     * @return the QueryProvider able of performing analysis for this language
     */
    public static QueryProvider makeQueryAnalzyer(String name) {
        QueryProvider qp = analysisQueryProviders.get(name);
        if (qp == null) {
            try {
                qp = (QueryProvider) providerClasses.get(name).newInstance();
                analysisQueryProviders.put(name, qp);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return qp;
    }

    /**
     * Provides the query execution environment corresponding to the query language.
     * 
     * TODO this should be refactored to use the same mechanism as for the dataDefinitionProvider
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
     * @throws LogicException
     *
     */
    public abstract Vector execute(String query, Map args, int offset, int limit) throws LogicException;

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
    public abstract QueryAnalysis getQueryAnalysis(String query);

    private String dataSource;

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

    /**
     * Transforms the pointer into a hibernate pointer if we use hibernate FIXME this should be provide by the
     * QueryProvider!!
     * 
     * @param query
     *            TODO
     * @param ptrExpr
     *            the expression we want to check
     * @return A modified expression adapted to Hibernate
     */
    public abstract String transformPointer(String ptrExpr, String fromSection);

}
