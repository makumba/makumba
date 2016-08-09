package org.makumba.controller.jsp;
import org.makumba.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.io.*;

public class ResponseTag extends javax.servlet.jsp.tagext.TagSupport
{
  public int doStartTag() throws JspException
  {
    try{
      Object response= pageContext.getRequest().getAttribute
	(org.makumba.controller.http.Responder.RESPONSE_STRING_NAME);
      
      // response is null only during login, maybe a more strict check should be made
      if(response!=null)
	pageContext.getOut().print(response);
    }catch(IOException e) { org.makumba.controller.http.ControllerFilter.treatException(e, (HttpServletRequest)pageContext.getRequest(), (HttpServletResponse)pageContext.getResponse());}

    return EVAL_BODY_INCLUDE;
  }
}
