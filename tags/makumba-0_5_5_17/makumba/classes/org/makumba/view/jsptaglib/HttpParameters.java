package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.util.*;

public class HttpParameters
{
  HttpServletRequest req;

  public HttpParameters(PageContext pc)
  {
    req=(HttpServletRequest)pc.getRequest();
  }

  public Object getParameter(String s)
  {
    Object value=null;
    String[]param=req.getParameterValues(s);
    if(param==null)
      return null;

    if(param.length==1)
      value=param[0];
    else
      {
	Vector v=new java.util.Vector();
	value=v;
	for(int i= 0; i<param.length; i++)
	  v.addElement(param[i]);
      }
    //    req.setAttribute(s, value);
    return value;
  }

}
