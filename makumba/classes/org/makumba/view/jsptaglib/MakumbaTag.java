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

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.CompositeValidationException;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.analyser.JspParseData;
import org.makumba.analyser.TagData;
import org.makumba.util.MultipleKey;

/**
 * This class provides utility methods for all makumba tags, such as
 * <ul>
 * <li>exception handling</li>
 * <li>page cache retrieval</li>
 * <li>storage of formatting parameters</li>
 * <li>database name setting/getting</li>
 * <li>cleanup</li>
 * <li>links to the JspParseData$TagData, representing the tag as parsed</li>
 * </ul>
 * 
 * TODO this should be refactored so that the analysis-specific things get refactored to a superclass which
 * would be part of the analyser
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public abstract class MakumbaTag extends TagSupport {
    static ThreadLocal analyzedTag = new ThreadLocal();

    static ThreadLocal runningTag = new ThreadLocal();

    static ThreadLocal tagStack = new ThreadLocal();

    static public TagData getRunningTag() {
        return (TagData) runningTag.get();
    }

    static public TagData getAnalyzedTag() {
        return (TagData) analyzedTag.get();
    }

    static public TagData getCurrentBodyTag() {
        if (getThreadTagStack().isEmpty())
            return null;
        return (TagData) getThreadTagStack().peek();
    }

    static public void initializeThread() {
        getThreadTagStack().clear();
        runningTag.set(null);
        analyzedTag.set(null);
    }

    static Stack getThreadTagStack() {
        Stack s = (Stack) tagStack.get();
        if (s == null)
            tagStack.set(s = new Stack());
        return s;
    }

    /**
     * The TagData object holding the composite data collected by the analysis. It is set by the tag parser at analysis
     * time. It is set at runtime after the key is computed
     */
    protected TagData tagData;

    void setTagDataAtAnalysis(TagData tagData) {
        this.tagData = tagData;
    }

    /** A tag key, used to find cached resources. Computed by some tags, both at analysis and at runtime */
    protected MultipleKey tagKey;

    /** Tag parameters */
    protected Hashtable params = new Hashtable(7); // holds certain 'normal' tag attributes

    protected Map extraFormattingParams = new HashMap(7); // container for html formatting params

    /** Extra html formatting, copied verbatim to the output */
    protected StringBuffer extraFormatting;

    static final String DB_ATTR = "org.makumba.database";

    /**
     * Dumps the tag line during analysis
     * 
     * @param sb
     *            StringBuffer holding the tag text
     */
    public void addTagText(StringBuffer sb) {
        JspParseData.tagDataLine(tagData, sb);
    }

    /**
     * Returns the declaration text of the tag parsed at analysis time
     * 
     * @return A string containing the declaration of the tag
     */
    public String getTagText() {
        StringBuffer sb = new StringBuffer();
        addTagText(sb);
        return sb.toString();
    }

    PageContext getPageContext() {
        return pageContext;
    }

    /**
     * Static method to get the PageCache object for the current page. Constructs a new one if none found. We put this
     * as static, as we may have to export it to pacakges like org.makumba.controller.jsp
     * 
     * @param pageContext
     *            The PageContext object of the current page
     */
    public static MakumbaJspAnalyzer.PageCache getPageCache(PageContext pageContext) {
        MakumbaJspAnalyzer.PageCache pageCache = (MakumbaJspAnalyzer.PageCache) pageContext
                .getAttribute("makumba.parse.cache");

        // if there is no PageCache stored in the PageContext, we run the analysis and store the result in the
        // PageContext
        if (pageCache == null) {
            Object result = JspParseData.getParseData(
                    pageContext.getServletConfig().getServletContext().getRealPath("/"),
                    TomcatJsp.getJspURI((HttpServletRequest) pageContext.getRequest()),
                    MakumbaJspAnalyzer.getInstance()).getAnalysisResult(null);

            if ((result instanceof Throwable) && result.getClass().getName().startsWith("org.makumba")) {
                if (result instanceof MakumbaError)
                    throw (MakumbaError) result;
                throw (RuntimeException) result;
            }
            pageContext.setAttribute("makumba.parse.cache", pageCache = (MakumbaJspAnalyzer.PageCache) result);
        }
        return pageCache;
    }

    /**
     * Finds the parentList of a list
     * 
     * @return The parent QueryTag of the Tag
     */
    public QueryTag getParentList() {
        return (QueryTag) findAncestorWithClass(this, QueryTag.class);
    }

    /**
     * Finds the key of the parentList of the Tag
     * 
     * @param pageCache
     *            The page cache for the current page
     * @return The MultipleKey identifying the parentList
     */
    public MultipleKey getParentListKey(MakumbaJspAnalyzer.PageCache pageCache) {
        QueryTag parentList = getParentList();
        return parentList == null ? null : parentList.getTagKey();
    }

    /**
     * Adds a key to the parentList, verifies if the tag has a parent.
     * 
     * @param o
     *            The key to be added
     */
    public void addToParentListKey(Object o) {
        QueryTag parentList = getParentList();
        if (parentList == null)
            throw new org.makumba.ProgrammerError(
                    "VALUE tags, INPUT, FORM or OPTION tags that compute a value should always be enclosed in a LIST or OBJECT tag");
        tagKey = new MultipleKey(parentList.getTagKey(), o);
    }

    /**
     * Sets tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before
     * doMakumbaStartTag()
     * 
     * @param pageCache
     *            The page cache for the current page
     * @see #doMakumbaStartTag(org.makumba.view.jsptaglib.MakumbaJspAnalyzer.PageCache)
     * @see #doStartAnalyze(org.makumba.view.jsptaglib.MakumbaJspAnalyzer.PageCache)
     */
    public void setTagKey(MakumbaJspAnalyzer.PageCache pageCache) {
    }

    /**
     * Gets the key that identifies this makumba tag
     * 
     * @return The MultipleKey used to identify the Makumba tag
     */
    public MultipleKey getTagKey() {
        return tagKey;
    }

    /**
     * Determines whether the tag can have the same key as others in the page
     * 
     * @return <code>true</code> if the tag is allowed to have the same key as others in the page, <code>false</code>
     *         otherwise
     */
    public boolean allowsIdenticalKey() {
        return true;
    }

    /**
     * Starts the analysis of the tag, without knowing what tags follow it in the page. Typically this method will
     * allocate initial data structures, that are then completed at doEndAnalyze()
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    public void doStartAnalyze(MakumbaJspAnalyzer.PageCache pageCache) {
    }

    /**
     * End the analysis of the tag, after all tags in the page were visited.
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    public void doEndAnalyze(MakumbaJspAnalyzer.PageCache pageCache) {
    }

    /**
     * makumba-specific startTag.
     * 
     * @param pageCache
     *            The page cache for the current page
     * @see #doStartTag()
     */
    public int doMakumbaStartTag(MakumbaJspAnalyzer.PageCache pageCache) throws LogicException, JspException {
        return SKIP_BODY;
    }

    /**
     * Checks if the tag needs the page cache
     * 
     * @return <code>true</code> if page cache is needed, <code>false</code> otherwise
     */
    protected boolean needPageCache() {
        return true;
    }

    /**
     * Handles exceptions, initialises state and calls doMakumbaStartTag()
     * 
     * @throws JspException
     */
    public int doStartTag() throws JspException {
        MakumbaJspAnalyzer.PageCache pageCache = null;
        // need to check if this is still needed, it was here only if the tag was root...
        if (pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) != null && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            setWasException();
        if (wasException() && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            return SKIP_PAGE;
        try {
            if (needPageCache())
                pageCache = getPageCache(pageContext);
            setTagKey(pageCache);
            if (pageCache != null) {
                tagData = (TagData) pageCache.tagData.get(tagKey);
                runningTag.set(tagData);
            }
            initialiseState();
            int n = doMakumbaStartTag(pageCache);
            if (tagData != null) {
                runningTag.set(null);
                getThreadTagStack().push(tagData);
            }
            return n;
        } catch (Throwable t) {
            treatException(t);
            return SKIP_PAGE;
        }
    }

    /**
     * Resets and initialises the tag's state, to work in a tag pool. See bug 583. If this method is overriden in a
     * child class, the child's method must call super.initaliseState().
     */
    public void initialiseState() {
        extraFormatting = new StringBuffer();

        for (Iterator it = extraFormattingParams.entrySet().iterator(); it.hasNext();) {
            Map.Entry me = (Map.Entry) it.next();
            extraFormatting.append(" ").append(me.getKey()).append("=\"").append(me.getValue()).append("\" ");
        }
    }

    /**
     * makumba-specific endTag
     * 
     * @param pageCache
     *            The page cache for the current page
     * @see #doEndTag()
     */
    public int doMakumbaEndTag(MakumbaJspAnalyzer.PageCache pageCache) throws LogicException, JspException {
        return EVAL_PAGE;
    }

    /**
     * Handles exceptions and calls doMakumbaEndTag()
     * 
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        try {
            if (wasException())
                return SKIP_PAGE;
            MakumbaJspAnalyzer.PageCache pageCache = null;
            if (needPageCache())
                pageCache = getPageCache(pageContext);
            if (tagData != null) {
                runningTag.set(tagData);
                getThreadTagStack().pop();
            }
            return doMakumbaEndTag(pageCache);
        } catch (Throwable t) {
            treatException(t);
            return SKIP_PAGE;
        } finally {
            runningTag.set(null);
            tagKey = null;
            params.clear();
            extraFormattingParams.clear();
            extraFormatting = null;
            if (findAncestorWithClass(this, MakumbaTag.class) == null)
                pageContext.removeAttribute(DB_ATTR);
        }
    }

    /**
     * Obtains the makumba database; this can be more complex (accept arguments, etc)
     * 
     * @return A String containing the name of the database
     */
    public String getDatabaseName() {
        return getDatabaseName(pageContext);
    }

    /**
     * Obtains the makumba database; this can be more complex (accept arguments, etc)
     * 
     * @param pc
     *            The PageContext object of this page
     * @return A String containing the name of the database
     */
    public static String getDatabaseName(PageContext pc) {
        String db = (String) pc.getAttribute(DB_ATTR);
        if (db == null)
            return MakumbaSystem.getDefaultDatabaseName();
        return db;
    }

    /**
     * Checks if this is not the root tag and throws an exception containing the name of the argument not allowed in
     * non-root tags.
     * 
     * @param s
     *            The name of the argument
     * @throws JspException
     */
    protected void onlyRootArgument(String s) throws JspException {
        if (findAncestorWithClass(this, MakumbaTag.class) != null)
            treatException(new MakumbaJspException(this, "the " + s
                    + " argument cannot be set for non-root makumba tags"));
    }

    /**
     * Sets the database argument
     * 
     * @param db
     *            The database argument
     * @throws JspException
     */
    public void setDb(String db) throws JspException {
        onlyRootArgument("db");
        if (pageContext != null)
            pageContext.setAttribute(DB_ATTR, db);
    }

    // --------- exception handling
    public boolean wasException() {
        return org.makumba.controller.http.ControllerFilter.wasException((HttpServletRequest) pageContext.getRequest());
    }

    public void setWasException() {
        org.makumba.controller.http.ControllerFilter.setWasException((HttpServletRequest) pageContext.getRequest());
    }

    protected void treatException(Throwable t) throws JspException {
        if (pageContext == null)
            throw (JspException) t;

        org.makumba.controller.http.ControllerFilter.treatException(t, (HttpServletRequest) pageContext.getRequest(),
                (HttpServletResponse) pageContext.getResponse());
    }

    // --------- html formatting, copied verbatim to the output
    public void setStyleId(String s) {
        extraFormattingParams.put("id", s);
    }

    public void setStyleClass(String s) {
        extraFormattingParams.put("class", s);
    }

    public void setStyle(String s) {
        extraFormattingParams.put("style", s);
    }

    public void setTitle(String s) {
        extraFormattingParams.put("title", s);
    }

    public void setOnClick(String s) {
        extraFormattingParams.put("onClick", s);
    }

    public void setOnDblClick(String s) {
        extraFormattingParams.put("onDblClick", s);
    }

    public void setOnKeyDown(String s) {
        extraFormattingParams.put("onKeyDown", s);
    }

    public void setOnKeyUp(String s) {
        extraFormattingParams.put("onKeyUp", s);
    }

    public void setOnKeyPress(String s) {
        extraFormattingParams.put("onKeyPress", s);
    }

    public void setOnMouseDown(String s) {
        extraFormattingParams.put("onMouseDown", s);
    }

    public void setOnMouseUp(String s) {
        extraFormattingParams.put("onMouseUp", s);
    }

    public void setOnMouseMove(String s) {
        extraFormattingParams.put("onMouseMove", s);
    }

    public void setOnMouseOut(String s) {
        extraFormattingParams.put("onMouseOut", s);
    }

    public void setOnMouseOver(String s) {
        extraFormattingParams.put("onMouseOver", s);
    }

    // --------- formatting properties, determine formatter behavior
    public void setUrlEncode(String s) {
        params.put("urlEncode", s);
    }

    public void setHtml(String s) {
        params.put("html", s);
    }

    public void setFormat(String s) {
        params.put("format", s);
    }

    public void setType(String s) {
        params.put("type", s);
    }

    public void setSize(String s) {
        params.put("size", s);
    }

    public void setMaxlength(String s) {
        params.put("maxlength", s);
    }

    public void setMaxLength(String s) {
        params.put("maxLength", s);
    }

    public void setEllipsis(String s) {
        params.put("ellipsis", s);
    }

    public void setEllipsisLength(String s) {
        params.put("ellipsisLength", s);
    }

    public void setAddTitle(String s) {
        params.put("addTitle", s);
    }

    public void setRows(String s) {
        params.put("rows", s);
    }

    public void setCols(String s) {
        params.put("cols", s);
    }

    public void setLineSeparator(String s) {
        params.put("lineSeparator", s);
    }

    public void setLongLineLength(String s) {
        params.put("longLineLength", s);
    }

    public void setDefault(String s) {
        params.put("default", s);
    }

    public void setEmpty(String s) {
        params.put("empty", s);
    }

    public void setLabelSeparator(String s) {
        params.put("labelSeparator", s);
    }

    public void setElementSeparator(String s) {
        params.put("elementSeparator", s);
    }

    public String toString() {
        return getClass().getName() + " " + params + "\n" + getPageTextInfo();
    }

    /**
     * Prints the page data collected during analysis in readable format
     * 
     * @return A String containing information about the page
     */
    public String getPageTextInfo() {
        if (tagData == null)
            return "";
        try {
            return tagData.getStart().getFile().getCanonicalPath() + ":" + tagData.getStart().getLine() + ":"
                    + tagData.getStart().getColumn() + ":" + tagData.getEnd().getLine() + ":"
                    + tagData.getEnd().getColumn();
        } catch (java.io.IOException e) {
            throw new MakumbaError(e.toString());
        }
    }
}
