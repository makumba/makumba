package org.makumba.view.jsptaglib;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.JspException;

public class ActionTag extends BodyTagSupport
{
  FormTagBase form;

  /** this always returns EVAL_BODY_TAG so we make sure {@link #doInitBody()} is called */
  public int doStartTag()
  {
    return EVAL_BODY_TAG;
  }

  public void doInitBody() throws JspException
  {
    form=(FormTagBase)findAncestorWithClass(this, FormTagBase.class);
    if(form==null)
      throw new JspException("\'action\' tag must be enclosed in any kind of 'form' tag or in 'deleteLink' tag");
  }
  
  public int doEndTag() throws JspException
  {
    form.responder.setAction(bodyContent.getString());
    return EVAL_PAGE;
  }

}

