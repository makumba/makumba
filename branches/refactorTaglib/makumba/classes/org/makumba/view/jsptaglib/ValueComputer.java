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
  public static ValueComputer getValueComputer(MakumbaTag analyzed, String expr)
  {
    expr=expr.trim();
    Object check= analyzed.pageCache.getQuery(analyzed.parentList.tagKey)
      .checkExprSetOrNullable(expr);

    FieldDefinition set=null;
    String nullableExpr=null;

    if(check instanceof String)
      nullableExpr=(String)check;

    if(check instanceof FieldDefinition)
      set=(FieldDefinition)check;

    if(nullableExpr==null && set==null)
      return new ValueComputer(analyzed, expr);

    if(set==null)
      return new NullableValueComputer(analyzed, nullableExpr, expr);
    return new SetValueComputer(analyzed, set, expr);
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
  ValueComputer(MakumbaTag analyzed, String expr)
  {
    parentKey= analyzed.parentList.tagKey;
    this.expr=expr;
    analyzed.pageCache.getQuery(parentKey).checkProjectionInteger(expr);
  }

  /** The key of the query in which this value is a projection. Return parentKey */
  MultipleKey getQueryKey(){ return parentKey; }

  /** Compute the queryProjection index in the currentListData, and the 
    queryProjection type*/
  public void doEndAnalyze(MakumbaTag analyzed)
  {
    ComposedQuery q= analyzed.pageCache.getQuery(getQueryKey());
    projectionIndex= q.checkProjectionInteger(expr).intValue();
    
    if(type==null) // if type is not set in the constructor
      type=q.getResultType().getFieldDefinition(projectionIndex);
  }
  
  /** Get the value of the queryProjection from the currentListData of the enclosing query. Used mostly by InputTag */
  public Object getValue(MakumbaTag running) throws LogicException
  {
    return ListQueryExecution.getFor(getQueryKey(), running.getPageContext())
      .currentListData().data[projectionIndex];
  }

  /** Format the value of the queryProjection from the currentListData of the enclosing query. Set the var and the printVar values*/  
  public void print(ValueTag running) throws JspException, LogicException
  {
    Object o= getValue(running);
    String s=null;
    if(running.printVar!=null || running.var==null)
      s=((RecordViewer)running.pageCache.formatters.get(getQueryKey()))
	.format(projectionIndex, o, running.params);

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
  public void makeQuery(MakumbaTag analyzed,
			String keyDifference, 
			String[] queryProps, 
			String expr)
			
  {
    this.expr=expr;
    parentKey=analyzed.parentList.tagKey;

    queryKey= new MultipleKey((Vector)parentKey, 6);
    queryKey.setAt(keyDifference, 5);

    analyzed.pageCache.cacheQuery(queryKey, queryProps, parentKey)
      .checkProjectionInteger(expr);
  }
  
  /** The key of the query in which this value is a projection. Return queryKey */
  MultipleKey getQueryKey(){ return queryKey; }

  /** if other ValueComputers sharing the same valueQuery did not analyze it yet, we analyze it here */
  public void doEndAnalyze(MakumbaTag analyzed)
  {
    if(analyzed.pageCache.formatters.get(queryKey)==null) 
      {
	ComposedQuery myQuery= analyzed.pageCache.getQuery(queryKey);
	myQuery.analyze();
	analyzed.pageCache.formatters.put(queryKey, new RecordViewer(myQuery));
      }
    super.doEndAnalyze(analyzed);
  }

  static final Object dummy= new Object();

  /** Obtain the iterationGroupData for the valueQuery */
  ListQueryExecution runQuery(MakumbaTag running) throws LogicException
  {
    ListQueryExecution ex= 
      ListQueryExecution.getFor(queryKey, running.getPageContext());

    ListQueryExecution parentEx= 
      ListQueryExecution.getFor(parentKey, running.getPageContext());

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
  NullableValueComputer(MakumbaTag analyzed, String nullableExpr, String expr)
  {
    makeQuery(analyzed, nullableExpr.trim(), emptyQueryProps, expr);
  }
  
  /** Check if the iterationGroupData is longer than 1, and throw an exception if so. Take the first result (if any) otherwise */
  public Object getValue(MakumbaTag running) throws LogicException
  {
    ListQueryExecution ex= runQuery(running);
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
  SetValueComputer(MakumbaTag analyzed, FieldDefinition set, String setExpr)
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

    makeQuery(analyzed, set.getName(), queryProps, label);
    
    if(analyzed instanceof ValueTag)
      analyzed.pageCache.getQuery(queryKey).checkProjectionInteger(name);   
  }

  /** Compute nameIndex */
  public void doEndAnalyze(MakumbaTag analyzed)
  {
    super.doEndAnalyze(analyzed);
    if(name!=null)
      nameIndex= analyzed.pageCache.getQuery(queryKey)
	.checkProjectionInteger(name).intValue();
  }
  
  /** Go through the iterationGroupData and return a vector with the set values. Used only by InputTag */
  public Object getValue(MakumbaTag running) throws LogicException
  {
    ListQueryExecution ex= runQuery(running);
    int n=ex.dataSize();
    Vector v= new Vector();

    for(ex.iteration =0; ex.iteration<n; ex.iteration++)
      v.addElement(ex.currentListData().data[projectionIndex]);
    return v;
  }

  /** Go through the iterationGroupData and print the set values, comma-separated; also set var (Vector with the set values) and printVar */
  public void print(ValueTag running) throws JspException, LogicException
  {
    ListQueryExecution ex= runQuery(running);
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
