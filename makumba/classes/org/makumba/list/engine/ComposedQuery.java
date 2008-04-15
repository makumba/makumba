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
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.LogicException;
import org.makumba.commons.ArgumentReplacer;
import org.makumba.list.tags.QueryTag;
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

    public QueryProvider qep = null;

    /**
     * Default constructor
     * 
     * @param sections
     * @param usesHQL
     */
    public ComposedQuery(String[] sections, String queryLanguage) {
        this.sections = sections;
        this.derivedSections = sections;
        this.qep = QueryProvider.makeQueryAnalzyer(queryLanguage);
    }

    /** The subqueries of this query */
    int subqueries = 0;

    /** The projections made in this query */
    Vector<Object> projections = new Vector<Object>();

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
    Vector previousKeyset;

    /** The labels of the keyset */
    Vector<String> keysetLabels;

    /** A Vector containing and empty vector. Used for empty keysets */
    static Vector empty;
    static {
        empty = new Vector();
        empty.addElement(new Vector());
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
        Enumeration e = ((Vector) projections.clone()).elements();
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
        if (!qep.selectGroupOrOrderAsLabels())
            return str;
        if (str == null)
            return null;
        if (str.trim().length() == 0)
            return null;
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

        for (Enumeration e = projections.elements(); e.hasMoreElements();) {
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
        ArgumentReplacer ar = new ArgumentReplacer(ret);
        Map<String, Object> d = new HashMap<String, Object>();
        int j = 1;
        for (Iterator<String> e = ar.getArgumentNames(); e.hasNext();)
            d.put(e.next(), "$" + (j++));
        return ar.replaceValues(d);
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
     * Checks if an expression is valid, nullable or set
     * 
     * @param expr
     *            the expression
     * @return The path to the null pointer (if the object is nullable), <code>null</code> otherwise
     */
    public Object checkExprSetOrNullable(String expr) {
        if (expr.toLowerCase().indexOf(" from ") != -1)
            // subqueries do not need separate queries
            return null;
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
            Object nl = checkLabelSetOrNullable(expr.substring(m, n));
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
    public Object checkLabelSetOrNullable(String referenceSequence) {
        int dot = referenceSequence.indexOf(".");
        if (dot == -1)
            return null;
        String substring = referenceSequence.substring(0, dot);
        try { // if the "label" is actually a real number as 3.0
            Integer.parseInt(substring);
            return null; // if so, just return
        } catch (NumberFormatException e) {
        }
        DataDefinition dd = qep.getQueryAnalysis(fromAnalyzerOQL).getLabelType(substring);
        if (dd == null)
            throw new org.makumba.InvalidValueException("no such label " + substring);
        while (true) {
            int dot1 = referenceSequence.indexOf(".", dot + 1);
            if (dot1 == -1) {
                String fn = referenceSequence.substring(dot + 1);
                FieldDefinition fd = dd.getFieldDefinition(fn);
                if (fd == null && (fd = qep.getAlternativeField(dd, fn)) == null)
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

    /**
     * allows to directly set a projection. Used for totalCount in
     * {@link QueryTag#doAnalyzedStartTag(org.makumba.analyser.PageCache)} to compose a query with 'count(*)' as the
     * only projection.
     */
    public void addProjection(Object o) {
        projections.add(o);
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
    public Vector<Object> getProjections() {
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
     * Computes the type that contains the field pointed by an expression, e.g. in the expression
     * "activity.season.responsible.name", this will return the type of "responsible".<br>
     * 
     * @param expr
     *            the expression of which to evaluate the parent type
     * @return a {@link DataDefinition} corresponding to the type containing the field. This can also return a
     *         setComplex, e.g. "general.Person->address"
     */
    public DataDefinition getTypeOfExprField(String expr) {

        if (expr.indexOf(".") == -1) {
            return getLabelType(expr);
        } else {
            DataDefinition result;
            int lastDot = expr.lastIndexOf(".");
            String beforeLastDot = expr.substring(0, lastDot);
            if (beforeLastDot.indexOf(".") == -1) {
                result = getLabelType(beforeLastDot);
            } else {
                // compute dummy query for determining pointed type
                String dummyQuery = "SELECT " + beforeLastDot + " AS projection FROM " + derivedSections[FROM];
                result = qep.getQueryAnalysis(dummyQuery).getProjectionType().getFieldDefinition("projection").getPointedType();
            }
            return result;

        }
    }

    /**
     * Gets the field pointed by an expression
     * 
     * @param expr
     *            the expression to analyse
     * @return the last field in an expression of the kind "a.b.c", the expression itself if there's no subfield
     */
    public String getFieldOfExpr(String expr) {
        if (expr.indexOf(".") > -1)
            return expr.substring(expr.lastIndexOf(".") + 1);
        else
            return expr;
    }

    /**
     * Gets the types of the labels in the FROM section
     * 
     * @return a Map containing the labels and their type
     */
    public Map<String, DataDefinition> getFromLabelTypes() {
        return qep.getQueryAnalysis(fromAnalyzerOQL).getLabelTypes();
    }
}
