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

package org.makumba.analyser;

import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringUtils;
import org.makumba.CompositeValidationException;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.commons.RuntimeWrappedException;

/**
 * Extend this class in order to get analysis support for your tag.<br/>
 * Do make sure that:
 * <ul>
 * <li>you make setters and getters for all the tag attributes</li>
 * <li>if attributes should have default values, these should be set in the initialiseState(), not directly in the class
 * </li>
 * <li>you implement the setTagKey method so as to be able to retrieve {@link TagData} from the cache if you need it</li>
 * <li>you use the doAnalyzedStartTag() and doAnalyzedEndTag() instead of doStartTag() and doEndTag() methods</li>
 * <li>you cleanup all resources by overriding doAnalyzedCleanup(), without forgetting to call the super() method</li>
 * <li>you correctly describe the behavior of the tag by overriding the canHaveBody() and allowsIdenticalKey()</li>
 * <li>you check for the validity of attributes by overriding the registerPossibleAttributeValues() method, and
 * registering possile attribute values using the registerAttributeValues(String attributeName, String... values)
 * method.</li>
 * </ul>
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id$
 */

public abstract class AnalysableTag extends AnalysableElement {
    private static final long serialVersionUID = 1L;

    /** Commonly used Attribute values. */
    public static final String[] ATTRIBUTE_VALUES_TRUE_FALSE = { "true", "false" };

    /** Cache names, for PageCache of analysis */
    public static final String TYPES = "org.makumba.types";

    private HashMap<String, String[]> attributeValues = new LinkedHashMap<String, String[]>();

    /**
     * The TagData object holding the composite data collected by the analysis. It is set by the tag parser at analysis
     * time. It is set at runtime after the key is computed
     */
    public TagData tagData;

    @Override
    public ElementData getElementData() {
        return this.tagData;
    }

    public void setTagDataAtAnalysis(TagData tagData) {
        this.tagData = tagData;
    }

    /** A tag key, used to find cached resources. Computed by some tags, both at analysis and at runtime */
    public MultipleKey tagKey;

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

    public PageContext getPageContext() {
        return pageContext;
    }

    /**
     * Sets tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before
     * doMakumbaStartTag()
     * 
     * @param pageCache
     *            The page cache for the current page
     * @see #doAnalyzedStartTag(org.makumba.analyser.PageCache)
     * @see #doStartAnalyze(org.makumba.analyser.PageCache)
     */
    public void setTagKey(PageCache pageCache) {
    }

    /**
     * Starts the analysis of the tag, without knowing what tags follow it in the page. Typically this method will
     * allocate initial data structures, that are then completed at doEndAnalyze()
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    public void doStartAnalyze(PageCache pageCache) {
    }

    /**
     * End the analysis of the tag, after all tags in the page were visited.
     * 
     * @param pageCache
     *            The page cache for the current page
     */
    public void doEndAnalyze(PageCache pageCache) {
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
            return tagData.getSourceSyntaxPoints().getFile().getCanonicalPath() + ":" + tagData.getStartLine() + ":"
                    + tagData.getStartColumn() + ":" + tagData.getEndLine() + ":" + tagData.getEndColumn();
        } catch (java.io.IOException e) {
            throw new MakumbaError(e.toString());
        }
    }

    /**
     * makumba-specific startTag.
     * 
     * @param pageCache
     *            The page cache for the current page
     * @see #doStartTag()
     */
    public int doAnalyzedStartTag(PageCache pageCache) throws LogicException, JspException {
        return SKIP_BODY;
    }

    /**
     * makumba-specific endTag
     * 
     * @param pageCache
     *            The page cache for the current page
     * @see #doEndTag()
     */
    public int doAnalyzedEndTag(PageCache pageCache) throws LogicException, JspException {
        return EVAL_PAGE;
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
     * Handles exceptions, initialises state and calls {@link doAnalyzedStartTag} FIXME some of the exception handling
     * should not be here
     * 
     * @throws JspException
     */
    public int doStartTag() throws JspException {
        PageCache pageCache = null;
        // FIXME: need to check if this is still needed, it was here only if the tag was root...
        if (pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) != null
                && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            getRequest().setAttribute("org.makumba.wasException", "yes");
        if ("yes".equals(getRequest().getAttribute("org.makumba.wasException"))
                && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            return SKIP_PAGE;
        if (needPageCache())
            pageCache = AnalysableElement.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance());
        setTagKey(pageCache);
        if (pageCache != null) {
            tagData = (TagData) pageCache.retrieve(MakumbaJspAnalyzer.TAG_DATA_CACHE, tagKey);
            setRunningElementData(tagData);
        }
        int n;
        try {
            n = doAnalyzedStartTag(pageCache);
        } catch (LogicException e) {
            throw new RuntimeWrappedException(e);
        }
        if (tagData != null) {
            setRunningElementData(null);
            getThreadElementStack().push(tagData);
        }
        return n;
    }

    /**
     * Handles exceptions and calls doMakumbaEndTag()
     * 
     * @throws JspException
     */
    public int doEndTag() throws JspException {
        try {
            if ("yes".equals(getRequest().getAttribute("org.makumba.wasException")))
                return SKIP_PAGE;
            PageCache pageCache = null;
            if (needPageCache())
                pageCache = AnalysableElement.getPageCache(pageContext, MakumbaJspAnalyzer.getInstance());
            if (tagData != null) {
                setRunningElementData(tagData);
                getThreadElementStack().pop();
            }
            return doAnalyzedEndTag(pageCache);
        } catch (LogicException e) {
            throw new RuntimeWrappedException(e);
        } finally {
            doAnalyzedCleanup();
            attributeValues.clear();
        }
    }

    /**
     * Called by doEndTag in its finally block. Use it to clean references that will not be used next time the servlet
     * container uses the tag object.
     */
    protected void doAnalyzedCleanup() {
        setRunningElementData(null);
        tagKey = null;
        tagData = null;
    }

    /**
     * Gets the key that identifies this makumba tag
     * 
     * @return The MultipleKey used to identify the Makumba tag
     */
    public MultipleKey getTagKey() {
        return tagKey;
    }

    private HttpServletRequest getRequest() {
        return (HttpServletRequest) pageContext.getRequest();
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
     * Determines whether this tag can have a body or not.
     * 
     * @return <code>true</code> if the tag is allowed to have a body, <code>false</code> otherwise
     */
    public boolean canHaveBody() {
        return false;
    }

    /** Checks whether the given value is of type integer, and throws a descriptive programmer error otherwise */
    protected void onlyInt(String attributeName, String value) {
        value = value.trim();
        if (value.startsWith("$")) {
            return;
        } else if (!StringUtils.isNumeric(value)) {
            throw new ProgrammerError("The attribute '" + attributeName + "' can only be an $attribute or an int");
        }
    }

    /**
     * Checks whether the given value is not blank, i.e. not null, and not an empty string or just contains whitespace,
     * and throws a descriptive programmer error otherwise
     */
    protected void notEmpty(String attributeName, String value) {
        if (StringUtils.isBlank(value)) {
            throw new ProgrammerError("The attribute '" + attributeName + "' can not be empty");
        }
    }

    /**
     * Override this in order to register possible attribute values using
     * {@link #registerAttributeValues(String, String...)}. The registered attributes are checked before
     * {@link #doStartAnalyze(PageCache)} and throw a {@link ProgrammerError} is thrown if the provided value is not
     * allowed.
     */
    protected void registerPossibleAttributeValues() {
    }

    /**
     * Registers one attribute and several possible values
     * 
     * @param attributeName
     *            the name of the attribute
     * @param values
     *            a number of possible values the attribute can take
     */
    protected final void registerAttributeValues(String attributeName, String... values) {
        attributeValues.put(attributeName, values);
    }

    /**
     * Checks if the provided attribute values are correct. Called before {@link #doStartAnalyze(PageCache)}
     */
    public void checkAttributeValues() {
        registerPossibleAttributeValues();

        for (String attributeName : attributeValues.keySet()) {
            String value = tagData.attributes.get(attributeName);
            if (value != null) {
                if (!org.makumba.commons.StringUtils.equalsAny(value, attributeValues.get(attributeName))) {
                    throw new ProgrammerError("Invalid value for attribute '" + attributeName + "': '" + value
                            + "'. Allowed values are "
                            + org.makumba.commons.StringUtils.toString(attributeValues.get(attributeName)));
                }
            }
        }
    }
}