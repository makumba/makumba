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
import java.util.*;
import java.util.regex.*;

import java.lang.reflect.Method;
import javax.servlet.jsp.tagext.Tag;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.makumba.MakumbaSystem;

/** This class performs a rudimentary detection of JSP-relevant tags in a JSP page.
 * @author cristi
 */
public class JspParseData
{
  /** this method will perform the analysis if not performed already, or if the file has changed
   * the method is synchronized, so other accesses are blocked if the current access determines that an analysis needs be performed 
   *@param initStatus an initial status to be passed to the JspAnalyzer. for example, the pageContext for an example-based analyzer
   */
  public synchronized Object getAnalysisResult(Object initStatus)
  {
    if(!unchanged())
	try
	{
	    parse(initStatus);
	}catch(Error e)
	    {
		holder=e;
		throw e;
	    }
	catch(RuntimeException re)
	    {
		holder=re;
		throw re;
	    }
    return holder;
  }

  static int analyzedPages=  NamedResources.makeStaticCache
  ("JSP mak:list root tags",
   new NamedResourceFactory()
   {
     public Object getHashObject(Object o)
       {
	 Object[]o1= (Object[])o;
	 return ((String)o1[0])+o1[1].getClass().getName();
       }

     public Object makeResource(Object o, Object hashName)
       throws Throwable
       {
	 Object[]o1= (Object[])o;
	 return new JspParseData((String)o1[0], (JspAnalyzer)o1[1], (String)o1[2]);
       }
   });

    /** return the pageData of the page at the given path in the given webapp 
    this is the only way for clients of this class to obtain instances 
    of JspPageData
   */
  static public JspParseData getParseData(String webappRoot, String path, JspAnalyzer an)
  {
    Object arg[]= {webappRoot+path, an, path };
    return (JspParseData)NamedResources.getStaticCache(analyzedPages).getResource(arg);
  }

  /** the interface of a JSP analyzer */
  public interface JspAnalyzer
  {
    /** make a status holder, which is passed to all other methods 
     *@param initStatus an initial status to be passed to the JspAnalyzer. for example, the pageContext for an example-based analyzer      
     */
    Object makeStatusHolder(Object initStatus);

    /** start a body tag 
     * @see endTag(TagData, Object)
     */
    void startTag(TagData td, Object status);
    
    /** the end of a body tag, like </...> */
    void endTag(TagData td, Object status);
    
    /** a simple tag, like <... /> */
    void simpleTag(TagData td, Object status);

    /** a system tag, like <%@ ...%> */
    void systemTag(TagData td, Object status);

    /** the end of the page
      @returns the result of the analysis */
    Object endPage(Object status);
  }

  /** a composite object passed to the analyzers */
  public class TagData
  {
    /** the parse data where this TagData was produced */
    JspParseData parseData;

    /**name of the tag*/
    public String name; 

    /**tag attributes */
    public Map attributes; 

    /**tag object, if one is created by the analyzer */
    public Object tagObject;
   
    /** the syntax points where the whole thing begins and ends */
    SyntaxPoint start, end;
  }

  /** the path of the JSP page */
  File file;
  
  /** the timestamp of the JSP page. if the page is found newer on disk, 
    the object is discarded */
  long lastChanged; 

  /** the analyzer plugged in */
  JspAnalyzer analyzer;

  /** the syntax points of this page */
  SourceSyntaxPoints syntaxPoints;

  public SourceSyntaxPoints getSyntaxPoints(){ return syntaxPoints; }

  /** the holder of the analysis status, and partial results */
  Object holder;

  /** the JSP URI, for debugging purposes */
  String uri;

  /** the patterns used to parse the page */
  static private Pattern JspSystemTagPattern, JspTagPattern, JspCommentPattern, 
    JspTagAttributePattern, Word, TagName;

  static String attribute(String quote){
    String bs="\\";
    String q=bs+quote;
    String backslash=bs+bs;
    
    return 
      bs+"s*"+
      bs+"w+"+
      bs+"s*="+
      bs+"s*"+
      q+
      "([^"+q+backslash+"]|"+
      backslash+q+")*"+
      q;
  }

  static{
    String attribute="("+attribute("\"")+"|"+attribute("\'")+")";

    try{
      JspTagAttributePattern= Pattern.compile(attribute);
      JspSystemTagPattern= Pattern.compile("<%@\\s*\\w+("+attribute+")*\\s*%>");
      JspTagPattern= Pattern.compile("<((\\s*\\w+:\\w+("+attribute+")*\\s*)/?|(/\\w+:\\w+\\s*))>");
      JspCommentPattern= Pattern.compile("<%--([^-]|(-[^-])|(--[^%])|(--%[^>]))*--%>", Pattern.DOTALL);
      Word= Pattern.compile("\\w+");
      TagName= Pattern.compile("\\w+:\\w+");
    }catch(Throwable t){ t.printStackTrace(); }
  }

  /** private  constructor, construction can only be made by getPageData() */
  private JspParseData(String path, JspAnalyzer an, String uri)
  {
    file= new File(path);
    this.uri=uri;
    lastChanged= 0l;
    analyzer=an;
  }

  boolean unchanged()
  {
    return file.lastModified()==lastChanged;
  }

  void parse(Object initStatus)
  {
    long start= new java.util.Date().getTime();
    lastChanged=file.lastModified();
    String content=readFile();    
   
    syntaxPoints= new SourceSyntaxPoints(content);

    holder= analyzer.makeStatusHolder(initStatus);

    // remove JSP comments from the text
    content= syntaxPoints.unComment(content, JspCommentPattern, "JSPComment");

    // the page analysis as such:
    treatTags(content, analyzer);
    
    holder= analyzer.endPage(holder);

    org.makumba.MakumbaSystem.getMakumbaLogger("jspparser.time").info
      ("analysis of "+uri+" took "+(new java.util.Date().getTime()-start)+" ms");
  }

  /** identify tag attributes from a tag string and put them in a Map. set the attribute syntax points */
  Map parseAttributes(String s, int origin)
  {
    Map ret= new HashMap(13);
    
    Matcher m= JspTagAttributePattern.matcher(s);
    while(m.find())
      {
	// here we have an attribute="value"
	String attr=s.substring(m.start(), m.end());
	int n=attr.indexOf('=');
	String val=attr.substring(n+1).trim();
	
	// we use a streamtokenizer to do the complicated parsing of "...\"\t ...\n...."
	StreamTokenizer st= new StreamTokenizer(new StringReader(val));
	char quote=val.charAt(0);
	st.quoteChar(quote);
	try{
	  if(st.nextToken()!=quote)
	    throw new RuntimeException("quoted string expected, found "+val);
	}catch(java.io.IOException ioe) { throw new RuntimeWrappedException(ioe);}
	String attName= attr.substring(0, n).trim();
	ret.put(attName, st.sval);
	
	// syntax points
	syntaxPoints.addSyntaxPoints(origin, origin+attName.length(), "JSPTagAttributeName", null);
	syntaxPoints.addSyntaxPoints(origin+n, origin+n+1, "JSPTagAttributeEquals", null);
	syntaxPoints.addSyntaxPoints(origin+s.indexOf('\"', n), origin+s.length(), "JSPTagAttributeValue", null);
      }
    return ret;
  }

  /** go thru the tags in the page */
  void treatTags(String content, JspAnalyzer an)
  {
    Matcher tags= JspTagPattern.matcher(content);
    Matcher systemTags= JspSystemTagPattern.matcher(content);   

    int tagStart=Integer.MAX_VALUE;
    if(tags.find())
	tagStart=tags.start();
    int systemStart=Integer.MAX_VALUE;
    if(systemTags.find())
	systemStart=systemTags.start();
	

    while(true)
      {
	if(tagStart< systemStart)
	  {
	    treatTag(tags, content, an);
	    tagStart=Integer.MAX_VALUE;
	    if(tags.find())
		tagStart=tags.start();
	  }
	else if(systemStart< tagStart)
	  {
	    treatSystemTag(systemTags, content, an);
	    systemStart=Integer.MAX_VALUE;
	    if(systemTags.find())
		systemStart=systemTags.start();
	  }
	if(tagStart==Integer.MAX_VALUE && systemStart==Integer.MAX_VALUE)
	  break;
      }
  }

  void treatTag(Matcher m, String content, JspAnalyzer an)
  {
    String tag= content.substring(m.start(), m.end());
    boolean tagEnd=tag.startsWith("</");
    boolean tagClosed=tag.endsWith("/>");
    Matcher m1= TagName.matcher(tag);
    m1.find();
    syntaxPoints.addSyntaxPoints(m.start()+m1.start(), m.start()+m1.end(), "JSPTagName", null);

    String type=tagEnd?"JspTagEnd":(tagClosed?"JspTagSimple":"JspTagBegin");

    SyntaxPoint end= syntaxPoints.addSyntaxPoints(m.start(), m.end(), type, null);
    SyntaxPoint start= (SyntaxPoint)end.getOtherInfo();

    String tagName= tag.substring(m1.start(), m1.end());
    
    TagData td= null;
    td=new TagData();
    td.name=tagName;
    td.parseData=this;
    td.start=start;
    td.end=end;

    if(!tagEnd)
	td.attributes= parseAttributes(tag, m.start());

    Logger log= MakumbaSystem.getMakumbaLogger("jspparser.tags");
    
    // we avoid evaluation of the logging expression
    if(log.isLoggable(Level.FINE))
      log.fine(uri+":"+start.line+":"+start.column+": "+
	       (tagEnd?("/"+tagName):(td.name+" "+td.attributes)));
    if(tagEnd)
      {
	an.endTag(td, holder);
	return;
      }
    
    if(tagClosed)
      an.simpleTag(td, holder);
    else
      an.startTag(td, holder);
  }

  /** treat system tags */
  void treatSystemTag(Matcher m, String content, JspAnalyzer an)
  {
    String tag= content.substring(m.start(), m.end());
    SyntaxPoint end= syntaxPoints.addSyntaxPoints(m.start(), m.end(), "JSPSystemTag", null);
    
    Matcher m1= Word.matcher(tag);
    m1.find();
    syntaxPoints.addSyntaxPoints(m.start()+m1.start(), m.start()+m1.end(), "JSPSystemTagName", null);
    SyntaxPoint start= (SyntaxPoint)end.getOtherInfo();

    TagData td= new TagData();
    td.name=tag.substring(m1.start(), m1.end());
    td.parseData=this;
    td.attributes= parseAttributes(tag, m.start());
    td.start=start;
    td.end=end;

    Logger log= MakumbaSystem.getMakumbaLogger("jspparser.tags");
    
    // we avoid evaluation of the logging expression
    if(log.isLoggable(Level.FINE))
      log.fine(uri+":"+start.line+":"+start.column+": "+td.name+" "+td.attributes);

    an.systemTag(td, holder);
  }
  
  /** return the content of the JSP file in a string */
  String readFile()
  {
    StringBuffer sb=new StringBuffer();
    try{
      BufferedReader rd= new BufferedReader(new FileReader(file));
      char[] buffer= new char[2048];
      int n;
      while((n=rd.read(buffer))!=-1)
	sb.append(buffer, 0, n);
    }catch(IOException e) { e.printStackTrace(); }
    return sb.toString();
  }

    public static void tagDataLine(JspParseData.TagData td, StringBuffer sb)
    {
	sb.append(td.start.getLine()).append(":\n").
	    append(td.parseData.getSyntaxPoints().getLineText(td.start.getLine())).
	    append('\n');
	for(int i=1; i<td.start.getColumn(); i++)
	    sb.append(' ');
	sb.append('^');
    }
    
  public static void fill(Tag t, Map attributes)
  {
    Class c= t.getClass();
    Class[] argTypes= {String.class};
    Object[] args= new Object[1];

    for(Iterator i= attributes.entrySet().iterator(); i.hasNext(); )
      {
	Map.Entry me= (Map.Entry)i.next();
	String s= (String)me.getKey();
	String methodName="set"+Character.toUpperCase(s.charAt(0))+s.substring(1);
	try{
	  Method m= c.getMethod(methodName, argTypes);
	  args[0]= me.getValue();
	  m.invoke(t, args);
	}
	catch(java.lang.reflect.InvocationTargetException ite){
	  System.out.println("error invoking method "+methodName +" on object of class "+c.getName()+" with argument "+args[0]);
	  throw new RuntimeWrappedException(ite.getTargetException());
	}
	catch(Throwable thr){ 
	  System.out.println("error invoking method "+methodName +" on object of class "+c.getName()+" with argument "+args[0]);
	  throw new RuntimeWrappedException(thr);
	}			 
      }
  }
}


