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

package org.makumba.controller.jsp;
import javax.servlet.*;
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

      HttpServletRequest req=(HttpServletRequest)pageContext.getRequest().getAttribute(org.makumba.controller.http.ControllerFilter.ORIGINAL_REQUEST);

      // retrieve the path info on original request
      String pathInfo=req.getPathInfo();
      if(pathInfo==null) pathInfo="";

      // retrieve the query string on original request
      String varInfo=req.getQueryString();
      if(varInfo==null) { 
         varInfo = ""; 
      } else { 
         varInfo = "?"+varInfo; 
      }
      
      //System.out.println(varInfo+"From LoginTag");
      //System.out.println(req.getRequestURI()+"From LoginTag");
     
      // now we know the FORM action
      bodyContent.print("<form action=\""+req.getContextPath()+req.getServletPath()+pathInfo+varInfo+"\" method=\"post\">");


      // ////////////////////////////////////////////////////////////////////////////////////////////////////////
      // next we print <input hidden ...> for all POST params: {POST-params} = { ALL params } minus { GET-params }
      //

      // we read the GET parameters in a Dictionary => we will avoid printing them as hidden POST param
      Dictionary dGetParams = new Hashtable(); //default: empty 
      if (req.getQueryString()!=null){
           // System.out.println(HttpUtils.parseQueryString(req.getQueryString())+"\n");
           dGetParams = HttpUtils.parseQueryString(req.getQueryString());
      }

      // Note on use of Request object (fred)
      //    in a previous version, the Request's parameters (below) were taken from the 
      //    current request, instead of the original request... 
      //    the problem is that some param can be added in the mean time (e.g. param given to a header)
      //    and we don't need to include those on the login form. So, I changed code to 
      //    work only with the original request's params.
      // => UNUSED ServletRequest pcReq = pageContext.getRequest();

      // for every parameter       
      for(Enumeration e = req.getParameterNames(); e.hasMoreElements();) {  //! was pcReq
          String name=(String)e.nextElement();
          
          // username and password are ignored, new ones should exist in the login form 
	 if( !name.equals("username") && !name.equals("password") ){

             // count the unique values for this parameter
             Dictionary dParamValues = countValues(req.getParameterValues(name)); //! was pcReq

             // count the unique values for this parameter in the GET-QueryString
             Dictionary dGetParamValues = null;
             if ( dGetParams.get(name)!=null ) dGetParamValues = countValues( (String[])dGetParams.get(name) ) ;

             // for each unique value for this parameter... print as hidden input
             for(Enumeration ee = dParamValues.keys(); ee.hasMoreElements(); ){
                 String value = (String)ee.nextElement();

                 // how many times is (paramName=value) among ALL params?
                 int cntAllParam = ((Integer)dParamValues.get(value)).intValue();
                 int cntHiddenInput = cntAllParam;

                 if(dGetParams.get(name)!=null && dGetParamValues.get(value)!=null) {
                     int cntGetParam =((Integer)dGetParamValues.get(value)).intValue();
                     cntHiddenInput -= cntGetParam;
                 }

                 /* we print HIDDEN INPUT tags for all occurences of this value that are in    
                    parameters but are not in GET. */
                 for(int i=0;i<cntHiddenInput;i++){
                     bodyContent.print("<input type=\"hidden\" name=\""+name+"\" value=\""+org.makumba.util.HtmlUtils.string2html(value)+"\">");
                 }
             }


          } //if not username...

      } // for

    } catch(IOException e){ throw new JspException(e.toString()); }

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
