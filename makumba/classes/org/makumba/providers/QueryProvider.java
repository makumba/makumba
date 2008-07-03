package org.makumba.providers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.NoSuchLabelException;
import org.makumba.ProgrammerError;
import org.makumba.DataDefinition.QueryFragmentFunction;
import org.makumba.commons.RegExpUtils;
import org.makumba.list.engine.ComposedQuery;
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
        return preprocessMDDFunctions(query);
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
        return preprocessMDDFunctions(query);
    }

    public String preprocessMDDFunctions(String query) {
        if (!query.contains("(")) { // a simple check to see if there is any potential function at all
            return query;
        }

        // need to split the queries into the SELECT, FROM & WHERE parts
        String[] parts = splitQueryInParts(query);

        String patternDefLogicalOperands = PATTERN_FUNCTION_CALL + "(?:(" + RegExpUtils.LineWhitespaces
                + PARTS_SEPARATOR_LOGICAL_OPERANDS + RegExpUtils.LineWhitespaces + ").*)*";

        String patternDefProjection = PATTERN_FUNCTION_CALL + "(?:(" + RegExpUtils.LineWhitespaces
                + PARTS_SEPARATOR_PROJECTION + RegExpUtils.LineWhitespaces + ").*)*";

        Pattern patternLogicalOperands = Pattern.compile(patternDefLogicalOperands);
        Pattern patternProjection = Pattern.compile(patternDefProjection);

        // System.out.println("patternDefLogicalOperands: " + patternDefLogicalOperands);
        // System.out.println("patternDefProjection: " + patternDefProjection);

        System.out.println("\ninitial query: " + query);
        // inline MDD functions in projections (SELECT)
        query = inlineSection(query, parts, patternProjection, parts[0]);
        // inline MDD functions in WHERE part
        query = inlineSection(query, parts, patternLogicalOperands, parts[2]);
        System.out.println("new query: " + query + "\n");

        // pre-process the WHERE part
        return query;
    }

    public String inlineSection(String query, String[] parts, Pattern pattern, String section) {
        int indexOf = query.indexOf(section);
        int endIndex = indexOf + section.length();
        String inlineFunction = inlineFunction(parts[1], section, pattern);
        if (!inlineFunction.equals(section)) {
            query = query.substring(0, indexOf) + inlineFunction + query.substring(endIndex);
        }
        return query;
    }

    public String inlineFunction(String from, String section, Pattern pattern) {
        if (StringUtils.isBlank(section)) {
            return section;
        }
        while (section.contains("  ")) {
            section = section.replaceAll("  ", " ");
        }
        Matcher matcher = pattern.matcher(section);
        if (!matcher.matches()) {
            return section;
        }
        String newSection = "";
        while (matcher.matches()) {
            RegExpUtils.evaluate(pattern, new String[] { section });
            String functionDef = matcher.group(1); // this works for now only for functions w/o any params
            String params = matcher.group(2);
            String conjunction = matcher.group(7);
            int lastDot = functionDef.lastIndexOf(".");
            String label = functionDef.substring(0, lastDot);
            String functionName = functionDef.substring(lastDot + 1);

            // we need to find out the type of the label the function is called on
            // thus, we need to do a dummy query to analyze the type
            // FIXME: this does not yet take great care of other things around..
            String[] queryProps = new String[5];
            queryProps[ComposedQuery.FROM] = from;
            ComposedQuery q = new ComposedQuery(queryProps, providerClassesReverse.get(getClass()));
            q.init();
            q.analyze();
            DataDefinition labelType = q.getLabelType(label);
            if (labelType == null) {
                throw new NoSuchLabelException("no such label '" + label + "'");
            }

            QueryFragmentFunction function = labelType.getFunction(functionName);
            if (function == null) {
                throw new ProgrammerError("No function '" + functionName + "' found in type '" + labelType + "'");
            }

            String whereBefore = section.substring(0, section.indexOf(functionDef));

            String inlinedFunction = inlineNestedFunctions(labelType, function.getQueryFragment());
            inlinedFunction = inlinedFunction.replaceAll("this", label);
            newSection += whereBefore + inlinedFunction + (conjunction != null ? conjunction : "");

            int index = section.indexOf(functionDef) + functionDef.length() + (params != null ? params.length() : 2)
                    + (conjunction != null ? conjunction.length() + 1 : 0);
            section = section.substring(index).trim();
            matcher = pattern.matcher(section);
        }
        return newSection;
    }

    /** Inlines MDD-functions that itself contain other query functions; does only one level of inlining yet. */
    private String inlineNestedFunctions(DataDefinition labelType, String s) {
        if (s.indexOf("(") > s.indexOf(".")) { // we only do that if we have a ( after a .
            String stripArgs = s.substring(0, s.indexOf("("));
            String fieldName = s.substring(0, stripArgs.lastIndexOf(".")).replaceAll("this.", "");
            FieldDefinition fd = labelType.getFieldOrPointedFieldDefinition(fieldName);
            String functionName = s.substring(s.lastIndexOf(".") + 1, s.indexOf("("));
            QueryFragmentFunction function = fd.getPointedType().getFunction(functionName);
            String queryFragment = function.getQueryFragment();
            // this in the function relates to the data-definition the function definition comes from
            // ==> we need to replace the "this" with "this" and the field-name the function came from
            queryFragment = queryFragment.replaceAll("this", "this." + fieldName);
            s = queryFragment;
        }
        return s;
    }

    /** Splits a query in projection, FROM and WHERE sections. */
    private String[] splitQueryInParts(String query) {
        String[] parts = new String[3];
        final String lower = query.toLowerCase();
        int indexSelect = lower.indexOf("select");
        int indexFrom = lower.indexOf("from");
        int indexWhere = lower.indexOf("where");
        int indexGroupBy = lower.indexOf("group by");
        int indexOrderBy = lower.indexOf("order by");
        int indexEnd = Math.max(indexGroupBy, indexOrderBy);
        parts[0] = query.substring(indexSelect + "select".length(), indexFrom);
        parts[1] = indexWhere == -1 ? query.substring(indexFrom + "from".length()) : query.substring(indexFrom
                + "from".length(), indexWhere);
        if (indexWhere != -1) {
            parts[2] = indexEnd == -1 ? query.substring(indexWhere + "where".length()).trim() : query.substring(
                indexWhere + "where".length(), indexEnd);
        } else {
            parts[2] = "";
        }
        return parts;
    }

    private String dataSource;

    private QueryAnalysisProvider qap;

    public static final String PARTS_SEPARATOR_LOGICAL_OPERANDS = "(AND|OR)";

    public static final String PARTS_SEPARATOR_PROJECTION = ",";

    public static final String PATTERN_FUNCTION_CALL = "(" + RegExpUtils.fieldName + ")" + "\\((?:" + "("
            + RecordParser.funcDefParamValueRegExp + ")" + "(?:" + RegExpUtils.LineWhitespaces + ","
            + RegExpUtils.LineWhitespaces + "(" + RecordParser.funcDefParamValueRegExp + "))*"
            + RegExpUtils.LineWhitespaces + ")?\\)";

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
