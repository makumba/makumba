package org.makumba.util;

public class HtmlUtils
{
  // special HTML codes
  static public String[] specials={"\"", "quot", "<", "lt", "&", "amp", ">", "gt"};
  static public String[] tagExamples={"<head>","<title>", "<html", "<meta", "<br>", "<p>", "</p>", "<b>", "</b>", "<font", "</font>", "</a>", "<ol>", "<ul>", "<li>", "<img ", "</table>", "<tr>", "</tr>", "<td>", "</td>"};

  /** heuristic HTML detection */
  public static boolean detectHtml(String s)
  {
    if(s.length()>1024)
      s=s.substring(0, 1024);
    s=s.toLowerCase();
    for(int i=1; i<specials.length; i+=2)
      if(s.indexOf("&"+specials[i]+";")!=-1)
	return true;

    for(int i=0; i<tagExamples.length; i++)
      if(s.indexOf(tagExamples[i])!=-1)
	return true;
    return false;
  }
  /** convert a string into its html correspondent using special codes */
  public static String string2html(String s)
  {
    boolean special;
    
    if(s==null)
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

  /** the maximum length of a line */
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
  
  /** print a text with very long lines */
  public static String text2html(String s, String startSeparator, String endSeparator)
  {
    // conver the special characters
    s= string2html(s);
    String r= startSeparator;
    while(true)
      {
	// look for end of line
	int i= s.indexOf('\n');
	// if it's the last line
	if(i==-1)
	  // add it to the previous formatted text, add a new line and return it
	  return r+s+endSeparator;
	if(i>0)
	  // add the nex line
	  r+=s.substring(0, i);
	// add a new line markup
	r+=endSeparator+startSeparator;
	// erase the current line already processed
	if(i+1<s.length())
	  s=s.substring(i+1);
	else
	  return r+endSeparator;
      }
  }
}
