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
