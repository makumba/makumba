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

public class InputTag extends ValueTag
{
  String name;
  String valueExprOriginal;
  String dataType;
  String display;

  // -------- page analysis
  FieldInfo dataTypeInfo;

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

  boolean isValue()
  {
    return expr!=null && !expr.startsWith("$");
  }

  public Object getRegistrationKey() 
  {
    expr=valueExprOriginal;
    if(expr==null)
      expr=getForm().getDefaultExpr(name);
    if(!isValue())
      return null;
    var= expr.replace('.', '_');
    return super.getRegistrationKey();
  }

  public TagStrategy makeNonRootStrategy(Object key)
  {
    if(isValue())
      return super.makeNonRootStrategy(key);
    return this;
  }

  public void doAnalyze()
  {
    if(name==null)
      throw new RuntimeException("name attribute is required");

    if(isValue())
      super.doAnalyze();
  }

    public int doStartTag() throws JspException
  {
      super.doStartTag();
      if(strategy!=this)
	  try{
	      doStart();
	  }catch(Throwable t){ treatException(t); return SKIP_BODY; }
      return EVAL_BODY_INCLUDE;
  }

  /** ask the enclosing query to present the expression */
  public int doStart() throws JspException, org.makumba.LogicException
  {
      if(dataType!=null)
        dataTypeInfo=FieldInfo.getFieldInfo(name, dataType, true);

      Object val=null;
      Object type=null;
      
      if(expr!=null)	
	{
	  String attrName;
	  if(expr.startsWith("$"))
	    attrName=expr.substring(1);
	  else
	    {
		if(strategy==this)
		    super.doStart();
	      attrName=var;
	    }
	  val=getAttributes().getAttribute(attrName);
	  try{
	      type=getAttributes().getAttribute(attrName+"_type");
	  }catch(AttributeNotFoundException anfe){}
	  if(type!=null && type.equals("unknown yet"))
	    throw new RuntimeException("type should be known");
	}
      if(type==null)
	{
	  type= getForm().getDefaultType(name);
	  if(type==null &&
	     getForm().canComputeTypeFromEnclosingQuery() &&
	     (type=getForm().computeTypeFromEnclosingQuery(getEnclosingQuery(), name))
	     ==null)
	    return EVAL_BODY_INCLUDE;
	}

      if(type!=null && !(type instanceof FieldInfo))
	  type= FieldInfo.getFieldInfo(name, type, true);

      if(dataTypeInfo!=null)
	  if(type!=null && !dataTypeInfo.compatible((FieldInfo)type))
	     throw new InvalidValueException("computed type for INPUT is different from the indicated dataType: "+this+" has dataType indicated to "+ dataType+ " type computed is "+type+" , value known is "+val);
	  else
	     if(type==null)
	       type=dataTypeInfo;
      
      if(type==null)
	throw new InvalidValueException("cannot determine input type: "+this+" value known: "+val+" . Please specify the type using dataType=...");
      if(val!=null)
	  val=((FieldInfo)type).checkValue(val);

      String formatted=getForm().responder.format(name, type, val, params);
      if(display==null ||! display.equals("false"))
	{
	  try{
	    getForm().bodyContent.print(formatted);
	  }catch(java.io.IOException e)	  {throw new JspException(e.toString());}
	}
      return EVAL_BODY_INCLUDE;
  }
}
