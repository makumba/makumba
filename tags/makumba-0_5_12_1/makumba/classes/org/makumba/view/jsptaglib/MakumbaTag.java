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

package org.makumba.view.jsptaglib;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.LogicException;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.util.JspParseData;
import org.makumba.util.MultipleKey;


/** this class provides utility methods for all makumba tags 
 * exception handling
 * page cache retrieval
 * storage of formatting parameters
 * database name setting/getting
 * cleanup
 * links to the JspParseData$TagData, representing the tag as parsed
 */
public abstract class MakumbaTag extends TagSupport 
{
  // set by the tag parser at analysis time. 
  // this is not yet guaranteed to be set at runtime!
  JspParseData.TagData tagData;

  /** A tag key, used to find cached resources. Computed by some tags, both at analysis and at runtime */
  MultipleKey tagKey;
  
  /** the cache containing page analysis data */
  MakumbaJspAnalyzer.PageCache pageCache; 

  /** Tag parameters */
  Hashtable params = new Hashtable(7);        // holds certain 'normal' tag attributes
  Map extraFormattingParams = new HashMap(7); // container for html formatting params

  /** Extra html formatting, copied verbatim to the output */
  StringBuffer extraFormatting;

  static final String DB_ATTR="org.makumba.database";

  /** used at analysis to dump the tag line */
  public void addTagText(StringBuffer sb)
  {
    JspParseData.tagDataLine(tagData, sb);
  }
  
  public String getTagText()
  {
    StringBuffer sb= new StringBuffer();
    addTagText(sb);
    return sb.toString();
  }

  PageContext getPageContext(){ return pageContext; }

  // we put this as static, as we may have to export it to packages like org.makumba.controller.jsp
  public static MakumbaJspAnalyzer.PageCache getPageCache(PageContext pageContext)
  {
    MakumbaJspAnalyzer.PageCache pageCache= (MakumbaJspAnalyzer.PageCache)pageContext.getAttribute("makumba.parse.cache");
    if(pageCache==null)
      {
	Object result= JspParseData.getParseData
	  (
	   pageContext.getServletConfig().getServletContext().getRealPath("/"),
	   TomcatJsp.getJspURI((HttpServletRequest)pageContext.getRequest()),
	   MakumbaJspAnalyzer.singleton
	    ).getAnalysisResult(null); 

	if((result instanceof Throwable)&& result.getClass().getName().startsWith("org.makumba"))
	  {
	    if(result instanceof MakumbaError)
	      throw (MakumbaError)result;
	    throw (RuntimeException)result;
	  }
	pageContext.setAttribute("makumba.parse.cache", 
				 pageCache=(MakumbaJspAnalyzer.PageCache)result);
      }
    return pageCache;
  }
  

  public QueryTag getParentList(){return (QueryTag)findAncestorWithClass(this, QueryTag.class); }

  
  public MultipleKey getParentListKey()
  {
    QueryTag parentList= getParentList();
    return parentList==null?null:parentList.tagKey;
  }

  public void addToParentListKey(Object o)
  {
    QueryTag parentList= getParentList();
    if(parentList== null)
      throw new org.makumba.ProgrammerError("VALUE tags, INPUT or FORM tags that compute a value should always be enclosed in a LIST or OBJECT tag: \n"+getTagText());
    tagKey= new MultipleKey(parentList.tagKey, o);
  }

  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey() {}

  /** can this tag have the same key as others in the page? */
  public boolean allowsIdenticalKey() { return true; }

  /** Start the analysis of the tag, without knowing what tags follow it in the page. Typically this method will allocate initial data structures, that are then completed at doEndAnalyze() */
  public void doStartAnalyze(){}

  /** End the analysis of the tag, after all tags in the page were visited. */
  public void doEndAnalyze(){}

  /** makumba-specific start tag. 
   * @see #doStartTag()
    */
  public int doMakumbaStartTag() throws LogicException, JspException
  {
    return SKIP_BODY;
  }

  /** does this tag need the page cache? */
  protected boolean needPageCache(){ return true; }

  /** Handle exceptions, initialise state and call doMakumbaStartTag() */
  public int doStartTag() throws JspException
  {
    // need to check if this is still needed, it was here only if the tag was root...
    if(pageContext.getAttribute(pageContext.EXCEPTION, pageContext.REQUEST_SCOPE)!=null)
      setWasException();
    if(wasException())
      return SKIP_PAGE;
    try{
      if(needPageCache())
	pageCache=getPageCache(pageContext);
      setTagKey();
      initialiseState();
      return doMakumbaStartTag();
    }
    catch(Throwable t){ treatException(t); return SKIP_PAGE; }
  }

  /** Reset and initialise the tag's state, to work in a tag pool. See bug 583. 
   *  If method is overriden in child class, the child's method must call super.resetState(). 
   */
  public void initialiseState() {
      extraFormatting= new StringBuffer();

      for (Iterator it = extraFormattingParams.entrySet().iterator(); it.hasNext();  ) {
          Map.Entry me = (Map.Entry)it.next();
          extraFormatting.append(" ").append(me.getKey()).append("=\"").append(me.getValue()).append("\" ");
      }
  }
   

  /** makumba-specific endTag. 
   * @see #doEndTag() 
    */
  public int doMakumbaEndTag() throws LogicException, JspException
  {
    return EVAL_PAGE;
  }

  /** HandleExceptions and call doMakumbaEndTag() */
  public int doEndTag() throws JspException
  {
    try{
      if(wasException())
	return SKIP_PAGE;
      return doMakumbaEndTag();
    } catch(Throwable t){ treatException(t); return SKIP_PAGE; }
    finally{ 
      pageCache=null; 
      tagKey=null;
      params.clear();
      extraFormattingParams.clear();
      extraFormatting= null;
      if(findAncestorWithClass(this, MakumbaTag.class)==null)
	pageContext.removeAttribute(DB_ATTR);
    }
  }

  //-------------- database name 
  /** obtain the makumba database; this can be more complex (accept arguments, etc) */
  public String getDatabaseName() {return getDatabaseName(pageContext); }

  public static String getDatabaseName(PageContext pc) 
  {
    String db= (String)pc.getAttribute(DB_ATTR);
    if(db==null)
      return MakumbaSystem.getDefaultDatabaseName();
    return db;
  }

  /** throw an exception if this is not the root tag */
  protected void onlyRootArgument(String s) 
  {
    if(findAncestorWithClass(this, MakumbaTag.class)!=null)
      treatException(new MakumbaJspException
		     (this, "the "+s+" argument cannot be set for non-root makumba tags"));   
  }

  /** set the database argument */
  public void setDb(String db) throws JspException
  {
    onlyRootArgument("db");
    if(pageContext!=null)
      pageContext.setAttribute(DB_ATTR, db);
  }

  // --------------------------------
  // -------------- exceptions
  public boolean wasException()
  {
    return org.makumba.controller.http.ControllerFilter.wasException
      ((HttpServletRequest)pageContext.getRequest());
  }

  public void setWasException()
  {
    org.makumba.controller.http.ControllerFilter.setWasException
      ((HttpServletRequest)pageContext.getRequest());
  }

  protected void treatException(Throwable t) 
  {
    org.makumba.controller.http.ControllerFilter.treatException
      (t,
       (HttpServletRequest)pageContext.getRequest(),
       (HttpServletResponse)pageContext.getResponse());
  }

  //--------- html formatting, copied verbatim to the output
  public void setStyleId(String s)    { extraFormattingParams.put("id", s); }
  public void setStyleClass(String s) { extraFormattingParams.put("class", s); }
  public void setStyle(String s)      { extraFormattingParams.put("style", s); }
  public void setTitle(String s)      { extraFormattingParams.put("title", s); } 
  public void setOnClick(String s)    { extraFormattingParams.put("onClick", s); } 
  public void setOnDblClick(String s) { extraFormattingParams.put("onDblClick", s); }
  public void setOnKeyDown(String s)  { extraFormattingParams.put("onKeyDown", s); }
  public void setOnKeyUp(String s)    { extraFormattingParams.put("onKeyUp", s); } 
  public void setOnKeyPress(String s) { extraFormattingParams.put("onKeyPress", s); } 
  public void setOnMouseDown(String s) { extraFormattingParams.put("onMouseDown", s); }
  public void setOnMouseUp(String s)   { extraFormattingParams.put("onMouseUp", s); } 
  public void setOnMouseMove(String s) { extraFormattingParams.put("onMouseMove", s); }
  public void setOnMouseOut(String s)  { extraFormattingParams.put("onMouseOut", s); } 
  public void setOnMouseOver(String s) { extraFormattingParams.put("onMouseOver", s); } 

  //--------- formatting properties, determine formatter behavior
  public void setUrlEncode(String s) { params.put("urlEncode", s); }
  public void setHtml(String s) { params.put("html", s); }
  public void setFormat(String s){ params.put("format", s); }
  public void setType(String s) { params.put("type", s); }
  public void setSize(String s) { params.put("size", s); }
  public void setMaxlength(String s) { params.put("maxlength", s); }
  public void setMaxLength(String s) { params.put("maxLength", s); }
  public void setEllipsis(String s) { params.put("ellipsis", s); }
  public void setEllipsisLength(String s) { params.put("ellipsisLength", s); }
  public void setAddTitle(String s) { params.put("addTitle", s); }
  public void setRows(String s) { params.put("rows", s); }
  public void setCols(String s) { params.put("cols", s); }
  public void setLineSeparator(String s) { params.put("lineSeparator", s); }
  public void setLongLineLength(String s) {  params.put("longLineLength", s); }
  public void setDefault(String s) {  params.put("default", s); }
  public void setEmpty(String s) {  params.put("empty", s); }
  public void setLabelSeparator(String s) {  params.put("labelSeparator", s); }
  public void setElementSeparator(String s) {  params.put("elementSeparator", s); }
  
  public String toString(){ return getClass().getName()+" "+params; }
}