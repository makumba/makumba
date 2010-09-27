package org.makumba.commons;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

/**
 * Utility class containing all kind of reserved words which may cause problems if used as e.g. field names
 * 
 * @author Manuel Gay
 * @author Rudolf Mayer
 * @version $Id$
 */
public class ReservedKeywords {

    private static Set<String> reservedKeywords;

    private static String[] javaReserved = { "abstract", "continue", "for", "new", "switch", "assert", "default",
            "goto", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double",
            "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum",
            "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final",
            "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",
            "native", "super", "while" };

    private static String[] hibernateReserved = {}; /*"id"*/

    // not sure if this list should be including all SQL keywords, or just such that can cause problems in the SQL
    // statements.
    // chose for now to just list those that would cause problems, list is for sure not complete
    private static String[] sqlReserved = { "avg", "count", "distinct", "group", "order", "sum", "database", "from" };

    static {
        reservedKeywords = new HashSet<String>();
        CollectionUtils.addAll(reservedKeywords, javaReserved);
        CollectionUtils.addAll(reservedKeywords, hibernateReserved);
        CollectionUtils.addAll(reservedKeywords, sqlReserved);
    }

    public static Set<String> getReservedKeywords() {
        return reservedKeywords;
    }

    public static boolean isReservedKeyword(String s) {
        return reservedKeywords.contains(s);
    }

    public static String getKeywordsAsString() {
        String reserved = new String();
        Iterator<String> i = reservedKeywords.iterator();
        while (i.hasNext()) {
            reserved += i.next();
            if (i.hasNext()) {
                reserved += ", ";
            }
        }
        return reserved;
    }

}
