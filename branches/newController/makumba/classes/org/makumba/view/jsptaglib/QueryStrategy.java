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
  String [] queryProps;
  String separator;
  String countVar;
  String maxCountVar;

  public QueryStrategy getQueryStrategy(){return this; }
  public RootQueryBuffer getBuffer(){ return (RootQueryBuffer)rootData.buffer; }
  public RootQueryStrategy getRoot() { return (RootQueryStrategy)root; }
  public QueryStrategy getParentStrategy(){return ((QueryTagStrategy)tag.getMakumbaParent().strategy).getQueryStrategy(); }

  public Object getKey(){return key; }

  // only init, no key transformation
  public void init(MakumbaTag root, MakumbaTag tag, Object key)
  {
    super.init(root, tag, key);
    adjustQueryProps();
  }

  protected void adjustQueryProps()
  {
    queryProps= getBuffer().bufferQueryProps;
    getBuffer().bufferQueryProps=new String[4];

    countVar= getBuffer().bufferCountVar;
    maxCountVar= getBuffer().bufferMaxCountVar; 
    getBuffer().bufferCountVar=getBuffer().bufferMaxCountVar=null;
  }

  public void loop()
  {
    separator= getBuffer().bufferSeparator;
    getBuffer().bufferSeparator="";

    for(int i=0; i<getBuffer().bufferQueryProps.length; i++)
      getBuffer().bufferQueryProps[i]=null;
  }

  boolean startedWithData=false;
  
  public ComposedQuery getQuery(){return query; }
  
  // the query that serves as parent for enclosed queries
  public ComposedQuery getParentingQuery(){return getQuery(); }


  /** See whether we execute the body or not. If we have a query, it depends on it, if not, 
   * we simulate execution, to see what are the projections that we need */
  public int doStart() throws JspException 
  {
    initCountVars();
    boolean unknown= query==null;
    if(unknown)
      setQuery(ComposedQuery.getQuery(key, queryProps, getParentStrategy().getParentingQuery()));
    resetQueryVersion();
    if(unknown)
      {
	getRoot().addQuery(this);
	return BodyTag.EVAL_BODY_TAG;
      }
    if(!knowsOfAnyProjection())
      return BodyTag.EVAL_BODY_TAG;
 
    int start= startLooping();
    if(tag.wasException())
      return BodyTag.SKIP_BODY;
    return start;
  }

  public int doRootStart() throws JspException
  {
    initCountVars();
    boolean unknown= query==null;
    if(unknown)
      setQuery(ComposedQuery.getQuery(getKey(), queryProps, null));
    resetQueryVersion();
    if(unknown)
      {
	getRoot().addQuery(this);
	return BodyTag.EVAL_BODY_TAG;
      }
    if(!knowsOfAnyProjection())
      return BodyTag.EVAL_BODY_TAG;
    
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
    //System.out.println(/*getRoot().currentData+" "+bigResults+" "+results+" "+ index +" "+*/System.identityHashCode(this));

    startedWithData=obtainData(getRoot().currentData)&&nextDataRow();
    if(startedWithData)
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
    if(startedWithData)
      popData();

    if(getRoot().foundMoreProjectionsInAnyTag())
      return BodyTag.SKIP_BODY;

    if(startedWithData && nextDataRow())
      {
	setCountVar();
	pushData();
	try{
	  bodyContent.print(separator);
	}catch(IOException e){ throw new JspException(e.toString()); }
	return BodyTag.EVAL_BODY_TAG;
      }
    return BodyTag.SKIP_BODY;    
  }
  
  public int doRootAfter() throws JspException
  {
    if(startedWithData)
      popData();

    if(getRoot().foundMoreProjectionsInAnyTag())
      {
	//	System.out.println(bigResults+"doing queries");
	getRoot().doQueries(false);
	if(tag.wasException())
	  {
	    //    System.out.println("was exception");
	    return BodyTag.SKIP_BODY;
	  }
	bodyContent.clearBody();
	//System.out.println("trying to repeat");
	return startLooping();
      }

    if(startedWithData)
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
    else
      {
	// we have no projections, we loop them anyway
	getRoot().doQueries(true);
	if(tag.wasException())
	  {
	    //  System.out.println("was exception 1");
	    return BodyTag.SKIP_BODY;
	  }
	bodyContent.clearBody();
	//	System.out.println("trying to repeat 1");
	return startLooping();
      }
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
      getRoot().include(rootData.header);
      if(startedWithData)
	getRoot().writeLoop();
      getRoot().include(rootData.footer);
    }catch(IOException e){ throw new JspException(e.toString()); }
    return BodyTag.EVAL_PAGE;
  }


  /** an enclosed VALUE tag requests the evaluation of a certain expression. 
   * We need to check if we have it in a query projection, if we do, we print the result, 
   * else the query has to be changed and the iteration will restart at the next doAfterBody()
   */
  public void insertEvaluation(String expr, Dictionary formatParams, String var, String printVar)throws JspException, NewProjectionException
  {
    int n= knewProjectionAtStart(expr);
    if(n!=-1 && startedWithData  && ! foundMoreProjections())
	{
	    String s=formatProjection(n, formatParams, var, printVar);
	    try{
		if(printVar==null && var==null)
		    pageContext.getOut().print(s);
	    }catch(IOException e){ throw new JspException(e.toString()); }
	}
    else
      {
	if(var!=null || printVar!=null)
	    reloadSelf(var, printVar);
      }
  }

    void reloadSelf(String var, String printVar)
    {
	String s=((HttpServletRequest)pageContext.getRequest()).getRequestURI();
	s=s.substring(((HttpServletRequest)pageContext.getRequest()).getContextPath().length());
	String vname=( var==null?printVar:var);
	
	System.out.println("could not determine \'"+vname+"\' -> reloading "+s);	    
	try{
	    pageContext.forward(s);

	    // the self-forward was succesful 
	    // we now give up this request. this will be caught by the 
	    // controller filter, and ignored
	    throw new NewProjectionException(vname, s);
	}
	catch(javax.servlet.ServletException se){ se.printStackTrace(); }
	catch(IOException ioe){ ioe.printStackTrace(); }
    }
  
  public String formatProjection(int n, Dictionary formatParams, String var, String printVar)
  {
    Object o= getProjectionValue(n);
    
    String s=null;
    if(printVar!=null || var==null)
      s=formatter.format(n, o, formatParams);
    
    if(var!=null)
      {
	if(query.getResultType()!=null)
	  {
	    pageContext.setAttribute(var+"_type", query.getResultType().getFieldDefinition(n));
	    PageAttributes.setAttribute(pageContext, var, o);
	  }
	else
	    reloadSelf(var, printVar);
      }
    if(printVar!=null)
      {
	pageContext.setAttribute(printVar+"_type", "char");
	pageContext.setAttribute(printVar, s);
      }
    return s;
  }

  public boolean executed(){ 
      return bigResults!=null && !foundMoreProjections(); 
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
    if(countVar!=null)
      pageContext.setAttribute(countVar, zero);
    if(maxCountVar!=null)
      pageContext.setAttribute(maxCountVar, zero);
  }

  protected void setCountVar()
  {
    if(countVar!=null)
      pageContext.setAttribute(countVar, new Integer(index+1));
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

  /** the projections that existed when this object was created. used as reference for query changes */
  Dictionary startProjections;

  /** the query version when this object was created */
  int queryVersion;

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
    startProjections=null;
    formatter=null;
  }

  /** the query has changed, we need to change */
  public void update(Observable model, Object diff)
  {
    formatter=new RecordViewer(query);
  }

  /** initialize the reference values for query change detection */
  public void resetQueryVersion()
  {
    //    System.out.print(getClass().getName()+System.identityHashCode(this)+" ");
    queryVersion= query.getVersion();
    startProjections=query.getProjections();
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

    if(maxCountVar!=null)
      {
	Integer i= zero;
	if(results!=null)
	  i= new Integer(results.size());
	pageContext.setAttribute(maxCountVar, i);
      }
    if(countVar!=null)
      pageContext.setAttribute(countVar, zero);
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
	bigResults=query.execute(db, a);
	getRoot().queryTime+= (new java.util.Date().getTime())-l;
	resetQueryVersion();
      }
  }

  /** tell whether there is data or not */
  protected boolean hasData() { return results!=null && results.size()>0; }
  
  /** goes to the next data row and returns whether there are nore. Should not be called if obtainData was not caled */
  protected boolean nextDataRow()
  {
    return ++index<results.size();
  }
  
  /** checks if any new projections were found in this tag */
  public boolean foundMoreProjections()
  {
    //    System.out.println(query.getVersion() + " "+queryVersion);
    //    System.out.print(getClass().getName()+System.identityHashCode(this)+" ");
    if(query.getVersion()!=queryVersion)
      {
	//	System.out.println(this+" "+query.getVersion()+" "+queryVersion);
	return true;
      }
    return false;
  }

  /** return whether the query projections are known or not */
  public boolean knowsOfAnyProjection()
  {
    return query.getVersion()>0;
  }

  /** Returns the index of the given projection, -1 if the projection was not known at the beginning of the iteration */
  public int knewProjectionAtStart(String expr)
  {
    Integer n= query.checkProjectionInteger(expr);
    if(startProjections.get(expr)==null)
      return -1;
    return n.intValue();
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
      printHeader("\nfrom",queryProps[ComposedQuery.FROM])+
      printHeader("\nwhere",queryProps[ComposedQuery.WHERE])+
      printHeader("\norderBy",queryProps[ComposedQuery.ORDERBY])+
      printHeader("\ngroupBy",queryProps[ComposedQuery.GROUPBY])+
      printHeader("\nseparator",separator);
  }
  
  public String getType() {return "LIST"; }
}

