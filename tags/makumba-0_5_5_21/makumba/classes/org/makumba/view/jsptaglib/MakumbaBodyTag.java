package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.io.*;

/** this class provides utility methods for all makumba tags */
public abstract class MakumbaBodyTag extends MakumbaTag implements BodyTag
{
  BodyContent bodyContent;
  public void setBody(BodyContent bc){ this.bodyContent=bc; }
  public BodyContent getBody(){ return null; }

  /*
  public String getSignature(int n)
  {
    StringWriter sw= new StringWriter();
    new Throwable().printStackTrace(new PrintWriter(sw));
    String signature= sw.toString();
    int j=0;
    int lastj=0;
    for(int i=0; i<n; i++)
      { 
	lastj=j;
	j=signature.indexOf('\n', j+1);
      }
    return signature.substring(lastj+7, j);
  }
  */

  //--- rest of methods needed to implement the BodyTag interface

  public void doInitBody(){ }

  public int doAfterBody()throws JspException{ return strategy.doAfter(); }

  public void setBodyContent(BodyContent b) {
    strategy.setBody(b);
  }

  /**
   * Get current bodyContent.
   *
   * @return the body content.
   */
  public BodyContent getBodyContent() {
    return strategy.getBody();
  }


  /**
   * Get surrounding out JspWriter.
   *
   * @return the enclosing JspWriter, from the bodyContent.
   */
  public JspWriter getPreviousOut() {
    return getBodyContent().getEnclosingWriter();
  }
}
