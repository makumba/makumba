package org.makumba.devel;
import org.makumba.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

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
    PrintWriter wr=resp.getWriter();
    Throwable t= (Throwable)req.getAttribute(javax.servlet.jsp.PageContext.EXCEPTION);
    Throwable t1=null;    

    Throwable original=t;
    while(true)
      {
	if(t instanceof LogicException)
	  t1=((LogicException)t).getReason();
	else if(t instanceof MakumbaError)
	  t1=((MakumbaError)t).getReason();
	else if(t instanceof LogicInvocationError)
	  t1=((LogicInvocationError)t).getReason();
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
	  wr.println("<h1>Makumba "+errors[i][1] +" error</h1><pre>"+highlight(t.getMessage()));
	  if(original instanceof LogicInvocationError || trcOrig.indexOf("at org.makumba.abstr.Logic")!=-1)
	    {
	      wr.println("<br></pre><pre>");
	      wr.print(highlight(trc));
	      wr.print("</pre>");
	    }
	  else
	    {
	      wr.print("</pre><!--\n");wr.print(trc);wr.print("\n-->");
	    }
	  wr.flush();
	  return;
	}
    unknownError(original, t, wr);
    wr.flush();
  }
  
  String trace(Throwable t) 
  {
    StringWriter sw= new StringWriter();
    t.printStackTrace(new PrintWriter(sw));
    return sw.toString();
  }
  
  String shortTrace(String s)
  {
    int i=s.indexOf("at org.makumba.abstr.Logic");
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
	  MakumbaSystem.getMakumbaLogger("devel").severe ("servlet call not found in stacktrace");
      }
    return s;
  }
  void unknownError(Throwable original, Throwable t, PrintWriter wr)
       throws IOException, ServletException
  {
    Throwable traced=t;
    if(original instanceof LogicInvocationError)
      {
	wr.print("<h1>Error in business logic code</h1>");
      }
    else if(trace(traced).indexOf("org.makumba")!=-1)
      {
	wr.print("<h1>Internal Makumba error</h1>Please report to the developers<p>");
	if(t instanceof ServletException)
	  traced=((ServletException)t).getRootCause();
      }
    else
      {
	wr.print("<h1>Error in JSP Java scriplet or servlet container</h1>");
      }
    wr.print("<pre>");
    wr.print(highlight(shortTrace(trace(traced))));
    wr.print("</pre>");
  }
  
  String highlight(String s)
  {
    try{
      StringWriter sw= new StringWriter();
      new LineViewer(new StringReader(s)).parseText(new PrintWriter(sw));
      return sw.toString();
    }catch(IOException e){ e.printStackTrace(); throw new org.makumba.util.RuntimeWrappedException(e);}
  }

}
