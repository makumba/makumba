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

    public static boolean anyNotEmpty(String[] o) {
        for (String element : o) {
            if(org.apache.commons.lang.StringUtils.isNotEmpty(element)) {
                return true;
            }
        } 
        return false;
    }
    
    public static boolean allNotEmpty(String[] o) {
        for (String element : o) {
            if(org.apache.commons.lang.StringUtils.isBlank(element)) {
                return false;
            }
        } 
        return true;
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

    public static String toString(Collection<?> collection, boolean frame, String delimeter) {
        return toString(collection.toArray(new Object[collection.size()]), frame, delimeter);
    }

    public static String toString(Collection<?> collection, boolean frame) {
        return toString(collection, frame, DEFAULT_DELIMETER);
    }

    public static String toString(Collection<?> collection) {
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
    public static boolean equalsAny(Object o, String[] options) {
        return o instanceof String && equalsAny((String) o, options);
    }

    /** Checks whether the given String equals any of the given options. */
    public static boolean equalsAny(String s, String[] options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.equals(element)) {
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
        for (String element : options) {
            if (s.startsWith(element)) {
                return true;
            }
        }
        return false;
    }

    public static String getStartsWith(String s, String[] options) {
        if (s == null) {
            return null;
        }
        for (String element : options) {
            if (s.startsWith(element)) {
                return element;
            }
        }
        return null;
    }
    
    /** Checks whether the given String ends with any of the given options. */
    public static boolean endsWith(String s, String[] options) {
        if (s == null) {
            return false;
        }
        for (String element : options) {
            if (s.endsWith(element)) {
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

    public static String toString(Enumeration<?> enumeration) {
        return toString(EnumerationUtils.toList(enumeration));
    }
    
    public static String[] append(String[] functionNames, String toAppend) {
        String[] s = new String[functionNames.length];
        for (int i = 0; i < s.length; i++) {
            s[i] = functionNames[i] + toAppend;
        }
        return s;
    }

}
