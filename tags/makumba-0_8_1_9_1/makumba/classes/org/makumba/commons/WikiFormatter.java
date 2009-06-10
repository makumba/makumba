package org.makumba.commons;

/**
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public interface WikiFormatter {

    /**
     * Formats a String containing wiki code into HTML code.
     * 
     * @param s a string containing wiki code.
     * @return The generated HTML code.
     */
    public String wiki2html(String s);
    
}