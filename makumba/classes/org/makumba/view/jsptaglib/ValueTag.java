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
import org.makumba.MakumbaSystem;
import org.makumba.LogicException;

import org.makumba.util.MultipleKey;
import org.makumba.controller.jsp.PageAttributes;
import org.makumba.view.html.RecordViewer;
import org.makumba.view.ComposedQuery;

import javax.servlet.jsp.JspException;

import java.util.Vector;

public class ValueTag extends MakumbaTag
{
  String expr;
  String var;
  String printVar;
  String defaultValue;
  
  public void setExpr(String expr){ this.expr=expr; }
  public void setVar(String var){ this.var=var; }
  public void setPrintVar(String var){ this.printVar=var; }
  public void setDefault(String var){this.defaultValue=var; }
  
  /** cleanup the state to make this reusable */
  public void cleanState()
  {
    super.cleanState();
    expr=var=printVar=null;
  }
  
  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey()
  {
    addToParentListKey(expr.trim());
  }

  /** determine the ValueComputer and associate it with the tagKey */
  public void doStartAnalyze()
  {
    pageCache.valueComputers.put(tagKey, ValueComputer.getValueComputer(this, expr));
  }
  
  /** tell the ValueComputer to finish analysis, and set the types for var and printVar */
  public void doEndAnalyze()
  {
    ValueComputer vc= (ValueComputer)pageCache.valueComputers.get(tagKey);
    vc.doEndAnalyze(this);

    if(var!=null)
      pageCache.types.setType(var, vc.type, this);

    if(printVar!=null)
      pageCache.types.setType(printVar, MakumbaSystem.makeFieldOfType(printVar, "char"), this);
  }
  
  /** ask the ValueComputer to present the expression */
  public int doMakumbaStartTag() throws JspException, org.makumba.LogicException
  {
    ((ValueComputer)getPageCache(pageContext).valueComputers.get(tagKey)).print(this);
    
    return EVAL_BODY_INCLUDE;
  }

  public String toString() { 
    return "VALUE expr="+expr+ 
      " parameters: "+ params; 
  }
}
