package org.makumba.controller.html;
import org.makumba.view.*;
import javax.servlet.*;
import java.util.*;

public class intEnumEditor extends intEditor
{
  public String formatShow(Object o, Dictionary formatParams)
  {
    StringBuffer sb=new StringBuffer();
    sb.append("<select name=\"").append(getInputName(formatParams)).append("\">");
    Enumeration v=getValues();
    Enumeration n=getNames();
    while(v.hasMoreElements())
      {
	Object vl=v.nextElement();
	sb.append("<option value=\"").append(vl).append("\"");
	if(vl.equals(o))
	  sb.append(" selected");
	sb.append(">").append(n.nextElement()).append("</option>");
      }
    sb.append("</select>");
    return sb.toString();
  }
}
