package org.makumba.analyser.interfaces;

/** The interface of a Java analyzer. */
public interface JavaAnalyzer {

    /**
     * the end of the page
     * 
     * @return the result of the analysis
     */
    Object endPage(Object status);

    /**
     * make a status holder, which is passed to all other methods
     * 
     * @param initStatus
     *            an initial status to be passed to the JavaAnalyzer. for example, the pageContext for an example-based analyzer
     */
    Object makeStatusHolder(Object initStatus);
}