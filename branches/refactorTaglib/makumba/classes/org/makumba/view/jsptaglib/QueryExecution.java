package org.makumba.view.jsptaglib;

import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.Database;
import org.makumba.MakumbaSystem;

import org.makumba.view.Grouper;
import org.makumba.view.ComposedQuery;
import org.makumba.util.ArrayMap;
import org.makumba.util.MultipleKey;
import org.makumba.controller.jsp.PageAttributes;

import java.util.Dictionary;
import java.util.Vector;
import java.util.HashMap;
import java.util.Stack;

import javax.servlet.jsp.PageContext;


/** This class holds the listData of a mak:list or the valueQuery data of a mak:value. It determines iterationGroups at every parentIteration, and iterates through the iterationGroupData */
public class ListQueryExecution
{
  /** the results of the query associated with the list or queryMak:value*/
  Grouper listData;

  /** the part of listData iterated for a certain parent iteration */
  Vector iterationGroupData;

  /** the index of iteration within the iteration group */
  int iteration;

  /** a reference to the currentDataSet _shared_ by all ListQueryExecutions in the listGroup */
  Stack currentDataSet;

  /** stuff for queryData to be kept */
  HashMap valueQueryData= new HashMap();

  static final private String EXECUTIONS= "org.makumba.ListQueryExecutions";
  static final private String CURRENT_DATA_SET="org.makumba.currentDataSet";  
  static final private Dictionary nothing= new ArrayMap();

  /** Allocate a currentDataSet and a container for the ListQueryExecutions of the listGroup.
   * Executed by the rootList
   */
  static void startListGroup(PageContext pageContext)
  {
    pageContext.setAttribute(EXECUTIONS, new HashMap());
    Stack currentDataSet= new Stack();
    // org.makumba.view.Grouper requires the stack not be empty
    currentDataSet.push(nothing);
    pageContext.setAttribute(CURRENT_DATA_SET, currentDataSet);
  }

  /** De-allocate all ListQueryExecutions in the listGroup, and the currentDataSet 
   * Executed by the rootList
   */
  static void endListGroup(PageContext pageContext)
  {
    pageContext.removeAttribute(EXECUTIONS);
    pageContext.removeAttribute(CURRENT_DATA_SET);
  }

  /** Get the ListQueryExecution for the given key, build one if needed.
   * Every list tag (QueryTag) calls this method. A ListQueryTag will be built only in the first
   parentIteration and will be returned at next parentIterations.
   */
  static ListQueryExecution getFor(MultipleKey key, PageContext pageContext)
       throws LogicException
  {
    HashMap executions= (HashMap)pageContext.getAttribute(EXECUTIONS);

    ListQueryExecution lqe=(ListQueryExecution)executions.get(key);
    if(lqe==null)
      executions.put(key, lqe= new ListQueryExecution(key, pageContext));
    return lqe;
  }

  /** Execute the given query, in the given db, with the given attributes, to form the listData; keep the reference to the currentDataSet for future push and pop operations, find the nested valueQueries */
  private ListQueryExecution(MultipleKey key, PageContext pageContext)
       throws LogicException
  {
    currentDataSet=(Stack)pageContext.getAttribute(CURRENT_DATA_SET);
    Database dbc= 
      MakumbaSystem.getConnectionTo(MakumbaTag.getDatabaseName(pageContext));
    try{
      listData=MakumbaTag.getPageCache(pageContext).getQuery(key)
	.execute(dbc, PageAttributes.getAttributes(pageContext));
    }finally { dbc.close(); }
  }

  public int getIterationGroupData()
  {
    iteration=0;
    iterationGroupData= listData.getData(currentDataSet);
    return dataSize();
  }
  
  public int dataSize()
  {
    if(iterationGroupData==null || iterationGroupData.size()==0)
      return 0;
    else
      return iterationGroupData.size();
  }

  /** A new parent iteration is made. Compute the iterationGroupData based on listData and currentDataSet. Push the first currentListData to the currentDataSet.
   * @return the number of iterations in the iterationGroup, possibly 0.
   */
  public int onParentIteration()
  {
    getIterationGroupData();
    int n=dataSize();
    if(n!=0)
      currentDataSet.push(currentListData());
    return n;
  }

  /** Pop the previous currentListData from the currentDataSet and check if there are more iterations to be made within the iterationGroup. 
   * If there are, add the currentListData to the currentDataSet
   * @return the current iteration position, relative to the start of the interationGroup or -1 if there are no more results
   */
  public int nextGroupIteration()
  {
    valueQueryData.clear();
    currentDataSet.pop();
    iteration++;
    if(iteration==iterationGroupData.size())
      return -1;
    currentDataSet.push(currentListData());
    return iteration;
  }
  
  /** The data of the current iteration; i.e. the currentListData */
  public ArrayMap currentListData(){ return (ArrayMap)iterationGroupData.elementAt(iteration); }


}




