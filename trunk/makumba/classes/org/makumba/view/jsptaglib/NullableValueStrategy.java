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
import org.makumba.util.*;

import java.util.Vector;
import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;

public class NullableValueStrategy extends QueryStrategy
{
  protected void adjustQueryProps()
  {
    queryProps=new String[4];
  }
  int done;
  int parentRun=-2;
  int parentIndex=-2;

  protected Vector obtainData1(Vector v)
  {
    // if the parent did not move through its results, 
    // we keep returning whatever data we had before
    if(getParentStrategy().index==parentIndex && getParentStrategy().run==parentRun)
	return results;

    parentRun=getParentStrategy().run;
    parentIndex=getParentStrategy().index;
    return super.obtainData1(v);
  }

  public int doStart() throws JspException 
  {
    bodyContent=((ValueTag)tag).getParentQueryStrategy().bodyContent;
    done=super.doStart();
    if(done!=BodyTag.EVAL_BODY_TAG)
      return done;
    ValueTag.displayIn(this);
    return BodyTag.EVAL_BODY_INCLUDE;
  }

  /** write the tag result and go on with the page */
  public int doEnd() throws JspException 
  {
    if(tag.wasException())
      return BodyTag.SKIP_PAGE;
    return BodyTag.EVAL_PAGE;
  }

  public void doRelease() {}

  // nothing to push for subqueries
  public void pushData(){} 
}
