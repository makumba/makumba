package org.makumba.view.jsptaglib;
import org.makumba.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import java.io.*;

public class ResponseTag extends MakumbaTag implements RootTagStrategy
{
  public TagStrategy makeNonRootStrategy(Object key)
  {
    return this;
  }

  public RootTagStrategy makeRootStrategy(Object key){ return this; }

  public void onInit(TagStrategy ts){ }

  public Class getParentClass(){return MakumbaTag.class; }

  public boolean canBeRoot() {return true; }

  public int doStart() throws JspException
  {
    try{
      pageContext.getOut().print(pageContext.getRequest().getAttribute(MakumbaTag.RESPONSE_STRING_NAME));
    }catch(IOException e) { treatException(new MakumbaJspException(e));}

    return EVAL_BODY_INCLUDE;
  }
}
