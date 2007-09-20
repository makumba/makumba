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
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.list.engine.QueryExecution;
import org.makumba.list.engine.valuecomputer.ValueComputer;
import org.makumba.list.html.RecordViewer;
import org.makumba.util.MultipleKey;
import org.makumba.view.RecordFormatter;
import org.makumba.view.jsptaglib.BasicValueTag;
import org.makumba.view.jsptaglib.FormTagBase;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer;
import org.makumba.view.jsptaglib.MakumbaJspException;

/**
 * Display of OQL query results in nested loops. The Query FROM, WHERE, GROUPBY and ORDERBY are indicated in the head of
 * the tag. The query projections are indicated by Value tags in the body of the tag. The sub-tags will generate
 * subqueries of their enclosing tag queries (i.e. their WHERE, GROUPBY and ORDERBY are concatenated). Attributes of the
 * environment can be passed as $attrName to the query
 * 
 * @author Cristian Bogdan
 * @version $Id$
 * 
 */
public class QueryTag extends MakumbaTag implements IterationTag {

    private static final long serialVersionUID = 1L;

    String[] queryProps = new String[5];

    String separator = "";

    String countVar;

    String maxCountVar;

    String offset, limit;

    static String standardCountVar = "org_makumba_view_jsptaglib_countVar";

    static String standardMaxCountVar = "org_makumba_view_jsptaglib_maxCountVar";

    static String standardLastCountVar = "org_makumba_view_jsptaglib_lastCountVar";

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

    protected void onlyOuterListArgument(String s) throws JspException {
        QueryTag t = (QueryTag) findAncestorWithClass(this, QueryTag.class);
        while (t != null && t instanceof ObjectTag)
            t = (QueryTag) findAncestorWithClass(t, QueryTag.class);
        if (t instanceof QueryTag)
            treatException(new MakumbaJspException(this, "the " + s
                    + " parameter can only be set for the outermost mak:list tag"));
    }

    protected void onlyInt(String s, String value) throws JspException {
        value = value.trim();
        if (value.startsWith("$"))
            return;
        try {
            Integer.parseInt(value);
        } catch (NumberFormatException nfe) {
            treatException(new MakumbaJspException(this, "the " + s + " parameter can only be an $attribute or an int"));

        }
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

    ValueComputer choiceComputer;

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
    public int doMakumbaStartTag(PageCache pageCache) throws LogicException, JspException {
        servletRequestThreadLocal.set(pageContext.getRequest());
        if (getParentList(this) == null)
            QueryExecution.startListGroup(pageContext);
        else {
            upperCount = pageContext.getRequest().getAttribute(standardCountVar);
            upperMaxCount = pageContext.getRequest().getAttribute(standardMaxCountVar);
        }

        execution = QueryExecution.getFor(tagKey, pageContext, offset, limit);

        int n = execution.onParentIteration();

        setNumberOfIterations(n);

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
    public int doMakumbaEndTag(PageCache pageCache) throws JspException {
        pageContext.getRequest().setAttribute(standardLastCountVar,
                pageContext.getRequest().getAttribute(standardMaxCountVar));

        pageContext.getRequest().setAttribute(standardCountVar, upperCount);
        pageContext.getRequest().setAttribute(standardMaxCountVar, upperMaxCount);
        execution.endIterationGroup();

        if (getParentList(this) == null)
            QueryExecution.endListGroup(pageContext);

        execution = null;
        queryProps[0] = queryProps[1] = queryProps[2] = queryProps[3] = null;
        countVar = maxCountVar = null;
        separator = "";
        return EVAL_PAGE;
    }

    /**
     * Finds the parentList of a list
     * 
     * @param tag TODO
     * @return The parent QueryTag of the Tag
     */
    public static AnalysableTag getParentList(AnalysableTag tag) {
        return (AnalysableTag) findAncestorWithClass(tag, QueryTag.class);
    }

    public static final String[] dummyQuerySections = { null, null, null, null, null };
    
    /**
     * Finds the key of the parentList of the Tag
     * 
     * @param tag TODO
     * @param pageCache TODO
     * @return The MultipleKey identifying the parentList
     */
    public static MultipleKey getParentListKey(AnalysableTag tag, PageCache pageCache) {
        
        if(tag instanceof BasicValueTag) {
            BasicValueTag dirtyHack = (BasicValueTag)tag;
            MultipleKey k = getParentListKeySimple(tag);
            if (k != null)
                return k;
            if (dirtyHack.isNull())
                return null;

            /* we don't have a query around us, so we must make a dummy query for computing the value via the database */
            QueryTag.cacheQuery(pageCache, dirtyHack.getForm().getTagKey(), dummyQuerySections, null);
            return dirtyHack.getForm().getTagKey();
        } else {
            return getParentListKeySimple(tag);
        }
        
    }
    
    private static MultipleKey getParentListKeySimple(AnalysableTag tag) {
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
        ComposedQuery ret = (ComposedQuery) pc.retrieve(MakumbaTag.QUERY, key);
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
        ComposedQuery ret = (ComposedQuery) pc.retrieve(MakumbaTag.QUERY, key);
        if (ret != null)
            return ret;
        boolean hql=  pc.retrieve(MakumbaTag.QUERY_LANGUAGE, MakumbaTag.QUERY_LANGUAGE).equals("hql");
        ret = parentKey == null ? new ComposedQuery(sections, hql) : new ComposedSubquery(sections,
                QueryTag.getQuery(pc, parentKey), hql);
    
        ret.init();
        pc.cache(MakumbaTag.QUERY, key, ret);
        return ret;
    }

    /**
     * Gives the value of the iteration in progress
     * 
     * @return The current count of iterations
     */
    public static int count() {
        Object countAttr = servletRequestThreadLocal.get().getAttribute(standardCountVar);
        if(countAttr == null) {
            throw new ProgrammerError("mak:count() can only be used inside a <mak:list> tag");
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
        if(maxAttr == null) {
            throw new ProgrammerError("mak:maxCount() can only be used inside a <mak:list> tag");
        }
        return ((Integer) maxAttr).intValue();
    }

    /**
     * Gives the total number of iterations of the previous iterationGroup
     * 
     * @return The total number of iterations performed within the previous iterationGroup
     */
    public static int lastCount() {
        return ((Integer) servletRequestThreadLocal.get().getAttribute(standardLastCountVar))
                .intValue();
    }
}
