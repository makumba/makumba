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
import org.makumba.abstr.FieldInfo;
import org.makumba.DataDefinition;
import org.makumba.LogicException;
import org.makumba.NoSuchFieldException;

import org.makumba.util.MultipleKey;

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

public class FormTagBase extends MakumbaTag  
{
  // the tag attributes
  String baseObject;
  String handler;

  // for add, edit, delete
  public void setObject(String s) { baseObject=s;}

  public void setAction(String s){ responder.setAction(s); }
  public void setHandler(String s){ handler=s; responder.setHandler(s); }
  public void setMethod(String s){ responder.setMethod(s); }
  public void setName(String s){ responder.setResultAttribute(s); }
  public void setMessage(String s){ responder.setMessage(s); }
  public void setMultipart(){ responder.setMultipart(true);}
  
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

  public void doEndAnalyze()
  {
    if(baseObject==null)
      return;
    ValueComputer vc= (ValueComputer)pageCache.valueComputers.get(tagKey);
    vc.doEndAnalyze(this);
    pageCache.basePointerTypes.put(tagKey, ((FieldInfo)vc.type).getPointedType().getName());
  }

  public int doMakumbaStartTag() throws JspException, LogicException
  {
    responder.setOperation(getOperation());
    responder.setBasePointerType((String)pageCache.basePointerTypes.get(tagKey));

    l= new java.util.Date().getTime();

    /** we compute the base pointer */
    String basePointerType=null;
    if(baseObject!=null)
      {
	Object o=((ValueComputer)getPageCache(pageContext).valueComputers.get(tagKey)).getValue(this);
	
	if(!(o instanceof Pointer))
	  throw new RuntimeException("Pointer expected");
	basePointer=((Pointer)o).toExternalForm();
      }

    try{
      responder.setHttpRequest((HttpServletRequest)pageContext.getRequest());
      StringBuffer sb= new StringBuffer();
      responder.writeFormPreamble(sb, basePointer);
      pageContext.getOut().print(sb.toString());
    }catch(LogicException e){ treatException(e); }
    catch(IOException ioe){throw new JspException(ioe.toString()); }
    return EVAL_BODY_INCLUDE; 
  }

  public int doMakumbaEndTag() throws JspException 
  {
    try{
      StringBuffer sb= new StringBuffer();
      responder.writeFormPostamble(sb, basePointer);
      pageContext.getOut().print(sb.toString());
      MakumbaSystem.getMakumbaLogger("taglib.performance").fine("form time: "+ ((new java.util.Date().getTime()-l)));
    }catch(IOException e){ throw new JspException(e.toString()); }
    return EVAL_PAGE;
  }

  // -------------- for input tags to compute types and values 

  public String getDefaultExpr(String fieldName) { return null; }
  public FieldDefinition getDefaultType(String fieldName) { return null; }


  public boolean canComputeTypeFromEnclosingQuery() 
  { return false; }

  public FieldDefinition computeTypeFromEnclosingQuery(String fieldName) 
  { return null;  }

  public static FieldDefinition deriveType(DataDefinition dd, String s)
  {
    int dot=-1;
    while(true)
      {
	int dot1=s.indexOf(".", dot+1);
	if(dot1==-1)
	  return dd.getFieldDefinition(s.substring(dot+1));
	String fname=s.substring(dot+1, dot1);
	FieldDefinition fd=dd.getFieldDefinition(fname);
	if(fd==null)
	  throw new org.makumba.NoSuchFieldException(dd, fname);
	if(!fd.getType().equals("ptr")|| !fd.isNotNull())
	  throw new org.makumba.InvalidFieldTypeException(fd, s+"must be linked via not null pointers, "+fname+" is not");  
	dd=((org.makumba.abstr.FieldInfo)fd).getPointedType();
	dot=dot1;
      }
  }
}

