package org.makumba.controller.http;
import org.makumba.controller.Logic;
import org.makumba.*;
import javax.servlet.http.*;
import java.util.*;
import org.makumba.*;

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
    this(Logic.getLogic(req.getServletPath()), req);
  }

  RequestAttributes(Object controller, HttpServletRequest req) throws LogicException
  {
    this.request=req;
    this.controller=controller;

    if(req.getAttribute(CONTROLLER_NAME+controller.getClass().getName())==null)
      {
	req.setAttribute(CONTROLLER_NAME+controller.getClass().getName(), controller);
	try{
	  Logic.doInit(controller, this, getRequestDatabase());
	}catch(UnauthorizedException e)
	  {
	    // if we are not in the login page
	    if(!req.getServletPath().endsWith("login.jsp"))
	      throw e;
	  }
      }
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
  
  public Object getAttribute(String s) 
       throws LogicException
  {
    Object o= checkSessionForAttribute(s);
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
  
  public Object checkLogicForAttribute(String s)
       throws LogicException
  {
    String snull=s+"_null";
    HttpSession ss= request.getSession(true);
    boolean nullValue=false;
    Object value=null;
    try{
      value=Logic.getAttribute(getRequestController(), s, this, getRequestDatabase());
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
	{
	    /*
	      if the parameter is passed by an include
	      and all its values are equal
	      we probably have a repeated include due to FLE protection
	      so we return only one of the equal values, not the whole vector
	    */
	      if(value instanceof Vector && 
		 // test for include:
		 !getParameters(request).knownAtStart(s))
		  // test for "all values are equal":
		{
		    Object p=null;
		    for(Enumeration e= ((Vector)value).elements(); 
			e.hasMoreElements();)
			{
			    Object o=(Object)e.nextElement();
			    if(p==null)
				p=o;
			    else
				if(!p.equals(o))
				    return value;
			}
		    return p;
		}
	    return value;
	}
    return notFound;
  }
}
