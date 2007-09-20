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
//  $Id: MakumbaTag.java 1546 2007-09-14 20:34:45Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.list.tags;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.makumba.CompositeValidationException;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.ProgrammerError;
import org.makumba.analyser.AnalysableTag;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.list.ListFormDataProvider;
import org.makumba.util.MultipleKey;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer;
import org.makumba.view.jsptaglib.MakumbaJspException;

/**
 * This class provides utility methods for all makumba tags, such as
 * <ul>
 * <li>exception handling</li>
 * <li>storage of formatting parameters</li>
 * <li>database name setting/getting</li>
 * <li>cleanup</li>
 * </ul>
 * It extends {@link org.makumba.analyser.AnalysableTag} which enables JSP analysis support. FIXME form classes extend
 * this one because they need to compute the base pointers and dummy queries. this dependency needs to be removed.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id: MakumbaTag.java 1546 2007-09-14 20:34:45Z manuel_gay $
 */
public abstract class MakumbaTag extends AnalysableTag {

    /** Tag parameters */
    protected Hashtable<String, Object> params = new Hashtable<String, Object>(7); // holds certain 'normal' tag attributes

    protected Map<String, String> extraFormattingParams = new HashMap<String, String>(7); // container for html formatting params

    /** Extra html formatting, copied verbatim to the output */
    protected StringBuffer extraFormatting;

    /** Cache names, for PageCache of analysis * */
    public static final String TYPES = "org.makumba.types";

    public static final String VALUE_COMPUTERS = "org.makumba.valueComputers";

    public static final String QUERY_LANGUAGE = "org.makumba.queryLanguage";

    public static final String QUERY = "org.makumba.query";

    static final String DB_ATTR = "org.makumba.database";
    
    protected ListFormDataProvider fdp = new ListFormDataProvider();

    /**
     * Adds a key to the parentList, verifies if the tag has a parent.
     * 
     * @param o
     *            The key to be added
     */
    public void addToParentListKey(Object o) {
        AnalysableTag parentList = QueryTag.getParentList(this);
        if (parentList == null)
            throw new org.makumba.ProgrammerError(
                    "VALUE tags, INPUT, FORM or OPTION tags that compute a value should always be enclosed in a LIST or OBJECT tag");
        tagKey = new MultipleKey(parentList.getTagKey(), o);
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
     * makumba-specific startTag.
     * 
     * @param pageCache
     *            The page cache for the current page
     * @see #doStartTag()
     */
    public int doMakumbaStartTag(PageCache pageCache) throws LogicException, JspException {
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
        PageCache pageCache = null;
        // need to check if this is still needed, it was here only if the tag was root...
        if (pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) != null
                && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            setWasException();
        if (wasException()
                && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            return SKIP_PAGE;
        try {
            if (needPageCache())
                pageCache = MakumbaTag.getPageCache(pageContext);
            setTagKey(pageCache);
            if (pageCache != null) {
                tagData = (TagData) pageCache.retrieve(MakumbaJspAnalyzer.TAG_DATA_CACHE, tagKey);
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
    public int doMakumbaEndTag(PageCache pageCache) throws LogicException, JspException {
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
            PageCache pageCache = null;
            if (needPageCache())
                pageCache = MakumbaTag.getPageCache(pageContext);
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

    public Hashtable getParams() {
        return params;
    }

    /**
     * Sets the type identified by the key of a tag
     * 
     * @param key
     *            the key of the tag
     * @param value
     *            the field definition containing the type
     * @param t
     *            the MakumbaTag
     */
    protected void setType(PageCache pc, String key, FieldDefinition value) {
        Object[] val1 = (Object[]) pc.retrieve(MakumbaTag.TYPES, key);
        FieldDefinition fd = null;

        if (val1 != null)
            fd = (FieldDefinition) val1[0];
        // if we get nil here, we keep the previous, richer type information
        if (fd != null && value.getType().equals("nil"))
            return;

        AnalysableTag.analyzedTag.set(tagData);
        if (fd != null && !value.isAssignableFrom(fd))
            throw new ProgrammerError("Attribute type changing within the page: in tag\n"
                    + ((AnalysableTag) val1[1]).getTagText() + " attribute " + key + " was determined to have type " + fd
                    + " and the from this tag results the incompatible type " + value);
        AnalysableTag.analyzedTag.set(null);

        Object[] val2 = { value, this };
        pc.cache(MakumbaTag.TYPES, key, val2);
    }

    /**
     * Static method to get the PageCache object for the current page. Constructs a new one if none found. We put this
     * as static, as we may have to export it to packages like org.makumba.controller.jsp
     * 
     * @param pageContext
     *            The PageContext object of the current page
     */
    public static PageCache getPageCache(PageContext pageContext) {
        PageCache pageCache = (PageCache) pageContext.getAttribute("makumba.parse.cache");

        // if there is no PageCache stored in the PageContext, we run the analysis and store the result in the
        // PageContext
        if (pageCache == null) {
            Object result = JspParseData.getParseData(
                pageContext.getServletConfig().getServletContext().getRealPath("/"),
                TomcatJsp.getJspURI((HttpServletRequest) pageContext.getRequest()), MakumbaJspAnalyzer.getInstance()).getAnalysisResult(
                null);

            if ((result instanceof Throwable) && result.getClass().getName().startsWith("org.makumba")) {
                if (result instanceof MakumbaError)
                    throw (MakumbaError) result;
                throw (RuntimeException) result;
            }
            pageContext.setAttribute("makumba.parse.cache", pageCache = (PageCache) result);
        }
        return pageCache;
    }

}
