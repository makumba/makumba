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

/** a simple implementation of TagStrategy */
public class TagStrategySupport implements TagStrategy
{
  MakumbaTag tag;
  RootTagStrategy root;
  RootData rootData;
  BodyContent bodyContent;
  PageContext pageContext;
  Object key;
  
  /** initialize */
  public void init(MakumbaTag root, MakumbaTag tag, Object key)
  {
    this.tag=tag;
    this.root=(RootTagStrategy)root.strategy;
    this.key=key;
    this.rootData= root.getRootData();
 }

  public void loop(){}
  public void setBody(BodyContent bc){ bodyContent=bc; }
  public void setPage(PageContext pc){ pageContext=pc; }
  public BodyContent getBody(){ return bodyContent; }
  public int doStart() throws JspException { return Tag.SKIP_BODY; }
  public void doAnalyze() { }
  public int doAfter()throws JspException { return Tag.SKIP_BODY; }
  public int doEnd() throws JspException 
  {
    if(tag.wasException())
      return BodyTag.SKIP_PAGE;
    return BodyTag.EVAL_PAGE;
  }

  public void doRelease() {bodyContent=null;}
  public void rootClose() {}
  public Object getKey(){ return key; }
}
