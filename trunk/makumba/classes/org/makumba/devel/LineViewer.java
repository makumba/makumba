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

package org.makumba.devel;
import org.makumba.abstr.*;
import java.io.*;
import javax.servlet.http.*;

/** a viewer that shows everything per line */
public class LineViewer implements SourceViewer
{
  String realPath, virtualPath, contextPath;
  Reader reader;
  boolean lineNumbers;
  File dir;

  /** if this resource is actually a directory, returns not null */
  public File getDirectory() 
  { 
    if(dir!=null && dir.isDirectory())
      return dir;
    return null;
  }
  public Reader getReader(){ return reader; }

  void readFromURL(java.net.URL u) throws IOException
  {
    if(u==null)
      throw new FileNotFoundException(virtualPath);
    realPath= u.getFile();
    dir= new File(realPath);
    if(!dir.isDirectory())
      reader= new InputStreamReader(new FileInputStream(dir)); 
  }

  public LineViewer(boolean b){ lineNumbers=b; }
  public LineViewer(){ this(false); }
  public LineViewer(Reader r){ this(false); this.reader=r; }
  
  /** parse the text and write the output */
  public void parseText(PrintWriter w) throws IOException
  {
    w.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
    w.println("<html><head>");
    w.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");
    String title="";
    if(realPath!=null && virtualPath!=null)
	title=virtualPath+" - source";
    w.println("<title>"+title+"</title>");
    if(lineNumbers)
	w.println("<style type=\"text/css\">\n A.lineNo {color:navy; background-color:lightblue; text-decoration:none; cursor:default;}\n</style>");
    w.println("</head><body bgcolor=white><table width=\"100%\" bgcolor=\"lightblue\"><tr>");
    if(realPath!=null && virtualPath!=null)
	w.print("<td><font size=\"+2\"><a href=\""+virtualPath+"\"><font color=\"darkblue\">"+virtualPath+"</font></a></font><br>"+new File(realPath).getCanonicalPath()+"</td>");
    intro(w);
    w.print("</tr></table>\n<pre style=\"margin-top:0\">");	

    // we go line by line as an MDD references cannot span over newlines
    // as a bonus, we print the line numbers as well.
    LineNumberReader lr= new LineNumberReader(reader);
    String s=null;
    while((s=lr.readLine())!=null)
      {
	parseLine(s);
	if(lineNumbers)
	  {
	    int n= lr.getLineNumber();
	    w.print("<a name=\""+n+"\" href=\"#"+n+"\" class=\"lineNo\">"+n+":\t</a>");
	  }
	printLine(w, s);
      }
    w.println("\n</pre>");
    footer(w);
    w.println("\n</body></html>");	
    reader.close();
  }

  void intro(PrintWriter pw) throws IOException {} 
  void footer(PrintWriter pw) throws IOException
  {
      pw.println("<hr><font size=\"-1\"><a href=\"http://www.makumba.org\">Makumba</a> developer support, version: "+org.makumba.MakumbaSystem.getVersion()+"</font>");
  }

  void printLine(PrintWriter pw, String s) throws IOException 
  {
    String t= getLineTag(s);
    if(t!=null)
      pw.print("<a name=\""+t+"\"></a>");
    pw.print(highlighted); 
    
    // not sure of this fix...was "<br>"
    pw.print("\n");
  }

  
  String getLineTag(String s){ return null; }

  StringBuffer highlighted;

  void parseLine(String s)
  {
    highlighted= new StringBuffer();

    while(true){
      int n= s.indexOf('.');
      if(n==-1)
	{
	  writeNonMdd(s);
	  return;
	}
      int j=n;
      int len= s.length();
      while(--j>0 && isMakumbaTypeChar(s.charAt(j)));
      j++;
      while(++n<len&& isMakumbaTypeChar(s.charAt(n)));

      String possibleMdd=s.substring(j, n);
      if(possibleMdd.indexOf("www.makumba.org")!=-1 )
	{
	  writeNonMdd(s.substring(0, j));
	  highlighted.append("<a href=\"http://www.makumba.org\">").append(possibleMdd).append("</a>");
	}
      else{
	Class c=null;
	String page=null;
	java.net.URL u1=null, u2=null;

	if((u1=org.makumba.abstr.RecordParser.findDataDefinition(possibleMdd, "mdd"))==null && 
	   (u2=org.makumba.abstr.RecordParser.findDataDefinition(possibleMdd, "idd"))==null &&
	   (c= findClass(possibleMdd))==null &&
	   (page=findPage(possibleMdd))==null)
	  writeNonMdd(s.substring(0, n));
	else
	{
	  writeNonMdd(s.substring(0, j));
	  if(u1!=null || u2!=null)
	    highlighted.append("<a href=\""+contextPath+"/dataDefinitions/").append(possibleMdd)
	      .append("\">").append(possibleMdd).append("</a>");
	  else if(c!=null)
	    highlighted.append("<a href=\""+contextPath+"/classes/").append(c.getName()).append("\">").append(possibleMdd).append("</a>");
	  else if(page!=null)
	    {
	      highlighted.append("<a href=\"").append(page);
	      if(page.endsWith("jsp"))
		highlighted.append("x");
	      highlighted.append("\">").append(possibleMdd).append("</a>");
	    }
	}
      }
      if(n==len)
	  return;
      s=s.substring(n, len);
    }
  }

  String findPage(String s)
  {
    return null;
  }

  // this should take into account import lines
  Class findClass(String s)
  {
    Class c=null;
    try{
      c=Class.forName(s);
    }catch(Throwable t) { return null; }
    if(org.makumba.util.ClassResource.get(c.getName().replace('.', '/')+".java")!=null)
      return c;
    return null;
  }

  boolean isMakumbaTypeChar(char c)
  {
    return c=='.' || Character.isJavaIdentifierPart(c) || c=='/' || c=='-';
  }

  int position;
  char current;
  String text;

  void writeNonMdd(String s)
  {
    text=s;
    for(position=0; position<s.length(); position++)
      {
	current=text.charAt(position);
	treat();
      }
  }

  void treat()
  {
    htmlEscape();
  }

  void htmlEscape()
  {
    switch(current){
    case '<': highlighted.append("&lt;"); break;
    case '>': highlighted.append("&gt;"); break;
    case '&': highlighted.append("&amp;"); break;
    default: highlighted.append(current);
    }   
  }

  String pattern;

  boolean lookup(String p)
  {
    int l=p.length();
    try{
    if(position+l>text.length()
       ||!text.substring(position, position+l).equals(p))
	return false;

    pattern=p;
    return true;
    }catch(RuntimeException e){
      org.makumba.MakumbaSystem.getMakumbaLogger("devel").log(java.util.logging.Level.SEVERE, position+" "+l+" "+text.length(), e);
      throw e;
    }
  }
  
  void advance()
  {
    position+=pattern.length()-1;
  }

}
