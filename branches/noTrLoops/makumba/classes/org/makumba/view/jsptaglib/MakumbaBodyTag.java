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
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;

/** this class provides utility methods for all makumba tags */
public abstract class MakumbaBodyTag extends MakumbaTag implements BodyTag
{
  BodyContent bodyContent;
  public void setBody(BodyContent bc){ this.bodyContent=bc; }
  public BodyContent getBody(){ return null; }

  /*
  public String getSignature(int n)
  {
    StringWriter sw= new StringWriter();
    new Throwable().printStackTrace(new PrintWriter(sw));
    String signature= sw.toString();
    int j=0;
    int lastj=0;
    for(int i=0; i<n; i++)
      { 
	lastj=j;
	j=signature.indexOf('\n', j+1);
      }
    return signature.substring(lastj+7, j);
  }
  */

  //--- rest of methods needed to implement the BodyTag interface

  public void doInitBody(){ }

  public int doAfterBody()throws JspException{ return strategy.doAfter(); }

  public void setBodyContent(BodyContent b) {
    strategy.setBody(b);
  }

  /**
   * Get current bodyContent.
   *
   * @return the body content.
   */
  public BodyContent getBodyContent() {
    return strategy.getBody();
  }


  /**
   * Get surrounding out JspWriter.
   *
   * @return the enclosing JspWriter, from the bodyContent.
   */
  public JspWriter getPreviousOut() {
    return getBodyContent().getEnclosingWriter();
  }
}
