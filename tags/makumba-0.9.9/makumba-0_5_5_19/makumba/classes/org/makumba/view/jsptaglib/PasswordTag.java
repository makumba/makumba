package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class PasswordTag extends TagSupport
{
  public int doStartTag() throws JspException 
  {
    try{
      pageContext.getOut().print("<input type=\"password\" name=\"password\">");
    }catch(java.io.IOException e) { throw new JspException(e.getMessage()); }
    return EVAL_BODY_INCLUDE;
  }
}
