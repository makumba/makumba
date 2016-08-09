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

package org.makumba.devel;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.LogicException;
import org.makumba.LogicInvocationError;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.OQLParseError;
import org.makumba.util.JspParseData;
import org.makumba.util.RuntimeWrappedException;
import org.makumba.view.jsptaglib.MakumbaTag;

/** 
 * the servlet that receives errors in any makumba page and treats them 
 * meant to be friendly for developer, so he'll see helpful stuff during development and not 
 * stupid stakctraces (which are shown only in case of unknown exceptions)
 * also indented for intercepting lack of authorization and show login pages
 * 
 * @version $Id$
 * @author Cristian Bogdan
 * @author Stefan Baebler
 * @author Rudolf Mayer
 */
public class TagExceptionServlet extends HttpServlet
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
static Object errors [][]=
  {
    { org.makumba.OQLParseError.class, "query" },
    { org.makumba.DataDefinitionNotFoundError.class, "data definition not found" },
    { org.makumba.DataDefinitionParseError.class, "data definition parse" },
    { org.makumba.DBError.class, "database" },
    { org.makumba.ConfigFileError.class, "configuration" },
    { org.makumba.ProgrammerError.class, "programmer" },
    { org.makumba.view.jsptaglib.MakumbaJspException.class, "page" },
    { org.makumba.AttributeNotFoundException.class, "attribute not set" },
    { org.makumba.UnauthorizedException.class, "authorization" },
    { org.makumba.InvalidValueException.class, "invalid value" },
    { org.makumba.InvalidFieldTypeException.class, "invalid field type" },
    { org.makumba.NoSuchFieldException.class, "no such field" },
    { org.makumba.LogicException.class, "business logic" }
  };

  public void service(HttpServletRequest req, HttpServletResponse resp) 
       throws IOException, ServletException
  {
    resp.setContentType("text/html");
    PrintWriter wr=resp.getWriter();
    Throwable t= (Throwable)req.getAttribute(javax.servlet.jsp.PageContext.EXCEPTION);
    Throwable t1=null;    

    Throwable original=t;

    while (true) {
        if (t instanceof LogicException)
            t1 = ((LogicException) t).getReason();
        else if (t instanceof MakumbaError && !(t instanceof OQLParseError))
            t1 = ((MakumbaError) t).getReason();
        else if (t instanceof LogicInvocationError)
            t1 = ((LogicInvocationError) t).getReason();
        else if (t instanceof RuntimeWrappedException)
            t1 = ((RuntimeWrappedException) t).getReason();
        else if (t instanceof ServletException && ((ServletException) t).getRootCause() instanceof NullPointerException) {
            // handle null pointer exception seperate, as otherwise tomcat 5.0.x reports only "null" as message, and no stacktrace.
            ServletException e = (ServletException) t;
            t1 = e.getRootCause();
            t1.setStackTrace(e.getRootCause().getStackTrace());
        }
        else
            break;
        if (t1 == null)
            break;
        t = t1;            
    }

    if(t.getClass().getName().startsWith(org.makumba.view.jsptaglib.TomcatJsp.getJspCompilerPackage())){
      knownError("JSP compilation error", t, original, req, wr);
      return;
    }

    for(int i=0; i<errors.length; i++)
      if((((Class)errors[i][0])).isInstance(t) || t1!=null && 
	 (((Class)errors[i][0])).isInstance(t=t1))
	{
	  knownError("Makumba "+errors[i][1] +" error", t, original, req, wr);
	  return;
	}
    unknownError(original, t, wr,req);
    wr.flush();
  }

  void knownError(String title, Throwable t, Throwable original, HttpServletRequest req, PrintWriter wr){
    String trcOrig=trace(t);
    String trc= shortTrace(trcOrig);
    String body=t.getMessage();
    String hiddenBody=null;
    
    body=formatTagData(req)+ body;
    if(original instanceof LogicInvocationError || trcOrig.indexOf("at org.makumba.abstr.Logic")!=-1)
      {
	body=body+"\n\n"+trc;
      }
    else
      {
	hiddenBody=trc;
      }
      try{
	SourceViewer sw=new errorViewer(req,this,title,body,hiddenBody);
	sw.parseText(wr);
      } catch (IOException e) {e.printStackTrace(); throw new org.makumba.util.RuntimeWrappedException(e);}
  }
    
  String formatTagData(HttpServletRequest req){
    String tagExpl="During analysis of the following tag (and possibly tags inside it):";
    JspParseData.TagData tagData=MakumbaTag.getAnalyzedTag();
    if(tagData==null){
      tagExpl="During running of: ";
      tagData= MakumbaTag.getRunningTag();
    }
    if(tagData==null){
      tagExpl="While executing inside this body tag, but most probably <b>not</b> due to the tag:";
      tagData= MakumbaTag.getCurrentBodyTag();
    }
    if(tagData==null) {
        String filePath;
        try {
            String serverName = req.getLocalName() + ":" + req.getLocalPort() + req.getContextPath();
            filePath = req.getRequestURL().toString().substring(req.getRequestURL().toString().indexOf(serverName)+serverName.length());
        } catch (Exception e) { // if some error occurs during the string parsing
            filePath = req.getRequestURL().toString(); // --> we just present the whole request URL
        }
        return tagExpl = "While executing page " + filePath + "\n\n";
    } 
    StringBuffer sb= new StringBuffer();
    try{
      JspParseData.tagDataLine(tagData, sb);
    }catch(Throwable t){ 
      // ignore source code retrieving bugs
      t.printStackTrace();
    }
    String filePath;
    try {
        filePath = "/" + tagData.getStart().getFile().getAbsolutePath().substring(getServletContext().getRealPath("/").length());
    } catch (Exception e) { // we might not have a servlet context available
        filePath = tagData.getStart().getFile().getAbsolutePath();
    }
    return tagExpl + filePath + ":" + tagData.getStart().getLine() + ":" + tagData.getStart().getColumn() + ":"
           + tagData.getEnd().getLine() + ":" + tagData.getEnd().getColumn() + "\n" + sb.toString() + "\n\n";
    
  }

  String trace(Throwable t) 
  {
    StringWriter sw= new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
  

  String shortTrace(String s)
  {
    int i=s.indexOf("at org.makumba.controller.Logic");
    if(i!=-1)
      {
	s=s.substring(0, i);
	i=s.indexOf("at sun.reflect");
	if(i!=-1)
	  s=s.substring(0, i);
      }
    else
      {
	i=s.indexOf("at javax.servlet.http.HttpServlet.service(HttpServlet.java");
	if(i!=-1)
	  s=s.substring(0,i);
	else
	    {
		i=s.indexOf("at org.makumba.controller.http.ControllerFilter.doFilter(ControllerFilter.java");
		if(i!=-1)
		    s=s.substring(0,i);
		else
		    MakumbaSystem.getMakumbaLogger("devel").severe ("servlet or filter call not found in stacktrace");
	    }

      }
    return s;
  }
  


  void unknownError(Throwable original, Throwable t, PrintWriter wr, HttpServletRequest req)
       throws IOException, ServletException
  {
    Throwable traced=t;
    String title="";
    String body="";
    if(original instanceof LogicInvocationError)
      {
	title="Error in business logic code";
      }
    else if(trace(traced).indexOf("org.makumba")!=-1)
      {
	title="Internal Makumba error";
	body="Please report to the developers.\n";
	if(t instanceof ServletException){
	  traced=((ServletException)t).getRootCause();
	  // maybe there's no root cause...
	  if(traced==null)
	    traced=t;
	}
      }
    else
      {
	title="Error in JSP Java scriplet or servlet container";
      }

    if(traced instanceof java.sql.SQLException)
      {
	title="SQL "+title;
	body="The problem is related to SQL:\n"
		+"	 SQLstate: "+((java.sql.SQLException)traced).getSQLState()+"\n"
		+"	ErrorCode: "+((java.sql.SQLException)traced).getErrorCode()+"\n"
		+"	  Message: "+traced.getMessage()+"\n\n"
		+"Refer to your SQL server\'s documentation for error explanation.\n"
		+"Please check the configuration of your webapp and SQL server.\n"
		+body;
      }

    body=formatTagData(req)+body+shortTrace(trace(traced));
    try{
       SourceViewer sw=new errorViewer(req,this,title,body,null);
       sw.parseText(wr);
    } catch (IOException e) {e.printStackTrace(); throw new org.makumba.util.RuntimeWrappedException(e);}

  }
  
    /**
     * Return a string describing the error that occured.
     * @param req
     * @return
     */
    public String getErrorMessage(HttpServletRequest req) {
        Throwable t = (Throwable) req.getAttribute(javax.servlet.jsp.PageContext.EXCEPTION);
        Throwable t1 = null;
        Throwable original = t;
        
        while (true) {
            if (t instanceof LogicException)
                t1 = ((LogicException) t).getReason();
            else if (t instanceof MakumbaError && !(t instanceof OQLParseError))
                t1 = ((MakumbaError) t).getReason();
            else if (t instanceof LogicInvocationError)
                t1 = ((LogicInvocationError) t).getReason();
            else if (t instanceof RuntimeWrappedException)
                t1 = ((RuntimeWrappedException) t).getReason();
            else if (t instanceof ServletException && ((ServletException) t).getRootCause() instanceof NullPointerException) {
                // handle null pointer exception seperate, as otherwise tomcat 5.0.x reports only "null" as message, and no stacktrace.
                ServletException e = (ServletException) t;
                t1 = e.getRootCause();
                t1.setStackTrace(e.getRootCause().getStackTrace());
            }
            else
                break;
            if (t1 == null)
                break;
            t = t1;            
        }

        if (t.getClass().getName().startsWith(org.makumba.view.jsptaglib.TomcatJsp.getJspCompilerPackage())) {
            return "JSP compilation error:\n" + formatTagData(req) + t.getMessage();
        }
        for (int i = 0; i < errors.length; i++) {
            if ((((Class) errors[i][0])).isInstance(t) || t1 != null && (((Class) errors[i][0])).isInstance(t = t1)) {
                return "Makumba " + errors[i][1] + " error:\n" + formatTagData(req) + t.getMessage();
            }
        }
        return unknownErrorMessage(original, t, req);
    }
    
    /**
     * Return a string describing an unknown error. 
     * TODO: this code is more or less a copy of unknownError() --> could be optimised
     * @param original
     * @param t
     * @return
     */
    String unknownErrorMessage(Throwable original, Throwable t, HttpServletRequest req) {
        System.out.println("unknown message:");
        Throwable traced = t;
        String title = "";
        String body = "";
        if (original instanceof LogicInvocationError) {
            title = "Error in business logic code";
        } else if (trace(traced).indexOf("org.makumba") != -1) {
            title = "Internal Makumba error";
            body = "Please report to the developers.\n\n";
            if (t instanceof ServletException) {
                traced = ((ServletException) t).getRootCause();
                // maybe there's no root cause...
                if (traced == null) {
                    traced = t;
                }
            }
        } else {
            title = "Error in JSP Java scriplet or servlet container";
        }
        if (traced instanceof java.sql.SQLException) {
            title = "SQL " + title;
            body = "The problem is related to SQL:\n" + "   SQLstate: "
                    + ((java.sql.SQLException) traced).getSQLState() + "\n" + "  ErrorCode: "
                    + ((java.sql.SQLException) traced).getErrorCode() + "\n" + "    Message: " + traced.getMessage()
                    + "\n\n" + "Refer to your SQL server\'s documentation for error explanation.\n"
                    + "Please check the configuration of your webapp and SQL server.\n" + body;
        }
        return title + ":\n" + formatTagData(req) + body + shortTrace(trace(traced),10);
    }

    /**
     * Cuts down a stack trace to the given number of lines.
     * @param s a stacktrace as string.
     * @param lineNumbers the number of lines to be displayed.
     * @return the cut down stack trace.
     */
    String shortTrace(String s, int lineNumbers) {
        String[] parts = s.split("\n", lineNumbers + 1);
        String result = "";
        for (int i = 0; i < parts.length && i < lineNumbers; i++) {
            result += parts[i] + "\n";
        }
        String[] allParts = s.split("\n");
        if (allParts.length > lineNumbers + 1) {
            result += "-- Rest of stacktrace cut --\n";
        }
        return result;
    }

}
