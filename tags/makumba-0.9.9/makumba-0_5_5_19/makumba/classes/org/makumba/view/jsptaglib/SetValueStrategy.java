package org.makumba.view.jsptaglib;
import org.makumba.util.*;
import org.makumba.*;
import org.makumba.view.ComposedQuery;
import java.util.*;

import javax.servlet.jsp.tagext.*;
import javax.servlet.jsp.*;

public class SetValueStrategy extends QueryStrategy
{
  String label;
  String name;

  protected void adjustQueryProps()
  {
    super.adjustQueryProps();
    name=getBuffer().bufferSet.getRelationType().getTitleFieldName();
    label=getBuffer().bufferExpr.replace('.', '_');
    queryProps[ComposedQuery.FROM]=getBuffer().bufferExpr+" "+label;
    queryProps[ComposedQuery.ORDERBY]=label+"."+name;
  }
  int done;

  public int doStart() throws JspException 
  {
    String     var= getBuffer().bufferVar;
    String     printVar= getBuffer().bufferPrintVar;

    if(var!=null){
      pageContext.setAttribute(var+"_type", getBuffer().bufferSet);
      HttpAttributes.setAttribute(pageContext, var, null);
    }
    Vector v=new Vector();
    bodyContent=((ValueTag)tag).getParentQueryStrategy().bodyContent;
    done=super.doStart();
    if(done!=BodyTag.EVAL_BODY_TAG)
      return done;
    String sep="";
    String total="";
    do{
      getBuffer().bufferExpr=label;
      getBuffer().bufferVar=label;
      ValueTag.displayIn(this);
      getBuffer().bufferVar=null;
      getBuffer().bufferPrintVar=label+"_print";
      getBuffer().bufferExpr=label+"."+name;
      ValueTag.displayIn(this);

      Object o=pageContext.getAttribute(label);
      if(o instanceof Pointer)
	{
	  v.addElement(o);
	  total+=sep+pageContext.getAttribute(label+"_print");
	  sep=",";
	}
    }while(super.doAfter()==BodyTag.EVAL_BODY_TAG);
    if(var!=null)
      HttpAttributes.setAttribute(pageContext, var, v);
    if(printVar!=null)
      HttpAttributes.setAttribute(pageContext, printVar, total);
    if(var==null && printVar==null){
      try{
	pageContext.getOut().print(total);
      }catch(java.io.IOException e){ throw new JspException (e.toString()); }
    }
    getBuffer().bufferVar=null;
    getBuffer().bufferPrintVar=null;
    getBuffer().bufferParams.clear();
    
    return BodyTag.SKIP_BODY;
  }

  /** write the tag result and go on with the page */
  public int doEnd() throws JspException 
  {
    if(tag.wasException())
      return BodyTag.SKIP_PAGE;
    return BodyTag.EVAL_PAGE;
  }

  public void doRelease() {}

  // nothing to push for subqueries
  public void pushData(){} 

  // nothing to pop for subqueries
  public void popData(){} 
}
