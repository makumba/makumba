///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.commons;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides several constants which are parts of patterns, and some methods for testing.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class RegExpUtils {
    public static final String identifier = "\\w[\\w|\\d]*";

    public static final String whitespace = "[\\s]*";

    public static final String minOneWhitespace = "[\\s]+";

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

    public static final String dotPath = "[a-zA-Z]" + word + "*" + "(?:\\.\\w+)+";

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

        // evaluate(org.makumba.analyser.engine.JspParseData.JSPELFunctionPattern, true, "mak:count()", "mak:Count()",
        // "mak:lastCount()", "mak:lastCountById('abc')", "mak:lastCountById('abc', 'def')", "mak:expr('l.name')");
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
    public static void evaluate(Pattern p, boolean details, String... rules) {
        System.out.println(p.pattern());
        for (String rule : rules) {
            Matcher matcher = p.matcher(rule.trim());
            System.out.println(rule.trim() + ":" + matcher.matches());
            if (matcher.matches() && details) {
                System.out.print("groups:" + matcher.groupCount());
                for (int j = 0; j < matcher.groupCount(); j++) {
                    System.out.print("\t|" + matcher.group(j + 1) + "|");
                }
                System.out.println();
            }
        }
    }

    public static void evaluate(Pattern p, String... rules) {
        evaluate(p, true, rules);
    }

}
