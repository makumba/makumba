package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.*;

/** A tag strategy. Many tags can be succesively assigned the same strategy */
public interface TagStrategy
{
  /** initialize */
  public void init(MakumbaTag root, MakumbaTag tag, Object key) 
       throws org.makumba.LogicException;

  /** one more loop with this strategy, by another tag */
  public void loop();

  /** called by the tag's setBodyContent, if any */
  public void setBody(BodyContent bc); 

  /** called by the tag's getBodyContent */
  public BodyContent getBody(); 

  /** called by the tag's setPageContext */
  public void setPage(PageContext pc); 
  
  /** called by the tag's doStartTag */
  public int doStart() throws JspException, org.makumba.LogicException;

  /** called by the tag's doAfterBody, if any */
  public int doAfter() throws JspException;

  /** called by the tag's doEndTag */
  public int doEnd() throws JspException;

  /** called by the tag's release */
  public void doRelease();

  /** called when the root closes */
  public void rootClose();
}


