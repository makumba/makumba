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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.IterationTag;

import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.util.MultipleKey;
import org.makumba.view.ComposedQuery;
import org.makumba.view.html.RecordViewer;

/** Display of OQL query results in nested loops. The Query FROM, WHERE, GROUPBY and ORDERBY are indicated in the head of the tag. The query projections are indicated by Value tags in the body of the tag. The sub-tags will generate subqueries of their enclosing tag queries (i.e. their WHERE, GROUPBY and ORDERBY are concatenated). Attributes of the environment can be passed as $attrName to the query 
 * 
 */
public class QueryTag extends MakumbaTag implements IterationTag
{
  String[] queryProps=new String[4];
  String separator="";
  String countVar;
  String maxCountVar;
  String offset, limit;
  
  static String standardCountVar="org_makumba_view_jsptaglib_countVar";
  static String standardMaxCountVar="org_makumba_view_jsptaglib_maxCountVar";
  static String standardLastCountVar="org_makumba_view_jsptaglib_lastCountVar";

  public void setFrom(String s) { queryProps[ComposedQuery.FROM]=s; }
  public void setWhere(String s){ queryProps[ComposedQuery.WHERE]=s; }
  public void setOrderBy(String s){ queryProps[ComposedQuery.ORDERBY]=s; }
  public void setGroupBy(String s){ queryProps[ComposedQuery.GROUPBY]=s; }
  public void setSeparator(String s){ separator=s; }
  public void setCountVar(String s){ countVar=s; }
  public void setMaxCountVar(String s){ maxCountVar=s; }
  public void setOffset(String s) throws JspException
  { onlyOuterListArgument("offset"); onlyInt("offset", s); offset=s.trim(); }
  public void setLimit(String s) throws JspException
  { onlyOuterListArgument("limit"); onlyInt("limit", s); limit=s.trim(); }

  protected void onlyOuterListArgument(String s) throws JspException
  {
    QueryTag t= (QueryTag)findAncestorWithClass(this, QueryTag.class);
    while(t!=null && t instanceof ObjectTag)
      t= (QueryTag)findAncestorWithClass(t, QueryTag.class);
    if(t instanceof QueryTag)
      treatException(new MakumbaJspException
		     (this, "the "+s+" parameter can only be set for the outermost mak:list tag"));   
  }

  protected void onlyInt(String s, String value) throws JspException{
    value=value.trim();
    if(value.startsWith("$"))
      return;
    try{
      Integer.parseInt(value);
    }catch(NumberFormatException nfe){
      treatException(new MakumbaJspException
		     (this, "the "+s+" parameter can only be an $attribute or an int"));   
      
    }
  }

  // runtime stuff
  QueryExecution execution;

  /** Compute and set the tagKey. At analisys time, the listQuery is associated with the tagKey, and retrieved at runtime. At runtime, the QueryExecution is discovered by the tag based on the tagKey */
  public void setTagKey(MakumbaJspAnalyzer.PageCache pageCache)
  {
    tagKey= new MultipleKey(queryProps.length+2);
    for(int i=0; i<queryProps.length; i++)
      tagKey.setAt(queryProps[i], i);

    // if we have a parent, we append the key of the parent
    tagKey.setAt(getParentListKey(null), queryProps.length);
    tagKey.setAt(id, queryProps.length+1);
  }

  /** can this tag have the same key as others in the page? */
  public boolean allowsIdenticalKey() { return false; }

  /** Start the analysis of the tag, without knowing what tags follow it in the page. 
    Define a query, set the types of variables to "int" */
  public void doStartAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    // we make ComposedQuery cache our query
    pageCache.cacheQuery(tagKey, queryProps, getParentListKey(pageCache));

    if(countVar!=null)
      pageCache.types.setType(countVar, MakumbaSystem.makeFieldOfType(countVar, "int"), this);

    if(maxCountVar!=null)
      pageCache.types.setType(maxCountVar, MakumbaSystem.makeFieldOfType(maxCountVar, "int"), this);
  }

  /** End the analysis of the tag, after all tags in the page were visited. 
    Now that we know all query projections, cache a RecordViewer as formatter for the mak:values nested in this tag */
  public void doEndAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    ComposedQuery cq= pageCache.getQuery(tagKey);
    cq.analyze();
    pageCache.formatters.put(tagKey, new RecordViewer(cq));
  }

  static final Integer zero= new Integer(0);
  static final Integer one= new Integer(1);

  Object upperCount=null;
  Object upperMaxCount=null;

  ValueComputer choiceComputer;

  /** Decide if there will be any tag iteration. The QueryExecution is found (and made if needed), and we check if there are any results in this iterationGroup */
  public int doMakumbaStartTag(MakumbaJspAnalyzer.PageCache pageCache) 
       throws LogicException, JspException
  {
    if(getParentList()==null)
      QueryExecution.startListGroup(pageContext);
    else {
      upperCount= pageContext.getRequest().getAttribute(standardCountVar);
      upperMaxCount= pageContext.getRequest().getAttribute(standardMaxCountVar);
    }

    execution= QueryExecution.getFor(tagKey, pageContext, offset, limit);

    int n= execution.onParentIteration();

    setNumberOfIterations(n);

    if(n>0)
      {
	if(countVar!=null)
	  pageContext.setAttribute(countVar, one);
	pageContext.getRequest().setAttribute(standardCountVar, one);
	return EVAL_BODY_INCLUDE;
      }
    if(countVar!=null)
      pageContext.setAttribute(countVar, zero);
    pageContext.getRequest().setAttribute(standardCountVar, zero);
    return SKIP_BODY;
  }

  /** Set the number of iterations in this iterationGroup. ObjectTag will redefine this and throw an exception if n>1 */
  protected void setNumberOfIterations(int n) throws JspException
  {
    Integer cnt= new Integer(n);
    if(maxCountVar!=null)
      pageContext.setAttribute(maxCountVar, cnt);
    pageContext.getRequest().setAttribute(standardMaxCountVar, cnt);
  }


  /** Decide if we do further iterations. Checks if we got to the end of the iterationGroup. */
  public int doAfterBody() throws JspException
  {
    runningTag.set(tagData);
    try{

    int n= execution.nextGroupIteration();

    if(n!=-1)
      {
	// print the separator
	try{
	  pageContext.getOut().print(separator);
	}catch(Exception e){ throw new MakumbaJspException(e); }

	Integer cnt= new Integer(n+1);
	if(countVar!=null)
	  pageContext.setAttribute(countVar, cnt);
	pageContext.getRequest().setAttribute(standardCountVar, cnt);
	return EVAL_BODY_AGAIN;
      }
    return SKIP_BODY;
    }finally{ runningTag.set(null); }
  }

  /** Cleanup operations, especially for the rootList */
  public int doMakumbaEndTag(MakumbaJspAnalyzer.PageCache pageCache) 
       throws JspException
  {
    pageContext.getRequest().setAttribute
      (standardLastCountVar, 
       pageContext.getRequest().getAttribute(standardMaxCountVar));
					  
    pageContext.getRequest().setAttribute(standardCountVar, upperCount);
    pageContext.getRequest().setAttribute(standardMaxCountVar, upperMaxCount);
    execution.endIterationGroup();

    if(getParentList()==null)
      execution.endListGroup(pageContext);

    execution=null;
    queryProps[0]=queryProps[1]=queryProps[2]=queryProps[3]=null;
    countVar= maxCountVar= null;
    separator="";
    return EVAL_PAGE;
  }

  public static int count()
  {
    return ((Integer)org.makumba.controller.http.ControllerFilter.getRequest().getAttribute(standardCountVar)).intValue();
  }

  public static int maxCount()
  {
    return ((Integer)org.makumba.controller.http.ControllerFilter.getRequest().getAttribute(standardMaxCountVar)).intValue();
  }

  public static int lastCount()
  {
    return ((Integer)org.makumba.controller.http.ControllerFilter.getRequest().getAttribute(standardLastCountVar)).intValue();
  }
}

