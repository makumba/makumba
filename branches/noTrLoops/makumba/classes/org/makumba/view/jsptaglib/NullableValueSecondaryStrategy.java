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
import org.makumba.view.ComposedQuery;
import org.makumba.Attributes;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;

/** this is a dummy, it contains no query but add projections to a main nullable query that has the same nullable root */
public class NullableValueSecondaryStrategy extends TagStrategySupport implements QueryTagStrategy
{
  NullableValueStrategy main;

  NullableValueSecondaryStrategy(NullableValueStrategy main) { this.main= main; }

  public int doStart() throws JspException 
  {
    int n=main.done;
    if(n!=BodyTag.EVAL_BODY_TAG)
      return n;
    main.insertEvaluation((ValueTag)tag);
    return BodyTag.EVAL_BODY_INCLUDE;
  }

  public QueryStrategy getQueryStrategy(){ return main; }
  public void setQuery(ComposedQuery q){ } 
  public ComposedQuery getQuery() { return main.getQuery(); }
  public void doQuery(org.makumba.Database db, Attributes a) {}
  public boolean foundMoreProjections(){ return main.foundMoreProjections(); }
  public boolean executed() { return main.executed(); }
}
