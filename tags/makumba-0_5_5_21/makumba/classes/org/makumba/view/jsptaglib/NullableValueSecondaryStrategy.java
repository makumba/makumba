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
    ValueTag.displayIn(main);
    return BodyTag.EVAL_BODY_INCLUDE;
  }

  public QueryStrategy getQueryStrategy(){ return main; }
  public void setQuery(ComposedQuery q){ } 
  public ComposedQuery getQuery() { return main.getQuery(); }
  public void doQuery(org.makumba.Database db, Attributes a) {}
  public boolean foundMoreProjections(){ return main.foundMoreProjections(); }
  public boolean executed() { return main.executed(); }
}
