package org.makumba.controller.jsp;
import org.makumba.*;
import org.makumba.controller.http.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.util.*;
import org.makumba.*;

public class PageAttributes implements Attributes
{
  public static PageAttributes getAttributes(PageContext pc)
  {
    if(pc.getAttribute(RequestAttributes.ATTRIBUTES_NAME)==null)
      pc.setAttribute(RequestAttributes.ATTRIBUTES_NAME, new PageAttributes(pc));
    return (PageAttributes)pc.getAttribute(RequestAttributes.ATTRIBUTES_NAME);
  }
  
  PageContext pageContext;

  PageAttributes(PageContext pageContext) 
  {
    this.pageContext=pageContext;
  }

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
    RequestAttributes reqAttrs= RequestAttributes.getAttributes((HttpServletRequest)pageContext.getRequest());

    Object o= reqAttrs.checkSessionForAttribute(s);
    if(o!=RequestAttributes.notFound)
      return o;
    
    o=checkPageForAttribute(s);
    if(o!=RequestAttributes.notFound)
      return o;
    
    o= reqAttrs.checkLogicForAttribute(s);
    if(o!=RequestAttributes.notFound)
      return o;

    o=reqAttrs.checkParameterForAttribute(s);
    if(o!=RequestAttributes.notFound)
      return o;

    throw new AttributeNotFoundException(s);

  }

  public Object checkPageForAttribute(String s)
  {
    String snull=s+"_null";
    
    Object value= pageContext.getAttribute(s);
    if(value!=null)
      return value;
    if(pageContext.getAttribute(snull)!=null)
      return null;
    return RequestAttributes.notFound;
  }


}
