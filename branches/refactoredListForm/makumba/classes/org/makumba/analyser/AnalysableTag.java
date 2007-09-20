package org.makumba.analyser;

import java.util.Stack;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.MakumbaError;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.util.MultipleKey;

/**
 * Extend this class in order to get analysis support for your tag.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id: AnalysableTag.java,v 1.1 15.09.2007 00:46:05 Manuel Exp $
 */

public abstract class AnalysableTag extends TagSupport {
    
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
     * @see #doMakumbaStartTag(org.makumba.analyser.PageCache)
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
     * Gets the key that identifies this makumba tag
     * 
     * @return The MultipleKey used to identify the Makumba tag
     */
    public MultipleKey getTagKey() {
        return tagKey;
    }

}
