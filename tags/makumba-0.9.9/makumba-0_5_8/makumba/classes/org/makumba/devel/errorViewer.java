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
import java.io.*;
import javax.servlet.http.*;

/** the error viewer. To be used from TagExceptionServlet.*/
public class errorViewer extends LineViewer
{
  String hBody;

  public errorViewer(HttpServletRequest req, HttpServlet sv, String title, String body, String hiddenBody) throws IOException
  {
    super(false);
    contextPath=req.getContextPath();
    super.title=title;
    hBody=hiddenBody;
    reader=new StringReader(body);
  }

  void footer(PrintWriter pw) throws IOException
  {
      if (hBody!=null)
	pw.println("<!--\n"+hBody+"\n-->");
      super.footer(pw);
  }

}
