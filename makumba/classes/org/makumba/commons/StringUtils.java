package org.makumba.commons;

import java.util.Collection;
import java.util.Enumeration;

import org.apache.commons.collections.EnumerationUtils;

/**
 * This class provides utility methods for String manipulation.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class StringUtils {

    private static final String DEFAULT_DELIMETER = ", ";

    /** Returns a string with lower-cased first letter. */
    public static String lowerCaseBeginning(String s) {
        return String.valueOf(s.charAt(0)).toLowerCase() + s.substring(1);
    }

    /** Returns a string with upper-cased first letter. */
    public static String upperCaseBeginning(String s) {
        return String.valueOf(s.charAt(0)).toUpperCase() + s.substring(1);
    }

    /** Checks whether a String is not null and has, after trimming, a length > 0. */
    public static boolean notEmpty(Object o) {
        return o != null && o instanceof String && o.toString().length() > 0;
    }

    public static boolean notEmpty(String[] o) {
        for (int i = 0; i < o.length; i++) {
            if(org.apache.commons.lang.StringUtils.isNotEmpty(o[i])) {
                return true;
            }
        } 
        return false;
    }
    
    /** Checks whether an Object is null or has, after trimming, a length == 0. */
    public static boolean isEmpty(Object o) {
        return o == null || (o instanceof String && org.apache.commons.lang.StringUtils.isEmpty((String) o));
    }

    /**
     * Converts an array to a String represenation, using the toString() method of each array element.
     */
    public static String toString(Object[] array) {
        return toString(array, true);
    }

    public static String toString(Object[] array, String delimeter) {
        return toString(array, true, delimeter);
    }

    public static String toString(Object[] array, boolean frame) {
        return toString(array, frame, DEFAULT_DELIMETER);
    }

    public static String toString(Object[] array, boolean frame, String delimeter) {
        StringBuffer b = new StringBuffer();
        if (frame) {
            b.append('[');
        }
        for (int i = 0; i < array.length; i++) {
            b.append(array[i]);
            if (i < (array.length - 1)) {
                b.append(delimeter);
            }
        }
        if (frame) {
            b.append(']');
        }
        return b.toString();
    }

    public static String toString(Collection collection, boolean frame, String delimeter) {
        return toString((Object[]) collection.toArray(new Object[collection.size()]), frame, delimeter);
    }

    public static String toString(Collection collection, boolean frame) {
        return toString(collection, frame, DEFAULT_DELIMETER);
    }

    public static String toString(Collection collection) {
        return toString(collection, true);
    }

    public static String concatAsString(Object[] array) {
        return concatAsString(array, "_");

    }

    public static String concatAsString(Object[] array, String delim) {
        StringBuffer b = new StringBuffer();
        for (int i = 0; i < array.length; i++) {
            b.append(array[i]);
            if (i < (array.length - 1)) {
                b.append(delim);
            }
        }
        return b.toString();
    }

    /** Checks whether the given Object equals the given String. */
    public static boolean equals(String s, Object o) {
        return o instanceof String && org.apache.commons.lang.StringUtils.equals(s, (String) o);
    }

    /** Checks whether the given Object equals the given String. */
    public static boolean equals(Object o, String s) {
        return equals(s, o);
    }

    /** Checks whether the given Object equals any of the given options. */
    public static boolean equals(Object o, String[] options) {
        return o instanceof String && equals((String) o, options);
    }

    /** Checks whether the given String equals any of the given options. */
    public static boolean equals(String s, String[] options) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < options.length; i++) {
            if (s.equals(options[i])) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether the given String starts with any of the given options. */
    public static boolean startsWith(String s, String[] options) {
        if (s == null) {
            return false;
        }
        for (int i = 0; i < options.length; i++) {
            if (s.startsWith(options[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert a <code>String</code> to an <code>int</code>, returning a default value if the conversion fails or
     * if the string is <code>null</code>.
     */
    public static int toInt(String str, int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException nfe) {
            return defaultValue;
        }
    }

    public static String toString(Enumeration enumeration) {
        return toString(EnumerationUtils.toList(enumeration));
    }

}
