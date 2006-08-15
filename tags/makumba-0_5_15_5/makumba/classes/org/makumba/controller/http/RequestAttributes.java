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
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.makumba.AttributeNotFoundException;
import org.makumba.Attributes;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.UnauthenticatedException;
import org.makumba.UnauthorizedException;
import org.makumba.controller.Logic;
import org.makumba.util.DbConnectionProvider;


public class RequestAttributes implements Attributes
{
  public static final String PARAMETERS_NAME="makumba.parameters";
  public static final String ATTRIBUTES_NAME="makumba.attributes";
  public static final String CONTROLLER_NAME="makumba.controller";

  HttpServletRequest request;
  Object controller;

  public String getRequestDatabase(){ return MakumbaSystem.getDefaultDatabaseName();} 
  public Object getRequestController(){ return controller; }


  public static RequestAttributes getAttributes(HttpServletRequest req)
       throws LogicException
  {
    if(req.getAttribute(ATTRIBUTES_NAME)==null)
      req.setAttribute(ATTRIBUTES_NAME, new RequestAttributes(req));
    return (RequestAttributes)req.getAttribute(ATTRIBUTES_NAME);
  }

  RequestAttributes(HttpServletRequest req) throws LogicException
  {
    this(Logic.getLogic(req.getServletPath()), req, null);
  }

  RequestAttributes(Object controller, HttpServletRequest req, String db) throws LogicException
  {
    if(db==null)
      db=getRequestDatabase();
    this.request=req;
    this.controller=controller;

    if(req.getAttribute(CONTROLLER_NAME+controller.getClass().getName())==null)
      {
	req.setAttribute(CONTROLLER_NAME+controller.getClass().getName(), controller);
	try{
	  Logic.doInit(controller, this, db, getConnectionProvider(req));
	}catch(UnauthorizedException e)
	  {
	    // if we are not in the login page
	    if(!req.getServletPath().endsWith("login.jsp"))
	      throw e;
	  }
      }
  }

  static final public String PROVIDER_ATTRIBUTE="org.makumba.providerAttribute";

  public static DbConnectionProvider getConnectionProvider(HttpServletRequest req){
    DbConnectionProvider prov= (DbConnectionProvider)req.getAttribute(PROVIDER_ATTRIBUTE);
    if(prov==null){
      prov= new DbConnectionProvider();
      req.setAttribute(PROVIDER_ATTRIBUTE, prov);
    }
    return prov;
  }
  

  public static HttpParameters getParameters(HttpServletRequest req)
  {
    if(req.getAttribute(PARAMETERS_NAME)==null)
      req.setAttribute(PARAMETERS_NAME, makeParameters(req));
    return (HttpParameters)req.getAttribute(PARAMETERS_NAME);
  }

  public static HttpParameters makeParameters(HttpServletRequest req) 
  {
    if(req.getContentType()!=null &&
       req.getContentType().indexOf("multipart")!=-1)
      return new MultipartHttpParameters(req);
    return new HttpParameters(req); 
  }

  static public void setAttribute(HttpServletRequest req, String var, Object o)
  {
    if(o!=null)
      {
	req.setAttribute(var, o);
	req.removeAttribute(var+"_null");
      }
    else
      {
	req.removeAttribute(var);
	req.setAttribute(var+"_null", "null");
      }
  }

  public static final Object notFound="not found";

  /**
   * @see org.makumba.Attributes#setAttribute(java.lang.String, java.lang.Object)
   */
  public Object setAttribute(String s, Object o){
    String snull=s+"_null";    
    HttpSession ss= request.getSession(true);

    Object value= ss.getAttribute(s);
    ss.setAttribute(s, o);
    if(o==null)
      ss.setAttribute(snull, "null");
    else
      ss.removeAttribute(snull);
    return value;
  }
  
    /**
     * @see org.makumba.Attributes#removeAttribute(java.lang.String)
     */  
    public void removeAttribute(String s) throws LogicException {
        request.getSession(true).removeAttribute(s);
    }

    /**
     * @see org.makumba.Attributes#hasAttribute(java.lang.String)
     */
    public boolean hasAttribute(String s) {
        try {
            return (checkSessionForAttribute(s) != RequestAttributes.notFound
                    || checkServletLoginForAttribute(s) != RequestAttributes.notFound
                    || checkLogicForAttribute(s) != RequestAttributes.notFound || checkParameterForAttribute(s) != RequestAttributes.notFound);
        } catch (LogicException e) {
            return false;
        }
    }
  
    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        String s = "Session: {";
        HttpSession ss = request.getSession(true);
        Enumeration e = ss.getAttributeNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            s += key + "=" + ss.getAttribute(key);
            if (e.hasMoreElements()) {
                s += ", ";
            }
        }
        s += "}\n";

        e = request.getAttributeNames();
        s += "Request: {";
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            s += key + "=" + request.getAttribute(key);
            if (e.hasMoreElements()) {
                s += ", ";
            }
        }
        s += "}\n";

        e = request.getParameterNames();
        s += "Parameters: {";
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            s += key + "=" + request.getParameter(key);
            if (e.hasMoreElements()) {
                s += ", ";
            }
        }
        s += "}";
        return s;
    }

  /**
   * @see org.makumba.Attributes#getAttribute(java.lang.String)
   */  
  public Object getAttribute(String s) 
       throws LogicException
  {
    Object o= checkSessionForAttribute(s);
    if(o!=notFound)
      return o;

    o= checkServletLoginForAttribute(s);
    if(o!=notFound)
      return o;

    o= checkLogicForAttribute(s);
    if(o!=notFound)
      return o;

    o=checkParameterForAttribute(s);
    if(o!=notFound)
      return o;

    throw new AttributeNotFoundException(s);
  }

  public Object checkSessionForAttribute(String s)
  {
    String snull=s+"_null";
    HttpSession ss= request.getSession(true);
    
    Object value= ss.getAttribute(s);
    if(value!=null)
      return value;
    if(ss.getAttribute(snull)!=null)
      return null;
    value= request.getAttribute(s);
    if(value!=null)
      return value;
    if(request.getAttribute(snull)!=null)
      return null;
    return notFound;
  }

  public Object checkServletLoginForAttribute(String s){
    if(request.getRemoteUser()!=null && request.isUserInRole(s))
      return request.getRemoteUser();
    return notFound;
  }
  
  public Object checkLogicForAttribute(String s)
       throws LogicException
  {
    String snull=s+"_null";
    HttpSession ss= request.getSession(true);
    boolean nullValue=false;
    Object value=null;
    try{
      value=Logic.getAttribute(getRequestController(), s, this, getRequestDatabase(), getConnectionProvider(request));
      if(value==null)
	nullValue=true;
    }catch(NoSuchMethodException e) {}
    catch(UnauthenticatedException ue) { ue.setAttributeName(s); throw ue; }
    // FIXME: should check HTTP argument illegalities
    
    if(value!=null)
      {
	ss.setAttribute(s, value);
	return value;
      }
    if(nullValue)
      {
	ss.removeAttribute(s);
	ss.setAttribute(snull, "x");
	return null;
      }    
    return notFound;
  }


  public Object checkParameterForAttribute(String s)
  {
    Object value= getParameters(request).getParameter(s);
    if(value!=null)
      return value;
    return notFound;
  }
}
