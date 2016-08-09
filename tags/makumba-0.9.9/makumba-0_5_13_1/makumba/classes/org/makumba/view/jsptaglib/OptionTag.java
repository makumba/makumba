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

import org.makumba.ProgrammerError;
import org.makumba.FieldDefinition;
import org.makumba.util.MultipleKey;
import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.JspException;

public class OptionTag extends BasicValueTag implements BodyTag
{
  ValueComputer choiceComputer=null;

  public void setTagKey(MakumbaJspAnalyzer.PageCache pageCache) 
  {
    expr=valueExprOriginal;
    if(expr==null)
      expr="nil";
    // a pretty long key but i can't come with a better idea
    Object[] keyComponents= {expr.trim(), getInput().tagKey, getParentListKey(pageCache)};
    tagKey= new MultipleKey(keyComponents);
  }

  InputTag getInput(){
     return (InputTag)findAncestorWithClass(this, InputTag.class);
  }

  FieldDefinition getTypeFromContext(MakumbaJspAnalyzer.PageCache pageCache){
    FieldDefinition t= (FieldDefinition)pageCache.inputTypes.get(getInput().tagKey);
    
    // for now, only sets and pointers are accepted
    if(!(t.getType().startsWith("set") || t.getType().startsWith("ptr")))
      throw new ProgrammerError("Only set and pointer <mak:input > can have options inside");

    return org.makumba.MakumbaSystem.makeFieldDefinition("dummy", "ptr "+t.getRelationType().getName());
  }

  public void doStartAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    if(getInput()==null)
      throw new ProgrammerError("\'option\' tag must be enclosed in a 'input' tag");
    getInput().isChoser=true;
    super.doStartAnalyze(pageCache);
  }

  public void doInitBody(){}

  BodyContent bodyContent;
  public void setBodyContent(BodyContent bc){ bodyContent=bc; }

  public int doMakumbaStartTag(MakumbaJspAnalyzer.PageCache pageCache)
  {
    return EVAL_BODY_BUFFERED;
  }

  /** a value was computed, do what's needed with it, cleanup and return the result of doMakumbaEndTag() */
  int computedValue(Object val, FieldDefinition type) throws JspException, org.makumba.LogicException{
    getInput().choiceSet.add(val, bodyContent.getString(), false, false);
    valueExprOriginal = dataType = expr = null;
    return EVAL_PAGE;
  }
}
