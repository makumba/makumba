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
import java.util.*;
import org.makumba.*;
import org.makumba.util.*;
import org.makumba.controller.jsp.PageAttributes;

public class ValueTag extends MakumbaTag
{
  String expr;
  String var;
  String printVar;

  FieldDefinition set;

  public void cleanState()
  {
    super.cleanState();
    expr=var=printVar=null;
    set=null;
  }

  static final String EVAL_BUFFER="makumba.eval.buffer";

  public static Object evaluate(String s, MakumbaTag t) throws JspException
  {
    ValueTag vt= new ValueTag();
    vt.setPageContext(t.getPageContext());
    vt.setParent(findAncestorWithClass(t, QueryTag.class));
    vt.setExpr(s);
    vt.setVar(EVAL_BUFFER);
    vt.doStartTag();
    return t.getPageContext().getAttribute(EVAL_BUFFER);
  }

  public Object getRegistrationKey() throws LogicException
  {
    String expr=this.expr.trim();
    QueryStrategy p=getParentQueryStrategy();
    Object check= p.query.checkExprSetOrNullable(expr, PageAttributes.getAttributes(pageContext));
    if(check==null)
      // an usual, non-nullable, non-set expression
      return null;
    MultipleKey mk= new MultipleKey((Vector)p.key, 10);
    mk.setAt(expr, 6);
    if(check instanceof String)
      mk.setAt((String)check, 7);
    else 
      {
	set=(FieldDefinition)check;
	mk.setAt(set.getName(), 7);
      }
    mk.setAt(var, 8);
    mk.setAt(printVar, 9);
    return mk;
  }
  
  public TagStrategy makeNonRootStrategy(Object key)
  {
    if(key==null)
      return this;
    Object o=((MultipleKey)key).elementAt(7);
    if(set==null)
      return getParentQueryStrategy().getNullableStrategy(o);
    return new SetValueStrategy();
  }
  

  /** demand a QueryTag enclosing query */
  protected Class getParentClass(){ return QueryTag.class; }

  public String toString() { 
    return "VALUE expr="+expr+ 
      " parameters: "+ params; 
  }

  /** return false, register an exception */ 
  protected boolean canBeRoot()
  {
    treatException(new MakumbaJspException(this, "VALUE tag should always be enclosed in a LIST or OBJECT tag"));
    return false;
  }

  /** set the expression */
  public void setExpr(String expr)
  { 
    this.expr=expr;
  }

  /** set the expression */
  public void setVar(String var)
  { 
    this.var=var;
  }

  /** set the expression */
  public void setPrintVar(String var)
  { 
    this.printVar=var;
  }
  
  /** ask the enclosing query to present the expression */
  public int doStart() throws JspException 
  {
    getParentQueryStrategy().insertEvaluation(this);
    return EVAL_BODY_INCLUDE;
  }
}
