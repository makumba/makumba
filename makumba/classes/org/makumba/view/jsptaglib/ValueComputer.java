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

package org.makumba.view.jsptaglib;

import java.util.Vector;

import javax.servlet.jsp.JspException;

import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.controller.jsp.PageAttributes;
import org.makumba.util.MultipleKey;
import org.makumba.view.ComposedQuery;
import org.makumba.view.html.RecordViewer;

/**
 * Every ValueTag will build a ValueComputer at page analysis, which it then retrieves and uses at page running
 * 
 * @author Cristian Bogdan
 * @version $Id
 */
public class ValueComputer {

    /**
     * Determines if 'analyzed' is a queryMak:value or a nonQueryMak:value
     * 
     * @param analyzed
     *            the analyzed tag
     * @param expr
     *            the expression passed in the tag
     * @param pageCache
     *            the page cache of the page
     */
    public static ValueComputer getValueComputerAtAnalysis(MakumbaTag analyzed, String expr,
            MakumbaJspAnalyzer.PageCache pageCache) {
        expr = expr.trim();
        Object check = pageCache.getQuery(analyzed.getParentListKey(pageCache)).checkExprSetOrNullable(expr);

        FieldDefinition set = null;
        String nullableExpr = null;

        if (check instanceof String)
            nullableExpr = (String) check;

        if (check instanceof FieldDefinition)
            set = (FieldDefinition) check;

        if (nullableExpr == null && set == null)
            return new ValueComputer(analyzed, expr, pageCache);

        if (set == null)
            return new NullableValueComputer(analyzed, nullableExpr, expr, pageCache);
        return new SetValueComputer(analyzed, set, expr, pageCache);
    }

    /** The key of the parentList */
    MultipleKey parentKey;

    /** The queryProjection index in the currentListData */
    int projectionIndex;

    /** The queryProjection expression */
    String expr;

    /** The queryProjection type */
    FieldDefinition type;

    ValueComputer() {
    }

    /**
     * A special ValueComputer made by mak:lists who want to select extra expressions
     * 
     * @param listKey
     *            the key of the list
     * @param expr
     *            the extra expression
     * @param pageCache
     *            the page cache
     */
    ValueComputer(MultipleKey listKey, String expr, MakumbaJspAnalyzer.PageCache pageCache) {
        parentKey = listKey;
        this.expr = expr;
        pageCache.getQuery(parentKey).checkProjectionInteger(expr);

    }

    /**
     * A nonQueryMak:value value computer
     * 
     * @param analyzed
     *            the analyzed tag
     * @param expr
     *            the expression of the tag
     * @param pageCache
     *            the page cache
     */
    ValueComputer(MakumbaTag analyzed, String expr, MakumbaJspAnalyzer.PageCache pageCache) {
        this(analyzed.getParentListKey(pageCache), expr, pageCache);
    }

    /**
     * The key of the query in which this value is a projection.
     * 
     * @return The key of the parent of the value
     */
    MultipleKey getQueryKey() {
        return parentKey;
    }

    /**
     * Computes the queryProjection index in the currentListData, and the queryProjection type.
     * 
     * @param analyzed
     *            the analyzed tag
     * @param pageCache
     *            the page cache
     */
    public void doEndAnalyze(MakumbaTag analyzed, MakumbaJspAnalyzer.PageCache pageCache) {
        ComposedQuery q = pageCache.getQuery(getQueryKey());
        projectionIndex = q.checkProjectionInteger(expr).intValue();

        if (type == null) // if type is not set in the constructor
            type = q.getResultType().getFieldDefinition(projectionIndex);
    }

    /**
     * Gets the value of the queryProjection from the currentListData of the enclosing query. Used mostly by InputTag
     * 
     * @param running
     *            the tag that is currently running
     * @throws LogicException
     */
    public Object getValue(MakumbaTag running) throws LogicException {
        return getValue(running.getPageContext());
    }

    /**
     * Gets the value of from the QueryExecution based on the projectionIndex
     * 
     * @param pc
     *            the page context
     * @return the computed value
     * @throws LogicException
     */
    Object getValue(javax.servlet.jsp.PageContext pc) throws LogicException {
        return QueryExecution.getFor(getQueryKey(), pc, null, null).currentListData().data[projectionIndex];
    }

    /**
     * Formats the value of the queryProjection from the currentListData of the enclosing query. Sets the var and the
     * printVar values.
     * 
     * @param running
     *            the tag that is currently running
     * @param pageCache
     *            the page cache of the current page
     * @throws JspException
     * @throws LogicException
     */
    public void print(ValueTag running, MakumbaJspAnalyzer.PageCache pageCache) throws JspException, LogicException {
        Object o = getValue(running);
        String s = null;
        if (running.printVar != null || running.var == null) {
            s = ((RecordViewer) pageCache.formatters.get(getQueryKey())).format(projectionIndex, o, running.params);
        }

        if (running.var != null)
            PageAttributes.setAttribute(running.getPageContext(), running.var, o);
        if (running.printVar != null)
            running.getPageContext().setAttribute(running.printVar, s);
        if (running.printVar == null && running.var == null) {
            try {
                running.getPageContext().getOut().print(s);
            } catch (Exception e) {
                throw new JspException(e.toString());
            }
        }
    }
}

/**
 * The ValueComputer of a queryMak:value
 * 
 * @author Cristian Bogdan
 */
abstract class QueryValueComputer extends ValueComputer {

    /** The key of the generated query */
    MultipleKey queryKey;

    /**
     * Makes a key that adds the given keyDifference to the tagKey of the parentList, and associates with it a subquery
     * of the parentQuery made from the given queryProps.
     * 
     * @param analyzed
     *            the analyzed tag
     * @param keyDifference
     * @param queryProps
     * @param expr
     * @param pageCache
     *            the page cache of the current page
     */
    public void makeQueryAtAnalysis(MakumbaTag analyzed, String keyDifference, String[] queryProps, String expr,
            MakumbaJspAnalyzer.PageCache pageCache) {
        this.expr = expr;
        parentKey = analyzed.getParentListKey(pageCache);

        queryKey = new MultipleKey(parentKey, keyDifference);

        pageCache.cacheQuery(queryKey, queryProps, parentKey).checkProjectionInteger(expr);
    }

    /** The key of the query in which this value is a projection. Returns queryKey */
    MultipleKey getQueryKey() {
        return queryKey;
    }

    /**
     * If other ValueComputers sharing the same valueQuery did not analyze it yet, we analyze it here.
     * 
     * @param analyzed
     *            the analyzed tag
     * @param pageCache
     *            the page cache of the current page
     */
    public void doEndAnalyze(MakumbaTag analyzed, MakumbaJspAnalyzer.PageCache pageCache) {
        if (pageCache.formatters.get(queryKey) == null) {
            ComposedQuery myQuery = pageCache.getQuery(queryKey);
            myQuery.analyze();
            pageCache.formatters.put(queryKey, new RecordViewer(myQuery));
        }
        super.doEndAnalyze(analyzed, pageCache);
    }

    static final Object dummy = new Object();

    /**
     * Obtains the iterationGroupData for the valueQuery
     * 
     * @param running
     *            the tag that is currently running
     * @throws LogicException
     * @return The QueryExecution that will give us the data
     */
    QueryExecution runQuery(MakumbaTag running) throws LogicException {
        QueryExecution ex = QueryExecution.getFor(queryKey, running.getPageContext(), null, null);

        QueryExecution parentEx = QueryExecution.getFor(parentKey, running.getPageContext(), null, null);

        // if the valueQuery's iterationGroup for this parentIteration was not computed, do it now...
        if (parentEx.valueQueryData.get(queryKey) == null) {
            ex.getIterationGroupData();

            // ... and make sure it won't be done this parentIteration again
            parentEx.valueQueryData.put(queryKey, dummy);
        }
        return ex;
    }
}

/**
 * The manager of a nullableValueQuery
 * 
 * @author Cristian Bogdan
 */
class NullableValueComputer extends QueryValueComputer {

    static final String emptyQueryProps[] = new String[5];

    /**
     * Makes a query that is identical to the parentQuery, but has expr as projection.
     * 
     * @param analyzed
     *            the tag that is analyzed
     * @param nullableExpr
     *            the nullable expression
     * @param expr
     *            the expression we use as projection
     * @param pageCache
     *            the page cache of the current page
     */
    NullableValueComputer(MakumbaTag analyzed, String nullableExpr, String expr, MakumbaJspAnalyzer.PageCache pageCache) {
        makeQueryAtAnalysis(analyzed, nullableExpr.trim(), emptyQueryProps, expr, pageCache);
    }

    /**
     * Checks if the iterationGroupData is longer than 1, and throws an exception if so. Takes the first result (if any)
     * otherwise.
     * 
     * @param running
     *            the tag that is currently running
     * @throws LogicException
     */
    public Object getValue(MakumbaTag running) throws LogicException {
        QueryExecution ex = runQuery(running);
        int n = ex.dataSize();
        if (n > 1)
            throw new RuntimeException("nullable query with more than one result ??? " + n);
        if (n == 0)
            return null;
        return ex.currentListData().data[projectionIndex];
    }
}

/**
 * The manager of a setValueQuery.
 * 
 * @author Cristian Bogdan
 */
class SetValueComputer extends QueryValueComputer {
    /** If we are in a value tag, the name of the queryProjection that computes the title field, otherwise null */
    String name = null;

    /** If we are in a value tag, the index of the queryProjection that computes the title field, otherwise null */
    int nameIndex;

    /**
     * Makes a query that has an extra FROM: the set requested. As projections, add the key of the set type and, if we
     * are in a value tag, the title field.
     * 
     * @param analyzed
     *            the tag that is analyzed
     * @param set
     *            the FieldDefinition of the set we want to compute a value of
     * @param setExpr
     *            the expression of the set
     * @param pageCache
     *            the page cache of the current page
     */
    SetValueComputer(MakumbaTag analyzed, FieldDefinition set, String setExpr, MakumbaJspAnalyzer.PageCache pageCache) {
        type = set;
        String label = setExpr.replace('.', '_');
        String queryProps[] = new String[5];
        queryProps[ComposedQuery.FROM] = setExpr + " " + label;

        if (analyzed instanceof ValueTag) {
            name = label + "." + set.getForeignTable().getTitleFieldName();
            queryProps[ComposedQuery.ORDERBY] = name;
        }

        makeQueryAtAnalysis(analyzed, set.getName(), queryProps, label, pageCache);

        if (analyzed instanceof ValueTag)
            pageCache.getQuery(queryKey).checkProjectionInteger(name);
    }

    /**
     * Computes nameIndex
     * 
     * @param analyzed
     *            the analyzed tag
     * @param pageCache
     *            the page cache of the current page
     */
    public void doEndAnalyze(MakumbaTag analyzed, MakumbaJspAnalyzer.PageCache pageCache) {
        super.doEndAnalyze(analyzed, pageCache);
        if (name != null)
            nameIndex = pageCache.getQuery(queryKey).checkProjectionInteger(name).intValue();
    }

    /**
     * Goes through the iterationGroupData and returns a vector with the set values. Used only by InputTag
     * 
     * @param running
     *            the tag that is currently running
     * @throws LogicException
     */
    public Object getValue(MakumbaTag running) throws LogicException {
        QueryExecution ex = runQuery(running);
        int n = ex.dataSize();
        Vector v = new Vector();

        for (ex.iteration = 0; ex.iteration < n; ex.iteration++)
            v.addElement(ex.currentListData().data[projectionIndex]);
        return v;
    }

    /**
     * Goes through the iterationGroupData and prints the set values, comma-separated; also sets var (Vector with the
     * set values) and printVar
     * 
     * @param running
     *            the tag that is currently running
     * @param pageCache
     *            the pageCache of the current page
     * @throws JspException
     * @throws LogicException
     */
    // FIXME (fred) shouldn't the formatting be in view.html package, instead of here?
    public void print(ValueTag running, MakumbaJspAnalyzer.PageCache pageCache) throws JspException, LogicException {
        QueryExecution ex = runQuery(running);
        int n = ex.dataSize();
        Vector v = null;

        if (running.var != null)
            v = new Vector();

        String sep = "";
        StringBuffer print = new StringBuffer();
        for (ex.iteration = 0; ex.iteration < n; ex.iteration++) {
            print.append(sep);
            sep = ",";
            if (running.var != null)
                v.addElement(ex.currentListData().data[projectionIndex]);
            print.append(ex.currentListData().data[nameIndex]);
        }
        String s = print.toString();

        // replace by 'default' or 'empty' if necessary
        if (n == 0 && running.params.get("default") != null)
            s = (String) running.params.get("default");

        if (s.length() == 0 && running.params.get("empty") != null)
            s = (String) running.params.get("empty");

        if (running.var != null)
            PageAttributes.setAttribute(running.getPageContext(), running.var, v);
        if (running.printVar != null)
            running.getPageContext().setAttribute(running.printVar, s);
        if (running.printVar == null && running.var == null) {
            try {
                running.getPageContext().getOut().print(s);
            } catch (Exception e) {
                throw new JspException(e.toString());
            }
        }
    }
}
