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






