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
import org.makumba.abstr.*;

public class InputTag extends MakumbaTag
{
  String name;
  String valueExprOriginal;
  String dataType;
  FieldInfo dataTypeInfo;
  String display;

  /** demand a QueryTag enclosing query */
  protected Class getParentClass(){ return FormTagBase.class; }

  public String toString() { return "INPUT name="+name+" value="+valueExprOriginal+" dataType="+dataType; }
  

  /** return false, register an exception */ 
  protected boolean canBeRoot()
  {
    treatException(new MakumbaJspException(this, "INPUT tag should always be enclosed in a form, editForm, newForm or addForm tag"));
    return false;
  }

  /** set the name */
  public void setField(String field)  { setName(field);}

  /** set the name */
  public void setName(String field) {   this.name=field.trim(); }

  /** set the expression */
  public void setValue(String value) {   this.valueExprOriginal=value.trim(); }

  /** set the type */
  public void setDataType(String dt) {   this.dataType=dt.trim();  }

  /** set display */
  public void setDisplay(String d) {   this.display=d; }

  public void setType(String s) 
  {
    super.setType(s);
    if(s.equals("file"))
      getForm().setMultipart();
  }  

  FormTagBase getForm() { return (FormTagBase)getMakumbaParent(); }


  /** ask the enclosing query to present the expression */
  public int doStart() throws JspException, org.makumba.LogicException
  {
    if(dataType!=null)
      dataTypeInfo=FieldInfo.getFieldInfo(name, dataType, true);
    if(name==null)
      throw new JspException("name attribute is required");
    try{
      Object val=null;
      Object type=null;
      
      String valueExpr=valueExprOriginal;
      if(valueExpr==null)
	valueExpr=getForm().getDefaultExpr(name);
      if(valueExpr!=null)	
	{
	  String attrName;
	  if(valueExpr.startsWith("$"))
	    attrName=valueExpr.substring(1);
	  else
	    {
	      ValueTag.evaluate(valueExpr, this);
	      attrName=ValueTag.EVAL_BUFFER;
	    }
	  val=getAttributes().getAttribute(attrName);
	  try{
	      type=getAttributes().getAttribute(attrName+"_type");
	  }catch(AttributeNotFoundException anfe){ }
	  if(type!=null && type.equals("unknown yet"))
	    return EVAL_BODY_INCLUDE;
	}
      else
	{
	  type= getForm().getDefaultType(name);
	  
	  if(type==null &&
	     getForm().canComputeTypeFromEnclosingQuery() &&
	     (type=getForm().computeTypeFromEnclosingQuery(getEnclosingQuery(), name))
	     ==null)
	    return EVAL_BODY_INCLUDE;
	}
      
      if(dataTypeInfo!=null)
	if(type!=null && !dataTypeInfo.compatible(FieldInfo.getFieldInfo(name, type, true)))
	  throw new InvalidValueException("computed type for INPUT is different from the indicated dataType: "+this+" has dataType indicated to "+ dataType+ " type computed is "+type+" , value known is "+val);
	else
	  type=dataTypeInfo;
      
      if(type==null)
	throw new InvalidValueException("cannot determine input type: "+this+" value known: "+val+" . Please specify the type using dataType=...");
      
      String formatted=getForm().responder.format(name, type, val, getRootQueryBuffer().bufferParams);
      if(display==null ||! display.equals("false"))
	{
	  try{
	    getForm().bodyContent.print(formatted);
	  }catch(java.io.IOException e)	  {throw new JspException(e.toString());}
	}
      return EVAL_BODY_INCLUDE;
    }finally
      {
	getRootQueryBuffer().bufferParams.clear();
      }
  }
}
