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
import org.makumba.util.*;
import org.makumba.*;
import org.makumba.view.ComposedQuery;
import java.util.*;
import org.makumba.controller.jsp.PageAttributes;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;

public class SetValueStrategy extends QueryStrategy
{
  String label;
  String name;
  QueryTag dummy= new QueryTag();
  public QueryTag getQueryTag(){ return dummy; }
  public ValueTag getValueTag(){ return (ValueTag)tag; }

  protected void adjustQueryProps()
  {
    super.adjustQueryProps();
    name=getValueTag().set.getRelationType().getTitleFieldName();
    label=getValueTag().expr.replace('.', '_');
    getQueryTag().queryProps[ComposedQuery.FROM]=getValueTag().expr+" "+label;
    getQueryTag().queryProps[ComposedQuery.ORDERBY]=label+"."+name;
  }
  int done;

  public void doAnalyze() 
  { 
    super.doAnalyze();
    getQuery().checkProjectionInteger(label);
    if(getValueTag().var==null || getValueTag().printVar!=null)
      getQuery().checkProjectionInteger(label+"."+name);
  }

  public int doStart() throws JspException 
  {
    String     var= getValueTag().var;
    String     printVar= getValueTag().printVar;

    if(var!=null){
      pageContext.setAttribute(var+"_type", getValueTag().set);
      PageAttributes.setAttribute(pageContext, var, null);
    }
    Vector v=new Vector();
    bodyContent=getValueTag().getEnclosingQuery().bodyContent;
    done=super.doStart();
    if(done!=BodyTag.EVAL_BODY_TAG)
	{
	    return done;
	}
    String sep="";
    String total="";
    do{
      Hashtable prms= getValueTag().params;
      insertEvaluation(label, prms, label, null);
      if(var==null || printVar!=null)
	insertEvaluation(label+"."+name, prms, null, label+"_print");

      Object o=pageContext.getAttribute(label);
      if(o instanceof Pointer)
	{
	  v.addElement(o);
	  if(var==null || printVar!=null)
	    total+=sep+pageContext.getAttribute(label+"_print");
	  sep=",";
	}
    }while(super.doAfter()==BodyTag.EVAL_BODY_TAG);
    if(var!=null)
	{
	    pageContext.setAttribute(var+"_type", getValueTag().set);
	    PageAttributes.setAttribute(pageContext, var, v);
	}
    if(printVar!=null)
      PageAttributes.setAttribute(pageContext, printVar, total);
    if(var==null && printVar==null){
      try{
	pageContext.getOut().print(total);
      }catch(java.io.IOException e){ throw new JspException (e.toString()); }
    }
    
    return BodyTag.SKIP_BODY;
  }

  /** write the tag result and go on with the page */
  public int doEnd() throws JspException 
  {
    if(tag.wasException())
      return BodyTag.SKIP_PAGE;
    return BodyTag.EVAL_PAGE;
  }

  public void doRelease() {}

  // nothing to push for subqueries
  public void pushData(){} 

  // nothing to pop for subqueries
  public void popData(){} 
}
