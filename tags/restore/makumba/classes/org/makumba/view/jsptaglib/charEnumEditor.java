package org.makumba.view.jsptaglib;
import org.makumba.view.*;
import javax.servlet.*;
import java.util.*;

public class charEnumEditor extends FieldEditor
{
  public String formatShow(Object o, Dictionary formatParams)
  {
    StringBuffer sb=new StringBuffer();
    sb.append("<select name=\"").append(getInputName()).append("\">");
    Enumeration n=getNames();
    while(n.hasMoreElements())
      {
	Object nl=n.nextElement();
	sb.append("<option value=\"").append(nl).append("\"");
	if(nl.equals(o))
	  sb.append(" selected");
	sb.append(">").append(nl).append("</option>");
      }
    sb.append("</select>");
    return sb.toString();
  }
}
