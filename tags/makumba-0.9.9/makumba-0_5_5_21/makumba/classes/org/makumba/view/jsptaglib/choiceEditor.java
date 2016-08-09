package org.makumba.view.jsptaglib;
import org.makumba.*;
import java.util.*;

public abstract class choiceEditor extends FieldEditor
{
  public abstract String formatOptionValue(Object opts, int i, Object val);

  public abstract Object getOptions();

  public abstract int getOptionsLength(Object opts);

  public abstract Object getOptionValue(Object options, int i);
  
  public abstract String formatOptionTitle(Object options, int i);

  public abstract String getMultiple();

  public abstract int getDefaultSize();


  static String[] params= { "size" };
  static String[][] paramValues= { null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  // height? orderBy? where?
  public String format(Object o, Dictionary formatParams) 
  {
    boolean hidden= "hidden".equals(formatParams.get("type"));
    StringBuffer sb= new StringBuffer();
    if(!hidden)
      {
	sb.append("<select name=\"").append(getInputName()).append("\"");
	sb.append(getMultiple());
	int size=getIntParam(formatParams,"size");
	if(size==-1)
	  size=getDefaultSize();
	sb.append(" size=\""+size+"\"");
	sb.append(">");
      }
    
    Vector value;
    if(o instanceof Vector)
      value=(Vector)o;
    else 
      {
	value=new Vector(1);
	if(o!=null)
	  value.addElement(o);
      }

    if(!hidden)
      {
	Object opt=getOptions(); 
	
	for(int i=0; i<getOptionsLength(opt); i++)
	  {
	    Object val=getOptionValue(opt, i);
	    
	    sb.append("<option value=\"").append(formatOptionValue(opt, i, val)).append("\" ");
	    for(Enumeration f= value.elements(); f.hasMoreElements(); )
	      if(val.equals(f.nextElement()))
		sb.append("selected ");
	    sb.append(">");
	    sb.append(formatOptionTitle(opt, i)).append("</option>");
	  }
	sb.append("</select>");
      }
    else
      {
	for(Enumeration f= value.elements(); f.hasMoreElements(); )
	  {
	    Object val=f.nextElement();
	    sb.append("<input type=\"hidden\" name=\"").append(getInputName()).append("\" value=\"").append(formatOptionValue(null, 0, val)).append("\">");
	  }
      }
    return sb.toString();
  }
  
}
