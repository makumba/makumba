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
import java.util.*;

/** A tag strategy. Many tags can be succesively assigned the same strategy */
public interface TagStrategy
{
  /** initialize */
  public void init(MakumbaTag root, MakumbaTag tag, Object key) 
       throws org.makumba.LogicException;

  /** one more loop with this strategy, by another tag */
  public void loop();

  /** called by the tag's setBodyContent, if any */
  public void setBody(BodyContent bc); 

  /** called by the tag's getBodyContent */
  public BodyContent getBody(); 

  /** called by the tag's setPageContext */
  public void setPage(PageContext pc); 
  
  /** called by the tag's doStartTag */
  public int doStart() throws JspException, org.makumba.LogicException;

  /** called by the analyzer */
  public void doAnalyze() ;

  /** called by the tag's doAfterBody, if any */
  public int doAfter() throws JspException;

  /** called by the tag's doEndTag */
  public int doEnd() throws JspException;

  /** called by the tag's release */
  public void doRelease();

  /** called when the root closes */
  public void rootClose();
}


