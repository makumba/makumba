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
import org.makumba.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import org.makumba.util.RuntimeWrappedException;
import org.makumba.util.JspParseData;
import org.makumba.view.jsptaglib.MakumbaTag;

/** 
 * the servlet that receives errors in any makumba page and treats them 
 * meant to be friendly for developer, so he'll see helpful stuff during development and not 
 * stupid stakctraces (which are shown only in case of unknown exceptions)
 * also indented for intercepting lack of authorization and show login pages
 */
public class TagExceptionServlet extends HttpServlet
{
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
    while(true)
      {
	if(t instanceof LogicException)
	  t1=((LogicException)t).getReason();
	else if(t instanceof MakumbaError && !(t instanceof OQLParseError))
	  t1=((MakumbaError)t).getReason();
	else if(t instanceof LogicInvocationError)
	  t1=((LogicInvocationError)t).getReason();
	else if(t instanceof RuntimeWrappedException)
	  t1=((RuntimeWrappedException)t).getReason();
	else 
	  break;
	if(t1==null)
	  break;
	t=t1;
      }

    for(int i=0; i<errors.length; i++)
      if((((Class)errors[i][0])).isInstance(t) || t1!=null && 
	 (((Class)errors[i][0])).isInstance(t=t1))
	{
	  String trcOrig=trace(t);
	  String trc= shortTrace(trcOrig);
	  String title="Makumba "+errors[i][1] +" error";
	  String body=t.getMessage();
	  String hiddenBody=null;

	  body=formatTagData()+ body;
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
	  return;
	}
    unknownError(original, t, wr,req);
    wr.flush();
  }
  
  String formatTagData(){
    String tagExpl="During analysis of:";
    JspParseData.TagData tagData=MakumbaTag.getAnalyzedTag();
    if(tagData==null){
      tagExpl="During running of:";
      tagData= MakumbaTag.getRunningTag();
    }
    if(tagData==null){
      tagExpl="While executing inside this body tag, but most probably _not_ due to the tag:";
      tagData= MakumbaTag.getCurrentBodyTag();
    }
    if(tagData==null) return "";
    StringBuffer sb= new StringBuffer();
    JspParseData.tagDataLine(tagData, sb);
    try{
      return tagExpl+"\n"+tagData.getStart().getFile().getCanonicalPath()+":"+
	tagData.getStart().getLine()+":"+tagData.getStart().getColumn()+":"+
	tagData.getEnd().getLine()+":"+tagData.getEnd().getColumn()+"\n"+
	sb.toString()+"\n\n";
    }catch(java.io.IOException e) { throw new MakumbaError(e.toString()); }
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
	body="Please report to the developers.\n\n";
	if(t instanceof ServletException)
	  traced=((ServletException)t).getRootCause();
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

    body=formatTagData()+body+shortTrace(trace(traced));
    try{
       SourceViewer sw=new errorViewer(req,this,title,body,null);
       sw.parseText(wr);
    } catch (IOException e) {e.printStackTrace(); throw new org.makumba.util.RuntimeWrappedException(e);}

  }
  

}
