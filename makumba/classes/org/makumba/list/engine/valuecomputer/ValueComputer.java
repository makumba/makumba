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
//  $Id: ValueComputer.java 1394 2007-07-20 11:37:36Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.list.engine.valuecomputer;

import java.util.Vector;

import javax.servlet.jsp.JspException;

import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.PageAttributes;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.html.RecordViewer;
import org.makumba.list.tags.MakumbaTag;
import org.makumba.list.tags.QueryTag;
import org.makumba.list.tags.ValueTag;
import org.makumba.util.MultipleKey;
import org.makumba.view.RecordFormatter;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer;

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
    public static ValueComputer getValueComputerAtAnalysis(AnalysableTag analyzed, String expr,
            PageCache pageCache) {
        expr = expr.trim();
        Object check = QueryTag.getQuery(pageCache, QueryTag.getParentListKey(analyzed, pageCache)).checkExprSetOrNullable(expr);

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
    protected FieldDefinition type;

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
    ValueComputer(MultipleKey listKey, String expr, PageCache pageCache) {
        parentKey = listKey;
        this.expr = expr;
        QueryTag.getQuery(pageCache, parentKey).checkProjectionInteger(expr);

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
    ValueComputer(AnalysableTag analyzed, String expr, PageCache pageCache) {
        this(QueryTag.getParentListKey(analyzed, pageCache), expr, pageCache);
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
     * @param pageCache
     *            the page cache
     */
    public void doEndAnalyze(PageCache pageCache) {
        ComposedQuery q = QueryTag.getQuery(pageCache, getQueryKey());
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
    public void print(ValueTag running, PageCache pageCache) throws JspException, LogicException {
        Object o = getValue(running);
        String s = null;
        if (running.getPrintVar() != null || running.getVar() == null) {
            s = ((RecordViewer) pageCache.retrieve(RecordFormatter.FORMATTERS, getQueryKey())).format(projectionIndex, o, running.getParams());
        }

        if (running.getVar() != null)
            PageAttributes.setAttribute(running.getPageContext(), running.getVar(), o);
        if (running.getPrintVar() != null)
            running.getPageContext().setAttribute(running.getPrintVar(), s);
        if (running.getPrintVar() == null && running.getVar() == null) {
            try {
                running.getPageContext().getOut().print(s);
            } catch (Exception e) {
                throw new JspException(e.toString());
            }
        }
    }

    public FieldDefinition getType() {
        return type;
    }
}