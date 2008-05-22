package org.makumba.analyser.interfaces;

import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;

/** The interface of a JSP analyzer. */
public interface JspAnalyzer {
    /**
     * Makes a status holder, which is passed to all other methods
     * 
     * @param initStatus
     *            an initial status to be passed to the JspAnalyzer. for example, the pageContext for an
     *            example-based analyzer
     */
    Object makeStatusHolder(Object initStatus);

    /**
     * Start of a body tag
     * 
     * @param td
     *            the TagData holding the parsed data
     * @param status
     *            the status of the parsing
     * @see #endTag(TagData, Object)
     */
    void startTag(TagData td, Object status);

    /**
     * End of a body tag, like </...>
     * 
     * @param td
     *            the TagData holdking the parsed data
     * @param status
     *            the status of the parsing
     */
    void endTag(TagData td, Object status);

    /**
     * A simple tag, like <... />
     * 
     * @param td
     *            the TagData holdking the parsed data
     * @param status
     *            the status of the parsing
     */
    void simpleTag(TagData td, Object status);

    /**
     * A system tag, like <%@ ...%>
     * 
     * @param td
     *            the TagData holdking the parsed data
     * @param status
     *            the status of the parsing
     */
    void systemTag(TagData td, Object status);

    /**
     * The end of the page
     * 
     * @param status
     *            the status of the parsing
     * @return The result of the analysis
     */
    Object endPage(Object status);
}