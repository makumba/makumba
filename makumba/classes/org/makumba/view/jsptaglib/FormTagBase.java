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
import org.makumba.controller.html.FormResponder;

import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.FieldDefinition;
import org.makumba.DataDefinition;

import org.makumba.LogicException;
import org.makumba.NoSuchFieldException;
import org.makumba.ProgrammerError;

import org.makumba.util.MultipleKey;

import javax.servlet.jsp.tagext.BodyTag;
import javax.servlet.jsp.tagext.BodyContent;

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

public class FormTagBase extends MakumbaTag implements BodyTag
{
  // the tag attributes
  String baseObject;
  String handler;

  BodyContent bodyContent;

  public void setBodyContent(BodyContent bc){ bodyContent=bc; }
  public void doInitBody() {}

  // for add, edit, delete
  public void setObject(String s) { baseObject=s;}

  public void setAction(String s){ responder.setAction(s); }
  public void setHandler(String s){ handler=s; responder.setHandler(s); }
  public void setMethod(String s){ responder.setMethod(s); }
  public void setName(String s){ responder.setResultAttribute(s); }
  public void setMessage(String s){ responder.setMessage(s); }
  public void setMultipart() { responder.setMultipart(true); }
  
  FormResponder responder= new FormResponder();
  
  String getOperation()
  {
    String classname= getClass().getName();

    if(classname.endsWith("FormTagBase"))
      return "simple";
    int n= classname.lastIndexOf("Tag");
    if(n!=classname.length()-3)
      throw new RuntimeException("the tag class name was expected to end with \'Tag\': "+classname);
    classname= classname.substring(0, n);
    int m=classname.lastIndexOf(".");
    return classname.substring(m+1).toLowerCase();
  }

  long l;
  String basePointer;
  
  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey()
  {
    Object[] keyComponents= {baseObject, handler, getParentListKey(), getClass()};
    tagKey=new MultipleKey(keyComponents);
  }

  public void doStartAnalyze()
  {
    if(baseObject==null)
      return;
    ValueComputer vc= ValueComputer.getValueComputer(this, baseObject);
    pageCache.valueComputers.put(tagKey, vc);
  }

  static final Object dummy=new Object();

  public void doEndAnalyze()
  {
    if(responder.getAction()==null)
      throw new ProgrammerError("Forms must have either action= defined, or an enclosed <mak:action>...</mak:action>");
    if(baseObject==null)
      return;
    ValueComputer vc= (ValueComputer)pageCache.valueComputers.get(tagKey);
    vc.doEndAnalyze(this);
    pageCache.basePointerTypes.put(tagKey, vc.type.getReferredType().getName());
  }

  public int doMakumbaStartTag() throws JspException, LogicException
  {
    responder.setOperation(getOperation());
    responder.setExtraFormatting(extraFormatting);
    responder.setBasePointerType((String)pageCache.basePointerTypes.get(tagKey));

    l= new java.util.Date().getTime();

    /** we compute the base pointer */
    if(baseObject!=null)
      {
	Object o=((ValueComputer)getPageCache(pageContext).valueComputers.get(tagKey)).getValue(this);
	
	if(!(o instanceof Pointer))
	  throw new RuntimeException("Pointer expected");
	basePointer=((Pointer)o).toExternalForm();
      }
    try{
      responder.setHttpRequest((HttpServletRequest)pageContext.getRequest());
    }catch(LogicException e){ treatException(e); }

    return EVAL_BODY_BUFFERED; 
  }

  public int doMakumbaEndTag() throws JspException 
  {
    try{
      StringBuffer sb= new StringBuffer();
      responder.writeFormPreamble(sb, basePointer);
      bodyContent.getEnclosingWriter().print(sb.toString()); 

      bodyContent.writeOut(bodyContent.getEnclosingWriter());

      sb= new StringBuffer();
      responder.writeFormPostamble(sb, basePointer);
      bodyContent.getEnclosingWriter().print(sb.toString()); 
      MakumbaSystem.getMakumbaLogger("taglib.performance").fine("form time: "+ ((new java.util.Date().getTime()-l)));
    }catch(IOException e){ throw new JspException(e.toString()); }
    return EVAL_PAGE;
  }

  /** The default expression for an input tag, if none is indicated */
  public String getDefaultExpr(String fieldName) { return null; }

  /** The basic data type inside the form. null for generic forms */
  public DataDefinition getDataType(){return null; }

  /** The type of an input tag */
  public FieldDefinition getInputType(String fieldName) 
  { 
    DataDefinition dd= getDataType();
    if(dd==null)
      return null;
    int dot=-1;
    while(true)
      {
	int dot1=fieldName.indexOf(".", dot+1);
	if(dot1==-1)
	  return dd.getFieldDefinition(fieldName.substring(dot+1));
	String fname=fieldName.substring(dot+1, dot1);
	FieldDefinition fd=dd.getFieldDefinition(fname);
	if(fd==null)
	  throw new org.makumba.NoSuchFieldException(dd, fname);
	if(!(fd.getType().equals("ptr") && fd.isNotNull()) && !fd.getType().equals("ptrOne"))
	  throw new org.makumba.InvalidFieldTypeException(fieldName+" must be linked via not null pointers, "+fd.getDataDefinition().getName()+"->"+fd.getName()+" is not");  
	dd= fd.getReferredType();
	dot=dot1;
      }
  }
}

