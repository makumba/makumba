package org.makumba.view.jsptaglib;
import org.makumba.*;
import org.makumba.abstr.*;
import org.makumba.view.*;
import java.util.*;
import javax.servlet.jsp.*;

public class RecordEditor extends RecordViewer
{
  String database;

  public RecordEditor(RecordInfo ri, Hashtable h)   {  super(ri, h); }  

  public Dictionary readFrom(PageContext pc)
  {
    Dictionary data= new Hashtable();
    for(Enumeration e=handlerOrder.elements(); e.hasMoreElements(); )
      {
	FieldEditor fe= (FieldEditor)e.nextElement();
	if(fe.getInputName()==null)
	  continue;
	Object o= fe.readFrom(HttpAttributes.getParameters(pc));
	if(o!=null)
	  o=fe.getFieldInfo().checkValue(o);
	else
	  o=fe.getNull();

	pc.setAttribute(fe.getInputName()+"_type", fe.getFieldInfo());

	// FIXME: semantics of EDIT might be wrong here
	if(o!=null)
	  data.put(fe.getInputName(), o);
	HttpAttributes.setAttribute(pc, fe.getInputName(), o);
      }
    return data;
  }

  public void config()
  {
    Object a[]= { this} ;
    try{
      callAll(getHandlerMethod("onStartup"), a);
    }catch(java.lang.reflect.InvocationTargetException e)
      {
	throw new org.makumba.MakumbaError(e.getTargetException());
      }
  }

}
