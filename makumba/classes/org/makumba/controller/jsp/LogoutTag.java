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
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class LogoutTag extends TagSupport
{
  String attr=null;

  public void setActor(String a){ attr=a; }
  
  public int doStartTag() throws JspException 
  {
    if(attr == null) {
        pageContext.getSession().invalidate();

    } else if(pageContext.getAttribute(attr, PageContext.SESSION_SCOPE)!=null) {
        pageContext.removeAttribute(attr, PageContext.SESSION_SCOPE);
    }
    return EVAL_BODY_INCLUDE;
  }
}
