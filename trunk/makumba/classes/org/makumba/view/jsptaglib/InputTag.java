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
import javax.servlet.jsp.tagext.TagSupport;

import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.ProgrammerError;
import org.makumba.controller.jsp.PageAttributes;
import org.makumba.util.MultipleKey;
import org.makumba.Pointer;
import org.makumba.FieldDefinition;
import javax.servlet.jsp.tagext.BodyContent;

public class InputTag extends BasicValueTag
implements javax.servlet.jsp.tagext.BodyTag
{
  String name = null;
  String dataType = null;
  String display=null;
  String nameVar=null;

  // input whith body, used only for chosers as yet
  BodyContent bodyContent=null;
  int bodyContentMark=0;
  org.makumba.util.ChoiceSet choiceSet;

  // unused for now, set when we know at analysis that this input has 
  // a body and will generate a choser (because it has <mak:option > inside)
  boolean isChoser;

  public String toString() { return "INPUT name="+name+" value="+valueExprOriginal+" dataType="+dataType+"\n"; }
  

  public void setField(String field)  { setName(field);}
  public void setName(String field) {   this.name=field.trim(); }
  public void setDisplay(String d) {   this.display=d; }
  public void setNameVar(String var){ this.nameVar=var; }

  //Extra html formatting parameters
  public void setAccessKey(String s){ extraFormattingParams.put("accessKey", s); }
  public void setDisabled(String s) { extraFormattingParams.put("disabled", s); }
  public void setOnChange(String s) { extraFormattingParams.put("onChange", s); }
  public void setOnBlur(String s)   { extraFormattingParams.put("onBlur", s); }
  public void setOnFocus(String s)  { extraFormattingParams.put("onFocus", s); }
  public void setOnSelect(String s) { extraFormattingParams.put("onSelect", s); }
  public void setTabIndex(String s) { extraFormattingParams.put("tabIndex", s); }

  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey(MakumbaJspAnalyzer.PageCache pageCache) 
  {
    expr=valueExprOriginal;
    if(expr==null)
      expr=getForm().getDefaultExpr(name);
    Object[] keyComponents= {name, getForm().tagKey};
    tagKey= new MultipleKey(keyComponents);
  }

  FieldDefinition getTypeFromContext(MakumbaJspAnalyzer.PageCache pageCache){
    return getForm().getInputTypeAtAnalysis(name, pageCache);
  }
 
  /** determine the ValueComputer and associate it with the tagKey */
  public void doStartAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    if(name==null)
      throw new ProgrammerError("name attribute is required");
    super.doStartAnalyze(pageCache);
  }

  /** tell the ValueComputer to finish analysis, and set the types for var and printVar */
  public void doEndAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    if(nameVar!=null)
      pageCache.types.setType(nameVar, MakumbaSystem.makeFieldOfType(nameVar, "char"), this);

    super.doEndAnalyze(pageCache);
  }

  /** Reset and initialise the tag's state, to work in a tag pool. See bug 583. 
   *  If method is overriden in child class, the child's method must call super.initialiseState(). 
   */
  public void initialiseState() {
      super.initialiseState();

      // if type is "file", make the form multipart (should this be here or in doMakumbaStartTag() ?)
      if ("file".equals(params.get("type"))) {
          getForm().setMultipart();
      }
  }

  public void setBodyContent(BodyContent bc){ 
    bodyContent=bc; 
    bodyContentMark=0;
    
    // for now, only chosers can have body
    choiceSet= new org.makumba.util.ChoiceSet();
  }

  public void doInitBody() {}

  public int doMakumbaStartTag(MakumbaJspAnalyzer.PageCache pageCache) 
  {
    // we do everything in doMakumbaEndTag, to give a chance to the body to set more attributes, etc
    return EVAL_BODY_BUFFERED;
  }

  /** a value was computed, do what's needed with it, cleanup and return the result of doMakumbaEndTag() */
  int computedValue(Object val, FieldDefinition type)
       throws JspException, LogicException
  {
    if(bodyContent!=null && bodyContent.getString().trim().length()>0)
      throw new ProgrammerError("cannot have non-whitespace content in a choice mak:input");

    if(choiceSet!=null)
      params.put(org.makumba.util.ChoiceSet.PARAMNAME, choiceSet);

    String formatted=getForm().responder.format(name, type, val, params, extraFormatting.toString());

    if(nameVar!=null)
      getPageContext().setAttribute(nameVar, name+getForm().responder.getSuffix());

    if(display==null ||! display.equals("false"))
      {
	try{
	  pageContext.getOut().print(formatted);
	}catch(java.io.IOException e)	  {throw new JspException(e.toString());}
      }

    name = valueExprOriginal = dataType = display = expr = nameVar= null;
    return EVAL_PAGE;
  } 

}
