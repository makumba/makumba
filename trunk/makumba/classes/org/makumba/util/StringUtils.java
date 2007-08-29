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

    /** Checks whether a String is not null and has, after trimming, a length > 0. */
    public static boolean notEmpty(String s) {
        return s != null && s.length() > 0;
    }

    /** Checks whether a String is not null and has, after trimming, a length > 0. */
    public static boolean notEmpty(Object o) {
        return o != null && o.toString().length() > 0;
    }
}
