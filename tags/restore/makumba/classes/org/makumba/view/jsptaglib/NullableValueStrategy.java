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
