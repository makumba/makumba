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
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspException;

public class ActionTag extends BodyTagSupport
{
  FormTagBase form;

  /** this always returns EVAL_BODY_TAG so we make sure {@link #doInitBody()} is called */
  public int doStartTag()
  {
    return EVAL_BODY_TAG;
  }

  public void doInitBody() throws JspException
  {
    form=(FormTagBase)findAncestorWithClass(this, FormTagBase.class);
    if(form==null)
      throw new JspException("\'action\' tag must be enclosed in any kind of 'form' tag or in 'deleteLink' tag");
  }
  
  public int doEndTag() throws JspException
  {
    form.responder.setAction(bodyContent.getString());
    return EVAL_PAGE;
  }

}

