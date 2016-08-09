package org.makumba.controller.http;
import javax.servlet.http.*;
import java.util.*;

public class HttpParameters
{
  HttpServletRequest request;
  Hashtable atStart;
  
  public boolean knownAtStart(String s)
  {
    return atStart.get(s)!=null;  
  } 
 
  public HttpParameters(HttpServletRequest req)
  {
    request=req;
	computeAtStart();
  }
  
  void computeAtStart()
  {
	atStart=new Hashtable();
	Object dummy=new Object();
	for(Enumeration e= request.getParameterNames(); e.hasMoreElements(); )
		atStart.put(e.nextElement(), dummy);
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
