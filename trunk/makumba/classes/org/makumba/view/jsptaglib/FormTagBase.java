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
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.controller.html.FormResponder;
import org.makumba.util.MultipleKey;
import org.makumba.view.ComposedQuery;

public class FormTagBase extends MakumbaTag implements BodyTag
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
// the tag attributes
  String baseObject = null;
  String handler = null;
  String formMethod = null;
  String formAction = null;
  String formName = null;
  String formMessage = null;
  FormResponder responder = null;

  long starttime;
  String basePointer = null;

  BodyContent bodyContent = null;

  public void setBodyContent(BodyContent bc){ bodyContent=bc; }
  public void doInitBody() {}

  // for add, edit, delete
  public void setObject(String s) { baseObject=s;}

  public void setAction(String s){ formAction=s; }
  public void setHandler(String s){ handler=s; }
  public void setMethod(String s){ checkNoParent("method"); formMethod=s; }
  public void setName(String s){ 
	formName = s;
	extraFormattingParams.put("name", s); 
  }
  public void setMessage(String s){ checkNoParent("message"); formMessage = s ; }
  public void setMultipart() { 
    FormTagBase parent= findParentForm();
    if(parent!=null) // propagate multipart to the root form
      parent.setMultipart();
    else
      responder.setMultipart(true); 
  }

  //additional html attributes:
  public void setTarget(String s)   
  { checkNoParent("target"); extraFormattingParams.put("target", s); }
  public void setOnReset(String s)  
  { checkNoParent("onReset"); extraFormattingParams.put("onReset", s); }
  public void setOnSubmit(String s) 
  { checkNoParent("onSubmit"); extraFormattingParams.put("onSubmit", s); }
  // setName is defined above (approx 10 lines higher)
  
  
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

  boolean shouldComputeBasePointer(){
    return baseObject!=null;
  }
  
  /** Set tagKey to uniquely identify this tag. Called at analysis time before doStartAnalyze() and at runtime before doMakumbaStartTag() */
  public void setTagKey(MakumbaJspAnalyzer.PageCache pageCache)
  {
    Object[] keyComponents= {baseObject, handler, getParentListKey(null), getClass()};
    tagKey=new MultipleKey(keyComponents);
  }

  static final String[] dummyQuerySections= {null, null, null, null, null};
 
  /** cache a dummy query when there is no mak:list around us */
  public void cacheDummyQueryAtAnalysis(MakumbaJspAnalyzer.PageCache pageCache)
  {
    pageCache.cacheQuery(tagKey, dummyQuerySections, null);
  }


  public void doStartAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    if(!shouldComputeBasePointer())
      return;
    ValueComputer vc= ValueComputer.getValueComputerAtAnalysis(this, baseObject, pageCache);
    pageCache.valueComputers.put(tagKey, vc);
  }

  FormTagBase findParentForm(){
    return (FormTagBase)findAncestorWithClass(this, FormTagBase.class);
  }

  FormTagBase findRootForm(){
    FormTagBase parent=findParentForm();
    if(parent==null)
      return this;
    return parent.findRootForm();
  }
  
  void checkNoParent(String attrName){
    if(findParentForm()!=null)
      throw new ProgrammerError("Forms included in other forms cannot have a '"+attrName+"' attribute");
  }

  public void doEndAnalyze(MakumbaJspAnalyzer.PageCache pageCache)
  {
    ComposedQuery dummy= (ComposedQuery)pageCache.queries.get(tagKey);
    if(dummy!=null)
      dummy.analyze();
    if(formAction==null && findParentForm()==null)
      throw new ProgrammerError("Forms must have either action= defined, or an enclosed <mak:action>...</mak:action>");
    if(findParentForm()!=null){
      if(formAction!=null)
	throw new ProgrammerError("Forms included in other forms cannot have action= defined, or an enclosed <mak:action>...</mak:action>");
    }
    if(!shouldComputeBasePointer())
      return;
    ValueComputer vc= (ValueComputer)pageCache.valueComputers.get(tagKey);
    vc.doEndAnalyze(this, pageCache);
    pageCache.basePointerTypes.put(tagKey, vc.type.getPointedType().getName());
  }

  /** Reset and initialise the tag's state, to work in a tag pool. See bug 583. 
   *  If method is overriden in child class, the child's method must call super.resetState(). 
   */
  public void initialiseState() {
      super.initialiseState();
      	  
      responder= new FormResponder();
      if (formName    != null) responder.setResultAttribute(formName);
      if (handler != null)     responder.setHandler(handler);
      if (formAction  != null) responder.setAction(formAction); 
      if (formMethod  != null) responder.setMethod(formMethod); 
      if (formMessage != null) responder.setMessage(formMessage); 

      if(findParentForm()!=null) 
	responder.setParentResponder(findParentForm().responder, findRootForm().responder);
  }

  public int doMakumbaStartTag(MakumbaJspAnalyzer.PageCache pageCache) 
       throws JspException, LogicException
  {
    // if we have a dummy query, we simulate an iteration
    if(pageCache.queries.get(tagKey)!=null)
      {
	QueryExecution.startListGroup(pageContext);
	QueryExecution.getFor(tagKey, pageContext, null, null).onParentIteration();
      }

    responder.setOperation(getOperation());
    responder.setExtraFormatting(extraFormatting);
    responder.setBasePointerType((String)pageCache.basePointerTypes.get(tagKey));

    starttime = new java.util.Date().getTime();

    /** we compute the base pointer */
    if(shouldComputeBasePointer())
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

  public int doMakumbaEndTag(MakumbaJspAnalyzer.PageCache pageCache) 
       throws JspException 
  {
    // if we have a dummy query, we simulate end iteration
    if(pageCache.queries.get(tagKey)!=null)
      QueryExecution.endListGroup(pageContext);
    try{
      StringBuffer sb= new StringBuffer();
      responder.writeFormPreamble(sb, basePointer);
      bodyContent.getEnclosingWriter().print(sb.toString()); 

      bodyContent.writeOut(bodyContent.getEnclosingWriter());

      sb= new StringBuffer();
      responder.writeFormPostamble(sb, basePointer, pageContext.getSession().getId());
   
      bodyContent.getEnclosingWriter().print(sb.toString()); 
      if(findParentForm()!=null)
	MakumbaSystem.getMakumbaLogger("taglib.performance").fine("form time: "+ ((new java.util.Date().getTime()-starttime)));
    }catch(IOException e){ throw new JspException(e.toString()); }
    finally{
      baseObject = handler = formMethod = formAction = formName =  formMessage = basePointer= null;
      responder = null;
      bodyContent = null;
    }
    return EVAL_PAGE;
  }



  /** The default expression for an input tag, if none is indicated */
  public String getDefaultExpr(String fieldName) { return null; }

  /** The basic data type inside the form. null for generic forms */
  public DataDefinition getDataTypeAtAnalysis(MakumbaJspAnalyzer.PageCache pageCache){return null; }

  /** The type of an input tag */
  public FieldDefinition getInputTypeAtAnalysis(String fieldName, MakumbaJspAnalyzer.PageCache pageCache) 
  { 
    DataDefinition dd= getDataTypeAtAnalysis(pageCache);
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
	dd= fd.getPointedType();
	dot=dot1;
      }
  }
  
}

