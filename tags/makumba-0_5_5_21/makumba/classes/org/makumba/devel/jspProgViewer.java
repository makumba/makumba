package org.makumba.devel;
import org.makumba.abstr.*;
import java.io.*;
import javax.servlet.http.*;

public class jspProgViewer extends jspViewer
{
  jspProgViewer(HttpServletRequest req, HttpServlet sv) throws Exception {super(req, sv); }

  int extraLength() {return 2 ; }

  void writeSourceLink(PrintWriter w) throws IOException
  {
    w.print("<td align=\"center\"><a href=\""+virtualPath+"x\"><font color=\"darkblue\">source</font></a></td>");
  }
  
  void writeProgLink(PrintWriter w) throws IOException
  {
    w.print("<td align=\"center\" bgcolor=\"darkblue\"><font color=\"lightblue\">outline</font></td>");
  }

  boolean inMak=false;
  boolean inQuotes=false;
  void treat()
  {
    if(lookup("<mak:") )
      {
	advance();
	inMak=true;
	highlighted.append("--><table><span style=\"background:#eecccc\"><font color=green><b>&lt;"+pattern.substring(1)+"<tr><td>&nbsp;&nbsp;</td><td><!--");
	return;
      }
    if( lookup("</mak:"))
      {
	advance();
	inMak=true;
	highlighted.append("--><span style=\"background:#eecccc\"><font color=green><b>&lt;"+pattern.substring(1)+"</table><!--");
	return;
      }
    if(lookup("=\""))
      {
	advance();
	highlighted.append("<font color=black>=</font><font color=darkblue>\"");
	inQuotes=true;
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
	    highlighted.append("&gt;</b></font></span><!--");
	  }	
	else
	  super.treat();
	break;
      default:
	super.treat();
      }
  }
}
