package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class LogoutTag extends TagSupport
{
  String attr;

  public void setActor(String a){ attr=a; }
  
  public int doStartTag() throws JspException 
  {
    if(pageContext.getAttribute(attr, PageContext.SESSION_SCOPE)!=null)
      {
	pageContext.removeAttribute(attr, PageContext.SESSION_SCOPE);
      }
    return EVAL_BODY_INCLUDE;
  }
}
