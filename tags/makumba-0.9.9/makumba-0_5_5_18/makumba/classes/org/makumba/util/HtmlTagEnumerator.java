package org.makumba.util;

import java.io.*;

public class HtmlTagEnumerator 
{
  Reader source;
  StringBuffer tag, string;

  String lastString;
  String lastTag;
  boolean inTag=false;
  boolean ended=false;

  void init()
  {
    tag= new StringBuffer();
    string=new StringBuffer();
  }

  public HtmlTagEnumerator(Reader r)
  {
    this.source=r;
    init();
  }

  public boolean next() throws IOException
  {
    int c;

    if(ended)
      return false;
    while((c=source.read())!=-1)
      {
	if(c=='<')
	  {
	    lastString=string.toString();
	    inTag=true;
	    string= new StringBuffer();
	    continue;
	  }
	else if(c=='>' && inTag)
	  {
	    lastTag=tag.toString();
	    tag=new StringBuffer();
	    inTag=false;
	    return true;
	  }
	else
	  if(inTag)
	    tag.append((char)c);
	  else
	    string.append((char)c);
      }
    source.close();
    ended=true;
    return false;
  }

  public String getNonHtml()
  {
    return lastString;
  }

  public String getTag()
  {
    return lastTag;
  }

  public String getTagType()
  {
    String s= lastTag.trim();
    int i=0;
    if(s.startsWith("/"))
       i=1;
    while(i<s.length() && Character.isLetter(s.charAt(i))) i++;
    return s.substring(0, i);
  }
  
  public static void main(String argv[])throws IOException
  {
    HtmlTagEnumerator e= new HtmlTagEnumerator(new FileReader(argv[0]));
    while(e.next())
      {
	System.out.println(e.getNonHtml()+" "+e.getTag()+" "+e.getTagType());
      }
  }
}






