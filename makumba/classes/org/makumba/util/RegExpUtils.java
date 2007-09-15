package org.makumba.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides several constants which are parts of patterns, and some methods for testing.
 * 
 * @author Rudolf Mayer
 * @version $Id: RegExpUtils.java,v 1.1 Sep 15, 2007 3:44:49 AM rudi Exp $
 */
public class RegExpUtils {
    public static final String identifier = "\\w[\\w|\\d]*";

    public static final String whitespace = "[\\s]*";

    public static final String minOneWhitespace = whitespace + "+";

    public static final String minOneLineWhitespace = "[ \\t]+";

    public static final String LineWhitespaces = "[ \\t]*";

    public static final String digit = "\\d";

    public static final String minOneDigit = digit + "+";

    public static final String nonDigit = "\\D";

    public static final String nonWhitespace = "[^\\s]";

    public static final String nonWhitespaces = nonWhitespace + "+";

    public static final String minOneNonWhitespace = "[^\\s]+";

    public static final String word = "\\w";

    public static final String minOneWord = word + "+";

    public static final String nonWord = "\\W";

    public static final String fieldName = "[a-zA-Z]" + word + "*" + "(?:\\.\\w+)?";

    public static final String fieldNameAndSpaces = fieldName + LineWhitespaces;

    public static String or(String[] options) {
        String s = "(";
        for (int i = 0; i < options.length; i++) {
            s += options[i];
            if (i + 1 < options.length) {
                s += "|";
            }
        }
        return s + ")";
    }

    public static void main(String[] args) {
        System.out.println(or(new String[] { "a", "b", "c" }));
        testIdentifiers();
    }

    /** testing method. */
    public static void testIdentifiers() {
        for (int i = 0; i < 256; i++) {
            char ch = (char) i;
            if (Character.isJavaIdentifierPart(ch)) {
                System.out.print(i + ":\t");
                if (Character.isJavaIdentifierStart(ch)) {
                    System.out.print(ch);
                }
                System.out.println("\t" + ch);
            }
        }
    }

    /** Testing method to see if some Strings match a pattern. */
    public static void evaluate(Pattern p, String[] rules, boolean details) {
        System.out.println(p.pattern());
        for (int i = 0; i < rules.length; i++) {
            Matcher matcher = p.matcher(rules[i].trim());
            System.out.println(rules[i].trim() + ":" + matcher.matches());
            if (matcher.matches() && details) {
                System.out.print("groups:" + matcher.groupCount());
                for (int j = 0; j < matcher.groupCount(); j++) {
                    System.out.print("\t|" + matcher.group(j + 1) + "|");
                }
                System.out.println();
            }
        }
    }

    public static void evaluate(Pattern p, String[] rules) {
        evaluate(p, rules, true);
    }

}
