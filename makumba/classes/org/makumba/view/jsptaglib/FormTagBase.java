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
import org.makumba.view.*;
import org.makumba.*;
import org.makumba.abstr.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import java.io.*;
import org.makumba.controller.html.FormResponder;

public class FormTagBase extends MakumbaBodyTag  implements RootTagStrategy
{
  // jsptaglib1-specific methods
  public Class getParentClass() { return MakumbaTag.class; }
  public boolean canBeRoot() { return true; }
  protected RootTagStrategy makeRootStrategy(Object key) { return this; }
  public void onInit(TagStrategy ts) {}

  // the tag attributes
  String baseObject;

  // for add, edit, delete
  public void setObject(String s) { baseObject=s;}

  public void setAction(String s){ responder.setAction(s); }
  public void setHandler(String s){ responder.setHandler(s); }
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
  
  public void doAnalyze()
  {
    if(baseObject!=null)
      getEnclosingQuery().getQuery().checkProjectionInteger(baseObject);
  }

  public int doStart() throws JspException 
  {
    l= new java.util.Date().getTime();

    responder.setOperation(getOperation());

    /** we compute the base pointer */
    String basePointerType=null;
    if(baseObject!=null)
      {
	int n= getEnclosingQuery().getQuery().checkProjectionInteger(baseObject).intValue();
	Object o= getEnclosingQuery().getProjectionValue(n);
	if(!(o instanceof Pointer))
	  throw new RuntimeException("Pointer expected");
	responder.setBasePointerType
	  (((FieldInfo)getEnclosingQuery().getQuery().getResultType().getFieldDefinition(n))
	   .getPointedType().getName());
	basePointer=((Pointer)o).toExternalForm();
      }

    try{
      responder.setHttpRequest((HttpServletRequest)pageContext.getRequest());
    }catch(LogicException e){ treatException(e); }
    return EVAL_BODY_TAG; 
  }

  public int doEnd() throws JspException 
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

  // -------------- for input tags to compute types and values 

  public String getDefaultExpr(String fieldName) { return null; }
  public FieldDefinition getDefaultType(String fieldName) { return null; }


  public boolean canComputeTypeFromEnclosingQuery() 
  { return false; }

  public FieldDefinition computeTypeFromEnclosingQuery(QueryStrategy qs, String fieldName) 
  {
    return null;
  }

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

