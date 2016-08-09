/*
 * Created on Apr 1, 2011
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.list.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Projections {
    /** The projections made in this query */
    List<String> projections = new ArrayList<String>();

    /** The expression associated to each projection */
    Map<String, Integer> projectionExpr = new HashMap<String, Integer>();

    public Projections(Projections pr) {
        projections.addAll(pr.projections);
        projectionExpr.putAll(pr.projectionExpr);
    }

    public Projections() {

    }

    /**
     * Gets a given projection
     * 
     * @param n
     *            the index of the projection
     * @return A String containing the projection
     */
    public String getProjectionAt(int n) {
        return projections.get(n);
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
        projections.add(expr);
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
        Integer index = getProjectionIndex(expr);
        if (index == null) {
            addProjection(expr);
            // FIXME: if DISTINCT is true, need to recompute the keyset and notify the subqueries to recompute their
            // previous keyset
            return null;
        }
        return index;
    }

    public Integer getProjectionIndex(String expr) {
        return projectionExpr.get(expr);
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
        if (i == null) {
            return null;
        }
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

}