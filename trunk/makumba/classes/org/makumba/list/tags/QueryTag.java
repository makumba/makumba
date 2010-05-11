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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.IterationTag;

import org.apache.commons.lang.StringUtils;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableElement;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.controller.http.MakumbaJspFactory;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.html.RecordViewer;
import org.makumba.providers.DataDefinitionProvider;

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

    String[] queryProps = new String[6];

    String separator = "";

    String countVar;

    String maxCountVar;

    String offset, limit;

    String editable, logicClass, editPage;

    private int defaultOffset = 0;

    private String defaultLimit = "-1";

    protected String authorize = "filter";

    static String standardCountVar = "org_makumba_view_jsptaglib_countVar";

    static String standardMaxCountVar = "org_makumba_view_jsptaglib_maxCountVar";

    static String standardLastCountVar = "org_makumba_view_jsptaglib_lastCountVar";

    static String standardNextCountVar = "org_makumba_view_jsptaglib_nextCountVar";

    static String standardMaxResultsVar = "org_makumba_view_jsptaglib_MaxResultsVar";

    static String standardMaxResultsKey = "org_makumba_view_jsptaglib_MaxResultsKey";

    static String lastFinishedListKey = "org_makumba_view_jsptaglib_LastListKey";

    static String runningListKeyStack = "org_makumba_view_jsptaglib_RunningListKeyStack";

    static String queryExecuted = "org_makumba_view_jsptaglib_QueryExecuted";

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

    public void setAuthorize(String s) {
        this.authorize = s;
    }

    public void setEditable(String s) {
        editable = s.trim();
    }

    public String getLogicClass() {
        return logicClass;
    }

    public void setLogicClass(String logicClass) {
        this.logicClass = logicClass;
    }

    public String getEditPage() {
        return editPage;
    }

    public void setEditPage(String editPage) {
        this.editPage = editPage;
    }

    public String getEditable() {
        return this.editable;
    }

    public int getLimitInt() {
        int limitInt = -1;
        try {
            int defaultLimitInt = QueryExecution.computeLimit(pageContext, defaultLimit, -1, -1);
            limitInt = QueryExecution.computeLimit(pageContext, limit, defaultLimitInt, limitInt);
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
        while (t != null && t instanceof ObjectTag) {
            t = (QueryTag) findAncestorWithClass(t, QueryTag.class);
        }
        if (t instanceof QueryTag) {
            throw new RuntimeWrappedException(new MakumbaJspException(this, "the " + s
                    + " parameter can only be set for the outermost mak:list tag"));
        }
    }

    // runtime stuff
    QueryExecution execution;

    /**
     * Computes and set the tagKey. At analysis time, the listQuery is associated with the tagKey, and retrieved at
     * runtime. At runtime, the QueryExecution is discovered by the tag based on the tagKey.
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    @Override
    public void setTagKey(PageCache pageCache) {
        tagKey = new MultipleKey(queryProps.length + 2);
        for (int i = 0; i < queryProps.length; i++) {
            tagKey.setAt(queryProps[i], i);
        }

        // if we have a parent, we append the key of the parent
        tagKey.setAt(getParentListKey(this, pageCache), queryProps.length);
        tagKey.setAt(id, queryProps.length + 1);
        // FIXME: add limit and offset to tag key if they are not null; requires initial length to be >
        // (queryProps.length + 2)
    }

    /**
     * Determines whether the tag can have the same key as others in the page
     * 
     * @return <code>true</code> if the tag is allowed to have the same key as others in the page, <code>false</code>
     *         otherwise
     */
    @Override
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
    @Override
    public void doStartAnalyze(PageCache pageCache) {
        // check whether we have an $.. in the order by (not supported, only #{..} is allowed
        String orderBy = queryProps[ComposedQuery.ORDERBY];
        if (orderBy != null && orderBy.indexOf("$") != -1) {
            throw new ProgrammerError("Illegal use of an $attribute orderBy: '" + orderBy
                    + "' ==> only JSP Expression Language using #{..} is allowed!");
        }

        // we make ComposedQuery cache our query
        QueryTag.cacheQuery(pageCache, tagKey, queryProps, getParentListKey(this, pageCache));

        if (countVar != null) {
            setType(pageCache, countVar, DataDefinitionProvider.getInstance().makeFieldOfType(countVar, "int"));
        }

        if (maxCountVar != null) {
            setType(pageCache, maxCountVar, DataDefinitionProvider.getInstance().makeFieldOfType(maxCountVar, "int"));
        }
    }

    /**
     * Ends the analysis of the tag, after all tags in the page were visited. As all the query projections are known, a
     * RecordViewer is cached as formatter for the mak:values nested in this tag.
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    @Override
    public void doEndAnalyze(PageCache pageCache) {
        ComposedQuery cq = QueryTag.getQuery(pageCache, tagKey);
        cq.analyze();
        pageCache.cache(MakumbaJspAnalyzer.FORMATTERS, tagKey, new RecordViewer(cq));
    }

    static final Integer zero = new Integer(0);

    static final Integer one = new Integer(1);

    Object upperCount = null;

    Object upperMaxCount = null;

    Object upperMaxResults = null;

    /**
     * Decides if there will be any tag iteration. The QueryExecution is found (and made if needed), and we check if
     * there are any results in the iterationGroup. Calls {@link #initiateQueryExecution(PageContext, boolean)} and
     * {@link #doTagExecution(PageCache, PageContext)}
     * 
     * @param pageCache
     *            The page cache for the current page
     * @return The tag return state as defined in the {@link javax.servlet.jsp.tagext.Tag} interface
     * @see QueryExecution
     */
    @Override
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        initiateQueryExecution(pageContext, false);
        return doTagExecution(pageCache, pageContext);
    }

    /**
     * This method is initiating the query execution. It is intended to be called by two means:
     * <ul>
     * <li>In the standard flow at the start of the tag, by {@link #doAnalyzedStartTag(PageCache)}</li>
     * <li>or before the start of the tag, by {@link #nextCount()}</li>
     * </ul>
     * It thus needs to know whether the query has already been executed; it does so by setting variables in the
     * {@link ServletRequest}
     */
    private void initiateQueryExecution(PageContext pageContext, boolean preTagStartInitialisation)
            throws LogicException {

        // we need to figure out whether the query was already initiated before, which can be either
        // a.) we are in the beginning of the tag, and there was a mak:nextCount() before
        // b.) we are in a mak:nextCount(), and there was another mak:nextCount() before
        // we know that the query was executed it the pageContext holds an attribute with the exact query key
        // the key is computed by getListKey(), and contains the current list key, and all parent list keys & iteration
        // numbers

        String listKey = getListKey(pageContext);
        final boolean wasStarted = pageContext.getRequest().getAttribute(listKey) != null;

        // start the list group if it wasn't started before (by nextCount()), AND if it is a root list
        if (!wasStarted && getParentList(this) == null) {
            QueryExecution.startListGroup(pageContext);
        }
        // if we are not in preTagStartInitialisation, i.e. not triggered by nextCount, AND in a nested list
        // then set the values of the outer/parent list in this nested list
        if (!preTagStartInitialisation && getParentList(this) != null) {
            upperCount = pageContext.getRequest().getAttribute(standardCountVar);
            upperMaxCount = pageContext.getRequest().getAttribute(standardMaxCountVar);
            upperMaxResults = pageContext.getRequest().getAttribute(standardMaxResultsVar);
        }

        execution = QueryExecution.getFor(tagKey, pageContext, offset, limit, defaultLimit);

        if (!wasStarted) {
            // if this tag is at its first iteration, compute the iterationGroupData, and set attributes
            int n = execution.onParentIteration();
            pageContext.getRequest().setAttribute(standardNextCountVar, n);
            pageContext.getRequest().setAttribute(listKey + "_" + standardNextCountVar, n);

            // mark this list (iteration) to be run
            pageContext.getRequest().setAttribute(listKey, Boolean.TRUE);
        }
    }

    private int doTagExecution(PageCache pageCache, PageContext pageContext) throws LogicException, JspException {
        // we retrieve the number of iterations from the request, as it was set there before by initiateExecution
        int n = (Integer) pageContext.getRequest().getAttribute(getListKey(pageContext) + "_" + standardNextCountVar);
        setNumberOfIterations(n);

        // for nextCount() to know which list to relate to, store the key of the currently execute mak:list in the
        // request. Need to keep a stack of keys to support nested lists
        Stack<MultipleKey> currentListKeyStack = getRunningQueryTagStack(pageContext);
        if (currentListKeyStack == null) { // create a new stack, if there's none yet
            currentListKeyStack = new Stack<MultipleKey>();
            pageContext.getRequest().setAttribute(runningListKeyStack, currentListKeyStack);
        }
        currentListKeyStack.push(getTagKey());

        // set the total result count, i.e. the count this list would have w/o limit & offset
        int maxResults = Integer.MIN_VALUE;
        int defaultLimitInt = QueryExecution.computeLimit(pageContext, defaultLimit, -1, -1);
        int limitEval = QueryExecution.computeLimit(pageContext, limit, defaultLimitInt, -1);
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
            String ql = MakumbaJspAnalyzer.getQueryLanguage(pageCache);
            query = parentKey == null ? new ComposedQuery(simpleQueryProps, ql) : new ComposedSubquery(
                    simpleQueryProps, QueryTag.getQuery(pageCache, parentKey), ql);
            query.addProjectionDirectly("count(*)");
            query.init();
            pageCache.cache(MakumbaJspAnalyzer.QUERY, maxResultsKey, query);
            // we need to pass these variables in request to the method doing the query
            // TODO: this looks like a hack, and might not be safe if there are more lists in the same page
            pageContext.getRequest().setAttribute(standardMaxResultsKey, maxResultsKey);
        }
        pageContext.getRequest().setAttribute(standardMaxResultsVar, maxResults);

        if (n > 0) {
            if (countVar != null) {
                pageContext.setAttribute(countVar, one);
            }
            pageContext.getRequest().setAttribute(standardCountVar, one);
            pageContext.getRequest().setAttribute(getListSpecificCountVar(this), one);
            return EVAL_BODY_INCLUDE;
        } else {
            if (countVar != null) {
                pageContext.setAttribute(countVar, zero);
            }
            pageContext.getRequest().setAttribute(standardCountVar, zero);
            pageContext.getRequest().setAttribute(getListSpecificCountVar(this), zero);
            return SKIP_BODY;
        }
    }

    /**
     * Compute a {@link String} that uniquely identifies this mak:list/object inside the current request; for nested
     * mak:list/objects, we also append the key of all the parent mak:list/objects, and their current iteration.<br/>
     * The key starts being built from the root list.
     */
    private String getListKey(PageContext pageContext) {
        String listStartedKey = "";
        QueryTag current = this;
        while (getParentList(current) != null) {
            QueryTag queryTag = (QueryTag) getParentList(current);
            String key = "Key:" + queryTag.getTagKey().hashCode();
            final String listSpecificCountVar = getListSpecificCountVar(queryTag);
            key += "_iteration:" + pageContext.getRequest().getAttribute(listSpecificCountVar);
            listStartedKey = "_" + key + listStartedKey;
            current = queryTag;
        }
        listStartedKey += "_Key:" + getTagKey().hashCode();
        return queryExecuted + listStartedKey;
    }

    /**
     * Returns a variable name that is specific for the given query tag inside the current page
     * 
     * @return {@link #standardCountVar} appended {@link AnalysableTag#getPageTextInfo()}, i.e. the source file name,
     *         start &amp; end line and column of the tag
     */
    private String getListSpecificCountVar(QueryTag tag) {
        return standardCountVar + "_" + tag.getPageTextInfo();
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
        if (maxCountVar != null) {
            MakumbaJspFactory.getPageContext().setAttribute(maxCountVar, cnt);
        }
        MakumbaJspFactory.getPageContext().getRequest().setAttribute(standardMaxCountVar, cnt);
    }

    /**
     * Gets the number of iterations of this list
     */
    public int getNumberOfIterations() {
        return (Integer) pageContext.getRequest().getAttribute(standardMaxCountVar);
    }

    /**
     * Gets the number of the current iteration
     */
    public int getCurrentIterationNumber() {
        return (Integer) pageContext.getRequest().getAttribute(standardCountVar);
    }

    /**
     * Decides whether to do further iterations. Checks if we got to the end of the iterationGroup.
     * 
     * @return The tag return state as defined in the {@link javax.servlet.jsp.tagext.Tag} interface
     * @throws JspException
     */
    @Override
    public int doAfterBody() throws JspException {
        setRunningElementData(tagData);
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
                if (countVar != null) {
                    pageContext.setAttribute(countVar, cnt);
                }
                pageContext.getRequest().setAttribute(standardCountVar, cnt);
                pageContext.getRequest().setAttribute(getListSpecificCountVar(this), cnt);
                return EVAL_BODY_AGAIN;
            }
            return SKIP_BODY;
        } finally {
            setRunningElementData(null);
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
    @Override
    public int doAnalyzedEndTag(PageCache pageCache) throws JspException {
        pageContext.getRequest().setAttribute(standardLastCountVar,
            pageContext.getRequest().getAttribute(standardMaxCountVar));
        if (StringUtils.isNotBlank(id)) {
            pageContext.getRequest().setAttribute(standardLastCountVar + "_" + id,
                pageContext.getRequest().getAttribute(standardMaxCountVar));
        }

        pageContext.getRequest().setAttribute(standardCountVar, upperCount);
        pageContext.getRequest().setAttribute(standardMaxCountVar, upperMaxCount);
        pageContext.getRequest().setAttribute(standardMaxResultsVar, upperMaxResults);
        execution.endIterationGroup();

        if (getParentList(this) == null) {
            QueryExecution.endListGroup(pageContext);
            // also remove the attribute saying that this page was pre-started
            // this is important if a mak:list is repeated inside a loop for example
            // then the mak:list keys are equal, but we need to start a new iteration group
            String listKey = getListKey(pageContext);
            pageContext.removeAttribute(listKey);
        }

        // this code is here, as doAfterBody is not execute for mak:lists that don't have a body
        // to support nextCount(), remove the current list key from the stack of running lists
        Stack<MultipleKey> currentListKeyStack = getRunningQueryTagStack(pageContext);
        // and set it as the last finished list
        pageContext.getRequest().setAttribute(lastFinishedListKey, currentListKeyStack.pop());

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
        ComposedQuery ret = (ComposedQuery) pc.retrieve(MakumbaJspAnalyzer.QUERY, key);
        if (ret == null) {
            throw new MakumbaError("unknown query for key " + key);
        }
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
        ComposedQuery ret = (ComposedQuery) pc.retrieve(MakumbaJspAnalyzer.QUERY, key);
        if (ret != null) {
            return ret;
        }
        String ql = MakumbaJspAnalyzer.getQueryLanguage(pc);
        ret = parentKey == null ? new ComposedQuery(sections, ql) : new ComposedSubquery(sections, QueryTag.getQuery(
            pc, parentKey), ql);

        ret.init();
        pc.cache(MakumbaJspAnalyzer.QUERY, key, ret);

        return ret;
    }

    /**
     * Gives the value of the iteration in progress
     * 
     * @return The current count of iterations
     */
    public static int count() {
        Object countAttr = MakumbaJspFactory.getPageContext().getRequest().getAttribute(standardCountVar);
        if (countAttr == null) {
            // throw new ProgrammerError("mak:count() can only be used inside a <mak:list> tag");
            // FIXME: above error throwing led to some not-yet-known-anymore errors (manu might know more)
            // however, it is a good indication to the user -> should be tried again
            return -1;
        }
        return (Integer) countAttr;
    }

    /**
     * Gives the maximum number of iteration of the iterationGroup
     * 
     * @return The maximum number of iterations within the current iterationGroup
     */
    public static int maxCount() {
        Object maxAttr = MakumbaJspFactory.getPageContext().getRequest().getAttribute(standardMaxCountVar);
        if (maxAttr == null) {
            // throw new ProgrammerError("mak:maxCount() can only be used inside a <mak:list> tag");
            // FIXME: above error throwing led to some not-yet-known-anymore errors (manu might know more)
            // however, it is a good indication to the user -> should be tried again
            return -1;
        }
        return (Integer) maxAttr;
    }

    /**
     * Gives the maximum number of results returned as if the query would not contain any limit / offset. <br>
     * TODO: we need to pass quite some information in the request attributes, as this method has to be static. Not sure
     * what happens if there are more lists in the same page, if that would overlap or not.
     * 
     * @return The maximum number of results returned as if the query would not contain any limit / offset.
     */
    public static int maxResults() {
        ServletRequest servletRequest = MakumbaJspFactory.getPageContext().getRequest();
        Object totalAttr = servletRequest.getAttribute(standardMaxResultsVar);
        if (totalAttr == null) {
            // throw new ProgrammerError("mak:maxResults() can only be used inside a <mak:list> tag");
            // FIXME: above error throwing led to some not-yet-known-anymore errors (manu might know more)
            // however, it is a good indication to the user -> should be tried again
            return -1;
        }

        Integer total = ((Integer) totalAttr);
        if (total == Integer.MIN_VALUE) { // we still need to evaluate this total count
            PageContext pageContext = MakumbaJspFactory.getPageContext();
            MultipleKey keyMaxResults = (MultipleKey) servletRequest.getAttribute(standardMaxResultsKey);
            PageCache pageCache = AnalysableElement.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance());
            try {
                QueryExecution exec = QueryExecution.getFor(keyMaxResults, pageContext, null, null);
                int dataSize = exec.getIterationGroupData();
                MultipleKey tagKey = (MultipleKey) keyMaxResults.clone();
                tagKey.remove(standardMaxResultsVar);
                ComposedQuery query = (ComposedQuery) pageCache.retrieve(MakumbaJspAnalyzer.QUERY, tagKey);

                // if the query has a group-by section, then check the result size of the iteration data
                // this is because then, we have one row for each key we grouped by
                // FIXME: This fix is not ideal in performance; better would be to modify the query in case of a group,
                // in a way that the query would become a subquery to an outer query that would count the resutls
                if (StringUtils.isNotBlank(query.getGroupBySection())) {
                    total = dataSize;
                } else {
                    total = ((Integer) exec.currentListData().get("col1"));
                }
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
        Object attribute = MakumbaJspFactory.getPageContext().getRequest().getAttribute(standardLastCountVar);
        if (attribute == null) {
            return -1;
        }
        return ((Integer) attribute).intValue();
    }

    /**
     * Gives the total number of iterations of the iterationGroup identified by the given ID string
     * 
     * @param id
     *            the id of the mak:list or mak:object tag to relate to
     * @return The total number of iterations performed within the given iterationGroup
     */
    public static int lastCountById(String id) {
        Object attribute = MakumbaJspFactory.getPageContext().getRequest().getAttribute(standardLastCountVar + "_" + id);
        if (attribute == null) {
            return -1;
        }
        return ((Integer) attribute).intValue();
    }

    /**
     * Gives the total number of iterations of the next iterationGroup.<br/>
     * Invoking this method in the JSP page will cause this mak:list/object to pre-execute it's query, for the number of
     * iterations to be known before the tag will actually be executed.
     * 
     * @return The total number of iterations that will be performed within the next iterationGroup
     */
    public static int nextCount() throws LogicException, JspException {
        // This method requires quite some trickery:
        //
        // 1. as a static method, it requires the pageContext, which it will get from MakumbaJspFactory.getPageContext()
        // this is equivalent to the other mak:xxxCount() functions
        //
        // 2. the function needs to find the query tag it relates to. This is done as follows
        // a.) the stack of currently running QueryTags is retrieved from the pageContext
        // If it is not empty, the function starts from the top element on the stack, and finds the tag that comes next
        // in the page, by using the MakumbaJspAnalyzer.TAG_CACHE in PageCache
        // b.) if the stack was empty, then retrieve the list that was finished last
        // if that list is set, find the next tag as above
        // c.) if neither stack nor last finished list are set, just use the first tag in the page
        //
        // 3. the function needs to execute the query before the QueryTag actually starts, before doAnalyzedStartTag
        // it does so by calling initiateExecution(), which then will execute the query
        //
        // 4. finally, the number of iterations can be retrieved from the pageContext

        PageContext pageContext = MakumbaJspFactory.getPageContext();
        if (pageContext == null) {
            return -1;
        }

        // get all query tags
        Map<Object, Object> tagcache = AnalysableElement.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance()).retrieveCache(
            MakumbaJspAnalyzer.TAG_DATA_CACHE);
        ArrayList<TagData> queryTags = new ArrayList<TagData>();
        for (Object tagData : tagcache.values()) {
            if (((TagData) tagData).getTagObject() instanceof QueryTag) {
                queryTags.add((TagData) tagData);
            }
        }
        // find the correct query tag from the tagCache
        QueryTag nextQueryTag = null;

        Stack<MultipleKey> listKeyStack = getRunningQueryTagStack(pageContext);

        if (listKeyStack != null && listKeyStack.size() > 0) {
            // we have some running/open mak:lists => find the next list after
            final MultipleKey currentListKey = listKeyStack.peek();
            nextQueryTag = findNextTag(queryTags, currentListKey);
        } else {
            // check if we have already passed any list
            MultipleKey lastFinished = (MultipleKey) pageContext.getRequest().getAttribute(lastFinishedListKey);
            if (lastFinished != null) {
                // find the next one
                nextQueryTag = findNextTagAfterEnd(queryTags, lastFinished);
            } else {
                // if we haven't passed a mak:list/object yet, just take the first one
                nextQueryTag = (QueryTag) queryTags.get(0).getTagObject();
            }
        }

        // TODO: some error handling in case the nextQueryTag could not be found

        nextQueryTag.initiateQueryExecution(pageContext, true);
        return ((Integer) pageContext.getRequest().getAttribute(standardNextCountVar)).intValue();
    }

    /** Gets the stack of currently running (nested) Query Tags from the pageContext */
    public static Stack<MultipleKey> getRunningQueryTagStack(PageContext pageContext) {
        return (Stack<MultipleKey>) pageContext.getRequest().getAttribute(runningListKeyStack);
    }

    private static QueryTag findNextTag(List<TagData> queryTags, final MultipleKey currentListKey) {
        for (int i = 0; i < queryTags.size(); i++) {
            TagData queryTag = queryTags.get(i);
            if (queryTag.getTagObject().getTagKey().equals(currentListKey)) {
                return (QueryTag) queryTags.get(i + 1).getTagObject();
            }
        }
        return null;
    }

    private static QueryTag findNextTagAfterEnd(List<TagData> queryTags, final MultipleKey currentListKey) {
        // find the active open tag
        for (int i = 0; i < queryTags.size(); i++) {
            TagData queryTag = queryTags.get(i);
            if (queryTag.getTagObject().getTagKey().equals(currentListKey)) {
                TagData activeTag = queryTag;

                // now find the next query tag after this one is closed
                for (int j = i + 1; j < queryTags.size(); j++) {
                    if (queryTags.get(j).afterClosing(activeTag)) {
                        return (QueryTag) queryTags.get(j).getTagObject();
                    }
                }
            }
        }
        return null;
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
        countVar = maxCountVar = offset = limit = null;
        separator = "";
    }
}