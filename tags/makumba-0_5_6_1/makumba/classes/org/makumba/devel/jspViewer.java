package org.makumba.devel;
import org.makumba.abstr.*;
import java.io.*;
import javax.servlet.http.*;

public class jspViewer extends LineViewer
{
  String logicPath;
  boolean hasLogic;
  HttpServlet sv;

  int extraLength() { return 1; }

  
  public jspViewer(HttpServletRequest req, HttpServlet sv) throws Exception
  {
    super(true);
    this.sv=sv;
    contextPath=req.getContextPath();
    String servletPath=req.getServletPath();
    virtualPath=servletPath.substring(0, req.getServletPath().length()-extraLength());
    realPath=sv.getServletConfig().getServletContext().getRealPath(virtualPath);
    reader= new FileReader(realPath);
    servletPath=servletPath.substring(0, servletPath.indexOf("."))+".jsp";
    logicPath= contextPath+"/logic"+servletPath;
    hasLogic=!(org.makumba.controller.Logic.getLogic(servletPath) instanceof org.makumba.LogicNotFoundException);
  }

  String findPage(String s)
  {
    if(s.startsWith("/"))
      return (new File(sv.getServletConfig().getServletContext().getRealPath(s)).exists())?s:null;
    return (new File(realPath.substring(0, realPath.lastIndexOf(File.separatorChar))+File.separatorChar+s.replace('/', File.separatorChar)).exists())?s:null;
  }

  void writeSourceLink(PrintWriter w) throws IOException
  {
    w.print("<td align=\"center\" bgcolor=\"darkblue\"><font color=\"lightblue\">source</font></td>");
  }
  
  void writeProgLink(PrintWriter w) throws IOException
  {
    w.print("<td align=\"center\"><a href=\""+contextPath+virtualPath+"xp\"><font color=\"darkblue\">outline</font></a></td>");
  }

  public void intro(PrintWriter w) throws IOException
  {
    w.print("<td align=\"center\"><a href=\""+contextPath+virtualPath+"\"><font color=\"darkblue\">execute</fontmd></a></td>");
    writeSourceLink(w);    
    w.print("<td align=\"center\"><a href=\""+logicPath+"\"><font color=\"darkblue\">business logic"+(hasLogic?"":" (none)")+"</font></a></td>");
    String lg= org.makumba.controller.http.ControllerFilter.getLoginPage(virtualPath);
    if(lg!=null)
      w.print("<td align=\"center\"><a href=\""+contextPath+lg+"x\"><font color=\"darkblue\">login page</font></a></td>");
    writeProgLink(w);
  }

  boolean inMak=false;
  boolean inQuotes=false;
  void treat()
  {
    if(lookup("<mak:") || lookup("</mak:"))
      {
	advance();
	inMak=true;
	highlighted.append("<span style=\"background:#eecccc\"><font color=green><b>&lt;"+pattern.substring(1));
	return;
      }
    if(inMak && inQuotes && lookup("\\\""))
      {
	advance();
	highlighted.append(pattern);
	return;
      }

    switch(current)
      {
      case '\"': 
	if(inQuotes)
	  {
	    highlighted.append("\"</font>");
	    inQuotes=false;
	  }    
	else
	  super.treat();
	break;
      case '>': 
	if(inMak && !inQuotes)
	  {
	    inMak=false;
	    highlighted.append("&gt;</b></font></span>");
	  }	
	else
	  super.treat();
	break;
      default:
	super.treat();
      }
  }
}
