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
import org.makumba.view.*;
import org.makumba.*;
import javax.servlet.jsp.*;
import org.makumba.abstr.Logic;
import java.io.*;

public class DeleteTag extends EditTag
{
  public FormResponder makeResponder() { return new DeleteResponder(); }

  // no input tags allowed!!

  public void writeFormPreamble(JspWriter pw) throws JspException, IOException
  {
    String sep="?";
    if( ((String)action).indexOf('?')>=0) { sep="&"; }
    pw.print("<a href=\""+action+sep+FormResponder.basePointerName+"="+getBasePointer()+"&"+FormResponder.responderName+"="+responder.getIdentity(getEditedType())+"\">");
  }

  public void writeFormPostamble(JspWriter pw) throws JspException, IOException
  {
    pw.print("</a>");
  }

  //  public Object getKeyDifference(){ return ""+super.getKeyDifference()+"DELETE"; }
}

class DeleteResponder extends FormResponder
{
  public Object respondTo(PageContext pc) throws LogicException
  {
    return Logic.doDelete(controller, type, getHttpBasePointer(pc), makeAttributes(pc), database);
  }


}

