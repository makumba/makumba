package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

/** a simple implementation of TagStrategy */
public class TagStrategySupport implements TagStrategy
{
  MakumbaTag tag;
  RootTagStrategy root;
  RootData rootData;
  BodyContent bodyContent;
  PageContext pageContext;
  Object key;
  
  /** initialize */
  public void init(MakumbaTag root, MakumbaTag tag, Object key)
  {
    this.tag=tag;
    this.root=(RootTagStrategy)root.strategy;
    this.key=key;
    this.rootData= root.getRootData();
 }

  public void loop(){}
  public void setBody(BodyContent bc){ bodyContent=bc; }
  public void setPage(PageContext pc){ pageContext=pc; }
  public BodyContent getBody(){ return bodyContent; }
  public int doStart() throws JspException { return Tag.SKIP_BODY; }
  public int doAfter()throws JspException { return Tag.SKIP_BODY; }
  public int doEnd() throws JspException 
  {
    if(tag.wasException())
      return BodyTag.SKIP_PAGE;
    return BodyTag.EVAL_PAGE;
  }

  public void doRelease() {bodyContent=null;}
  public void rootClose() {}
  public Object getKey(){ return key; }
}
