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
import org.makumba.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.io.*;

public class ResponseTag extends MakumbaTag implements RootTagStrategy
{
  public TagStrategy makeNonRootStrategy(Object key)
  {
    return this;
  }

  public RootTagStrategy makeRootStrategy(Object key){ return this; }

  public void onInit(TagStrategy ts){ }

  public Class getParentClass(){return MakumbaTag.class; }

  public boolean canBeRoot() {return true; }

  public int doStart() throws JspException
  {
    try{
      pageContext.getOut().print(pageContext.getRequest().getAttribute(MakumbaTag.RESPONSE_STRING_NAME));
    }catch(IOException e) { treatException(new MakumbaJspException(e));}

    return EVAL_BODY_INCLUDE;
  }
}
