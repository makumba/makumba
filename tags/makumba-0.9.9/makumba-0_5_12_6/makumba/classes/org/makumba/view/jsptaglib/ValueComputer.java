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
import org.makumba.util.MultipleKey;
import org.makumba.controller.jsp.PageAttributes;

import org.makumba.FieldDefinition;
import org.makumba.LogicException;

import org.makumba.view.ComposedQuery;
import org.makumba.view.html.RecordViewer;

import javax.servlet.jsp.JspException;

import java.util.Vector;

/** Every ValueTag will build a ValueComputer at page analysis, which it then retrieves and uses at page running */
public class ValueComputer
{
  /** Determine if 'analyzed' is a queryMak:value or a nonQueryMak:value */
  public static ValueComputer getValueComputerAtAnalysis(MakumbaTag analyzed, String expr, MakumbaJspAnalyzer.PageCache pageCache)
  {
    expr=expr.trim();
    Object check= pageCache.getQuery(analyzed.getParentListKey())
      .checkExprSetOrNullable(expr);

    FieldDefinition set=null;
    String nullableExpr=null;

    if(check instanceof String)
      nullableExpr=(String)check;

    if(check instanceof FieldDefinition)
      set=(FieldDefinition)check;

    if(nullableExpr==null && set==null)
      return new ValueComputer(analyzed, expr, pageCache);

    if(set==null)
      return new NullableValueComputer(analyzed, nullableExpr, expr, pageCache);
    return new SetValueComputer(analyzed, set, expr, pageCache);
  }

  /** the key of the parentList */
  MultipleKey parentKey;

  /** the queryProjection index in the currentListData */
  int projectionIndex;

  /** the queryProjection expression */
  String expr;

  /** the queryProjection type */
  FieldDefinition type;

  ValueComputer(){}

  /** a nonQueryMak:value value computer */
  ValueComputer(MakumbaTag analyzed, String expr, MakumbaJspAnalyzer.PageCache pageCache)
  {
    parentKey= analyzed.getParentListKey();
    this.expr=expr;
    pageCache.getQuery(parentKey).checkProjectionInteger(expr);
  }

  /** The key of the query in which this value is a projection. Return parentKey */
  MultipleKey getQueryKey(){ return parentKey; }

  /** Compute the queryProjection index in the currentListData, and the 
    queryProjection type*/
  public void doEndAnalyze(MakumbaTag analyzed, MakumbaJspAnalyzer.PageCache pageCache)
  {
    ComposedQuery q= pageCache.getQuery(getQueryKey());
    projectionIndex= q.checkProjectionInteger(expr).intValue();

    if(type==null) // if type is not set in the constructor
      type=q.getResultType().getFieldDefinition(projectionIndex);
  }
  
  /** Get the value of the queryProjection from the currentListData of the enclosing query. Used mostly by InputTag */
  public Object getValue(MakumbaTag running) throws LogicException
  {
    return QueryExecution.getFor(getQueryKey(), running.getPageContext(), null, null)
      .currentListData().data[projectionIndex];
  }

  /** Format the value of the queryProjection from the currentListData of the enclosing query. Set the var and the printVar values*/  
  public void print(ValueTag running, MakumbaJspAnalyzer.PageCache pageCache) throws JspException, LogicException
  {
    Object o= getValue(running);
    String s=null;
    if(running.printVar!=null || running.var==null){
      s=((RecordViewer)pageCache.formatters.get(getQueryKey()))
	.format(projectionIndex, o, running.params);
    }

    if(running.var!=null)
      PageAttributes.setAttribute(running.getPageContext(), running.var, o);
    if(running.printVar!=null)
      running.getPageContext().setAttribute(running.printVar, s);
    if(running.printVar==null && running.var==null){
      try{	
	running.getPageContext().getOut().print(s);
      }catch(Exception e){ throw new JspException(e.toString()); }
    }
  }
}

/** the ValueComputer of a queryMak:value */
abstract class QueryValueComputer extends ValueComputer
{
  /** the key of the generated query */
  MultipleKey queryKey;
  
  /** make a key that adds the given keyDifference to the tagKey of the parentList, and associate with it a subquery of the parentQuery made from the given queryProps */
  public void makeQueryAtAnalysis(MakumbaTag analyzed,
			String keyDifference, 
			String[] queryProps, 
			String expr,
			MakumbaJspAnalyzer.PageCache pageCache)
  {
    this.expr=expr;
    parentKey=analyzed.getParentListKey();

    queryKey= new MultipleKey(parentKey, keyDifference);

    pageCache.cacheQuery(queryKey, queryProps, parentKey)
      .checkProjectionInteger(expr);
  }
  
  /** The key of the query in which this value is a projection. Return queryKey */
  MultipleKey getQueryKey(){ return queryKey; }

  /** if other ValueComputers sharing the same valueQuery did not analyze it yet, we analyze it here */
  public void doEndAnalyze(MakumbaTag analyzed, MakumbaJspAnalyzer.PageCache pageCache)
  {
    if(pageCache.formatters.get(queryKey)==null) 
      {
	ComposedQuery myQuery= pageCache.getQuery(queryKey);
	myQuery.analyze();
	pageCache.formatters.put(queryKey, new RecordViewer(myQuery));
      }
    super.doEndAnalyze(analyzed, pageCache);
  }

  static final Object dummy= new Object();

  /** Obtain the iterationGroupData for the valueQuery */
  QueryExecution runQuery(MakumbaTag running) throws LogicException
  {
    QueryExecution ex= 
      QueryExecution.getFor(queryKey, running.getPageContext(), null, null);

    QueryExecution parentEx= 
      QueryExecution.getFor(parentKey, running.getPageContext(), null, null);

    // if the valueQuery's iterationGroup for this parentIteration was not computed, do it now...
    if(parentEx.valueQueryData.get(queryKey)==null)
      {
	ex.getIterationGroupData();

	// ... and make sure it won't be done this parentIteration again
	parentEx.valueQueryData.put(queryKey, dummy);
      }
    return ex;
  }
}

/** The manager of a nullableValueQuery */
class NullableValueComputer extends QueryValueComputer
{
  static final String emptyQueryProps[]= new String[4];
  
  /** Make a query that is identical to the parentQuery, but has expr as projection */
  NullableValueComputer(MakumbaTag analyzed, String nullableExpr, String expr, MakumbaJspAnalyzer.PageCache pageCache)
  {
    makeQueryAtAnalysis(analyzed, nullableExpr.trim(), emptyQueryProps, expr, pageCache);
  }
  
  /** Check if the iterationGroupData is longer than 1, and throw an exception if so. Take the first result (if any) otherwise */
  public Object getValue(MakumbaTag running) throws LogicException
  {
    QueryExecution ex= runQuery(running);
    int n=ex.dataSize();
    if(n>1)
      throw new RuntimeException("nullable query with more than one result ??? "+n);
    if(n==0)
      return null;
    return ex.currentListData().data[projectionIndex];
  }
}

/** The manager of a setValueQuery */
class SetValueComputer extends QueryValueComputer
{
  /** if we are in a value tag, the name of the queryProjection that computes the title field, otherwise null */
  String name=null;

  /** if we are in a value tag, the index of the queryProjection that computes the title field, otherwise null */
  int nameIndex;

  /** Make a query that has an extra FROM: the set requested. As projections, add the key of the set type and, if we are in a value tag, the title field */
  SetValueComputer(MakumbaTag analyzed, FieldDefinition set, String setExpr, MakumbaJspAnalyzer.PageCache pageCache)
  {
    type=set;
    String label= setExpr.replace('.', '_');
    String queryProps[]= new String[4];
    queryProps[ComposedQuery.FROM]=setExpr+" "+label;

    if(analyzed instanceof ValueTag)
      {
	name= label+"."+set.getRelationType().getTitleFieldName();
	queryProps[ComposedQuery.ORDERBY]= name;
      }

    makeQueryAtAnalysis(analyzed, set.getName(), queryProps, label, pageCache);
    
    if(analyzed instanceof ValueTag)
      pageCache.getQuery(queryKey).checkProjectionInteger(name);   
  }

  /** Compute nameIndex */
  public void doEndAnalyze(MakumbaTag analyzed, MakumbaJspAnalyzer.PageCache pageCache)
  {
    super.doEndAnalyze(analyzed, pageCache);
    if(name!=null)
      nameIndex= pageCache.getQuery(queryKey)
	.checkProjectionInteger(name).intValue();
  }
  
  /** Go through the iterationGroupData and return a vector with the set values. Used only by InputTag */
  public Object getValue(MakumbaTag running) throws LogicException
  {
    QueryExecution ex= runQuery(running);
    int n=ex.dataSize();
    Vector v= new Vector();

    for(ex.iteration =0; ex.iteration<n; ex.iteration++)
      v.addElement(ex.currentListData().data[projectionIndex]);
    return v;
  }

  /** Go through the iterationGroupData and print the set values, comma-separated; also set var (Vector with the set values) and printVar */
  // FIXME (fred) shouldn't the formatting be in view.html package, instead of here?
  public void print(ValueTag running) throws JspException, LogicException
  {
    QueryExecution ex= runQuery(running);
    int n=ex.dataSize();
    Vector v=null;

    if(running.var!=null)
      v= new Vector();

    String sep="";
    StringBuffer print= new StringBuffer();
    for(ex.iteration =0; ex.iteration<n; ex.iteration++)
      {
	print.append(sep);
	sep=",";
	if(running.var!=null)
	  v.addElement(ex.currentListData().data[projectionIndex]);
	print.append(ex.currentListData().data[nameIndex]);
      }
    String s= print.toString();

    // replace by 'default' or 'empty' if necessary
    if (n==0 && running.params.get("default") != null)
        s = (String) running.params.get("default");

    if (s.length()==0 && running.params.get("empty") != null)
        s = (String) running.params.get("empty");

    if(running.var!=null)
      PageAttributes.setAttribute(running.getPageContext(), running.var, v);
    if(running.printVar!=null)
      running.getPageContext().setAttribute(running.printVar, s);
    if(running.printVar==null && running.var==null){
      try{	
	running.getPageContext().getOut().print(s);
      }catch(Exception e){ throw new JspException(e.toString()); }
    }
  }
}
