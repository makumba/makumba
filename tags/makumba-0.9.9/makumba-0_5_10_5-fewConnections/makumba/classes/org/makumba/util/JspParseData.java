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
import java.util.*;
import java.util.regex.*;

import java.io.File;
import java.lang.reflect.Method;
import javax.servlet.jsp.tagext.Tag;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.makumba.MakumbaSystem;


/** This class performs a rudimentary detection of JSP-relevant tags in a JSP page.
 * @author cristi
 */
public class JspParseData implements SourceSyntaxPoints.PreprocessorClient
{
  /** The JSP file path */
  File file;

  /** The analyzer plugged in. */
  JspAnalyzer analyzer;

  /** The syntax points of this page. */
  SourceSyntaxPoints syntaxPoints;

  /** The holder of the analysis status, and partial results. */
  Object holder;

  /** The JSP URI, for debugging purposes. */
  String uri;

  /** The patterns used to parse the page. */
  static private Pattern JspSystemTagPattern, JspTagPattern, JspCommentPattern, JspScriptletPattern, JspIncludePattern, JspTagAttributePattern, Word, TagName;

  static private String[] JspCommentPatternNames={"JspComment", "JspScriptlet"};
  static private Pattern[] JspCommentPatterns;

  /** Cache of all analyzed pages. */
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


  static String attribute(String attName){
    return 
      "(" +attribute(attName, "\"")+ "|" +attribute(attName,"\'")+ ")";
  }

  /** This helps to create regex for the 'attribute' pattern easily. @param quote is either " or ' . */
  static String attribute(String attName, String quote){
    String bs="\\";         // backslash in a java String (escaped)
    String q=bs+quote;
    String backslash=bs+bs; // backslash in a regex in a java String (escaped)

    // BAD TRY: pattern is (?s)\s*\w+\s*=\s*"(.*?[^\\])??" or idem with single quote ' 
    // the pattern is \s*\w+\s*=\s*"(\\.|[^"\\])*?" or idem with single quote ' 
    return 
      bs+ "s*" +
      attName+
      bs+ "s*=" +
      bs+ "s*" +
      q+
      "(" +backslash+ ".|[^" +q+backslash+ "])*?" +
      q;
  }

  /** Initialiser for the class variables. */ 
  static{
    String attribute= attribute("\\w+");

    try{
      JspTagAttributePattern= Pattern.compile(attribute);
      JspSystemTagPattern= Pattern.compile("<%@\\s*\\w+("+attribute+")*\\s*%>");
      JspIncludePattern= Pattern.compile("<%@\\s*include"+attribute("file")+"\\s*%>");
      JspTagPattern= Pattern.compile("<((\\s*\\w+:\\w+("+attribute+")*\\s*)/?|(/\\w+:\\w+\\s*))>");
      //JspCommentPattern= Pattern.compile("<%--([^-]|(-[^-])|(--[^%])|(--%[^>]))*--%>", Pattern.DOTALL);
      JspCommentPattern=  Pattern.compile("<%--.*?[^-]--%>", Pattern.DOTALL);
      JspScriptletPattern= Pattern.compile("<%[^@].*?%>", Pattern.DOTALL); 
      Pattern[] cp= { JspCommentPattern, JspScriptletPattern };
      JspCommentPatterns= cp;
      Word= Pattern.compile("\\w+");
      TagName= Pattern.compile("\\w+:\\w+");
    }catch(Throwable t){ t.printStackTrace(); }
  }



  /** This method will perform the analysis if not performed already, or if the file has changed.
   * the method is synchronized, so other accesses are blocked if the current access determines that an analysis needs be performed 
   * @param initStatus an initial status to be passed to the JspAnalyzer. for example, the pageContext for an example-based analyzer
   */
  public synchronized Object getAnalysisResult(Object initStatus)
  {
    if(getSyntaxPoints()==null || !getSyntaxPoints().unchanged())
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


  /** 
   * Return the pageData of the page at the given path in the given webapp.
   * This is the only way for clients of this class to obtain instances 
   * of JspPageData
   */
  static public JspParseData getParseData(String webappRoot, String path, JspAnalyzer an)
  {
    Object arg[]= {webappRoot+path, an, path };
    return (JspParseData)NamedResources.getStaticCache(analyzedPages).getResource(arg);
  }


  /** Gets the collection of syntax points. */
  public SourceSyntaxPoints getSyntaxPoints(){ return syntaxPoints; }


  /** Private constructor, construction can only be made by getParseData(). */
  private JspParseData(String path, JspAnalyzer an, String uri)
  {
    this.file= new File(path);
    this.uri=uri;
    this.analyzer=an;
  }

  /** Parses the file. */
  void parse(Object initStatus)
  {
    long start= new java.util.Date().getTime();
    syntaxPoints= new SourceSyntaxPoints(file, this);

    holder= analyzer.makeStatusHolder(initStatus);

    // the page analysis as such:
    treatTags(syntaxPoints.getContent(), analyzer);
    
    holder= analyzer.endPage(holder);

    org.makumba.MakumbaSystem.getMakumbaLogger("jspparser.time").info
      ("analysis of "+uri+" took "+(new java.util.Date().getTime()-start)+" ms");
  }


  /** Identify tag attributes from a tag string and put them in a Map. Sets the attribute syntax points. */
  Map parseAttributes(String s, int origin)
  {
    Map attributes = new HashMap(13);
    // System.out.println("tag = " + s); //debugging
    
    Matcher m= JspTagAttributePattern.matcher(s);
    while(m.find())
      {
	// here we have an attributeName="attributeValue"
	String attr = s.substring(m.start(), m.end());
	int n = attr.indexOf('='); // position of the equal sign
	String attName      = attr.substring(0, n).trim();
	String attValQuoted = attr.substring(n+1 ).trim(); // the part after the equal sign.
	char chQuote=attValQuoted.charAt(0);
	
	// the following assertion must be ensured by the attributePattern matching
	if (attValQuoted.charAt(0) != attValQuoted.charAt(attValQuoted.length()-1))
	  throw new RuntimeException("Properly quoted string expected, found "+attValQuoted);
	
	// unescape the "escaped quotes" in the attributeValue
	if (chQuote == '\"'){
	  attValQuoted = attValQuoted.replaceAll("\\\\\"", "\""); // replace \" by "
	} else if (chQuote == '\''){
	  attValQuoted = attValQuoted.replaceAll("\\\\\'", "\'"); // replace \' by '
	}
	attValQuoted = attValQuoted.replaceAll("(\\\\){2}", "\\\\"); // replace \\ by \
	  
	String attValue = attValQuoted.substring(1, attValQuoted.length()-1); 
	attributes.put(attName, attValue);

	if(origin!=-1){ 
	// syntax points, only set if the origin is given
	  syntaxPoints.addSyntaxPoints(origin  , origin+n  , "JSPTagAttributeName"  , null);
	  syntaxPoints.addSyntaxPoints(origin+n, origin+n+1, "JSPTagAttributeEquals", null);
	  syntaxPoints.addSyntaxPoints(origin+n+1, origin+s.length(), "JSPTagAttributeValue", null);
	}
         //debug
         Logger log= MakumbaSystem.getMakumbaLogger("jspparser.tags.attribute");
         log.finest("< Attribute : " +attr);
         log.finest("> AttrParse : " +attName+ " = " +attValue);
      }
    return attributes;
  }

  public void treatInclude(int position, String includeDirective, SourceSyntaxPoints host){
    Map m= parseAttributes(includeDirective, -1);
    String fileName= (String)m.get("file");
    host.include(new File(host.file.getParent(), fileName), position, includeDirective);
  }

  public Pattern[] getCommentPatterns(){
    return JspCommentPatterns;
  }
  public String[] getCommentPatternNames(){
    return JspCommentPatternNames;
  }
  public Pattern getIncludePattern(){
    return JspIncludePattern;
  }
  public String getIncludePatternName(){
    return "JspInclude";
  }

  /** Go thru the tags in the page. */
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


  /** Treat a jsp or taglib tag: parse its different parts and store the analysis. */
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


  /** Treat a system tag: parse its different parts and store the analysis. */
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

  
  /** FIXME: This seems to build some nice illustration for a parse-error message */
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


  // ==========================================================================================
  //
  // THIS CLASS ALSO HAS AN INNER INTERFACE, AND INNER CLASS DEFINITION:
  // 
  // ==========================================================================================


  /** The interface of a JSP analyzer. */
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


  /** A composite object passed to the analyzers. */
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


} // end class

