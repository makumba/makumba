package org.makumba.analyser;

import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.CompositeValidationException;
import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.list.tags.TomcatJsp;
import org.makumba.util.MultipleKey;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer;

/**
 * Extend this class in order to get analysis support for your tag.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id: AnalysableTag.java,v 1.1 15.09.2007 00:46:05 Manuel Exp $
 */

public abstract class AnalysableTag extends TagSupport {
    
    /** Cache names, for PageCache of analysis * */
    public static final String TYPES = "org.makumba.types";

    public static ThreadLocal<TagData> analyzedTag = new ThreadLocal<TagData>();

    public static ThreadLocal<TagData> runningTag = new ThreadLocal<TagData>();

    static ThreadLocal<Stack<TagData>> tagStack = new ThreadLocal<Stack<TagData>>();

    static public TagData getRunningTag() {
        return runningTag.get();
    }

    static public TagData getAnalyzedTag() {
        return analyzedTag.get();
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

    public static Stack<TagData> getThreadTagStack() {
        Stack<TagData> s = tagStack.get();
        if (s == null)
            tagStack.set(s = new Stack<TagData>());
        return s;
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

    /**
     * The TagData object holding the composite data collected by the analysis. It is set by the tag parser at analysis
     * time. It is set at runtime after the key is computed
     */
    public TagData tagData;

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
            return tagData.getStart().getFile().getCanonicalPath() + ":" + tagData.getStart().getLine() + ":"
                    + tagData.getStart().getColumn() + ":" + tagData.getEnd().getLine() + ":"
                    + tagData.getEnd().getColumn();
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
     * Handles exceptions, initialises state and calls {@link doAnalyzedStartTag}
     * 
     * @throws JspException
     */
    public int doStartTag() throws JspException {
        PageCache pageCache = null;
        // need to check if this is still needed, it was here only if the tag was root...
        if (pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) != null
                && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            getRequest().setAttribute("org.makumba.wasException", "yes");
        if ("yes".equals(getRequest().getAttribute("org.makumba.wasException"))
                && !(pageContext.getAttribute(PageContext.EXCEPTION, PageContext.REQUEST_SCOPE) instanceof CompositeValidationException))
            return SKIP_PAGE;
        try {
            if (needPageCache())
                pageCache = getPageCache(pageContext);
            setTagKey(pageCache);
            if (pageCache != null) {
                tagData = (TagData) pageCache.retrieve(TagData.TAG_DATA_CACHE, tagKey);
                runningTag.set(tagData);
            }
            int n = doAnalyzedStartTag(pageCache);
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

    // should be overriden
    protected void treatException(Throwable t) throws JspException {
        
        
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
                pageCache = getPageCache(pageContext);
            if (tagData != null) {
                runningTag.set(tagData);
                getThreadTagStack().pop();
            }
            return doAnalyzedEndTag(pageCache);
        } catch (Throwable t) {
            treatException(t);
            return SKIP_PAGE;
        } finally {
            runningTag.set(null);
            tagKey = null;
        }
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
    
    public boolean canHaveBody() {
        return false;
    }

}
