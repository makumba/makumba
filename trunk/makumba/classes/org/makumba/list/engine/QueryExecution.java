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
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.jsp.PageContext;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.attributes.PageAttributes;
import org.makumba.list.tags.GenericListTag;
import org.makumba.list.tags.QueryTag;
import org.makumba.providers.QueryProvider;

/**
 * This class holds the listData of a mak:list or the valueQuery data of a mak:value. It determines iterationGroups at
 * every parentIteration, and iterates through the iterationGroupData
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class QueryExecution {
    /** The results of the query associated with the list or queryMak:value */
    Grouper listData;

    /** The part of listData iterated for a certain parent iteration */
    Vector iterationGroupData;

    /** The index of iteration within the iteration group */
    public int iteration;

    /** A reference to the currentDataSet <strong>shared</strong> by all ListQueryExecutions in the listGroup */
    Stack currentDataSet;

    /** Stores the data for queryData */
    public HashMap valueQueryData = new HashMap();

    static final private String EXECUTIONS = "org.makumba.taglibQueryExecutions";

    static final private String CURRENT_DATA_SET = "org.makumba.currentDataSet";

    static final private Dictionary NOTHING = new ArrayMap();

    /**
     * Allocates a currentDataSet and a container for the QueryExecutions of the listGroup. Executed by the rootList.
     * 
     * @param pageContext
     *            The PageContext object of the current page
     */
    public static void startListGroup(PageContext pageContext) {
        pageContext.setAttribute(EXECUTIONS, new HashMap());

        Stack currentDataSet = new Stack();
        // org.makumba.view.Grouper requires the stack not be empty
        currentDataSet.push(NOTHING);
        pageContext.setAttribute(CURRENT_DATA_SET, currentDataSet);
    }

    /**
     * De-allocates all QueryExecutions in the listGroup, and the currentDataSet. Executed by the rootList.
     * 
     * @param pageContext
     *            The PageContext object of the current page
     */
    public static void endListGroup(PageContext pageContext) {
        pageContext.removeAttribute(EXECUTIONS);
        pageContext.removeAttribute(CURRENT_DATA_SET);
    }

    /**
     * Gets the QueryExecution for the given key, builds one if needed. Every list tag (QueryTag) calls this method. A
     * ListQueryTag will be built only in the first parentIteration and will be returned at next parentIterations.
     * 
     * @param key
     *            The tag key of the tag calling the QueryExecution
     * @param pageContext
     *            The PageContext object of the current page
     * @param offset
     *            Offset at which the iteration should start
     * @param limit
     *            Limit at which the iteration should stop
     * @throws LogicException
     */
    public static QueryExecution getFor(MultipleKey key, PageContext pageContext, String offset, String limit)
            throws LogicException {
        HashMap executions = (HashMap) pageContext.getAttribute(EXECUTIONS);

        QueryExecution lqe = (QueryExecution) executions.get(key);
        if (lqe == null)
            executions.put(key, lqe = new QueryExecution(key, pageContext, offset, limit));
        return lqe;
    }
    
    /** Like {@link #getFor(MultipleKey, PageContext, String, String)}, but uses the default values for offset/limit from the list tag. */
    public static QueryExecution getFor(MultipleKey key, PageContext pageContext, String offset, String limit,
            String defaultLimit) throws LogicException {
        HashMap executions = (HashMap) pageContext.getAttribute(EXECUTIONS);

        QueryExecution lqe = (QueryExecution) executions.get(key);
        if (lqe == null)
            executions.put(key, lqe = new QueryExecution(key, pageContext, offset, limit, defaultLimit));
        return lqe;
    }

    /**
     * Constructs a QueryExection which executes the given query, in the given database, with the given attributes, to
     * form the listData. Keeps the reference to the currentDataSet for future push and pop operations, finds the nested
     * valueQueries.
     * 
     * TODO implement an AttributeProvider to remove the dependency from controller
     * 
     * @param key
     *            The tag key of the tag calling the QueryExecution
     * @param pageContext
     *            The PageContext object of the current page
     * @param offset
     *            Offset at which the iteration should start
     * @param limit
     *            Limit at which the iteration should stop
     * @throws LogicException
     */
    private QueryExecution(MultipleKey key, PageContext pageContext, String offset, String limit) throws LogicException {
        currentDataSet = (Stack) pageContext.getAttribute(CURRENT_DATA_SET);
        ComposedQuery cq = QueryTag.getQuery(GenericListTag.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance()), key);
        QueryProvider qep = QueryProvider.makeQueryRunner(GenericListTag.getDataSourceName(pageContext), (String) GenericListTag.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance()).retrieve(MakumbaJspAnalyzer.QUERY_LANGUAGE, MakumbaJspAnalyzer.QUERY_LANGUAGE));
        
        try {
            Attributes.MA args = new Attributes.MA(PageAttributes.getAttributes(pageContext));
            listData = cq.execute(qep, args, new Evaluator(pageContext),
                    computeLimit(pageContext, offset, 0), computeLimit(pageContext, limit, -1));
        } finally {
            qep.close();
        }
    }

    /** Like {@link #QueryExecution(MultipleKey, PageContext, String, String)}, but uses default limit/offset parameters */
    private QueryExecution(MultipleKey key, PageContext pageContext, String offset, String limit, String defaultLimit)
            throws LogicException {
        currentDataSet = (Stack) pageContext.getAttribute(CURRENT_DATA_SET);
        ComposedQuery cq = QueryTag.getQuery(
            GenericListTag.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance()), key);
        QueryProvider qep = QueryProvider.makeQueryRunner(GenericListTag.getDataSourceName(pageContext),
            (String) GenericListTag.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance()).retrieve(
                MakumbaJspAnalyzer.QUERY_LANGUAGE, MakumbaJspAnalyzer.QUERY_LANGUAGE));

        try {
            Attributes.MA args = new Attributes.MA(PageAttributes.getAttributes(pageContext));
            listData = cq.execute(qep, args, new Evaluator(pageContext), computeLimit(pageContext, offset, 0, 0),
                computeLimit(pageContext, limit, Integer.parseInt(defaultLimit), -1));
        } finally {
            qep.close();
        }
    }
    
    /**
     * Computes the limit from the value passed in the limit tag parameter.
     * 
     * @param pc
     *            The PageContext object of the current page
     * @param s
     *            The parameter value passed
     * @param defaultNonSpecified
     *            The default value of the limit/offset if the attribute is not specified in the list tag
     * @param defaultValue
     *            The fault value of the limit/offset if the attribute is specified in the list tag, but not present as
     *            attribute
     * @return The int value of the limit, if a correct one is passed as tag parameter
     * @throws LogicException
     */
    public static int computeLimit(PageContext pc, String s, int defaultValue, int defaultNonSpecified) throws LogicException {
        if (s == null) {
            return defaultNonSpecified;
        }
        s = s.trim();
        Object o = s;
        if (s.startsWith("$")) {
            // the limit param is a dynamic value
            try {
                o = PageAttributes.getAttributes(pc).getAttribute(s.substring(1));
            } catch (AttributeNotFoundException e) {
                // if the limit param cannot be found, we try to use the default value for the param
                if (defaultValue != Integer.MIN_VALUE) {
                    return defaultValue;
                } else {
                    // if there is no default, we throw on the exception
                    throw e;
                }
            }
        }

        if (o instanceof String) {
            try {
                return Integer.parseInt((String) o);
            } catch (NumberFormatException nfe) {
                throw new org.makumba.InvalidValueException("Integer expected for OFFSET and LIMIT: " + s + ", value: "
                        + o + ", type: " + o.getClass().getName());
            }
        }
        if (!(o instanceof Number) || ((Number) o).intValue() != ((Number) o).doubleValue()) {
            throw new org.makumba.InvalidValueException("Integer expected for OFFSET and LIMIT: " + s + ", value: " + o
                    + ", type: " + o.getClass().getName());
        }
        return ((Number) o).intValue();
    }

    private static int computeLimit(PageContext pc, String s, int defaultNonSpecified) throws LogicException {
        return computeLimit(pc, s, Integer.MIN_VALUE, defaultNonSpecified);
    }

    /**
     * Gets the iterationGroupData from the listData
     * 
     * @return The size of the current iterationGroupData
     * @see iterationGroupData
     * @see listData
     */
    public int getIterationGroupData() {
        iteration = 0;
        iterationGroupData = listData.getData(currentDataSet);
        return dataSize();
    }

    /**
     * Computes the size of the iterationGroupData
     * 
     * @return The int value of the current iterationGroupData size
     */

    public int dataSize() {
        if (iterationGroupData == null || iterationGroupData.size() == 0)
            return 0;
        else
            return iterationGroupData.size();
    }

    /**
     * Computes the iterationGroupData based on listData and currentDataSet. Pushes the first currentListData to the
     * currentDataSet. Only if the dataSize is 0, nothing is pushed to the stack, and at endIterationGroup, nothing is
     * popped.
     * 
     * @return The number of iterations in the iterationGroup, possibly 0.
     */
    public int onParentIteration() {
        getIterationGroupData();
        int n = dataSize();
        if (n != 0)
            currentDataSet.push(currentListData());
        return n;
    }

    /**
     * Pops the previous currentListData from the currentDataSet and checks if there are more iterations to be made
     * within the iterationGroup. If there are, adds the currentListData to the currentDataSet; else pushes a DUMMY data
     * (cfr bug 555).
     * 
     * @return The current iteration position, relative to the start of the interationGroup or -1 if there are no more
     *         results
     */
    public int nextGroupIteration() {
        valueQueryData.clear();
        currentDataSet.pop();
        iteration++;
        if (iteration == iterationGroupData.size()) {
            currentDataSet.push(NOTHING); // push a dummy Data; will be removed by endIterationGroup
            return -1;
        }
        currentDataSet.push(currentListData());
        return iteration;
    }

    /**
     * Pops the the last data of an iterationGroup. In case of an empty-body/simple QueryTag, this will pop the first
     * element; otherwise it pops the dummy data. cfr Bug 555.
     */
    public void endIterationGroup() {
        if (dataSize() > 0) {
            currentDataSet.pop();
        }
    }

    /**
     * Returns the data of the current iteration
     * 
     * @return The current listData
     */
    public ArrayMap currentListData() {
        return (ArrayMap) iterationGroupData.elementAt(iteration);
    }

}
