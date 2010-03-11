package org.makumba.providers;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.commons.SingletonHolder;

/**
 * This provider makes it possible to run queries against a data source.
 * 
 * @author Manuel Gay
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @version $Id: QueryExecutionProvider.java,v 1.1 17.09.2007 15:16:57 Manuel Exp $
 */
public abstract class QueryProvider implements SingletonHolder {

    private static String[] queryProviders = { "oql", "org.makumba.db.makumba.MQLQueryProvider", "hql",
            "org.makumba.db.hibernate.HQLQueryProvider" };

    static final Map<String, QueryAnalysisProvider> analyzersByClass = new HashMap<String, QueryAnalysisProvider>();

    static final Map<String, QueryAnalysisProvider> analyzersByName = new HashMap<String, QueryAnalysisProvider>();

    static final Map<String, Class<?>> providerClasses = new HashMap<String, Class<?>>();

    private String dataSource;

    private QueryAnalysisProvider qap;
    
    public QueryProvider() {
        org.makumba.commons.SingletonReleaser.register(this);
        
        try {
            qap = analyzersByClass.get(getQueryAnalysisProviderClass());
        } catch (NullPointerException e) {
            // this only happens at startup
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
                QueryProvider qp = (QueryProvider) Class.forName(queryProviders[i + 1]).newInstance();
                QueryAnalysisProvider qa = (QueryAnalysisProvider) Class.forName(qp.getQueryAnalysisProviderClass()).newInstance();
                analyzersByClass.put(qp.getQueryAnalysisProviderClass(), qa);
                analyzersByName.put(queryProviders[i], qa);
                providerClasses.put(queryProviders[i], Class.forName(queryProviders[i + 1]));
            } catch (Throwable t) {
                t.printStackTrace();
            }
    }

    /**
     * Provides the QueryAnalysisProvider for a given query language.<br>
     * <br>
     * 
     * @param name
     *            the name of the query language
     * @return the QueryProvider able of performing analysis for this language
     */
    public static QueryAnalysisProvider getQueryAnalzyer(String name) {
        return analyzersByName.get(name);
    }

    /**
     * Provides the query execution environment corresponding to the query language.
     * 
     * @param dataSource
     *            the source on which the query should be run
     * @param name
     *            the name of the query execution provider (oql, hql, castorOql, ...)
     * @return
     */
    public static QueryProvider makeQueryRunner(String dataSource, String name, Attributes a) {
        QueryProvider qeep = null;

        try {
            qeep = (QueryProvider) providerClasses.get(name).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        qeep.init(dataSource, a);

        return qeep;
    }

    public static QueryProvider makeQueryRunner(String string, String queryLang) {
        return makeQueryRunner(string, queryLang, null);
    }
    /**
     * Initalises the provider with the datasource
     * 
     * @param dataSource
     *            the source on which the query should be run
     * @param a 
     */
    
    protected void init(String dataSource, Attributes a) {
        this.dataSource = dataSource;
    }

    protected abstract Vector<Dictionary<String, Object>> executeRaw(String query, Map args, int offset, int limit);

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
    public Vector<Dictionary<String, Object>> execute(String query, Map args, int offset, int limit){
        return executeRaw(qap.inlineFunctions(query), args, offset, limit);
    }
    
    /**
     * Closes the environment, when all queries were executed
     */
    public abstract void close();

    /**
     * Gets the data source of the QueryProvider.
     * 
     * @return the data source of the provider, may be null if it just does analysis
     */
    public String getDataSource() {
        return dataSource;
    }
    
    public void release() {
        analyzersByClass.clear();
        analyzersByName.clear();
        providerClasses.clear();
    }

}
