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
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;
import java.util.*;

/** The Tag class used for <mak:login>. This class is a normal {@link javax.servlet.jsp.tagext.BodyTag} not a {@link org.makumba.view.jsptaglib.MakumbaTag }
*/
public class LoginTag extends BodyTagSupport
{
  static final String pageAttr="org.makumba.original.request";

  /** this always returns EVAL_BODY_TAG so we make sure {@link #doInitBody()} is called */
  public int doStartTag() 
  {
    return EVAL_BODY_TAG;
  }
  
  /** given a list of parameter values A= {a1, a2, ... an}, 
    this method makes a set {[a, t]}  where t is the number of times a occurs in A */
  static Dictionary countValues(String[] parameterValues)
  {
   Dictionary values=new Hashtable();
   for(int i=0; i<parameterValues.length;i++)
     {
      Integer n=(Integer)values.get(parameterValues[i]);
      if(n==null)
         n=new Integer(1);
      else
         n=new Integer(n.intValue()+1);
      values.put(parameterValues[i],n);
     }
    return values;
  }

  /** prepend a HTML FORM to the tag body. 
    the action of the form is the page that provoked the login.
    the path info and query string are identical to the original access
    the HTTP parameters that are not in the query string (POST params) written in the form as hidden INPUT tags 
   */
  public void doInitBody() throws JspException
  {
    try{
      // get the original request, as written in the request attributes by MakumbaTag            
      HttpServletRequest req=(HttpServletRequest)pageContext.getRequest().getAttribute(pageAttr);

      // retrieve the path info 
      String pathInfo=req.getPathInfo();
      if(pathInfo==null)
	  pathInfo="";

      // retrieve the query string      
      String varInfo=req.getQueryString();
      if(varInfo==null) 
          varInfo="";
      else 
         varInfo="?"+varInfo; 
      
      // System.out.println(varInfo+"From LoginTag");
      //System.out.println(req.getRequestURI()+"From LoginTag");
     
      // now we know the FORM action
      bodyContent.print("<form action=\""+req.getContextPath()+req.getServletPath()+pathInfo+varInfo+"\" method=\"post\">");

      // we read the GET parameters in a Dictionary 
/* IMPROVE d could have a better name */
      Dictionary d=new Hashtable();
      if (req.getQueryString()!=null){
      // System.out.println(HttpUtils.parseQueryString(req.getQueryString())+"\n");

      d=HttpUtils.parseQueryString(req.getQueryString());}

      // fort each parameter
      for(Enumeration e= pageContext.getRequest().getParameterNames(); e.hasMoreElements();)
	{
	  String name=(String)e.nextElement();
            // username and password are ignored, new ones should exist in the login form 
	  if(!name.equals("username")&& !name.equals("password"))
	    {
             // we count the unique values of the parameter
             Dictionary valAll=countValues(pageContext.getRequest().getParameterValues(name));


             // for each unique value of this parameter,
             for(Enumeration ee=valAll.keys();ee.hasMoreElements();)
              {
               String val1=(String)ee.nextElement();
/* IMPROVE: why not "value" instead of "val1" ? :) */
               int j=0;

               if(d.get(name)!=null)  
                     {
/* IMPROVE PERFORMANCE: valGet is computed once per parameter value, no need, should be computed once per parameter, outside this for loop, if d.get(name) is not null */
                       Dictionary valGet=countValues((String [])d.get(name));

                       if (valGet.get(val1)!=null) 
                          {

/* IMPROVE: a should be computed anyway (even if d.get(name) is null), outside this for loop, because it's needed below */
/* IMPROVE: a and b can have better names :) */
                           int a=((Integer)valAll.get(val1)).intValue();
                           int b=((Integer)valGet.get(val1)).intValue();
                           /* we print INPUT tags for all occurences of this value that are in    
                                parameters but are not in GET. that is, we print it a-b times */
                           for(int ii=1;ii<a-b;ii++)
                              bodyContent.print("<input type=\"hidden\" name=\""+name+"\" value=\""+val1+"\">");
                          }
                      }
               else
/* FIXME when the value is not present in GET, the INPUT should be printed "a" times, not just once! */
             {
               int a=((Integer)valAll.get(val1)).intValue();
               for(int ii=1;ii<a;ii++)
	       bodyContent.print("<input type=\"hidden\" name=\""+name+"\" value=\""+val1+"\">");
             }


/* IMPROVE
 basically the for() loop that prints INPUTs should be common, just the number of iterations is different (a-b and a respectively). so i guess it'll be sth like
int iterations= valAll.get(...);
if(d.get(name)!=null) iterations -= vallGet.get(...);
for(... i<iterations...)bodyContent.print(...);
*/

              }
	    }
	}
    }catch(IOException e){ throw new JspException(e.toString()); }
  }
  
  /** appends a /FORM to the tag body, closing the login form */
  public int doEndTag() throws JspException
  {
    try{
      getPreviousOut().print(bodyContent.getString()+"</form>");
    }catch(IOException e){ throw new JspException(e.toString()); }
    return EVAL_PAGE;
  }
}
