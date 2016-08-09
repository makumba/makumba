package org.makumba.view.jsptaglib;
import org.makumba.abstr.Logic;
import org.makumba.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.util.*;
import org.makumba.*;

public class HttpAttributes implements Attributes
{
  public static final String ATTRIBUTES_NAME="makumba.attributes";
  public static final String PARAMETERS_NAME="makumba.parameters";

  public static HttpAttributes getAttributes(PageContext pc)
       throws LogicException
  {
    if(pc.getAttribute(ATTRIBUTES_NAME)==null)
      pc.setAttribute(ATTRIBUTES_NAME, new HttpAttributes(pc, MakumbaTag.getDatabaseName(pc)));
    return (HttpAttributes)pc.getAttribute(ATTRIBUTES_NAME);
  }

  HttpAttributes(PageContext pageContext, String db) throws LogicException
  {
    this(Logic.getLogic(((HttpServletRequest)pageContext.getRequest()).getServletPath()), pageContext, db);
  }

  HttpAttributes(Object controller, PageContext pageContext, String db) throws LogicException
  {
    this.controller=controller;
    this.pageContext=pageContext;
    this.db=db;

    // probably we're doing this too often, once per request should be enough
    // don't remember why the login is not checked per request
    try{
      Logic.doInit(controller, this, db);
    }catch(UnauthorizedException e)
      {
	//	e.printStackTrace();
	if(!((HttpServletRequest)pageContext.getRequest()).getServletPath().endsWith("login.jsp"))
	  throw e;
      }
  }

  public static HttpParameters getParameters(PageContext pc)
  {
    if(pc.getRequest().getAttribute(PARAMETERS_NAME)==null)
      pc.getRequest().setAttribute(PARAMETERS_NAME, makeParameters(pc));
    return (HttpParameters)pc.getRequest().getAttribute(PARAMETERS_NAME);
  }

  public static HttpParameters makeParameters(PageContext pc) 
  {
    if(pc.getRequest().getContentType()!=null &&
       pc.getRequest().getContentType().indexOf("multipart")!=-1)
      return new MultipartHttpParameters(pc);
    return new HttpParameters(pc); 
  }

  PageContext pageContext;
  Object controller;
  String db;

  static public void setAttribute(PageContext pc, String var, Object o)
  {
    if(o!=null)
      {
	pc.setAttribute(var, o);
	pc.removeAttribute(var+"_null");
      }
    else
      {
	pc.removeAttribute(var);
	pc.setAttribute(var+"_null", "null");
      }
  }

  public Object getAttribute(String s) 
       throws LogicException
  {
    String snull=s+"_null";
    Object value= pageContext.getAttribute(s, PageContext.SESSION_SCOPE);
    if(value!=null)
      return value;
    if(pageContext.getAttribute(snull, PageContext.SESSION_SCOPE)!=null)
      return null;
    value=pageContext.getAttribute(s, PageContext.PAGE_SCOPE);
    if(value!=null)
      return value;
    if(pageContext.getAttribute(snull, PageContext.PAGE_SCOPE)!=null)
      return null;
    boolean nullValue=false;
    try{
      value=Logic.getAttribute(controller, s, this, db);
      if(value==null)
	nullValue=true;
    }catch(NoSuchMethodException e) {}
    catch(UnauthenticatedException ue) { ue.setAttributeName(s); throw ue; }
	// FIXME: should check HTTP argument illegalities

    if(value!=null)
      {
	pageContext.setAttribute(s, value, PageContext.SESSION_SCOPE);
	return value;
      }
    if(nullValue)
      {
	pageContext.removeAttribute(s, PageContext.SESSION_SCOPE);
	pageContext.setAttribute(snull, "x", PageContext.SESSION_SCOPE);
	return null;
      }    
    value= getParameters(pageContext).getParameter(s);
    if(value!=null)
      return value;

    throw new AttributeNotFoundException(s);
  }
}
