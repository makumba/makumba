package org.makumba.controller.html;
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

  public Object readFrom(org.makumba.controller.http.HttpParameters par, String suffix)
  { 
    Object o=par.getParameter(getInputName(suffix));
    
    if(o instanceof java.util.Vector)
      { throw new InvalidValueException(this, "multiple value not accepted for integer: "+o); }
    return toInt(o);
  }

}
