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

package org.makumba.controller.http;
import org.makumba.*;
import org.makumba.util.*;
import org.makumba.abstr.RecordInfo;
import org.makumba.abstr.FieldInfo;
import org.makumba.LogicException;
import org.makumba.controller.Logic;

import java.util.*;
import java.io.*;
import javax.servlet.http.*;
import java.util.logging.Level;
 
/** A responder is created for each form and stored internally, to respond when the form is submitted. To reduce memory space, identical respodners are stored only once */
public abstract class Responder implements java.io.Serializable
{ 
  /** the name of the CGI parameter that passes the responder key, so the responder can be retrieved from the cache, "__makumba__responder__" */
  public final static String responderName="__makumba__responder__";

  /** the default label used to store the add and new result, "___mak___edited___"*/
  static public final String anonymousResult="___mak___edited___";

  /** the default response message, "changes done" */
  static public final String defaultMessage="changes done";

  /** the name of the CGI parameter that passes the base pointer, see {@link basePointerType}, "__makumba__base__" */
  public final static String basePointerName="__makumba__base__";

  /** the responder key, as computed from the other fields*/
  protected int identity;

  /** the controller object, on which the handler method that performs the operation is invoked */
  protected Object controller;

  /** the database in which the operation takes place */
  protected String database;

  /** a response message to be shown in the response page */
  protected String message=defaultMessage;

  /** new and add responders set their result to a result attribute */
  protected String resultAttribute=anonymousResult;

  /** the business logic handler, for simple forms */
  protected String handler;

  /** edit, add and delete makumba operations have a special pointer called 
   the base pointer  */
  protected String basePointerType;
  
  /** the type where the new operation is made*/
  protected String newType;

  /** the field on which the add operation is made */
  protected String addField;

  /** the operation name: add, edit, delete, new, simple */
  protected String operation;

  /** the operation handler, computed from the operation*/
  protected ResponderOp op;


  //--------------- form time, responder preparation -------------------
  /** pass the http request, so the responder computes its default controller and database */
  public void setHttpRequest(HttpServletRequest req) throws LogicException
  {
    controller=RequestAttributes.getAttributes(req).getRequestController();
    database=RequestAttributes.getAttributes(req).getRequestDatabase();
  }

  /** pass the operation */
  public void setOperation(String operation)
  {
    this.operation=operation;
    op= (ResponderOp)responderOps.get(operation);
  }
  
  /** pass the form response message */
  public void setMessage(String message) { this.message=message; }

  /** pass the response handler, if other than the default one */
  public void setHandler(String handler) { this.handler=handler; }
  
  /** pass the base pointer type, needed for the response */
  public void setBasePointerType(String basePointerType) { this.basePointerType=basePointerType;}

  /** pass the name of the result attribute */
  public void setResultAttribute(String resultAttribute) { this.resultAttribute=resultAttribute;}

  /** pass the field to which the add operation is made */
  public void setAddField(String s){ addField=s; }

  /** pass the type on which the new operation is made */
  public void setNewType(DataDefinition dd){ newType=dd.getName(); }

  //--------------- responder caching section ------------------------
  static Hashtable indexedCache= new Hashtable();
  
  static NamedResources cache=new NamedResources
  ("controller.responders", new NamedResourceFactory()
   {
     public Object getHashObject(Object o)
       {
	 return ((Responder)o).responderKey();
       }

     public Object makeResource(Object name, Object hashName)
       {
	 Responder f= (Responder)name;
	 f.identity= hashName.hashCode();
	 indexedCache.put(new Integer(f.identity), name);
	 return name;
       }
   });

  /** a key that should identify this responder among all */
  public String responderKey()
  {
    return basePointerType+message+resultAttribute+database+operation+controller.getClass().getName()+handler+addField+newType;
  }

  /** get the integer key of this form, and register it if not already registered */
  public int getPrototype() 
  { 
    // at this point we should have all data set, so we should be able to verify the responder
    String s= op.verify(this);
    if(s!=null)
      throw new MakumbaError("Bad responder configuration "+s);
    return ((Responder)cache.getResource(this)).identity; 
  }

  //------------------ multiple form section
  /** the form counter, 0 for the root form, one increment for each subform of this form */
  // NOTE: transient data here is not used during response, only at form building time
  transient int groupCounter=0;

  /** the form suffix, "" for the root form, _+increment for subforms */
  protected transient String storedSuffix="";

  protected transient String storedParentSuffix="";

  static final char suffixSeparator='_';

  /** pass the parent responder */
  public void setParentResponder(Responder resp, Responder root)
  {
    storedSuffix=""+suffixSeparator+(++root.groupCounter);
    storedParentSuffix= resp.storedSuffix;
  }
  
  public String getSuffix(){ return storedSuffix; }

  static Integer ZERO= new Integer(0);
  static Integer suffix(String s)
  {
    int n=s.indexOf(suffixSeparator);
    if(n==-1)
      return ZERO;
    s=s.substring(n+1);
    n=s.indexOf(suffixSeparator);
    if(n!=-1)
      s=s.substring(0, n);
    return new Integer(Integer.parseInt(s));
  }

  static Comparator bySuffix=new Comparator(){
    public int compare(Object o1, Object o2)
      { return suffix((String)o1).compareTo(suffix((String)o2)); }
    public boolean equals(Object o){ return false; }
  };
  
  /** read all responder codes from a request (all code_suffix values of __mak__responder__) and order them by suffix, return the enumeration of responder codes */
  static Iterator getResponderCodes(HttpServletRequest req)
  {
    TreeSet set= new TreeSet(bySuffix);
    
    Object o= RequestAttributes.getParameters(req).getParameter(responderName);
    if(o!=null)
      {
	if(o instanceof String)
	  set.add(o);
	else
	  set.addAll((Vector)o);
      }
    return set.iterator();
  }
  
  //----------------- response section ------------------
  static public final String RESPONSE_STRING_NAME="makumba.response";

  static final String resultNamePrefix= "org.makumba.controller.resultOf_";

  /** respond to a http request */
  static void response(HttpServletRequest req, HttpServletResponse resp)
  {
    if(req.getAttribute(RESPONSE_STRING_NAME)!=null)
      return;
    req.setAttribute(RESPONSE_STRING_NAME, "");
    String message="";
    for(Iterator responderCodes= getResponderCodes(req); responderCodes.hasNext();)
      {
	String code=(String)responderCodes.next();
	String responderCode=code;
	String suffix="";
	String parentSuffix=null;
	int n=code.indexOf(suffixSeparator);
	if(n!=-1)
	  {
	    responderCode=code.substring(0, n);
	    suffix=code.substring(n);
	    parentSuffix="";
	    n= suffix.indexOf(suffixSeparator, 1);
	    if(n!=-1){
	      parentSuffix=suffix.substring(n);
	      suffix=suffix.substring(0, n);
	    }
	  }
	Integer i= new Integer(Integer.parseInt(responderCode));
	Responder fr= ((Responder)indexedCache.get(i));
	if(fr==null)
	  throw new org.makumba.InvalidValueException("Responder cannot be found, probably due to server restart. Please reload the form page.");
	try{
	  Object result=fr.op.respondTo(req, fr, suffix, parentSuffix);
	  message="<font color=green>"+fr.message+"</font>";
	  if(result!=null){
	    req.setAttribute(fr.resultAttribute, result);
	    req.setAttribute(resultNamePrefix+suffix, result);
	  }
	  req.setAttribute("makumba.successfulResponse", "yes");
	}
	catch(AttributeNotFoundException anfe)
	  {
	    // attribute not found is a programmer error and is reported
	    ControllerFilter.treatException(anfe, req, resp); 
	    continue;
	  }
	catch(LogicException e){
	  MakumbaSystem.getLogger("logic.error").log(Level.INFO, "error", e);
	  message=errorMessage(e);
	  req.setAttribute(fr.resultAttribute, Pointer.Null);
	  req.setAttribute(resultNamePrefix+suffix, Pointer.Null);
	}
	catch(Throwable t){
	  // all included error types should be considered here
	  ControllerFilter.treatException(t, req, resp);
	}
	// messages of inner forms are ignored
	if(suffix.equals(""))
	   req.setAttribute(RESPONSE_STRING_NAME, message);
      }
  }

  /** format an error message */
  public static String errorMessage(Throwable t)
  {
    return "<font color=red>"+t.getMessage()+"</font>";
  }

  /** reads the HTTP base pointer */
  public Pointer getHttpBasePointer(HttpServletRequest req, String suffix)
  {
    // for add forms, the result of the enclosing new form may be used
    return new Pointer(basePointerType, (String)RequestAttributes.getParameters(req).getParameter(basePointerName+suffix));
  }

  /** read the data needed for the logic operation, from the http request. 
   * org.makumba.controller.html.FormResponder provides an implementation */
  public abstract Dictionary getHttpData(HttpServletRequest req, String suffix);

  static Hashtable responderOps= new Hashtable();
  static
  {
    responderOps.put("edit", new ResponderOp()
		    {
		      public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix) 
			throws LogicException
			{
			  return Logic.doEdit(resp.controller,
					      resp.basePointerType, 
					      resp.getHttpBasePointer(req, suffix), 
					      resp.getHttpData(req, suffix), 
					      new RequestAttributes(resp.controller, req),
					      resp.database);
			}
		      public String verify(Responder resp){ return null; }
		    });
    
    responderOps.put("simple", new ResponderOp()
		     {
		       public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix) 
			 throws LogicException
			 {
		           return Logic.doOp(resp.controller,
					     resp.handler, 
					     resp.getHttpData(req, suffix), 
					     new RequestAttributes(resp.controller, req),
					     resp.database);
			 }
		       public String verify(Responder resp){ return null; }
		     });

    responderOps.put("new", new ResponderOp()
		     {
		       public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix) 
			 throws LogicException
			 {
		           return Logic.doNew(resp.controller,
					      resp.newType,
					      resp.getHttpData(req, suffix), 
					      new RequestAttributes(resp.controller, req),
					      resp.database);
			 }
		       public String verify(Responder resp){ return null; }
		     });


    responderOps.put("add", new ResponderOp()
		     {
		       public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix) 
			 throws LogicException
			 {
		           return Logic.doAdd(resp.controller,
					      resp.basePointerType+"->"+resp.addField,
					      resp.getHttpBasePointer(req, suffix), 
					      resp.getHttpData(req, suffix), 
					      new RequestAttributes(resp.controller, req),
					      resp.database);
			 }
		       public String verify(Responder resp){ return null; }
		     });

    responderOps.put("addToNew", new ResponderOp()
		     {
		       public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix) 
			 throws LogicException
			 {
		           return Logic.doAdd(resp.controller,
					      resp.newType+"->"+resp.addField,
					      (Pointer)req.getAttribute(resultNamePrefix+parentSuffix), 
					      resp.getHttpData(req, suffix), 
					      new RequestAttributes(resp.controller, req),
					      resp.database);
			 }
		       public String verify(Responder resp){ return null; }
		     });

    responderOps.put("delete", new ResponderOp()
		     {
		       public Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix) 
			 throws LogicException
			 {
		           return Logic.doDelete(resp.controller,
						 resp.basePointerType,
						 resp.getHttpBasePointer(req, suffix),
						 new RequestAttributes(resp.controller, req),
						 resp.database);
			 }

		       public String verify(Responder resp){ return null; }
		     });
  }
}

/** this class helps to differentiate between the different types of forms */
abstract class ResponderOp
{
  /** respond to the given request, with the data from the given responder, read using the given multiple form suffix */
  public abstract Object respondTo(HttpServletRequest req, Responder resp, String suffix, String parentSuffix) 
       throws LogicException;

  /** check the validity of the given responder data, return not-null if there is a problem*/
  public abstract String verify(Responder resp);
}


