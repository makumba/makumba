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
import org.makumba.util.*;
import org.makumba.*;
import org.makumba.view.html.RecordViewer;
import org.makumba.controller.jsp.PageAttributes;

import javax.servlet.jsp.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.*;
import java.util.*;
import java.io.*;

public class QueryStrategy extends TagStrategySupport 
implements Observer, QueryTagStrategy
{
  public static final Integer zero= new Integer(0);

  public QueryStrategy getQueryStrategy(){return this; }
  public RootQueryStrategy getRoot() { return (RootQueryStrategy)root; }

  public QueryTag getQueryTag(){ return (QueryTag)tag; }

  public Object getKey(){return key; }

  // only init, no key transformation
  public void init(MakumbaTag root, MakumbaTag tag, Object key)
  {
    super.init(root, tag, key);
    adjustQueryProps();
  }

  protected void adjustQueryProps(){  }

  boolean startedWithData=false;
  
  public ComposedQuery getQuery(){return query; }
  
  public void doAnalyze() 
  { 
    setQuery(ComposedQuery.getQuery(key, getQueryTag().queryProps, tag.getEnclosingQuery().getQuery()));
    getRoot().addQuery(this);
  }

  /** See whether we execute the body or not. If we have a query, it depends on it, if not, 
   * we simulate execution, to see what are the projections that we need */
  public int doStart() throws JspException 
  {
    initCountVars();
    if(query==null)
      throw new RuntimeException("query should be known");

    if(!knowsOfAnyProjection())
      throw new RuntimeException("empty query?");
 
    int start= startLooping();
    if(tag.wasException())
      return BodyTag.SKIP_BODY;
    return start;
  }

  public void doRootAnalyze() 
  { 
    setQuery(ComposedQuery.getQuery(getKey(), getQueryTag().queryProps, null));
    getRoot().addQuery(this);
  }

  public int doRootStart() throws JspException
  {
    initCountVars();
    if(query==null)
      throw new RuntimeException("query should be known");
    if(!knowsOfAnyProjection())
      throw new RuntimeException("Empty query?");
    
    getRoot().doQueries(false);
    
    int start= startLooping();
    if(tag.wasException())
      return BodyTag.SKIP_BODY;
    return start;
  }

  protected void pushData()
  {
    getRoot().currentData.push(getCurrentObject());
  }

  protected void popData()
  {
    getRoot().currentData.pop();
  }

  /** the typical condition at the begining of looping */
  protected int startLooping() throws JspException
  {
    if(startedWithData= obtainData(getRoot().currentData)&&nextDataRow())
      {
	setCountVar();
	pushData();
	return BodyTag.EVAL_BODY_TAG;
      }
    return BodyTag.SKIP_BODY;
  }

  /** see whether we execute again or we quit. If there were changes to the query, or any
   * of its colleagues in the big tag, we clear what we had, re-read the data and re-execute
   * the whole big tag
   */
  public int doAfter() throws JspException 
  {
    popData();

    if(nextDataRow())
      {
	setCountVar();
	pushData();
	try{
	  bodyContent.print(getQueryTag().separator);
	}catch(IOException e){ throw new JspException(e.toString()); }
	return BodyTag.EVAL_BODY_TAG;
      }
    return BodyTag.SKIP_BODY;    
  }
  
  public int doRootAfter() throws JspException
  {
    popData();

    if( nextDataRow())
      {
	setCountVar();
	pushData();
	try{
	  getRoot().nextLoop();
	}catch(IOException e){ throw new JspException(e.toString()); }
	return BodyTag.EVAL_BODY_TAG;
      }
    else
      return BodyTag.SKIP_BODY;
  }


  public int doEnd() throws JspException 
  {
    if(tag.wasException())
      return BodyTag.SKIP_PAGE;
    try{
      if(startedWithData)
	writeBody(bodyContent.getEnclosingWriter());
    }catch(IOException e){ throw new JspException(e.toString()); }
    return BodyTag.EVAL_PAGE;
  }

  public void writeBody(Writer w) throws IOException
  {
    bodyContent.writeOut(w);
  }

  public void writeBody(org.makumba.util.LongData l) throws IOException
  {
    l.appendFrom(bodyContent.getReader());
  }

  /** write the tag result and go on with the page */
  public int doRootEnd() throws JspException 
  {
    if(tag.wasException())
      return BodyTag.SKIP_PAGE;
    try{
      getRoot().include(tag.getRootData().header);
      if(startedWithData)
	getRoot().writeLoop();
      getRoot().include(tag.getRootData().footer);
    }catch(IOException e){ throw new JspException(e.toString()); }
    return BodyTag.EVAL_PAGE;
  }

  public void insertEvaluation(ValueTag t)throws JspException
  {
    insertEvaluation(t.expr, t.params, t.var, t.printVar);
  }

  /** an enclosed VALUE tag requests the evaluation of a certain expression. 
   * We need to check if we have it in a query projection, if we do, we print the result, 
   * else the query has to be changed and the iteration will restart at the next doAfterBody()
   */
  public void insertEvaluation(String expr, Dictionary formatParams, String var, String printVar)throws JspException
  {
    if(query.checkProjectionInteger(expr, false)==null)
      throw new RuntimeException("unknown projection "+expr);
    int n= query.checkProjectionInteger(expr).intValue();

    String s=formatProjection(n, formatParams, var, printVar);
    try{	
      if(printVar==null && var==null)
	pageContext.getOut().print(s);
    }catch(IOException e){ throw new JspException(e.toString()); }
  }

  public String formatProjection(int n, Dictionary formatParams, String var, String printVar)
  {
    Object o= getProjectionValue(n);
    
    String s=null;
    if(printVar!=null || var==null)
      s=formatter.format(n, o, formatParams);
    
    if(var!=null)
      {
	pageContext.setAttribute(var+"_type", query.getResultType().getFieldDefinition(n));
	PageAttributes.setAttribute(pageContext, var, o);
      }

    if(printVar!=null)
      {
	pageContext.setAttribute(printVar+"_type", "char");
	pageContext.setAttribute(printVar, s);
      }
    return s;
  }

  public boolean executed(){ 
    return bigResults!=null //&& !foundMoreProjections()
	;
  }

  Hashtable nullables= new Hashtable();

  public TagStrategy getNullableStrategy(Object k)
  {
    NullableValueStrategy str=(NullableValueStrategy)nullables.get(k);
    if(str==null)
      {
	str=new NullableValueStrategy();
	nullables.put(k, str);
	return str;
      }
    else
      return new NullableValueSecondaryStrategy(str);
  }

  protected void initCountVars()
  {
    if(getQueryTag().countVar!=null)
      pageContext.setAttribute(getQueryTag().countVar, zero);
    if(getQueryTag().maxCountVar!=null)
      pageContext.setAttribute(getQueryTag().maxCountVar, zero);
  }

  protected void setCountVar()
  {
    if(getQueryTag().countVar!=null)
      pageContext.setAttribute(getQueryTag().countVar, new Integer(index+1));
  }
// ---- the result walker part 

  /** the results through which the tag is looping. all the tags that represent the same physical tag share the same results */
  Grouper bigResults;

  /** the (sub)query that is enriched by this tag */
  ComposedQuery query;

  /** the results for the current tag iteration */
  Vector results;
  
  /** the iteration index */
  int index;

  /** a composite that contains field formatters for each projection */
  RecordFormatter formatter;

  public void setQuery(ComposedQuery q)
  {
    if(query!=null)
      query.deleteObserver(this);
    query=q;
    if(q==null)
      return;
    q.addObserver(this);
    DataDefinition ri=q.getResultType();
    if(ri!=null)
      update(null, null);
  }

  public void rootClose() 
  {
    if(query!=null)
      query.deleteObserver(this);
    bigResults=null;
    results=null;
    formatter=null;
  }

  /** the query has changed, we need to change */
  public void update(Observable model, Object diff)
  {
    formatter=new RecordViewer(query);
  }

  protected Vector obtainData1(Vector v)
  {
    return bigResults.getData(v);
  }

  int run=0;
  /** try to obtain the data, return false if there isn't any */
  public boolean obtainData(Vector v)
  {
    index=-1;
    run++;
    if(v==null || bigResults==null)
      return false;
    results= obtainData1(v);

    if(getQueryTag().maxCountVar!=null)
      {
	Integer i= zero;
	if(results!=null)
	  i= new Integer(results.size());
	pageContext.setAttribute(getQueryTag().maxCountVar, i);
      }
    if(getQueryTag().countVar!=null)
      pageContext.setAttribute(getQueryTag().countVar, zero);
    return results!=null;
  }

  /** execute the associated query */
  public void doQuery(Database db, Attributes a, boolean noProj)
       throws LogicException
  {
    boolean proj= noProj || query.getVersion()>0;
    //    System.out.println(index+" "+(executed()?(""+bigResults.size()):""));
    if(proj && !(executed() && 
		 // if the grouper has been emptied, we have to re-do the query
		 !bigResults.isEmpty()))
      {
	long l= new java.util.Date().getTime();
	//	System.out.println("executing "+query+"  "+query.getVersion()+">"+queryVersion);
	bigResults=query.execute(db, a);
	getRoot().queryTime+= (new java.util.Date().getTime())-l;
      }
    //    else
    //	System.out.println("not executing "+query+"  "+query.getVersion()+", "+queryVersion+ "  "+query.getProjections());
  }

  /** tell whether there is data or not */
  protected boolean hasData() { return results!=null && results.size()>0; }
  
  /** goes to the next data row and returns whether there are nore. Should not be called if obtainData was not caled */
  protected boolean nextDataRow()
  {
    return ++index<results.size();
  }
  
  /** return whether the query projections are known or not */
  public boolean knowsOfAnyProjection()
  {
    return query.getVersion()>0;
  }

  /** returns the current value of indicated projection. Should not be called if obtainData was not called and the index was not returned by knewProjectionAtStart */
  public Object getProjectionValue(int n)
  {
      return getCurrentObject().data[n];
  }

  /** the current object from the result set */
  protected ArrayMap getCurrentObject()
  {
      return (ArrayMap)results.elementAt(index);
  }
  
  // --- debug stuff

  public static String printHeader(String name, String s)
  {
    if(s==null)
      return "";
    return name+"="+s;
  }

  public String toString(){ 
    return getType()+" "+
      printHeader("\nfrom",getQueryTag().queryProps[ComposedQuery.FROM])+
      printHeader("\nwhere",getQueryTag().queryProps[ComposedQuery.WHERE])+
      printHeader("\norderBy",getQueryTag().queryProps[ComposedQuery.ORDERBY])+
      printHeader("\ngroupBy",getQueryTag().queryProps[ComposedQuery.GROUPBY])+
      printHeader("\nseparator",getQueryTag().separator);
  }
  
  public String getType() {return "LIST"; }
}
