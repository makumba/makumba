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
import org.makumba.view.*;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import org.makumba.*;
import org.makumba.controller.jsp.PageAttributes;
import java.util.Hashtable;

/** this class provides utility methods for all makumba tags */
public abstract class MakumbaTag extends TagSupport implements TagStrategy
{
  Hashtable params= new Hashtable(7);

  public void cleanState(){ params.clear(); strategy=null; } 

  static final String ROOT_DATA_NAME="makumba.root.data";

  TagStrategy strategy;

  public PageContext getPageContext() {return pageContext; }

  protected RootData created;
  protected RootData pushed;

  /** identifies the closest parent of the class desired by getParentClass() */
  public void setParent(Tag t)
  { 
    super.setParent(t);
    if(getMakumbaParent()==null)
      {
	// there can be more than one root tag in a tag structure. only the 
	// "rootest" should create root data
	pushed=getRootData();
	
	pageContext.setAttribute(ROOT_DATA_NAME, created= new RootData(this, pageContext), PageContext.PAGE_SCOPE);
	canBeRoot();
      }
  }

  /** return true if this tag can be root tag, else throw an exception */
  protected abstract boolean canBeRoot();

  /** what kind of makumba tag should the parent tag be? */
  protected abstract Class getParentClass();

  /** get the makumba parent, of the desired class */
  protected MakumbaTag getMakumbaParent() 
  { return (MakumbaTag)findAncestorWithClass(this, getParentClass()); }

  static public RootData getRootData(PageContext pc)
  {
    return (RootData)pc.getAttribute(ROOT_DATA_NAME, PageContext.PAGE_SCOPE);
  }

  /** get the root data */
  protected RootData getRootData() 
  { 
    return getRootData(pageContext);
  }

  /** make a strategy, returns this object by default */
  public TagStrategy makeNonRootStrategy(Object key) { return this; }

  /** factory method, make a root strategy if this tag is root */
  protected RootTagStrategy makeRootStrategy(Object key) { throw new MakumbaError("must be redefined"); }

  public TagStrategy makeStrategy(Object key)
  {
    if(getMakumbaParent()==null)
      return makeRootStrategy(key);
    else
      return makeNonRootStrategy(key);
  }

  /** return a key for registration in root. If null is returned, registration in root is not made */
  public Object getRootRegistrationKey() throws LogicException { return null; }

  /** return a key for registration in root. If null is returned, registration in root is not made */
  public Object getRegistrationKey() throws LogicException { return null; }

  /** get the closest enclosing query, or null if nothing */
  public QueryStrategy getEnclosingQuery()
  {
    QueryTag qt= (QueryTag)findAncestorWithClass(this, QueryTag.class);
    if(qt==null)
      return null;
    return ((QueryTagStrategy)qt.strategy).getQueryStrategy();
  }

  /** only works for tags that have query parents */
  public QueryStrategy getParentQueryStrategy() 
  {
    return ((QueryTagStrategy)getMakumbaParent().strategy).getQueryStrategy();
  }

  public PageAttributes getAttributes()
  {
    try{
      return PageAttributes.getAttributes(pageContext);
    }catch(Throwable e){ throw new MakumbaError("should not have error here "+e); } 
  }

  /** find the strategy, then delegate it to execute the preamble */
  public int doStartTag() throws JspException
  {
    try{
      Object key=null;
      if(getMakumbaParent()==null)
	key= getRootRegistrationKey();
      else
	key= getRegistrationKey();
    
      if(key==null)
	strategy=makeStrategy(null);
      else
	getRootData().setStrategy(key, this);
      strategy.setPage(pageContext);
      return strategy.doStart();
    }
    catch(NewProjectionException e){ throw e; }
    catch(Throwable t){ treatException(t); return SKIP_BODY; }
  }

  /** delegate the strategy to end */
  public int doEndTag() throws JspException
  {
    try{
      if(wasException())
	return SKIP_PAGE;
      return strategy.doEnd();
    }    finally
      {
	if(created!=null)
	  {
	    getRootData().close();
	    if(pushed!=null)
	      pageContext.setAttribute(ROOT_DATA_NAME, pushed, PageContext.PAGE_SCOPE);
	    else
	      pageContext.removeAttribute(ROOT_DATA_NAME, PageContext.PAGE_SCOPE);
	  }
      }
  }

  public void release(){ cleanState(); if(strategy!=null)strategy.doRelease(); }

  /** obtain the makumba database; this can be more complex (accept arguments, etc) */
  public String getDatabaseName() {return getDatabaseName(pageContext); }

  public static String getDatabaseName(PageContext pc) 
  {
    if(getRootData(pc).db==null)
      return MakumbaSystem.getDefaultDatabaseName();
    return getRootData(pc).db;
  }


  //--- dummy implementation of TagStrategy
  public void init(MakumbaTag root, MakumbaTag tag, Object key){}
  public void setBody(BodyContent bc){}
  public void setPage(PageContext pc){}
  public BodyContent getBody(){ return null; }
  public void loop(){}
  public int doStart() throws JspException, LogicException { return Tag.SKIP_BODY; }
  public int doAfter() throws JspException { return Tag.SKIP_BODY; }
  public int doEnd() throws JspException{ return Tag.EVAL_PAGE; } 
  public void doRelease() {}
  public void rootClose() {}

  
  // ------------------------   
  // ------------  only root arguments
  // ----------------------
  /** throw an exception if this is not the root tag */
  protected void onlyRootArgument(String s) 
  {
    if(getMakumbaParent()!=null)
      treatException(new MakumbaJspException
		     (this, "the "+s+" argument cannot be set for non-root makumba tags"));   
  }

  /** set the database argument */
  public void setDb(String db) throws JspException
  {
    onlyRootArgument("db");
    getRootData().db=db;
  }

  public void setHeader(String s) throws JspException  
  { 
    onlyRootArgument("header"); 
    getRootData().header=s;
  }

  public void setFooter(String s) throws JspException  
  {
    onlyRootArgument("footer"); 
    getRootData().footer=s;
  }
  
  /** root tag properties */
  static final String ROOT_PREFIX="makumba.root.prefix.";

  public Object getRootProperty(String s)
  {
    return pageContext.getAttribute(ROOT_PREFIX+s, pageContext.REQUEST_SCOPE);
  }


  public void setRootProperty(String s, Object o)
  {
    pageContext.setAttribute(ROOT_PREFIX+s, o, pageContext.REQUEST_SCOPE);
  }

  public void removeRootProperty(String s)
  {
    pageContext.removeAttribute(ROOT_PREFIX+s, pageContext.REQUEST_SCOPE);
  }

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
}
