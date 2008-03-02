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

package org.makumba.list.tags;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.IterationTag;

import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.html.RecordViewer;

/**
 * Display of OQL query results in nested loops. The Query FROM, WHERE, GROUPBY and ORDERBY are indicated in the head of
 * the tag. The query projections are indicated by Value tags in the body of the tag. The sub-tags will generate
 * subqueries of their enclosing tag queries (i.e. their WHERE, GROUPBY and ORDERBY are concatenated). Attributes of the
 * environment can be passed as $attrName to the query
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class QueryTag extends GenericListTag implements IterationTag {

    private static final long serialVersionUID = 1L;

    String[] queryProps = new String[5];

    String separator = "";

    String countVar;

    String maxCountVar;

    String offset, limit;

    private int defaultOffset = 0;

    private String defaultLimit = "-1";

    static String standardCountVar = "org_makumba_view_jsptaglib_countVar";

    static String standardMaxCountVar = "org_makumba_view_jsptaglib_maxCountVar";

    static String standardLastCountVar = "org_makumba_view_jsptaglib_lastCountVar";

    static String standardMaxResultsVar = "org_makumba_view_jsptaglib_MaxResultsVar";

    static String standardMaxResultsContext = "org_makumba_view_jsptaglib_MaxResultsContext";

    static String standardMaxResultsKey = "org_makumba_view_jsptaglib_MaxResultsKey";

    public void setFrom(String s) {
        queryProps[ComposedQuery.FROM] = s;
    }

    public void setVariableFrom(String s) {
        queryProps[ComposedQuery.VARFROM] = s;
    }

    public void setWhere(String s) {
        queryProps[ComposedQuery.WHERE] = s;
    }

    public void setOrderBy(String s) {
        queryProps[ComposedQuery.ORDERBY] = s;
    }

    public void setGroupBy(String s) {
        queryProps[ComposedQuery.GROUPBY] = s;
    }

    public void setSeparator(String s) {
        separator = s;
    }

    public void setCountVar(String s) {
        countVar = s;
    }

    public void setMaxCountVar(String s) {
        maxCountVar = s;
    }

    public void setOffset(String s) throws JspException {
        onlyOuterListArgument("offset");
        onlyInt("offset", s);
        offset = s.trim();
    }

    public void setLimit(String s) throws JspException {
        onlyOuterListArgument("limit");
        onlyInt("limit", s);
        limit = s.trim();
    }

    public void setDefaultLimit(String s) throws JspException {
        onlyOuterListArgument("defaultLimit");
        onlyInt("defaultLimit", s);
        defaultLimit = s.trim();
    }

    public int getLimitInt() {
        int limitInt = -1;
        try {
            limitInt = QueryExecution.computeLimit(pageContext, limit, Integer.parseInt(defaultLimit), limitInt);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return limitInt;
    }

    public int getOffsetInt() {
        int defaultOffsetInt = 0;
        try {
            defaultOffsetInt = QueryExecution.computeLimit(pageContext, offset, defaultOffset, defaultOffsetInt);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defaultOffsetInt;
    }

    protected void onlyOuterListArgument(String s) throws JspException {
        QueryTag t = (QueryTag) findAncestorWithClass(this, QueryTag.class);
        while (t != null && t instanceof ObjectTag)
            t = (QueryTag) findAncestorWithClass(t, QueryTag.class);
        if (t instanceof QueryTag)
            throw new RuntimeWrappedException(new MakumbaJspException(this, "the " + s
                    + " parameter can only be set for the outermost mak:list tag"));
    }

    // runtime stuff
    QueryExecution execution;

    /**
     * Computes and set the tagKey. At analisys time, the listQuery is associated with the tagKey, and retrieved at
     * runtime. At runtime, the QueryExecution is discovered by the tag based on the tagKey.
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    public void setTagKey(PageCache pageCache) {
        tagKey = new MultipleKey(queryProps.length + 2);
        for (int i = 0; i < queryProps.length; i++)
            tagKey.setAt(queryProps[i], i);

        // if we have a parent, we append the key of the parent
        tagKey.setAt(getParentListKey(this, pageCache), queryProps.length);
        tagKey.setAt(id, queryProps.length + 1);
    }

    /**
     * Determines whether the tag can have the same key as others in the page
     * 
     * @return <code>true</code> if the tag is allowed to have the same key as others in the page, <code>false</code>
     *         otherwise
     */
    public boolean allowsIdenticalKey() {
        return false;
    }

    /**
     * Starts the analysis of the tag, without knowing what tags follow it in the page. Defines a query, sets the types
     * of variables to "int".
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    public void doStartAnalyze(PageCache pageCache) {
        // check whether we have an $.. in the order by (not supported, only #{..} is allowed
        String orderBy = queryProps[ComposedQuery.ORDERBY];
        if (orderBy != null && orderBy.indexOf("$") != -1) {
            throw new ProgrammerError("Illegal use of an $attribute orderBy: '" + orderBy
                    + "' ==> only JSP Expression Language using #{..} is allowed!");
        }

        // we make ComposedQuery cache our query
        QueryTag.cacheQuery(pageCache, tagKey, queryProps, getParentListKey(this, pageCache));

        if (countVar != null)
            setType(pageCache, countVar, MakumbaSystem.makeFieldOfType(countVar, "int"));

        if (maxCountVar != null)
            setType(pageCache, maxCountVar, MakumbaSystem.makeFieldOfType(maxCountVar, "int"));
    }

    /**
     * Ends the analysis of the tag, after all tags in the page were visited. As all the query projections are known, a
     * RecordViewer is cached as formatter for the mak:values nested in this tag.
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    public void doEndAnalyze(PageCache pageCache) {
        ComposedQuery cq = QueryTag.getQuery(pageCache, tagKey);
        cq.analyze();
        pageCache.cache(RecordFormatter.FORMATTERS, tagKey, new RecordViewer(cq));
    }

    static final Integer zero = new Integer(0);

    static final Integer one = new Integer(1);

    Object upperCount = null;

    Object upperMaxCount = null;

    private static ThreadLocal<ServletRequest> servletRequestThreadLocal = new ThreadLocal<ServletRequest>();

    /**
     * Decides if there will be any tag iteration. The QueryExecution is found (and made if needed), and we check if
     * there are any results in the iterationGroup.
     * 
     * @param pageCache
     *            The page cache for the current page
     * @return The tag return state as defined in the {@link javax.servlet.jsp.tagext.Tag} interface
     * @see QueryExecution
     */
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        servletRequestThreadLocal.set(pageContext.getRequest());
        if (getParentList(this) == null)
            QueryExecution.startListGroup(pageContext);
        else {
            upperCount = pageContext.getRequest().getAttribute(standardCountVar);
            upperMaxCount = pageContext.getRequest().getAttribute(standardMaxCountVar);
        }

        execution = QueryExecution.getFor(tagKey, pageContext, offset, limit, defaultLimit);

        int n = execution.onParentIteration();

        setNumberOfIterations(n);

        // set the total result count, i.e. the count this list would have w/o limit & offset
        int maxResults = Integer.MIN_VALUE;
        int limitEval = QueryExecution.computeLimit(pageContext, limit, Integer.parseInt(defaultLimit), -1);
        int offsetEval = QueryExecution.computeLimit(pageContext, offset, defaultOffset, 0);
        if ((offsetEval == 0 && limitEval == -1) || (offsetEval == 0 && (limitEval > 00 && limitEval < n))) {
            // we can set the total count if there is no limit / offset in the page
            maxResults = n;
        } else {
            // otherwise we need to make a new query
            // we only prepare the query, but do not run it, that will happen on demand in the method getting the result
            ComposedQuery query = null;
            String[] simpleQueryProps = queryProps.clone();
            simpleQueryProps[ComposedQuery.ORDERBY] = "";
            MultipleKey maxResultsKey = getMaxResultsKey(tagKey);
            MultipleKey parentKey = getParentListKey(this, pageCache);
            String ql = (String) pageCache.retrieve(MakumbaJspAnalyzer.QUERY_LANGUAGE,
                MakumbaJspAnalyzer.QUERY_LANGUAGE);
            query = parentKey == null ? new ComposedQuery(simpleQueryProps, ql) : new ComposedSubquery(
                    simpleQueryProps, QueryTag.getQuery(pageCache, parentKey), ql);
            query.addProjection("count(*)");
            query.init();
            pageCache.cache(GenericListTag.QUERY, maxResultsKey, query);
            // we need to pass these variables in request to the method doing the query
            // TODO: this looks like a hack, and might not be safe if there are more lists in the same page
            pageContext.getRequest().setAttribute(standardMaxResultsContext, pageContext);
            pageContext.getRequest().setAttribute(standardMaxResultsKey, maxResultsKey);
        }
        pageContext.getRequest().setAttribute(standardMaxResultsVar, maxResults);

        if (n > 0) {
            if (countVar != null)
                pageContext.setAttribute(countVar, one);
            pageContext.getRequest().setAttribute(standardCountVar, one);
            return EVAL_BODY_INCLUDE;
        }
        if (countVar != null)
            pageContext.setAttribute(countVar, zero);
        pageContext.getRequest().setAttribute(standardCountVar, zero);
        return SKIP_BODY;
    }

    private MultipleKey getMaxResultsKey(MultipleKey tagKey) {
        MultipleKey totalKey = (MultipleKey) tagKey.clone();
        totalKey.add(standardMaxResultsVar);
        return totalKey;
    }

    /**
     * Sets the number of iterations in the iterationGroup. ObjectTag will redefine this and throw an exception if n>1
     * 
     * @param n
     *            The number of iterations in the iterationGroup
     * @throws JspException
     * @see ObjectTag
     */
    protected void setNumberOfIterations(int n) throws JspException {
        Integer cnt = new Integer(n);
        if (maxCountVar != null)
            pageContext.setAttribute(maxCountVar, cnt);
        pageContext.getRequest().setAttribute(standardMaxCountVar, cnt);
    }

    /**
     * Decides whether to do further iterations. Checks if we got to the end of the iterationGroup.
     * 
     * @return The tag return state as defined in the {@link javax.servlet.jsp.tagext.Tag} interface
     * @throws JspException
     */
    public int doAfterBody() throws JspException {
        runningTag.set(tagData);
        try {

            int n = execution.nextGroupIteration();

            if (n != -1) {
                // print the separator
                try {
                    pageContext.getOut().print(separator);
                } catch (Exception e) {
                    throw new MakumbaJspException(e);
                }

                Integer cnt = new Integer(n + 1);
                if (countVar != null)
                    pageContext.setAttribute(countVar, cnt);
                pageContext.getRequest().setAttribute(standardCountVar, cnt);
                return EVAL_BODY_AGAIN;
            }
            return SKIP_BODY;
        } finally {
            runningTag.set(null);
        }
    }

    /**
     * Cleans up variables, especially for the rootList.
     * 
     * @param pageCache
     *            The page cache for the current page
     * @return The tag return state as defined in the {@link javax.servlet.jsp.tagext.Tag} interface in order to
     *         continue evaluating the page.
     * @throws JspException
     */
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        pageContext.getRequest().setAttribute(standardLastCountVar,
            pageContext.getRequest().getAttribute(standardMaxCountVar));

        pageContext.getRequest().setAttribute(standardCountVar, upperCount);
        pageContext.getRequest().setAttribute(standardMaxCountVar, upperMaxCount);
        execution.endIterationGroup();

        if (getParentList(this) == null)
            QueryExecution.endListGroup(pageContext);

        return EVAL_PAGE;
    }

    /**
     * Finds the parentList of a list
     * 
     * @param tag
     *            the tag we want to discover the parent of
     * @return the parent QueryTag of the Tag
     */
    public static AnalysableTag getParentList(AnalysableTag tag) {
        return (AnalysableTag) findAncestorWithClass(tag, QueryTag.class);
    }

    /**
     * Finds the key of the parentList of the Tag
     * 
     * @param tag
     *            the tag we want to discover the parent of
     * @param pageCache
     *            the pageCache of the current page
     * @return The MultipleKey identifying the parentList
     */
    public static MultipleKey getParentListKey(AnalysableTag tag, PageCache pageCache) {
        AnalysableTag parentList = getParentList(tag);
        return parentList == null ? null : parentList.getTagKey();
    }

    /**
     * Gets the query for a given key
     * 
     * @param key
     *            the key of the tag for which we want to get a query
     * @return The OQL query corresponding to this tag
     */
    public static ComposedQuery getQuery(PageCache pc, MultipleKey key) {
        ComposedQuery ret = (ComposedQuery) pc.retrieve(GenericListTag.QUERY, key);
        if (ret == null)
            throw new MakumbaError("unknown query for key " + key);
        return ret;
    }

    /**
     * Gets a composed query from the cache, and if none is found, creates one and caches it.
     * 
     * @param key
     *            the key of the tag
     * @param sections
     *            the sections needed to compose a query
     * @param parentKey
     *            the key of the parent tag, if any
     */
    public static ComposedQuery cacheQuery(PageCache pc, MultipleKey key, String[] sections, MultipleKey parentKey) {
        ComposedQuery ret = (ComposedQuery) pc.retrieve(GenericListTag.QUERY, key);
        if (ret != null)
            return ret;
        String ql = (String) pc.retrieve(MakumbaJspAnalyzer.QUERY_LANGUAGE, MakumbaJspAnalyzer.QUERY_LANGUAGE);
        ret = parentKey == null ? new ComposedQuery(sections, ql) : new ComposedSubquery(sections, QueryTag.getQuery(
            pc, parentKey), ql);

        ret.init();
        pc.cache(GenericListTag.QUERY, key, ret);
        return ret;
    }

    /**
     * Gives the value of the iteration in progress
     * 
     * @return The current count of iterations
     */
    public static int count() {
        Object countAttr = servletRequestThreadLocal.get().getAttribute(standardCountVar);
        if (countAttr == null) {
            // throw new ProgrammerError("mak:count() can only be used inside a <mak:list> tag");
            return -1;
        }
        return ((Integer) countAttr).intValue();
    }

    /**
     * Gives the maximum number of iteration of the iterationGroup
     * 
     * @return The maximum number of iterations within the current iterationGroup
     */
    public static int maxCount() {
        Object maxAttr = servletRequestThreadLocal.get().getAttribute(standardMaxCountVar);
        if (maxAttr == null) {
            // throw new ProgrammerError("mak:maxCount() can only be used inside a <mak:list> tag");
            return -1;
        }
        return ((Integer) maxAttr).intValue();
    }

    /**
     * Gives the maximum number of results returned as if the query would not contain any limit / offset. <br>
     * TODO: we need to pass quite some information in the request attributes, as this method has to be static. Not sure
     * what happens if there are more lists in the same page, if that would overlap or not.
     * 
     * @return The maximum number of results returned as if the query would not contain any limit / offset.
     */
    public static int maxResults() {
        ServletRequest servletRequest = servletRequestThreadLocal.get();
        Object totalAttr = servletRequest.getAttribute(standardMaxResultsVar);
        if (totalAttr == null) {
            // throw new ProgrammerError("mak:maxResults() can only be used inside a <mak:list> tag");
            return -1;
        }

        Integer total = ((Integer) totalAttr);
        if (total == Integer.MIN_VALUE) { // we still need to evaluate this total count
            PageContext pageContext = (PageContext) servletRequest.getAttribute(standardMaxResultsContext);
            MultipleKey keyMaxResults = (MultipleKey) servletRequest.getAttribute(standardMaxResultsKey);
            try {
                QueryExecution exec = QueryExecution.getFor(keyMaxResults, pageContext, null, null);
                exec.getIterationGroupData();
                total = ((Integer) exec.currentListData().get("col1"));
                servletRequest.setAttribute(standardMaxResultsVar, total);
            } catch (LogicException e) {
                e.printStackTrace();
                throw new RuntimeWrappedException(e);
            }
        }
        return total;
    }

    /**
     * Gives the total number of iterations of the previous iterationGroup
     * 
     * @return The total number of iterations performed within the previous iterationGroup
     */
    public static int lastCount() {
        if (servletRequestThreadLocal.get() == null)
            return -1;
        return ((Integer) servletRequestThreadLocal.get().getAttribute(standardLastCountVar)).intValue();
    }

    @Override
    public boolean canHaveBody() {
        return true;
    }
    
    @Override
    protected void doAnalyzedCleanup() {
        super.doAnalyzedCleanup();
        execution = null;
        queryProps[0] = queryProps[1] = queryProps[2] = queryProps[3] = null;
        countVar = maxCountVar = offset= limit= defaultLimit= null;
        separator = "";
    }
}