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
import org.makumba.*;
import org.makumba.view.*;
import org.makumba.util.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;
import java.util.*;

/** Display of OQL query results in nested loops. The Query FROM, WHERE, GROUPBY and ORDERBY are indicated in the head of the tag. The query projections are indicated by Value tags in the body of the tag. The tag can learn more projections during the various threads of execution. The sub-tags will generate subqueries of their enclosing tag queries (i.e. their WHERE, GROUPBY and ORDERBY are concatenated). Attributes of the environment can be passed as $attrName to the query */
public class QueryTag extends MakumbaBodyTag 
{
  String[] queryProps=new String[4];
  String separator="";

  String countVar;
  String maxCountVar;


  public void setParent(Tag t)
  {
    super.setParent(t);
    if(getMakumbaParent()==null)
      pageContext.setAttribute(getRootDataName(), new RootData(this, pageContext), PageContext.PAGE_SCOPE);
  }
  
  public int doEndTag() throws JspException
  {
    try{
      return super.doEndTag();
    }    finally
      {
	if(getMakumbaParent()==null)
	  {
	    getRootData().close();
	    pageContext.removeAttribute(getRootDataName(), PageContext.PAGE_SCOPE);
	  }
      }
  }

  protected Class getParentClass(){ return QueryTag.class; }
  
  public void cleanState()
  {
    super.cleanState();
    queryProps[0]=queryProps[1]=queryProps[2]=queryProps[3]=null;
    countVar=maxCountVar=null;
    separator="";
  }

  MultipleKey getBasicKey()
  {
    MultipleKey mk= new MultipleKey(queryProps.length+2);
    for(int i=0; i<queryProps.length; i++)
      mk.setAt(queryProps[i], i);
    mk.setAt(getKeyDifference(), queryProps.length);
    return mk;
  }

  public Object getRootRegistrationKey()
  {
    MultipleKey mk= getBasicKey();
    mk.setAt(new Integer(System.identityHashCode
			 (pageContext.getPage().getClass())), 
	     queryProps.length+1);
    //    System.out.println(mk);
    return mk;
  }

  public Object getRegistrationKey()
  {
    MultipleKey mk= getBasicKey();
    mk.setAt(getEnclosingQuery().getKey(), queryProps.length+1);
    //    System.out.println(mk);
    return mk;
  }

  public Object getKeyDifference(){ return "VIEW"; }

  public RootTagStrategy makeRootStrategy(Object key)
  { return new RootQueryStrategy((QueryStrategy)makeNonRootStrategy(key)); }

  public TagStrategy makeNonRootStrategy(Object key)
  { return new QueryStrategy(); }

  /** return true */
  protected boolean canBeRoot() {return true; }


  public void setFrom(String s) { queryProps[ComposedQuery.FROM]=s; }
  public void setWhere(String s){ queryProps[ComposedQuery.WHERE]=s; }
  public void setOrderBy(String s){ queryProps[ComposedQuery.ORDERBY]=s; }
  public void setGroupBy(String s){ queryProps[ComposedQuery.GROUPBY]=s; }
  public void setSeparator(String s){ separator=s; }
  public void setCountVar(String s){ countVar=s; }
  public void setMaxCountVar(String s){ maxCountVar=s; }

}

