package org.makumba.controller.html;
import java.util.*;

public class setintEnumEditor extends setcharEnumEditor
{
  public Object getOptionValue(Object options, int i)
  { return new Integer(getIntAt(i)); }

  public Object readFrom(org.makumba.controller.http.HttpParameters par, String suffix)
  { 
    Object o=par.getParameter(getInputName(suffix));

    if(o==null || o==org.makumba.Pointer.NullSet)
      return o;
    if(o instanceof Vector)
      {
	Vector v=(Vector)o;
	for(int i=0; i<v.size(); i++)
	  v.setElementAt(toInt(v.elementAt(i)), i);
	return v;
      }
    return toInt(o);
  }
}
