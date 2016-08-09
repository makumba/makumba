package org.makumba.view.jsptaglib;
import org.makumba.view.*;

public class intEditor extends charEditor
{
  static String[] params= {  "size", "maxlength" };
  static String[][] paramValues= { null, null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public int getWidth() { return 10; }

  public String getLiteral(Object o, java.util.Dictionary formatParams) 
  {
    return o.toString();
  }

  public Object readFrom(HttpParameters par)
  { 
    Object o=par.getParameter(getInputName());
    
    if(o instanceof java.util.Vector)
      { throw new InvalidValueException(this, "multiple value not accepted for integer: "+o); }
    return toInt(o);
  }

}
