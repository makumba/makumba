package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

public class MakumbaVersionTag extends TagSupport
{
  public int doStartTag() throws JspException 
  {
    try{
	  String Version=("$Name$".substring(7,"$Name$".length()-2));
	  pageContext.getOut().print(Version);
    }catch(java.io.IOException e) { throw new JspException(e.getMessage()); }
    return EVAL_BODY_INCLUDE;
  }
}
