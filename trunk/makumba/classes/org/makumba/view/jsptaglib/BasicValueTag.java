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
import org.makumba.view.ComposedQuery;
import org.makumba.view.jsptaglib.MakumbaJspAnalyzer.PageCache;

/** This is a a base class for InputTag and OptionTag but may be used for other tags that need to compute a value in similar manner (value="$attribute" or value="OQL expr" */
public abstract class BasicValueTag extends MakumbaTag 
{
  String valueExprOriginal = null;
  /* cannot be set here, subclasses who need it will set it */
  String dataType = null;
  String expr = null;

  public void setValue(String value) {   this.valueExprOriginal=value.trim(); }

  FormTagBase getForm() 
  { return (FormTagBase)TagSupport.findAncestorWithClass(this, FormTagBase.class); }

  boolean isNull(){ return expr==null || expr.trim().length()==0 ||expr.trim().equals("nil"); }

  boolean isValue()
  {
    return expr!=null && !expr.startsWith("$")  && !isNull();
  }

  boolean isAttribute()
  {
    return expr!=null && expr.startsWith("$");
  }

  public MultipleKey getParentListKey(MakumbaJspAnalyzer.PageCache pageCache)
  {
    MultipleKey k= super.getParentListKey(pageCache);
    if(k!=null)
      return k;
    if(isNull())
      return null;

    /** we don't have a query around us, so we must make a dummy query for computing the value via the database */
    getForm().cacheDummyQueryAtAnalysis(pageCache);
    return getForm().tagKey;
  }

  /** determine the ValueComputer and associate it with the tagKey */
  public void doStartAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    if (isValue()) {
        pageCache.valueComputers.put(tagKey, ValueComputer.getValueComputerAtAnalysis(this, checkPtrExpr(expr, pageCache), pageCache));
    }
  }

  protected String checkPtrExpr(String expr2, PageCache pageCache) {
    return expr2;
}

  abstract FieldDefinition getTypeFromContext(MakumbaJspAnalyzer.PageCache pageCache);

  /** tell the ValueComputer to finish analysis, and set the types for var and printVar */
  public void doEndAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    FieldDefinition contextType=getTypeFromContext(pageCache);

    FieldDefinition dataTypeInfo=null;  
    FieldDefinition type=null;

    if(dataType!=null)
      {
	dataTypeInfo= MakumbaSystem.makeFieldDefinition("dummyName", dataType);
	if(contextType!=null && ! contextType.isAssignableFrom(dataTypeInfo))
	  throw new ProgrammerError("declared data type '"+dataType+"' not compatible with the type computed from context '"+contextType+"'");
      }

    if(isValue())
      {
	ValueComputer vc= (ValueComputer)pageCache.valueComputers.get(tagKey);
	vc.doEndAnalyze(this, pageCache);
	type= vc.type;
      }
    if(isAttribute())
      type=(FieldDefinition)pageCache.types.get(expr.substring(1));

    if(type!=null && dataTypeInfo!=null && !dataTypeInfo.isAssignableFrom(type))
      throw new ProgrammerError
	("computed type for INPUT is different from the indicated dataType. The dataType is indicated to '"+ 
	 dataType+ "' type computed is '"+type+"'");

    if(type!=null && contextType!=null && !contextType.isAssignableFrom(type))
      throw new ProgrammerError
	("computed type is different from the type resulting from form analysis. The context type was determined to '"+ 
	 contextType+ "', type computed is '"+type+"'");
    
    
    if(type==null && contextType==null && dataTypeInfo==null)
      throw new ProgrammerError("cannot determine input type. Please specify the type using dataType=...");

    // we give priority to the type as computed from the form
    if(contextType==null)
      contextType=dataTypeInfo!=null? dataTypeInfo: type;
    
    pageCache.inputTypes.put(tagKey, contextType);
  }

  public int doMakumbaEndTag(MakumbaJspAnalyzer.PageCache pageCache)
       throws JspException, LogicException
  {
    FieldDefinition type= (FieldDefinition)pageCache.inputTypes.get(tagKey);
    Object val=null;

    if(isValue())
      val=((ValueComputer)getPageCache(pageContext).valueComputers.get(tagKey)).getValue(this);


    if(isAttribute()){
      val=PageAttributes.getAttributes(pageContext).getAttribute(expr.substring(1));
    }
    
    if(val!=null)
      val=type.checkValue(val);

    return computedValue(val, type);
  }

  /** a value was computed, do what's needed with it, cleanup and return the result of doMakumbaEndTag() */
  abstract int computedValue(Object o, FieldDefinition type) throws JspException, LogicException;
}
