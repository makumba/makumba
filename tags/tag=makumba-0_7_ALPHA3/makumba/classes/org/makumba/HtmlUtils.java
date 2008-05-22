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

/**
 * This class provides basic 'text-to-HTML' functionality.
 * Note: previously this was an existing class in the internal "util" package (fred, 2003-08-24).
 */

public class HtmlUtils {

  // special HTML codes
  static public String[] specials={"\"", "quot", "<", "lt", "&", "amp", ">", "gt"};
  static public String[] tagExamples={"<head>","<title>", "<html", "<meta", "<br>", "<p>", "</p>", "<b>", "</b>", "<font", "</font>", "</a>", "<ol>", "<ul>", "<li>", "<img ", "</table>", "<tr>", "</tr>", "<td>", "</td>", "<strong>", "<h1>", "<h2>", "<h3>", "<h4>", "<h5>", "<h6>", "<em>", "<div>", "<span>"};

  /** Tries to detect whether input string is HTML-formatted; heuristic detection. 
   *  Implementation note: For long input String, checks only first 1024 characters. 
   */
  public static boolean detectHtml(String s) {
    if (s.length()>1024) { 
        s = s.substring(0, 1024); 
    }
    s = s.toLowerCase();

    // try to find HTML specific "&XXX;" strings
    for (int i=1; i<specials.length; i+=2){
        if (s.indexOf("&"+specials[i]+";") != -1) return true;
    }

    // try to find HTML tags
    for (int i=0; i<tagExamples.length; i++){
        if (s.indexOf(tagExamples[i]) != -1) return true;
    }

    return false;
  }


  /** Converts a string into its HTML correspondent using special codes. */
  public static String string2html(String s)
  {
    boolean special;
    
    if (s==null)
      return "null";
    StringBuffer sb= new StringBuffer();
    int l=s.length();
    for(int i= 0; i<l; i++)
      {
	special=false;
	for(int j=0; j<specials.length; j++)
	  if(s.charAt(i)==specials[j++].charAt(0))
	    {
	      sb.append('&');
	      sb.append(specials[j]+";");
	      special=true;
	    }
	if (!special)
	  sb.append(s.charAt(i));
      }
    return sb.toString();
  }


  /** Determines the maximum length of a line in a text. */
  public static int maxLineLength(String s)
  {
    int r=0;
    
    while(true)
      {
	// llok to determine the current line
	int i= s.indexOf('\n');
	// if this was the last line
	if(i==-1)
	  // if the previous max line length was bigger
	  if (r > s.length())
	    return r;
	  else
	    return s.length();
	// if the current line is the bigest
	if(i>r)
	  r=i;
	if(i+1<s.length())
	    // erase the current line
	  s=s.substring(i+1);
	else
	  return r;
      }
  }
  

  /** 
   * Prints a text with very long lines. Surrounds every paragraph with start|end- separator.
   * FIXME bug 38.
   */
  public static String text2html(String s, String startSeparator, String endSeparator)
  {
    // convert the special characters
    s = string2html(s);

    String formatted = startSeparator;
    while (true) {
        // look for "newline"
        int i= s.indexOf('\n');

        if (i == -1) { // not found!
          // add last part to the previously formatted text, add finisher and return it
          return formatted + s + endSeparator;
        }
        else if (i>0) { // found, add it to formatted text.
          formatted += s.substring(0, i) + endSeparator + startSeparator;
        }

        // test if there is more text
        if (i+1 < s.length())
          s = s.substring(i+1); // continue with the rest of the input string
        else
          return formatted + endSeparator;
      }
  }
}
