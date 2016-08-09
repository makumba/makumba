package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class ObjectStrategy extends QueryStrategy
{
  /** the typical condition at the begining of looping */
  protected int startLooping() throws JspException
  {
    int n= super.startLooping();
    if(n==BodyTag.EVAL_BODY_TAG && results.size()>1)
      {
	tag.treatException(new MakumbaJspException(this, "Object tag should have only one result"));
	return BodyTag.SKIP_BODY;
      }
    return n;
  }

  public String getType() {return "OBJECT"; }
}
