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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.OQLAnalyzer;
import org.makumba.db.hibernate.hql.HqlAnalyzer;
import org.makumba.providers.QueryExecutionProvider;
import org.makumba.util.MultipleKey;
import org.makumba.util.NamedResourceFactory;
import org.makumba.util.NamedResources;

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

    /**
     * Default constructor
     * 
     * @param sections
     * @param usesHQL
     */
    public ComposedQuery(String[] sections, boolean usesHQL) {
        this.sections = sections;
        this.derivedSections = sections;
        this.useHibernate = usesHQL;
    }

    /** The subqueries of this query */
    Vector subqueries = new Vector();

    /** The projections made in this query */
    Vector projections = new Vector();

    /** The expression associated to each projection */
    Hashtable projectionExpr = new Hashtable();

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
    Vector keyset;

    /** The keyset of all the parent queries */
    Vector previousKeyset;

    /** The labels of the keyset */
    Vector keysetLabels;

    /** Do we use hibernate for execution or do we use makumba? */
    boolean useHibernate;

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
            return getOQLAnalyzer(typeAnalyzerOQL).getProjectionType();
        }
    }

    /**
     * Gets the OQL analyzer used for analysis
     * 
     * @param type
     *            the string we want to analyze
     * @return An OQLAnalyzer performing the OQL analysis
     */
    public OQLAnalyzer getOQLAnalyzer(String type) {
        OQLAnalyzer oqa = null;
        if (useHibernate) {
            oqa = MakumbaSystem.getHqlAnalyzer(type);
        } else {
            oqa = MakumbaSystem.getOQLAnalyzer(type);
        }
        return oqa;
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
            return getOQLAnalyzer(typeAnalyzerOQL).getLabelType(s);
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
        keyset = new Vector();
        keysetLabels = new Vector();
    }

    /**
     * Adds a subquery to this query. Makes it aware that it has subqueries at all. Makes it be able to announce its
     * subqueries about changes (this will be needed when unique=true will be possible)
     * 
     * @param q
     *            the subquery
     */
    protected void addSubquery(ComposedSubquery q) {
        if (subqueries.size() == 0)
            prependFromToKeyset();
        subqueries.addElement(q);
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

        for (StringTokenizer st = new StringTokenizer(sections[FROM] == null ? "" : sections[FROM], ","); st
                .hasMoreTokens();) {
            String label = st.nextToken().trim();
            int j = label.lastIndexOf(" ");
            if (j == -1)
                throw new RuntimeException("invalid FROM");
            label = label.substring(j + 1).trim();

            // this is specific to Hibernate: we add '.id' in order to get the id as in makumba
            if (this.useHibernate && label.indexOf('.') == -1)
                label += ".id";

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
            String p = checkProjection(s);
            if (p == null)
                p = checkProjection(s);
            ret.append(p).append(rest);
        }
        if (this.useHibernate)
            return str;
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
        Dictionary d = new Hashtable();
        int j = 1;
        for (Enumeration e = ar.getArgumentNames(); e.hasMoreElements();)
            d.put(e.nextElement(), "$" + (j++));
        return ar.replaceValues(d);
    }

    // ------------
    /**
     * Executes the contained query in the given database
     * 
     * @param qep
     *            the database where the query should be ran
     * @param a
     *            the attributes we may need during the execution
     * @param v
     *            the evaluator evaluating the expressions
     * @param offset
     *            at which iteration this query should start
     * @param limit
     *            how many times should this query be ran
     * @throws LogicException
     */
    public Grouper execute(QueryExecutionProvider qep, Attributes a, Evaluator v, int offset, int limit)
            throws LogicException {
        analyze();
        String[] vars = new String[5];
        vars[0] = getFromSection();
        for (int i = 1; i < 5; i++)
            vars[i] = derivedSections[i] == null ? null : v.evaluate(derivedSections[i]);


       return new Grouper(previousKeyset,  qep.execute(
                computeQuery(vars, false), a, offset, limit).elements());
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
     * @param s
     *            the expression
     * @return The path to the null pointer (if the object is nullable), <code>null</code> otherwise
     */
    public Object checkExprSetOrNullable(String s) {
        int n = 0;
        int m = 0;
        while (true) {
            // FIXME: this is a not that good algorithm for finding stuff like a.b.c
            while (n < s.length() && !isMakId(s.charAt(n)))
                n++;

            if (n == s.length())
                return null;
            m = n;
            while (n < s.length() && isMakId(s.charAt(n)))
                n++;
            Object nl = checkId(s.substring(m, n));
            if (nl != null)
                return nl;
            if (n == s.length())
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
     * @param s
     *            the id
     * @return The path to the null pointer (if the object is nullable), <code>null</code> otherwise
     */
    public Object checkId(String s) {
        int dot = s.indexOf(".");
        if (dot == -1)
            return null;
        String substring = s.substring(0, dot);
        try { // if the "label" is actually a real number as 3.0
            Integer.parseInt(substring);
            return null; // if so, just return
        } catch (NumberFormatException e) {
        }
        DataDefinition dd = getOQLAnalyzer(fromAnalyzerOQL).getLabelType(substring);
        if (dd == null)
            throw new org.makumba.InvalidValueException("no such label " + substring);
        while (true) {
            int dot1 = s.indexOf(".", dot + 1);
            if (dot1 == -1) {
                String fn = s.substring(dot + 1);
                FieldDefinition fd = dd.getFieldDefinition(fn);
                if (fd == null) {
                    if (!this.useHibernate || !(fn.equals("id") || fn.startsWith("hibernate_")))
                        throw new org.makumba.NoSuchFieldException(dd, fn);
                    if (fn.equals("id"))
                        fd = dd.getFieldDefinition(dd.getIndexPointerFieldName());
                    else {
                        fd = dd.getFieldDefinition(fn.substring("hibernate_".length()));
                        if (fd == null)
                            throw new org.makumba.NoSuchFieldException(dd, fn);
                    }
                }
                if (fd.getType().equals("set"))
                    return fd;
                return null;
            }
            FieldDefinition fd = dd.getFieldDefinition(s.substring(dot + 1, dot1));
            if (fd == null)
                throw new org.makumba.NoSuchFieldException(dd, s.substring(dot + 1, dot1));
            if (!fd.getType().startsWith("ptr"))
                throw new InvalidFieldTypeException(fd, "pointer");
            if (!fd.isNotNull())
                return s.substring(0, dot1);
            dd = fd.getPointedType();
            dot = dot1;
        }
    }

    /**
     * Transforms the pointer into a hibernate pointer if we use hibernate
     * 
     * @param expr2
     *            the expression we want to check
     * @return A modified expression adapted to Hibernate
     */
    public String transformPointer(String expr2) {
        if (this.useHibernate
                && getOQLAnalyzer("SELECT " + expr2 + " as gigi FROM " + getFromSection()).getProjectionType()
                        .getFieldDefinition("gigi").getType().equals("ptr")) {
            int dot = expr2.lastIndexOf('.') + 1;
            return expr2.substring(0, dot) + "hibernate_" + expr2.substring(dot);
        }
        return expr2;
    }
}
