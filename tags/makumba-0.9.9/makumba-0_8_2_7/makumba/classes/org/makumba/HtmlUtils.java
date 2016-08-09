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

package org.makumba;

import org.apache.commons.lang.StringUtils;
import org.makumba.commons.ReadableFormatter;

/**
 * This class provides basic 'text-to-HTML' functionality.<br/>
 * Note: previously this was an existing class in the internal "util" package (fred, 2003-08-24).
 * 
 * @author
 * @version $Id$
 */
public class HtmlUtils {

    private static final int LENGTH_TO_CHECK = 1024 * 32;

    // special HTML codes
    static public String[] specials = { "\"", "quot", "<", "lt", "&", "amp", ">", "gt" };

    static public String[] tagExamples = { "<head>", "<title>", "<html", "<meta", "<br>", "<p>", "</p>", "<b>", "</b>",
            "<font", "</font>", "</a>", "<ol>", "<ul>", "<li>", "<img ", "</table>", "<tr>", "</tr>", "<td>", "</td>",
            "<strong>", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>", "<em>", "<div>", "<span>" };

    /**
     * Tries to detect whether input string is HTML-formatted; heuristic detection. Implementation note: For long input
     * String, checks only first 32768 characters (32 KB).
     */
    public static boolean detectHtml(String s) {
        if (s.length() > LENGTH_TO_CHECK) {
            s = s.substring(0, LENGTH_TO_CHECK);
        }
        s = s.toLowerCase();

        // try to find HTML specific "&XXX;" strings
        for (int i = 1; i < specials.length; i += 2) {
            if (s.indexOf("&" + specials[i] + ";") != -1)
                return true;
        }

        // try to find HTML tags
        for (int i = 0; i < tagExamples.length; i++) {
            if (s.indexOf(tagExamples[i]) != -1)
                return true;
        }

        return false;
    }

    /** Converts a string into its HTML correspondent using special codes. */
    public static String string2html(String s) {
        boolean special;

        if (s == null)
            return "null";
        StringBuffer sb = new StringBuffer();
        int l = s.length();
        for (int i = 0; i < l; i++) {
            special = false;
            for (int j = 0; j < specials.length; j++)
                if (s.charAt(i) == specials[j++].charAt(0)) {
                    sb.append('&');
                    sb.append(specials[j] + ";");
                    special = true;
                }
            if (!special)
                sb.append(s.charAt(i));
        }
        return sb.toString();
    }

    /** Determines the maximum length of a line in a text. */
    public static int maxLineLength(String s) {
        int r = 0;

        while (true) {
            // llok to determine the current line
            int i = s.indexOf('\n');
            // if this was the last line
            if (i == -1)
                // if the previous max line length was bigger
                if (r > s.length())
                    return r;
                else
                    return s.length();
            // if the current line is the bigest
            if (i > r)
                r = i;
            if (i + 1 < s.length())
                // erase the current line
                s = s.substring(i + 1);
            else
                return r;
        }
    }

    /**
     * Prints a text with very long lines. Surrounds every paragraph with start|end- separator. FIXME bug 38.
     */
    public static String text2html(String s, String startSeparator, String endSeparator) {
        // convert the special characters
        s = string2html(s);

        String formatted = startSeparator;
        while (true) {
            // look for "newline"
            int i = s.indexOf('\n');

            if (i == -1) { // not found!
                // add last part to the previously formatted text, add finisher and return it
                return formatted + s + endSeparator;
            } else if (i > 0) { // found, add it to formatted text.
                formatted += s.substring(0, i) + endSeparator + startSeparator;
            }

            // test if there is more text
            if (i + 1 < s.length())
                s = s.substring(i + 1); // continue with the rest of the input string
            else
                return formatted + endSeparator;
        }
    }

    /**
     * removes HTML tags and replaces double quotes by the corresponding HTML entity, so that the passed text can safely
     * be included in HTML attributes (such as the &lt;A&gt; TITLE attribute)
     */
    public static String stripHTMLTags(String str) {
        // remove quotes
        str = str.replaceAll("\"", "&quot;");
        // zap all tags
        str = str.replaceAll("<[^>]*>", "");
        return str;
    }
    
    /**
     * escapes single and double quotes in a string by adding a forward slash before them
     */
    public static String escapeQuotes(String str) {
        if(str == null)
            return null;

        // escape double quotes
        str = str.replace("\"", "\\\"");
        // escape single quotes
        str = str.replace("'", "\\'");
        
        return str;
    }

    public static void main(String[] args) {
        String s = "Indeed, during this event, we'll make a great general overview on all kind of transport, from planes to trains, cars and even subways; with a visit in well-known companies for each transport (Airbus, Toyota, TGV.). So lets. So mainly, you.ll be provided a lot of information about the new transportation systems either made or used in France. Do you really believe that this great topic will be the only main thing we.ll do ?? Come on !! Of course not !!! Lets so go. Another main issue of our event is FUN, FUN, FUN and FUN again !!!! At the same time you will discover our city, our region and their secrets. And maybe have the great opportunity to enjoy a week-end trip to Paris (it.s far to be sure but we.re trying our BEST to make you happy !) But don.t worry we.ll find something else if it.s not possible  ;-)  and want to watch the video of our last event click Come and have a look what we can do and if there are any bugs that should be tested and rooted out of here whle at it so what do you think about themall dinosaurs <b> BOLD TEXT </b> if not then the HTML auto detection does not work for this text.";
        int repeats = 50;
        s = StringUtils.repeat(s, repeats);
        System.out.println("size: " + ReadableFormatter.readableBytes(s.length()));
        System.out.println("checking: " + ReadableFormatter.readableBytes(LENGTH_TO_CHECK));
        long start = System.currentTimeMillis();
        System.out.println("is HTML: " + detectHtml(s));
        long duration = System.currentTimeMillis() - start;
        System.out.println("took ms: " + duration);
    }

}
