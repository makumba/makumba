package org.makumba.controller.http;
import javax.servlet.http.*;
import java.util.*;

public class HttpParameters
{
  HttpServletRequest request;

  public HttpParameters(HttpServletRequest req)
  {
    request=req;
  }

  public Object getParameter(String s)
  {
    Object value=null;
    String[]param=request.getParameterValues(s);
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
    //request.setAttribute(s, value);
    return value;
  }

}
