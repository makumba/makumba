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
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import org.makumba.controller.jsp.PageAttributes;
import org.makumba.util.JspParseData;
import java.util.HashMap;


/** this class provides utility methods for all makumba tags 
 * exception handling
 * parameter storage
 * database name setting/getting
 * cleanup
 * links to the JspParseData$TagData, representing the tag as parsed
 */
public abstract class MakumbaTag extends TagSupport 
{
  // set by the tag parser at analysis time. 
  // this is not yet guaranteed to be set at runtime!
  JspParseData.TagData tagData;

  HashMap params= new HashMap(7);
  static final String DB_ATTR="org.makumba.database";

  public void cleanState()
  { 
    if(findAncestorWithClass(this, MakumbaTag.class)==null)
      pageContext.removeAttribute(DB_ATTR);
    tagData=null;
    params.clear(); 
  } 

  public PageContext getPageContext() {return pageContext; }

  // this looks reductible to PageContext.getAttributes(getPageContext())
  public PageAttributes getAttributes()
  {
    try{
      return PageAttributes.getAttributes(pageContext);
    }catch(Throwable e){ throw new MakumbaError("should not have error here "+e); } 
  }

  // we put this as static, as we may have to export it to packages like org.makumba.controller.jsp
  public static MakumbaJspAnalyzer.PageCache getPageCache(PageContext pc)
  {
    MakumbaJspAnalyzer.PageCache pageCache= (Map)pc.getAttribute("makumba.parse.cache");
    if(pageCache==null)
      {
	JspParseData jpd= JspParseData.getParseData
	  (
	   pageContext.getServletConfig().getServletContext().getRealPath("/"),
	   TomcatJsp.getJspURI((HttpServletRequest)pageContext.getRequest()),
	   MakumbaJspAnalyzer.singleton
	    ); 
	Object result=jpd.getAnalysisResult(pageContext);
	if(result instanceof MakumbaError)
	  throw (MakumbaError)result;
	
	pc.setAttribute("makumba.parse.cache", 
			pageCache=(MakumbaJspAnalyzer.PageCache)result);
      }
    return pageCache;
  }

  publi int doMakumbaStartTag() throws LogicException
  {
    return SKIP_BODY;
  }

  /** exception handling */
  public int doStartTag() throws JspException
  {
    // need to check if this is still needed, it was here only if the tag was root...
    if(pageContext.getAttribute(pageContext.EXCEPTION, pageContext.REQUEST_SCOPE)!=null)
      setWasException();
    if(wasException())
      return SKIP_PAGE;
    try{
      return doMakumbaStartTag();
    }
    catch(Throwable t){ treatException(t); return SKIP_BODY; }
  }

  publi int doMakumbaEndTag() throws LogicException
  {
    return EVAL_PAGE;
  }

  /** delegate the strategy to end */
  public int doEndTag() throws JspException
  {
    if(wasException())
      return SKIP_PAGE;
    return doMakumbaEndTag();
  }

  //------------------------------------------------
  //-------------- database name 
  /** obtain the makumba database; this can be more complex (accept arguments, etc) */
  public String getDatabaseName() {return getDatabaseName(pageContext); }

  public String getDatabaseName(PageContext pc) 
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
    pageContext.setAttribute(DB_ATTR, db);
  }

  public void setHeader(String s) throws JspException  
  { 
    onlyRootArgument("header"); 
    params.put("header", s);
  }

  public void setFooter(String s) throws JspException  
  {
    onlyRootArgument("footer"); 
    params.put("footer", s);
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

  //--------- various properties
  public void setUrlEncode(String s) 
  { 
    params.put("urlEncode", s); 
  }

  public void setHtml(String s) 
  { 
    params.put("html", s); 
  }
  
  public void setFormat(String s) 
  { 
    params.put("format", s); 
  }

  public void setType(String s) 
  { 
    params.put("type", s); 
  }

  public void setSize(String s) 
  { 
    params.put("size", s); 
  }

  public void setMaxlength(String s) 
  { 
    params.put("maxlength", s); 
  }

  public void setRows(String s) 
  { 
    params.put("rows", s); 
  }

  public void setCols(String s) 
  { 
    params.put("cols", s); 
  }

  public void setLineSeparator(String s) 
  { 
    params.put("lineSeparator", s); 
  }

  public void setLongLineLength(String s) 
  { 
    params.put("longLineLength", s); 
  }
  
  public String toString(){ return getClass().getName()+" "+params; }
}
