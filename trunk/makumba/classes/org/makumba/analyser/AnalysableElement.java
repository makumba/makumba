package org.makumba.analyser;

import java.util.Comparator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.commons.lang.StringUtils;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.engine.TomcatJsp;
import org.makumba.analyser.interfaces.JspAnalyzer;
import org.makumba.commons.MakumbaJspAnalyzer;

/**
 * An analyzable element of the page, e.g. a tag or an EL expression.<br>
 * <br>
 * This class contains a number of utility methods that help retrieving the currently analyzed or running JSP element
 * (tag or EL expression). This is useful when providing accurate error messages to the user.
 * 
 * @author Manuel Gay
 * @author Rudolf Mayer
 * @version $Id: AnalysableElement.java,v 1.1 Jan 27, 2010 11:49:06 AM manu Exp $
 */
public abstract class AnalysableElement extends TagSupport {

    private static final long serialVersionUID = 1L;

    public static final String ANALYSIS_STATE = "org.makumba.analysisState";

    private static ThreadLocal<ElementData> analyzedElement = new ThreadLocal<ElementData>();

    private static ThreadLocal<ElementData> runningElement = new ThreadLocal<ElementData>();

    private static ThreadLocal<Stack<ElementData>> elementStack = new ThreadLocal<Stack<ElementData>>();

    private static transient ThreadLocal<JspParseData> jspParser = new ThreadLocal<JspParseData>();

    /**
     * Initializes the element data thread stack, and loads previous analysis state if there was any
     */
    public static void initializeThread(HttpSession session, String key) {
        getThreadElementStack().clear();
        runningElement.set(null);
        analyzedElement.set(null);
        jspParser.set(null);

        AnalysisState analysisState = (AnalysisState) session.getServletContext().getAttribute(key);

        if (analysisState != null) {
            analyzedElement.set(analysisState.analyzedElement);
            runningElement.set(analysisState.runningElement);
            elementStack.set(analysisState.elementStack);
            jspParser.set(analysisState.jspParser);
        }
    }

    /**
     * Clears remaining page parsing data, which might be useful for error handling, in order to display the exact line
     * at which the error occured. This method is only called when there's no error on the page, so that the parsing
     * data is kept as long as necessary.
     */
    public static void discardJSPParsingData() {
        if (jspParser.get() != null) {
            jspParser.get().discardParsingData();
        }
    }

    static class AnalysisState {

        private ElementData analyzedElement;

        private ElementData runningElement;

        private Stack<ElementData> elementStack;

        private JspParseData jspParser;

        public AnalysisState(ElementData analyzedElement, ElementData runningElement, Stack<ElementData> elementStack,
                JspParseData jspParser) {
            this.analyzedElement = analyzedElement;
            this.runningElement = runningElement;
            this.elementStack = elementStack;
            this.jspParser = jspParser;
        }

    }

    public static void keepAnalysisState(HttpSession session, String key) {
        AnalysisState analysisState = new AnalysisState(analyzedElement.get(), runningElement.get(),
                getThreadElementStack(), jspParser.get());
        // we save the state in the servlet context, thus we won't have problems if the application server crashes
        session.getServletContext().setAttribute(key, analysisState);
    }

    /**
     * Gets the data of the currently analyzed element for this thread
     * 
     * @return an {@link ElementData} describing the currently analyzed element
     */
    public static ElementData getAnalyzedElementData() {
        return analyzedElement.get();
    }

    /**
     * Sets the element data of the currently analyzed element for this thread
     * 
     * @param data
     *            the {@link ElementData} of the currently analyzed element
     */
    public static void setAnalyzedElementData(ElementData data) {
        analyzedElement.set(data);
    }

    /**
     * Gets the data of the currently running element for this thread
     * 
     * @return an {@link ElementData} describing the currently running element
     */
    public static ElementData getRunningElementData() {
        return runningElement.get();
    }

    /**
     * Sets the element data of the currently running element for this thread
     * 
     * @param data
     *            the {@link ElementData} of the currently running element
     */
    public static void setRunningElementData(ElementData data) {
        runningElement.set(data);
    }

    /**
     * Gets the stack of elements currently running in this thread
     * 
     * @return a Stack of {@link ElementData}
     */
    public static Stack<ElementData> getThreadElementStack() {
        Stack<ElementData> s = elementStack.get();
        if (s == null) {
            elementStack.set(s = new Stack<ElementData>());
        }
        return s;
    }

    /**
     * Gets the first tag data found in the stack
     * 
     * @return the {@link TagData} of the first enclosing tag found in the stack or null if none was found
     */
    static public TagData getCurrentBodyTagData() {
        if (getThreadElementStack().isEmpty()) {
            return null;
        }

        ListIterator<ElementData> l = getThreadElementStack().listIterator(getThreadElementStack().size());
        while (l.hasPrevious()) {
            ElementData d = l.previous();
            if (d instanceof TagData) {
                return (TagData) d;
            }
        }
        return null;
    }

    public abstract ElementData getElementData();

    public static PageCache getPageCache(HttpServletRequest request, String realPath, JspAnalyzer analyzer)
            throws MakumbaError {
        JspParseData parseData = JspParseData.getParseData(realPath, TomcatJsp.getJspURI(request), analyzer);
        jspParser.set(parseData);

        Object result = parseData.getAnalysisResult(null);

        if (result instanceof Throwable) {
            if (result instanceof MakumbaError) {
                throw (MakumbaError) result;
            }
            if (result instanceof RuntimeException) {
                throw (RuntimeException) result;
            } else {
                throw new RuntimeException((Throwable) result);
            }
        }
        return (PageCache) result;
    }

    /**
     * Static method to get the PageCache object for the current page. Constructs a new one if none found. We put this
     * as static, as we may have to export it to packages like org.makumba.controller.jsp
     * 
     * @param pageContext
     *            The PageContext object of the current page
     * @param analyzer
     *            the JSP analyzer
     */
    public static PageCache getPageCache(PageContext pageContext, JspAnalyzer analyzer) {
        PageCache pageCache = (PageCache) pageContext.getAttribute("makumba.parse.cache");

        // if there is no PageCache stored in the PageContext, we run the analysis and store the result in the
        // PageContext
        if (pageCache == null) {
            pageCache = AnalysableElement.getPageCache((HttpServletRequest) pageContext.getRequest(),
                pageContext.getServletConfig().getServletContext().getRealPath("/"), analyzer);
            pageContext.setAttribute("makumba.parse.cache", pageCache);
        }
        return pageCache;
    }

    protected static AnalysableTag getTagById(PageCache pageCache, String id, Class<? extends AnalysableTag> klass) {
        return getTagByAttribute(pageCache, "id", id, klass);
    }

    protected static AnalysableTag getTagByAttribute(PageCache pageCache, String attributeName, String attributeValue,
            Class<? extends AnalysableTag> klass) {
        Map<Object, Object> tagDataCache = pageCache.retrieveCache(MakumbaJspAnalyzer.TAG_DATA_CACHE);
        AnalysableTag tag = null;
        for (Object key : tagDataCache.keySet()) {
            TagData tagData = (TagData) tagDataCache.get(key);
            String attribute = tagData.attributes.get(attributeName);
            if (StringUtils.equals(attribute, attributeValue)) {
                tag = tagData.getTagObject();
                break;
            }
        }
        if (tag != null && klass != null && !klass.isAssignableFrom(tag.getClass())) {
            throw new ProgrammerError("Tag with attribute '" + attributeName + "' of value '" + attributeValue
                    + "' is of type " + tag.getClass().getSimpleName() + ", required " + klass.getSimpleName());
        }
        return tag;
    }

    public static AnalysableTag checkTagFound(PageCache pageCache, String attributeName, String attributeValue,
            Class<? extends AnalysableTag> klass) {
        AnalysableTag tag = getTagByAttribute(pageCache, attributeName, attributeValue, klass);
        if (tag == null) {
            throw new ProgrammerError("Could not find tag with attribute '" + attributeName + "' of value '"
                    + attributeValue + "' in the page.");
        }
        return tag;
    }

    protected static TagData getElementBefore(PageCache pageCache, ElementData elData,
            Class<? extends AnalysableTag> klass) {
        return getElementNextTo(pageCache, elData, true, klass);
    }

    protected static TagData getElementAfter(PageCache pageCache, ElementData elData,
            Class<? extends AnalysableTag> klass) {
        return getElementNextTo(pageCache, elData, false, klass);
    }

    private static TagData getElementNextTo(PageCache pageCache, ElementData elData, boolean before,
            Class<? extends AnalysableTag> klass) {
        Map<Object, Object> tagDataCache = pageCache.retrieveCache(MakumbaJspAnalyzer.TAG_DATA_CACHE);
        if (tagDataCache == null) {
            return null;
        }
        TagData tagData = null;
        for (Object key : tagDataCache.keySet()) {
            TagData td = (TagData) tagDataCache.get(key);
            boolean correctClass = klass == null || klass.isAssignableFrom(td.getTagObject().getClass());
            boolean direction = before && td.before(elData) || !before && td.after(elData);
            if (direction && correctClass) {
                if (tagData == null || before && td.after(tagData) || !before && td.before(tagData)) {
                    tagData = td;
                }
            }
        }
        return tagData;
    }

    public static final class FilePositionElementComparator implements Comparator<AnalysableElement> {
        public int compare(AnalysableElement o1, AnalysableElement o2) {
            int lineCompare = Double.compare(o1.getElementData().getStartLine(), o2.getElementData().getStartLine());
            if (lineCompare != 0) {
                return lineCompare;
            } else {
                return Double.compare(o1.getElementData().getStartColumn(), o2.getElementData().getStartColumn());
            }
        }
    }

}
