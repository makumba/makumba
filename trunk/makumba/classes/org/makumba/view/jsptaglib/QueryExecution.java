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

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

import javax.servlet.jsp.PageContext;

import org.makumba.Database;
import org.makumba.LogicException;
import org.makumba.controller.jsp.PageAttributes;
import org.makumba.util.ArrayMap;
import org.makumba.util.MultipleKey;
import org.makumba.view.Grouper;


/** This class holds the listData of a mak:list or the valueQuery data of a mak:value. It determines iterationGroups at every parentIteration, and iterates through the iterationGroupData */
public class QueryExecution
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

  static final private String EXECUTIONS= "org.makumba.taglibQueryExecutions";
  static final private String CURRENT_DATA_SET="org.makumba.currentDataSet";  
  static final private Dictionary NOTHING= new ArrayMap();

  /** Allocate a currentDataSet and a container for the QueryExecutions of the listGroup.
   * Executed by the rootList
   */
  static void startListGroup(PageContext pageContext)
  {
    pageContext.setAttribute(EXECUTIONS, new HashMap());
    Stack currentDataSet= new Stack();
    // org.makumba.view.Grouper requires the stack not be empty
    currentDataSet.push(NOTHING);
    pageContext.setAttribute(CURRENT_DATA_SET, currentDataSet);
  }

  /** De-allocate all QueryExecutions in the listGroup, and the currentDataSet 
   * Executed by the rootList
   */
  static void endListGroup(PageContext pageContext)
  {
    pageContext.removeAttribute(EXECUTIONS);
    pageContext.removeAttribute(CURRENT_DATA_SET);
  }

  /** Get the QueryExecution for the given key, build one if needed.
   * Every list tag (QueryTag) calls this method. A ListQueryTag will be built only in the first
   parentIteration and will be returned at next parentIterations.
   */
  static QueryExecution getFor(MultipleKey key, PageContext pageContext)
       throws LogicException
  {
    HashMap executions= (HashMap)pageContext.getAttribute(EXECUTIONS);

    QueryExecution lqe=(QueryExecution)executions.get(key);
    if(lqe==null)
      executions.put(key, lqe= new QueryExecution(key, pageContext));
    return lqe;
  }

  /** Execute the given query, in the given db, with the given attributes, to form the listData; keep the reference to the currentDataSet for future push and pop operations, find the nested valueQueries */
  private QueryExecution(MultipleKey key, PageContext pageContext)
       throws LogicException
  {
    currentDataSet=(Stack)pageContext.getAttribute(CURRENT_DATA_SET);
    Database dbc= 
      org.makumba.controller.Logic.getDatabaseConnection
      (
       PageAttributes.getAttributes(pageContext),
       MakumbaTag.getDatabaseName(pageContext)
      );
      listData=MakumbaTag.getPageCache(pageContext).getQuery(key)
	.execute(dbc, PageAttributes.getAttributes(pageContext));
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
   * Only if the dataSize is 0, nothing is pushed to the stack, and at endIterationGroup, nothing is popped.
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
   * If there are, add the currentListData to the currentDataSet; else push a DUMMY data (cfr bug 555).
   * @return the current iteration position, relative to the start of the interationGroup or -1 if there are no more results
   */
  public int nextGroupIteration()
  {
    valueQueryData.clear();
    currentDataSet.pop();
    iteration++;
    if(iteration==iterationGroupData.size()){
      currentDataSet.push(NOTHING); // push a dummy Data; will be removed by endIterationGroup
      return -1;
    }
    currentDataSet.push(currentListData());
    return iteration;
  }


  /** Pops the the last data of an iterationGroup. 
   *  In case of a empty-body/simple QueryTag, this will pop the first element; otherwise it pops the dummy data. cfr Bug 555.  
   */
  public void endIterationGroup(){
    if (dataSize() > 0) {
        currentDataSet.pop() ;
    }
  }

  
  /** The data of the current iteration; i.e. the currentListData */
  public ArrayMap currentListData(){ return (ArrayMap)iterationGroupData.elementAt(iteration); }


}




