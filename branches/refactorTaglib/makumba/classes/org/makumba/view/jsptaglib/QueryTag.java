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
import javax.servlet.jsp.tagext.IterationTag

/** Display of OQL query results in nested loops. The Query FROM, WHERE, GROUPBY and ORDERBY are indicated in the head of the tag. The query projections are indicated by Value tags in the body of the tag. The sub-tags will generate subqueries of their enclosing tag queries (i.e. their WHERE, GROUPBY and ORDERBY are concatenated). Attributes of the environment can be passed as $attrName to the query 
 * 
 */
public class QueryTag extends MakumbaTag implements IterationTag
{
  // formatters? keep in root strategy, copy to groupdata? mb also queries...
  // keep attribute type data in page analysis
  // ListGroupData, IterationGroupComputer, ValueComputer
  /*
    doAnalyze{
    cache.getCache("tagStrategies").put(key, IterationGroupComputer.make(this));
    }
   */

  /** A strategy computed at page analysis. The strategy is common for all runnings of a mak:list from a certain page, line and column, even for those that run in parallel (although a different QueryTag is used for each, extracted by the servlet engine- e.g. Tomcat- from a tag pool).
   */
  class QueryTagStrategy {
    public static String LIST_GROUP_DATA= "org.makumba.LisGroupData";

    /** the index of the query whose results are iterated by this mak:list
     * @see RootQueryStrategy#queries */
    int queryIndex; 
    
    /** The indexes of the other queries, iterated by special mak:value's (nullable, sets).
     * A mak:list may have more than one query, because some mak:value's and mak:input's inside it may introduce separate queries:<ul>
     <li> if the expression of a mak:value goes through nullable pointers
     <li> if the expression of a mak:value is a set
     </ul>
     * When a mak:list will prepare the results for its current execution (@see ListGroupData.results), it will do so also for its special mak:value's.
     */
    List subqueryIndexes;

    void getSubqueryResults(ListGroupData groupData)
    {
      for(Iterator i= subqueryIndexes.iterator(); i.hasNext(); )
	groupData.prepareIteration(((Integer)i.next()).intValue());
    }

    static ListGroupData getListGroupData(QueryTag t)
    {
      return (ListGroupData)t.getPageContext().getAttribute(LIST_GROUP_DATA);
    }
    
    int start(QueryTag t) throws LogicException
    {
      t.index=0;
      ListGroupData groupData= getListGroupData(t);
      Vector results= groupData.prepareIteration(queryIndex);
      if(results==null || 0== results.size())
	return 0;
      groupData.push(queryIndex, t.index);
      getSubqueryResults(groupData);
      return results.size;
    }

    boolean hasMore(QueryTag t)
    {
      ListGroupData groupData= getListGroupData(t);
      groupData.pop();
      t.index++;
      Vector v= groupData.results[queryIndex];
      if(t.index == v.size())
	return false;
      groupData.push(queryIndex, t.index);
      getSubqueryResults(groupData);
      return true;
    }
  }

  /** A strategy computed by root mak:list tags at page analysis. 
   * In addition to the data kept by normal list strategies, it contains the queries that are to be executed by all nested mak:list's and mak:value's */
  class RootQueryTagStrategy extends QueryTagStrategy {
    ArrayList queries;
    
    int start(QueryTag t) throws LogicException
    {
      MakumbaSystem.getMakumbaLogger("taglib.performance").fine("---- tag start ---");
      t.stamp= new Date().getTime();

      ListGroupData groupData= new ListGroupData(queries.size());
      t.getPageContext().setAttribute(LIST_GROUP_DATA, groupData);

      Database dbc= MakumbaSystem.getConnectionTo(t.getDatabaseName());
      long l= new java.util.Date().getTime();
      try{
	Attribues a= PageAttributes.getAttributes(t.getPageContext());
	for(int i=0; i<queries.size(); i++)
	  groupData.bigResults[i]=((ComposedQuery)queries.get(i)).execute(dbc, a);

      }finally{ dbc.close(); }
      MakumbaSystem.getMakumbaLogger("taglib.performance")
	.fine("queries: "+(new java.util.Date().getTime()-l)+" ms");

      return super.start(t);
    }
    
    boolean hasMore(QueryTag t)
    {
      boolean ret= super.hasMore();
      if(!ret)
	{
	  t.getPageContext().removeAttribute(LIST_GROUP_DATA);
	  MakumbaSystem.getMakumbaLogger("taglib.performance").fine("tag time: "+(new Date().getTime()-t.stamp)+" ms ");
	}
      return ret;
    }
  }
  
  /** Data allocated at runtime by the root tag, to keep data for all queries executed, corresponding to all mak:list's and mak:value's inside it (which we call a "list group").
   * The structure is made of arrays, so the number of allocations made at runtime is independent of the actual number of nested mak:list's in the list group. To find its particular data, in an array, the QueryTag will use its QueryTagStrategy.queryIndex
   */
  class ListGroupData{
    static final Dictionary nothing= new ArrayMap();

    ListGroupData(int n)
    {
      bigResults= new Grouper[n];
      results= new Vector[n];
      currentData=new Stack();
      currentData.push(nothing);
    }
    /** The overall results of the queries. 
     * After its query is ran (only once, by the root tag, @see RootQueryStrategy), a mak:list (or special mak:value) will go through parts of bigResults several times, once for each iteration of its parent mak:list. Since it has no parents, the root mak:list will therefore iterate only once. At each iteration, the mak:list (actually its QueryStrategy) will compute the "results" field out of parts of "bigResults"
     * @see results
     */
    Grouper[] bigResults; 

    /** The part of bigResults that is used at the current execution of the mak:list. 
     * At every execution, the mak:list will loop once for each of the results. Note that the mak:list may be executed more than once by its parent mak:list, once for each iteration of the parent. 
     * @see bigResults*/
    Vector[] results; 

    /** A stack made by the current data of the nested lists that are parent of the currently executing list. This data is used by the prepareIteration() to find the current "results" among its "bigResults" 
     */
    Stack currentData;

    /** The parent of the mak:list has iterated, so a new set of results needs be prepared */
    Vector prepareIteration(int queryIndex)
    {
      return results[queryIndex]= bigResults[queryIndex].get(groupData.currentData);
    }

    /** push in "currentData" the result indicated by resultIndex of the query indicated by queryIndex */
    void push(int queryIndex, int resultIndex)
    {
      currentData.push(results[queryIndex].elementAt(resultIndex));
    }

    /** pop from currentData */
    Object pop()
    {
      return currentData.pop();
    }
  }


  /** The index for the iteration through ListGroupData.results */
  int index;

  /*---------------- count var management ----------------*/
  public static final Integer zero= new Integer(0);

  protected void initCounts()
  {
    if(countVar!=null)
      pageContext.setAttribute(countVar, zero);
    if(maxCountVar!=null)
      pageContext.setAttribute(maxCountVar, zero);
  }

  protected void setCount()
  {
    if(countVar!=null)
      pageContext.setAttribute(countVar, new Integer(index+1));
  }

  /** redefined by ObjectTag */
  protected void setMaxCount(int max) throws JspException
  {
    if(maxCountVar!=null)
      pageContext.setAttribute(maxCountVar, new Integer(max));
  }


  /* ------------ usual tag data:  -----------------------*/

  String[] queryProps=new String[4];
  String separator="";

  String countVar;
  String maxCountVar;
  MultipleKey key;
  long stamp; // only used for root

  QueryTagStrategy getStrategy() 
  {
    return (QueryTagStrategy)getPageCache(pageContext).getCache("queryStrategies").get(key);
  }

  public int doMakumbaStart() throws LogicException, JspException
  {
    setKey();

    initCounts();
    int n= getStrategy().start(this);
    setMaxCount(n);
    if(params.get("header")!=null)
      pageContext.include((String)params.get("header"));
    if(n>0)
      {
	setCount();
	return EVAL_BODY_INCLUDE;
      }
    return SKIP_BODY;
  }

  public int doAfterBody() throws JspException
  {
    if(getStrategy().hasMore(this))
      {
	pageContext.getOut().print(separator);
	setCount();
	return EVAL_BODY_AGAIN;
      }
    if(params.get("footer")!=null)
      pageContext.include((String)params.get("footer"));
    return SKIP_BODY;
  }


  public void cleanState()
  {
    super.cleanState();
    queryProps[0]=queryProps[1]=queryProps[2]=queryProps[3]=null;
    countVar=maxCountVar=null;
    separator="";
    key=null;
  }

  void setKey()
  {
    key= new MultipleKey(queryProps.length+1);
    for(int i=0; i<queryProps.length; i++)
      mk.setAt(queryProps[i], i);

    QueryTag parent= (QueryTag)findAncestorWithClass(this, QueryTag.class);

    mk.setAt(parent==null?new Integer(System.identityHashCode
				      (pageContext.getPage().getClass()))
	     :parent.key
	     , queryProps.length);

  }

  public void setFrom(String s) { queryProps[ComposedQuery.FROM]=s; }
  public void setWhere(String s){ queryProps[ComposedQuery.WHERE]=s; }
  public void setOrderBy(String s){ queryProps[ComposedQuery.ORDERBY]=s; }
  public void setGroupBy(String s){ queryProps[ComposedQuery.GROUPBY]=s; }
  public void setSeparator(String s){ separator=s; }
  public void setCountVar(String s){ countVar=s; }
  public void setMaxCountVar(String s){ maxCountVar=s; }

}

