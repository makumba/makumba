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
import java.util.Hashtable;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

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
