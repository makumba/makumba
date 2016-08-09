package org.makumba.util;

/**
 * This class provides utility methods for String manipulation.
 * 
 * @author rudi
 * @version $Id$
 */
public class StringUtils {

    /** Returns a string with lower-cased first letter. */
    public static String lowerCaseBeginning(String s) {
        return String.valueOf(s.charAt(0)).toLowerCase() + s.substring(1);
    }

    /** Returns a string with upper-cased first letter. */
    public static String upperCaseBeginning(String s) {
        return String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1);
    }

}
