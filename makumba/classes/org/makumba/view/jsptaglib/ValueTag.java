package org.makumba.view.jsptaglib;
import javax.servlet.jsp.*;
import java.util.*;
import org.makumba.*;
import org.makumba.util.*;
import org.makumba.controller.jsp.PageAttributes;

public class ValueTag extends MakumbaTag
{
  // need to check the type of the first label in the enclosing query
  // if there is a possibly null pointer, there will be different strategy, that has its own query

  static final String EVAL_BUFFER="makumba.eval.buffer";

  public static Object evaluate(String s, MakumbaTag t) throws JspException
  {
    Hashtable h=t.getRootQueryBuffer().bufferParams;
    t.getRootQueryBuffer().bufferParams=new Hashtable();
    ValueTag vt= new ValueTag();
    vt.setPageContext(t.getPageContext());
    vt.setParent(findAncestorWithClass(t, QueryTag.class));
    vt.setExpr(s);
    vt.setVar(EVAL_BUFFER);
    vt.doStartTag();
    t.getRootQueryBuffer().bufferParams=h;
    return t.getPageContext().getAttribute(EVAL_BUFFER);
  }

  public Object getRegistrationKey() throws LogicException
  {
    String expr=getRootQueryBuffer().bufferExpr.trim();
    QueryStrategy p=getParentQueryStrategy();
    Object check= p.query.checkExpr(expr, PageAttributes.getAttributes(pageContext));
    if(check==null)
      return null;
    MultipleKey mk= new MultipleKey((Vector)p.key, 10);
    mk.setAt(expr, 6);
    if(check instanceof String)
      mk.setAt((String)check, 7);
    else 
      {
	getRootQueryBuffer().bufferSet=(FieldDefinition)check;
	mk.setAt(getRootQueryBuffer().bufferSet.getName(), 7);
      }
    mk.setAt(getRootQueryBuffer().bufferVar, 8);
    mk.setAt(getRootQueryBuffer().bufferPrintVar, 9);
    return mk;
  }
  
  public TagStrategy makeNonRootStrategy(Object key)
  {
    if(key==null)
      return this;
    Object o=((MultipleKey)key).elementAt(7);
    if(getRootQueryBuffer().bufferSet==null)
      return getParentQueryStrategy().getNullableStrategy(o);
    return new SetValueStrategy();
  }
  

  /** demand a QueryTag enclosing query */
  protected Class getParentClass(){ return QueryTag.class; }

  public String toString() { 
    if(getRootQueryBuffer()==null)
      return "uninitialized value tag";
    
    return "VALUE expr="+getRootQueryBuffer().bufferExpr+ 
      " parameters: "+ getRootQueryBuffer().bufferParams; 
  }

  /** return false, register an exception */ 
  protected boolean canBeRoot()
  {
    treatException(new MakumbaJspException(this, "VALUE tag should always be enclosed in a LIST or OBJECT tag"));
    return false;
  }

  /** set the expression */
  public void setExpr(String expr)
  { 
    getRootQueryBuffer().bufferExpr=expr;
  }

  /** set the expression */
  public void setVar(String var)
  { 
    getRootQueryBuffer().bufferVar=var;
  }

  /** set the expression */
  public void setPrintVar(String var)
  { 
    getRootQueryBuffer().bufferPrintVar=var;
  }
  
  /** ask the enclosing query to present the expression */
  public int doStart() throws JspException 
  {
    displayIn(getParentQueryStrategy());
    return EVAL_BODY_INCLUDE;
  }

  public static void displayIn(QueryStrategy qs) throws JspException
  {
    try{
      qs.insertEvaluation(qs.getBuffer().bufferExpr, 
			  qs.getBuffer().bufferParams,
			  qs.getBuffer().bufferVar,
			  qs.getBuffer().bufferPrintVar
			  );
    }finally{
      qs.getBuffer().bufferVar=null;
      qs.getBuffer().bufferPrintVar=null;
      qs.getBuffer().bufferParams.clear();
      qs.getBuffer().bufferSet=null;
    }
  }

}
