package org.makumba.providers;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.NoSuchLabelException;
import org.makumba.ProgrammerError;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.RegExpUtils;
import org.makumba.providers.datadefinition.makumba.RecordParser;

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

    static final Map<Class<?>, String> providerClassesReverse = new HashMap<Class<?>, String>();

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
                providerClassesReverse.put(Class.forName(queryProviders[i + 1]), queryProviders[i]);
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

  
    /**
     * Checks if an expression is valid, nullable or set
     * 
     * @param expr
     *            the expression
     * @return The path to the null pointer (if the object is nullable), <code>null</code> otherwise
     */
    public Object checkExprSetOrNullable(String from, String expr) {
        if (expr.toLowerCase().indexOf(" from ") != -1)
            // subqueries do not need separate queries
            return null;

        String query = "SELECT " + expr + " FROM " + from;
        query = preprocessMDDFunctionsAtQueryAnalysis(query);
        expr = query.substring(7);
        expr = expr.substring(0, expr.indexOf("FROM"));

        int n = 0;
        int m = 0;
        while (true) {
            // FIXME: this is a not that good algorithm for finding label.field1.fiel2.field3
            while (n < expr.length() && !isMakId(expr.charAt(n)))
                n++;

            if (n == expr.length())
                return null;
            m = n;
            while (n < expr.length() && isMakId(expr.charAt(n)))
                n++;
            Object nl = checkLabelSetOrNullable(from, expr.substring(m, n));
            if (nl != null)
                return nl;
            if (n == expr.length())
                return null;
        }
    }

    /**
     * Checks if a character can be part of a makumba identifier
     * 
     * @param c
     *            the character to check
     * @return <code>true</code> if the character can be part of a makumba identifier, <code>false</code> otherwise
     */
    static boolean isMakId(char c) {
        return Character.isJavaIdentifierPart(c) || c == '.';
    }

    /**
     * Checks if an id is nullable, and if so, return the path to the null pointer
     * 
     * @param referenceSequence
     *            a sequence like field1.field2.field3
     * @return The path to the null pointer (if the object is nullable), <code>null</code> otherwise
     */
    public Object checkLabelSetOrNullable(String from, String referenceSequence) {
        int dot = referenceSequence.indexOf(".");
        if (dot == -1)
            return null;
        String substring = referenceSequence.substring(0, dot);
        try { // if the "label" is actually a real number as 3.0
            Integer.parseInt(substring);
            return null; // if so, just return
        } catch (NumberFormatException e) {
        }
        DataDefinition dd = getQueryAnalysis("SELECT 1 FROM " + from).getLabelType(substring);
        if (dd == null) {
            throw new org.makumba.NoSuchLabelException("no such label '" + substring + "'.");
        }
        while (true) {
            int dot1 = referenceSequence.indexOf(".", dot + 1);
            if (dot1 == -1) {
                String fn = referenceSequence.substring(dot + 1);
                FieldDefinition fd = dd.getFieldDefinition(fn);
                if (fd == null && (fd = getAlternativeField(dd, fn)) == null)
                    throw new org.makumba.NoSuchFieldException(dd, fn);

                if (fd.getType().equals("set"))
                    return fd;
                return null;
            }
            FieldDefinition fd = dd.getFieldDefinition(referenceSequence.substring(dot + 1, dot1));
            if (fd == null)
                throw new org.makumba.NoSuchFieldException(dd, referenceSequence.substring(dot + 1, dot1));
            if (!fd.getType().startsWith("ptr"))
                throw new InvalidFieldTypeException(fd, "pointer");
            if (!fd.isNotNull())
                return referenceSequence.substring(0, dot1);
            dd = fd.getPointedType();
            dot = dot1;
        }
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
