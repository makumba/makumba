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
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

import org.makumba.util.MultipleKey;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;

import org.makumba.controller.html.FieldEditor;
import org.makumba.controller.jsp.PageAttributes;

import org.makumba.LogicException;
import org.makumba.ProgrammerError;

public class InputTag extends MakumbaTag
{
  String name = null;
  String valueExprOriginal = null;
  String dataType = null;
  String display = null;
  String expr = null;

  public String toString() { return "INPUT name="+name+" value="+valueExprOriginal+" dataType="+dataType; }
  

  public void setField(String field)  { setName(field);}
  public void setName(String field) {   this.name=field.trim(); }
  public void setValue(String value) {   this.valueExprOriginal=value.trim(); }
  public void setDataType(String dt) {   this.dataType=dt.trim();  }
  public void setDisplay(String d) {   this.display=d; }

  //Extra html formatting parameters
  public void setAccessKey(String s){ extraFormattingParams.put("accessKey", s); }
  public void setDisabled(String s) { extraFormattingParams.put("disabled", s); }
  public void setOnChange(String s) { extraFormattingParams.put("onChange", s); }
  public void setOnBlur(String s)   { extraFormattingParams.put("onBlur", s); }
  public void setOnFocus(String s)  { extraFormattingParams.put("onFocus", s); }
  public void setOnSelect(String s) { extraFormattingParams.put("onSelect", s); }
  public void setTabIndex(String s) { extraFormattingParams.put("tabIndex", s); }

  FormTagBase getForm() 
  { return (FormTagBase)TagSupport.findAncestorWithClass(this, FormTagBase.class); }

  boolean isValue()
  {
    return expr!=null && !expr.startsWith("$");
  }

  boolean isAttribute()
  {
    return expr!=null && expr.startsWith("$");
  }
  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey() 
  {
    expr=valueExprOriginal;
    if(expr==null)
      expr=getForm().getDefaultExpr(name);
    Object[] keyComponents= {name, getForm().tagKey};
    tagKey= new MultipleKey(keyComponents);
  }

  public MultipleKey getParentListKey()
  {
    MultipleKey k= super.getParentListKey();
    if(k!=null)
      return k;
    getForm().cacheDummyQuery();
    return getForm().tagKey;
  }

  /** determine the ValueComputer and associate it with the tagKey */
  public void doStartAnalyze()
  {
    if(name==null)
      throw new ProgrammerError("name attribute is required in\n"+getTagText());

    if(isValue())
      pageCache.valueComputers.put(tagKey, ValueComputer.getValueComputer(this, expr));
  }

  /** tell the ValueComputer to finish analysis, and set the types for var and printVar */
  public void doEndAnalyze()
  {
    FieldDefinition formType= getForm().getInputType(name);
    FieldDefinition dataTypeInfo=null;  
    FieldDefinition type=null;

    if(dataType!=null)
      {
	dataTypeInfo= MakumbaSystem.makeFieldDefinition(name, dataType);
	if(formType!=null && ! formType.isAssignableFrom(dataTypeInfo))
	  throw new ProgrammerError("declared data type '"+dataType+"' not compatible with the type computed from form '"+formType+"' in \n"+getTagText());
      }

    if(isValue())
      {
	ValueComputer vc= (ValueComputer)pageCache.valueComputers.get(tagKey);
	vc.doEndAnalyze(this);
	type= vc.type;
      }
    if(isAttribute())
      type=(FieldDefinition)pageCache.types.get(expr.substring(1));

    if(type!=null && dataTypeInfo!=null && !dataTypeInfo.isAssignableFrom(type))
      throw new ProgrammerError
	("computed type for INPUT is different from the indicated dataType: \n"+
	 getTagText()+"\n has dataType indicated to '"+ 
	 dataType+ "' type computed is '"+type+"'");

    if(type!=null && formType!=null && !formType.isAssignableFrom(type))
      throw new ProgrammerError
	("computed type for INPUT is different from the type resulting from form analysis:\n"+
	 getTagText()+"\n has form type determined to '"+ 
	 formType+ "' type computed is '"+type+"'");
    
    
    if(type==null && formType==null && dataTypeInfo==null)
      throw new ProgrammerError("cannot determine input type: "+this+
				" .\nPlease specify the type using dataType=...");

    // we give priority to the type as computed from the form
    if(formType==null)
      formType=dataTypeInfo!=null? dataTypeInfo: type;
    
    pageCache.inputTypes.put(tagKey, formType);
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

  public int doMakumbaStartTag() 
  {
    // we do everything in doMakumbaEndTag, to give a chance to the body to set more attributes, etc
    return EVAL_BODY_INCLUDE;
  }

  public int doMakumbaEndTag()throws JspException, LogicException
  {
    FieldDefinition type= (FieldDefinition)pageCache.inputTypes.get(tagKey);
    Object val=null;

    if(isValue())
      val=((ValueComputer)getPageCache(pageContext).valueComputers.get(tagKey)).getValue(this);

    if(isAttribute())
      val=PageAttributes.getAttributes(pageContext).getAttribute(expr.substring(1));

    if(val!=null)
      val=type.checkValue(val);

    params.put(FieldEditor.extraFormattingParam, extraFormatting.toString());
    String formatted=getForm().responder.format(name, type, val, params);

    if(display==null ||! display.equals("false"))
      {
	try{
	  pageContext.getOut().print(formatted);
	}catch(java.io.IOException e)	  {throw new JspException(e.toString());}
      }
    return EVAL_PAGE;
  } 

}
