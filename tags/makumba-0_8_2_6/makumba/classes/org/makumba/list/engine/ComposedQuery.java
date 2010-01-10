///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.list.engine;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.list.tags.QueryTag;
import org.makumba.providers.QueryAnalysisProvider;
import org.makumba.providers.QueryProvider;

/**
 * An OQL query composed from various elements found in script pages. It can be enriched when a new element is found. It
 * has a prepared Qyuery correspondent in a makumba database It may be based on a super query.
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class ComposedQuery {

    /**
     * Interface for an Evaluator which can evaluate expressions
     * 
     * @author Cristian Bogdan
     */
    public static interface Evaluator {

        /**
         * Evaluates the expression
         * 
         * @param s
         *            the expression to evaluate
         * @return The transformed expression after evaluation
         */
        String evaluate(String s);
    }

    public QueryAnalysisProvider qep = null;

    /**
     * Default constructor
     * 
     * @param sections
     * @param usesHQL
     */
    public ComposedQuery(String[] sections, String queryLanguage) {
        this.sections = sections;
        this.derivedSections = sections;
        this.qep = QueryProvider.getQueryAnalzyer(queryLanguage);
    }

    /** The subqueries of this query */
    int subqueries = 0;

    /** The projections made in this query */
    Vector<String> projections = new Vector<String>();

    /** The expression associated to each projection */
    Hashtable<String, Integer> projectionExpr = new Hashtable<String, Integer>();

    /** Standard index for the FROM query section */
    public static final int FROM = 0;

    /** Standard index for the WHERE query section */
    public static final int WHERE = 1;

    /** Standard index for the GROUPBY query section */
    public static final int GROUPBY = 2;

    /** Standard index for the ORDERBY query section */
    public static final int ORDERBY = 3;

    /** Standard index for the VARFROM query section */
    public static final int VARFROM = 4;

    /** Section texts, encoded with the standard indexes */
    String[] sections;

    /** Derived section texts, made from the sections of this query and the sections of its superqueries */
    String[] derivedSections;

    String typeAnalyzerOQL;

    String fromAnalyzerOQL;

    /**
     * The keyset defining the primary key for this query. Normally the primary key is made of the keys declared in
     * FROM, in this query and all the parent queries. Keys are kept as integers (indexes)
     */
    Vector<Integer> keyset;

    /** The keyset of all the parent queries */
    Vector<Vector<Integer>> previousKeyset;

    /** The labels of the keyset */
    Vector<String> keysetLabels;

    /** A Vector containing an empty vector. Used for empty keysets */
    static Vector<Vector<Integer>> empty;
    static {
        empty = new Vector<Vector<Integer>>();
        empty.addElement(new Vector<Integer>());
    }

    /**
     * Gets the type of the result
     * 
     * @return The DataDefinition corresponding to the type of the result
     */
    public DataDefinition getResultType() {
        if (typeAnalyzerOQL == null) {
            return null;
        } else {
            return qep.getQueryAnalysis(typeAnalyzerOQL).getProjectionType();
        }
    }

    /**
     * Gets the type of a given label
     * 
     * @param s
     *            the name of the label
     * @return A DataDefinition corresponding to the type of the label
     */
    public DataDefinition getLabelType(String s) {
        if (typeAnalyzerOQL == null) {
            return null;
        } else {
            return qep.getQueryAnalysis(typeAnalyzerOQL).getLabelType(s);
        }
    }

    /**
     * Initializes the object. This is a template method
     */
    public void init() {
        initKeysets();
        fromAnalyzerOQL = "SELECT 1 ";
        if (getFromSection() != null)
            fromAnalyzerOQL += "FROM " + getFromSection();
    }

    /**
     * Gets the FROM section
     * 
     * @return A String containing the FROM section of the query
     */
    public String getFromSection() {
        return derivedSections[FROM];
    }

    /**
     * Gets the GROUP BY section
     * 
     * @return A String containing the GROUP BY section of the query
     */
    public String getGroupBySection() {
        return derivedSections[GROUPBY];
    }

    /**
     * Initializes the keysets. previousKeyset is "empty"
     */
    protected void initKeysets() {
        previousKeyset = empty;
        keyset = new Vector<Integer>();
        keysetLabels = new Vector<String>();
    }

    /**
     * Adds a subquery to this query. Makes it aware that it has subqueries at all.
     * 
     * @param q
     *            the subquery
     */
    protected void addSubquery(ComposedSubquery q) {
        if (subqueries == 0)
            prependFromToKeyset();
        subqueries++;
    }

    /**
     * Adds all keys from the FROM section to the keyset, and their labels to the keyLabels. They are all added as
     * projections (this has to change)
     */
    protected void prependFromToKeyset() {
        projectionExpr.clear();
        Enumeration<String> e = ((Vector<String>) projections.clone()).elements();
        projections.removeAllElements();

        // add the previous keyset
        for (int i = 0; i < keyset.size(); i++)
            checkProjectionInteger((String) e.nextElement());

        for (StringTokenizer st = new StringTokenizer(sections[FROM] == null ? "" : sections[FROM], ","); st.hasMoreTokens();) {
            String label = st.nextToken().trim();
            int j = label.lastIndexOf(" ");
            if (j == -1)
                throw new RuntimeException("invalid FROM");
            label = label.substring(j + 1).trim();

            label = qep.getPrimaryKeyNotation(label);

            keysetLabels.addElement(label);

            keyset.addElement(addProjection(label));
        }

        while (e.hasMoreElements())
            checkProjectionInteger((String) e.nextElement());
    }

    /**
     * Gets a given projection
     * 
     * @param n
     *            the index of the projection
     * @return A String containing the projection
     */
    public String getProjectionAt(int n) {
        return (String) projections.elementAt(n);
    }

    /**
     * Adds a projection with the given expression
     * 
     * @param expr
     *            the expression to add
     * @return The index at which the expression was added
     */
    Integer addProjection(String expr) {
        Integer index = new Integer(projections.size());
        projections.addElement(expr);
        projectionExpr.put(expr, index);
        return index;
    }

    /**
     * Checks if a projection exists, and if not, adds it.
     * 
     * @param expr
     *            the expression to add
     * @return The index of the added projection
     */
    public Integer checkProjectionInteger(String expr) {
        Integer index = (Integer) projectionExpr.get(expr);
        if (index == null) {
            addProjection(expr);
            // FIXME: if DISTINCT is true, need to recompute the keyset and notify the subqueries to recompute their
            // previous keyset
            return null;
        }
        return index;
    }

    /**
     * Checks if a projection exists, and if not, adds it.
     * 
     * @param expr
     *            the expression to add
     * @return The column name of the projection
     */
    String checkProjection(String expr) {
        Integer i = checkProjectionInteger(expr);
        if (i == null)
            return null;
        return columnName(i);
    }

    /**
     * Gets the name of a column indicated by index
     * 
     * @param n
     *            the index of the column
     * @return A String containing the name of the column, of the kind "colN"
     */
    public static String columnName(Integer n) {
        return "col" + (n.intValue() + 1);
    }

    /**
     * Checks the orderBy or groupBy expressions to see if they are already selected, if not adds a projection. Only
     * group by and order by labels.
     * 
     * @param str
     *            an orderBy or groupBy expression
     * @return The checked expression, transformed according to the projections
     */
    String checkExpr(String str) {
        if (str == null)
            return null;
        if (str.trim().length() == 0)
            return null;
        if (!qep.selectGroupOrOrderAsLabels())
            return str;
        // if(projections.size()==1)
        // new Throwable().printStackTrace();

        StringBuffer ret = new StringBuffer();
        String sep = "";
        for (StringTokenizer st = new StringTokenizer(str, ","); st.hasMoreTokens();) {
            ret.append(sep);
            sep = ",";
            String s = st.nextToken().trim();
            String rest = "";
            int i = s.indexOf(" ");
            if (i != -1) {
                rest = s.substring(i);
                s = s.substring(0, i);
            }
            // if the projection doesnt exist, this returns null, but it adds a new projection
            String p = checkProjection(s);
            if (p == null)
                // and the second time this doesn#t return null, but the projection name
                p = checkProjection(s);
            ret.append(p).append(rest);
        }
        return ret.toString();
    }

    /**
     * Computes the query from its sections
     * 
     * @param derivedSections
     *            the sections of this query
     * @param typeAnalysisOnly
     *            indicates whether this is only a type analysis
     * @return The computed OQL query
     */
    protected String computeQuery(String derivedSections[], boolean typeAnalysisOnly) {
        String groups = null;
        String orders = null;
        if (!typeAnalysisOnly) {
            groups = checkExpr((String) derivedSections[GROUPBY]);
            orders = checkExpr((String) derivedSections[ORDERBY]);
        }

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        String sep = "";

        int i = 0;

        for (Enumeration<String> e = projections.elements(); e.hasMoreElements();) {
            sb.append(sep);
            sep = ",";
            sb.append(e.nextElement()).append(" AS ").append(columnName(new Integer(i++)));
        }
        Object o;

        if ((o = derivedSections[FROM]) != null) {
            sb.append(" FROM ");
            sb.append(o);

            // there can be no VARFROM without FROM
            // VARFROM is not part of type analysis
            // (i.e. projections don't know about it)
            if (!typeAnalysisOnly && derivedSections.length == 5 && derivedSections[VARFROM] != null
                    && derivedSections[VARFROM].trim().length() > 0)
                sb.append(",").append(derivedSections[VARFROM]);
        }
        if (!typeAnalysisOnly) {
            if ((o = derivedSections[WHERE]) != null && derivedSections[WHERE].trim().length() > 0) {
                sb.append(" WHERE ");
                sb.append(o);
            }
            if (groups != null) {
                sb.append(" GROUP BY ");
                sb.append(groups);
            }
            if (orders != null) {
                sb.append(" ORDER BY ");
                sb.append(orders);
            }
        }
        String ret = sb.toString();
        if (!typeAnalysisOnly)
            return ret;

        // replace names with numbers
/*        ArgumentReplacer ar = new ArgumentReplacer(ret);
        Map<String, Object> d = new HashMap<String, Object>();
        int j = 1;
        for (Iterator<String> e = ar.getArgumentNames(); e.hasNext();)
            d.put(e.next(), "$" + (j++));
        return ar.replaceValues(d);*/
        return ret;
    }

    // ------------
    /**
     * Executes the contained query in the given database
     * 
     * @param qep
     *            the database where the query should be ran
     * @param args
     *            the arguments we may need during the execution
     * @param v
     *            the evaluator evaluating the expressions
     * @param offset
     *            at which iteration this query should start
     * @param limit
     *            how many times should this query be ran
     * @throws LogicException
     */
    public Grouper execute(QueryProvider qep, Map args, Evaluator v, int offset, int limit) throws LogicException {
        analyze();
        String[] vars = new String[5];
        vars[0] = getFromSection();
        for (int i = 1; i < 5; i++)
            vars[i] = derivedSections[i] == null ? null : v.evaluate(derivedSections[i]);

        return new Grouper(previousKeyset, qep.execute(computeQuery(vars, false), args, offset, limit).elements());
    }

    public synchronized void analyze() {
        if (projections.isEmpty())
            prependFromToKeyset();
        if (typeAnalyzerOQL == null)
            typeAnalyzerOQL = computeQuery(derivedSections, true);
    }

    /**
     * allows to directly set a projection. Used for totalCount in
     * {@link QueryTag#doAnalyzedStartTag(org.makumba.analyser.PageCache)} to compose a query with 'count(*)' as the
     * only projection.
     */
    public void addProjectionDirectly(String s) {
        projections.add(s);
    }

    public String toString() {
        return "Composed query: " + typeAnalyzerOQL;
    }

    /**
     * Gets the query string.
     * 
     * @return the query string in a form that can be used by a type analyser, in the query language of the
     *         ComposedQuery
     */
    public String getTypeAnalyzerQuery() {
        return typeAnalyzerOQL;
    }

    /**
     * Gets the projections of this query
     * 
     * @return a {@link Vector} containing the projections of this ComposedQuery
     */
    public Vector<String> getProjections() {
        return projections;
    }

    /**
     * Gets the type of the fields between SELECT and FROM
     * 
     * @return A DataDefinition containing as fields the type and name of the query projections
     */
    public DataDefinition getProjectionTypes() {
        return qep.getQueryAnalysis(typeAnalyzerOQL).getProjectionType();
    }

    /**
     * Gets the types of the labels in the FROM section
     * 
     * @return a Map containing the labels and their type
     */
    public Map<String, DataDefinition> getFromLabelTypes() {
        return qep.getQueryAnalysis(fromAnalyzerOQL).getLabelTypes();
    }

    public Object checkExprSetOrNullable(String expr) {
        return qep.checkExprSetOrNullable(getFromSection(), expr);
    }
    
}
