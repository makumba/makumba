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
import org.makumba.abstr.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

/** 
 * invoke the necessary SourceViewer, depending on the type of the source
 * the architecture should change, and be organized in filters. example:
 * jspx: JSP syntax colouring | Java linking | MDD linking | line numbering | header
 * java: Java syntax colouring | Java linking | MDD linking | line numbering | header
 * mdd: syntax_colouring | MDD linking | line numbering | header
 * jspxp: JSP syntax colouring | line numbering | makumba reduction | java linking | mdd linking | header 
 * It's not difficult to get the current architecture to work like that
 * This will be slower but the big advantage is that the Java and JSP syntax colouring (and maybe Java linking) can be outsourced.
 */
public class SourceViewServlet extends HttpServlet
{
  public void doGet(HttpServletRequest req, HttpServletResponse res) 
       throws IOException, ServletException
  {
    PrintWriter w= res.getWriter();
    
    SourceViewer sw=null;
    String servletPath=req.getServletPath();
    try{
      if(servletPath.equals("/dataDefinitions"))
	sw= new mddViewer(req);
      else if(servletPath.endsWith(".jspx"))
	sw= new jspViewer(req, this);
      else if(servletPath.endsWith(".jspxp"))
	sw= new jspProgViewer(req, this);
      else if(servletPath.equals("/classes"))
	sw= new javaViewer(req, this);
      else if(servletPath.equals("/logic"))
	sw= new logicViewer(req, this);
    }catch(Exception e) 
      { 
	e.printStackTrace();
	res.sendError(404, e.toString()); 
	return; 
      }
    if(sw!=null)  //we have a known handler
      {
	File dir= sw.getDirectory();
	if(dir==null)
	  {
	    res.setContentType("text/html");
	    
	    try{
	      sw.parseText(w);
	    }catch(Exception e){e.printStackTrace(); }
	  }
	else //try to handle anyway
	  {
	    if(req.getPathInfo()==null)
	      {
		  if(servletPath.startsWith("/"))
		      servletPath=servletPath.substring(1);
		  res.sendRedirect(servletPath+"/");
		  return; 
	      }
	    if(!req.getPathInfo().endsWith("/"))
	      {
		res.sendRedirect(servletPath+req.getPathInfo()+"/");
		return; 
	      }

	    // make a directory listing
	    res.setContentType("text/html");
	    w.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
	    w.println("<html><head><title>"+dir.getName()+"</title>");
	    w.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");
	    w.println("</head>\n<body><pre>");
	    String[] list= dir.list();
	    
	    w.println("<b><a href=\"../\">../</a></b> (up one level)");
	    for(int i=0; i<list.length; i++)
	      {
		String s= list[i];
		if(s.indexOf(".")==-1 && !s.equals("CVS"))
		  w.println("<b><a href=\""+s+"/\">"+s+"/</a></b>");
	      }

	    for(int i=0; i<list.length; i++)
	      {
		String s= list[i];
		if(s.indexOf(".")!=-1 && !s.endsWith("~") && !s.endsWith("class"))
		  {
		    String addr=s;
		    if(s.endsWith("dd"))
		      {
			String dd=req.getPathInfo()+s;
			dd=dd.substring(1, dd.lastIndexOf(".")).replace('/', '.');
			addr=req.getContextPath()+"/dataDefinitions/"+dd;
		      }
		    w.println("<a href=\""+addr+"\">"+s+"</a>");
		  }
	      }
	    w.println("</pre></body></html>");
	  }
      }
    else
      w.println("unknown source type: "+servletPath);
  }
}

