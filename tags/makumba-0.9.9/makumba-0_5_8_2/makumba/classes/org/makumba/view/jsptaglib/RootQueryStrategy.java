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
import org.makumba.controller.jsp.PageAttributes;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.*;
import java.io.*;



public class RootQueryStrategy 
implements RootTagStrategy, QueryTagStrategy
{
  QueryStrategy decorated;

  RootQueryStrategy(QueryStrategy qs){ decorated=qs; }

  public QueryStrategy getQueryStrategy(){return decorated; }

  Dictionary nothing= new ArrayMap();

  static int tagQueries=  NamedResources.makeStaticCache
  ("JSP mak:list root tags",
   new NamedResourceFactory()
   {
     public Object makeResource(Object o){ return new Hashtable(); }
   });


  Dictionary queries;
  Stack currentData;
  boolean dirty=false;
  long queryTime=0l;

  public void init(MakumbaTag root, MakumbaTag tag, Object key) 
  {
    currentData=new Stack();
    currentData.push(nothing);
    queries= (Dictionary)NamedResources.getStaticCache(tagQueries).getResource(key);
    decorated.init(root, tag, key); 
  }

  public Object getKey(){ return decorated.getKey(); }

  public void onInit(TagStrategy ts) throws LogicException
  {
    QueryStrategy qs= ((QueryTagStrategy)ts).getQueryStrategy();
    ComposedQuery cq= (ComposedQuery)queries.get(qs.getKey());

    // we have a cached query for this key, see if it's not just being built
    if(qs.query==null)
      {
	qs.setQuery(cq);
	if(cq!=null && !qs.executed())
	  {
	    Database dbc= MakumbaSystem.getConnectionTo(decorated.tag.getDatabaseName());
	    try{
	      qs.doQuery(dbc, PageAttributes.getAttributes(decorated.tag.getPageContext()), false);
	    }finally{dbc.close(); }
	  }
      }
  }

  public void doRelease()
  {
    MakumbaSystem.getMakumbaLogger("taglib.performance").fine("queries: "+queryTime+" ms");
  }

  /** See whether we execute the body or not. If we have a query, it depends on it, if not, 
   * we simulate execution, to see what are the projections that we need */
  public int doStart() throws JspException 
  {
    return decorated.doRootStart();
  }

  public void doAnalyze() 
  {
    decorated.doRootAnalyze();
  }

  public int doAfter() throws JspException 
  {
    return decorated.doRootAfter();
  }

  /** write the tag result and go on with the page */
  public int doEnd() throws JspException 
  {
    return decorated.doRootEnd();
  }

  //------------------------

  public void include(String s) throws JspException
  {
    try{
      if(s!=null)
	decorated.tag.getRootData().pageContext.include(s); 
    }catch(Exception e) { throw new MakumbaJspException(e); }
  }

  protected ComposedQuery getSuperQuery() 
  { 
    return null;
  }

  //----------------------------
  protected void addQuery(QueryStrategy qs)
  {
    queries.put(qs.getKey(), qs.getQuery());
  }

  /** execute all queries from the tags */
  public void doQueries(boolean noProj)throws JspException
  {
    Database dbc= MakumbaSystem.getConnectionTo(decorated.tag.getDatabaseName());
    try
      {
	for(Enumeration e= decorated.tag.getRootData().subtagData.elements(); e.hasMoreElements();)
	  {
	    ((QueryTagStrategy)e.nextElement()).getQueryStrategy().doQuery(dbc, PageAttributes.getAttributes(decorated.tag.getPageContext()), noProj);
	  }
      }
    catch(Throwable e){ decorated.tag.treatException(e); }
    finally{ dbc.close(); }
    dirty=false;
  }

  //---------- memory sparing in the event of large output 
  org.makumba.util.LongData file= new org.makumba.util.LongData();

  int lastAv;
  long length;

  public void nextLoop() throws IOException
  {
    /*    decorated.bodyContent.print(decorated.separator);
    if(file==null)
      {
	int av= decorated.bodyContent.getRemaining();
	if(av>lastAv)
	  length+=av;
	lastAv=av;
	if(length<org.makumba.Text.FILE_LIMIT)
	  return;
	file= new org.makumba.util.LongData();
      }
    if(file!=null)*/
    //  {
    decorated.bodyContent.print(decorated.getQueryTag().separator);
    decorated.writeBody(file);
    decorated.bodyContent.clearBuffer();
	//   }
  }

  public void writeLoop() throws IOException
  {
    //if(file==null)
    //  decorated.writeBody(decorated.bodyContent.getEnclosingWriter());
    //else
    //  {
    decorated.writeBody(file);
    Reader r= new InputStreamReader(file.getInputStream());
    
    char buff[]= new char[org.makumba.Text.FILE_LIMIT];
    int n;
    while((n=r.read(buff))>0)
      decorated.bodyContent.getEnclosingWriter().write(buff, 0, n);
    r.close();
    // }
  }

  // ---- decorator methods
  /** one more loop with this strategy, by another tag */
  public void loop(){ decorated.loop(); }

  /** called by the tag's setBodyContent, if any */
  public void setBody(BodyContent bc){ decorated.setBody(bc); } 

  /** called by the tag's getBodyContent */
  public BodyContent getBody(){ return decorated.getBody(); }
  
  /** called by the tag's setBodyContent, if any */
  public void setPage(PageContext pc){ decorated.setPage(pc); } 

  /** called when the root closes */
  public void rootClose(){ 
    decorated.rootClose(); 
  }
}
