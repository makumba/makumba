package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;
import java.util.*;
import org.makumba.*;
import org.makumba.util.*;
import org.makumba.controller.jsp.PageAttributes;

public class AttributeTag extends MakumbaTag
{
  String name;
  String var;
  String exceptionVar;

  protected boolean canBeRoot()  { return true; }

  protected Class getParentClass()  { return MakumbaTag.class; }
  public TagStrategy makeStrategy(Object key){ return this; }

  public void setName(String s){ this.name=s; }
  public void setVar(String s){ this.var=s; }
  public void setExceptionVar(String s){ this.exceptionVar=s; }
  
  /** ask the enclosing query to present the expression */
  public int doStart() throws JspException 
  {
    Object o= null;
    Throwable t=null;
    try{
      o=PageAttributes.getAttributes(pageContext).getAttribute(name);
    }catch(Throwable t1) {t=t1; }
    if(t!=null)
      if(exceptionVar==null)
	{ treatException(t); return BodyTag.SKIP_PAGE; }
      else
	{
	  pageContext.setAttribute(exceptionVar, t);	
	  if(t instanceof AttributeNotFoundException)
	    pageContext.setAttribute(name+"_null", "null");
	}
    if(var==null)
      if(t==null)
	{
	  try
	    {
	      pageContext.getOut().print(o);
	    }catch(java.io.IOException e){ throw new JspException(e.toString()); }
	}
      else ;
    else
      PageAttributes.setAttribute(pageContext, var, o);

    return EVAL_BODY_INCLUDE;
  }
}



