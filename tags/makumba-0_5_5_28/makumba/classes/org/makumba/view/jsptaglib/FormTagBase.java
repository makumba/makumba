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
import javax.servlet.jsp.*;
import java.io.*;

public class FormTagBase extends MakumbaBodyTag  implements RootTagStrategy
{
  public Class getParentClass() { return MakumbaTag.class; }
  public boolean canBeRoot() { return true; }
  protected RootTagStrategy makeRootStrategy(Object key) { return this; }
  public void onInit(TagStrategy ts) {}

  static public final String anonymousLabel="___mak___edited___";

  public String getSubjectLabel(){ return name; }
  public String getMessage() { return message; }
  String action;
  String handler;
  String method="GET";
  String name= anonymousLabel;
  String message="changes done";

  String enclosingLabel;
  boolean multipart=false;

  // for add and edit
  public void setObject(String s) { enclosingLabel=s;}

  public void setAction(String s){ action=s; }
  public void setHandler(String s){ handler=s; }
  public void setMethod(String s){ method=s; }
  public void setName(String s){ name=s; }
  public void setMessage(String s){ message=s; }
  public void setMultipart(){ multipart=true; }

  //  public Object getKeyDifference(){ return "FORM#"+action+"#"+method+"#"; }

  public String getFormKey(){ return action+method+name+message+enclosingLabel+pageContext.getPage().getClass(); }

  FormResponder responder;

  public FormResponder makeResponder(){ return new SimpleFormResponder(); }

  public Object makeBuffer() { return new RootQueryBuffer(); }

  long l;
  public int doStart() throws JspException 
  {
    l= new java.util.Date().getTime();
    responder= makeResponder();
    try{
      responder.init(this);
    }catch(LogicException e){ treatException(e); }
    return EVAL_BODY_TAG; 
  }

  public int doEnd() throws JspException 
  {
    try{
      writeFormPreamble(bodyContent.getEnclosingWriter()); 
      bodyContent.writeOut(bodyContent.getEnclosingWriter());
      writeFormPostamble(bodyContent.getEnclosingWriter());
      MakumbaSystem.getMakumbaLogger("taglib.performance").fine("form time: "+ ((new java.util.Date().getTime()-l)));
    }catch(IOException e){ throw new JspException(e.toString()); }
    return EVAL_PAGE;
  }

  public void writeFormPreamble(JspWriter pw) throws JspException, IOException
  {
    pw.print("<form action=");
    pw.print("\""+action+"\"");
    pw.print(" method=");
    pw.print("\""+method+"\"");
    if(multipart)
      pw.print(" enctype=\"multipart/form-data\" ");
    pw.print(">");
  }

  public void writeFormPostamble(JspWriter pw) throws JspException, IOException
  {
    if(enclosingLabel!=null)
      writeBasePointer(pw);
    responder.writeInput(pw, getEditedType());
    pw.print("</form>");
  }

  public void writeBasePointer(JspWriter pw) throws JspException, IOException
  {
    responder.writeBasePointer(pw, getBasePointer());
  }

  public String getBasePointer() throws JspException
  {
    Object o= ValueTag.evaluate(enclosingLabel, this);
    if(o instanceof Pointer)
      {
	responder.pointerType=((FieldInfo)pageContext.getAttribute(ValueTag.EVAL_BUFFER+"_type")).getPointedType().getName();

	return ((Pointer)o).toExternalForm();
      }
    return null;
  }

  public String getEditedType(){ return responder.pointerType; }

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

class SimpleFormResponder extends FormResponder
{
  public int getIdentity(String type) {return getSimpleIdentity(); }

  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doOp(controller, handler, getHttpData(pc), makeAttributes(pc), database);
  }
}


