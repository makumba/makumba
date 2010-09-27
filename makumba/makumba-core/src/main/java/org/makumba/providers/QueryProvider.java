package org.makumba.providers;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;

/**
 * This provider makes it possible to run queries against a data source.
 * 
 * @author Manuel Gay
 * @author Cristian Bogdan
 * @author Rudolf Mayer
 * @version $Id$
 */
public abstract class QueryProvider {

    static Map<String, String> providerClasses = new HashMap<String, String>() {
        {
            put("oql", "org.makumba.db.makumba.MQLQueryProvider");
            put("hql", "org.makumba.db.hibernate.HQLQueryProvider");
        }
    };

    private String dataSource;

    private QueryAnalysisProvider qap;

    protected abstract String getQueryAnalysisProviderClass();

    public static int queryAnalyzers = NamedResources.makeStaticCache("Query analyzers", new NamedResourceFactory() {
        private static final long serialVersionUID = 1L;

        @Override
        public Object makeResource(Object key, Object hashName) throws Throwable {
            QueryProvider qp = (QueryProvider) Class.forName(providerClasses.get(key)).newInstance();
            qp.qap = (QueryAnalysisProvider) Class.forName(qp.getQueryAnalysisProviderClass()).newInstance();
            return qp.qap;
        }
    });

    /**
     * Provides the QueryAnalysisProvider for a given query language.<br>
     * <br>
     * 
     * @param name
     *            the name of the query language
     * @return the QueryProvider able of performing analysis for this language
     */
    public static QueryAnalysisProvider getQueryAnalzyer(String name) {
        return (QueryAnalysisProvider) NamedResources.getStaticCache(queryAnalyzers).getResource(name);
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
            qeep = (QueryProvider) Class.forName(providerClasses.get(name)).newInstance();
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

    protected abstract Vector<Dictionary<String, Object>> executeRaw(String query, Map<String, Object> args,
            int offset, int limit);

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
    public Vector<Dictionary<String, Object>> execute(String query, Map<String, Object> args, int offset, int limit) {
        return executeRaw(query, args, offset, limit);
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
}
