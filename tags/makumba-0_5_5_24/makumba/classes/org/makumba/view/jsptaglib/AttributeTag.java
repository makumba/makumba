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
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.*;
import org.makumba.*;
import org.makumba.util.*;

public class AttributeTag extends MakumbaTag
{
  String name;
  String var;
  String exceptionVar;

  protected boolean canBeRoot()  { return true; }

  protected Class getParentClass()  { return MakumbaTag.class; }
  public TagStrategy makeStrategy(Object key){ return this; }

  public void setName(String s){ this.name=s; }
  public void setVar(String s){ this.var=s; }
  public void setExceptionVar(String s){ this.exceptionVar=s; }
  
  /** ask the enclosing query to present the expression */
  public int doStart() throws JspException 
  {
    Object o= null;
    Throwable t=null;
    try{
      o=(new HttpAttributes(pageContext, getDatabaseName())).getAttribute(name);
    }catch(Throwable t1) {t=t1; }
    if(t!=null)
      if(exceptionVar==null)
	{ treatException(t); return BodyTag.SKIP_PAGE; }
      else
	{
	  pageContext.setAttribute(exceptionVar, t);	
	  if(t instanceof AttributeNotFoundException)
	    pageContext.setAttribute(name+"_null", "null");
	}
    if(var==null)
      if(t==null)
	{
	  try
	    {
	      pageContext.getOut().print(o);
	    }catch(java.io.IOException e){ throw new JspException(e.toString()); }
	}
      else ;
    else
      HttpAttributes.setAttribute(pageContext, var, o);

    return EVAL_BODY_INCLUDE;
  }
}



