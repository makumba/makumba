package org.makumba.abstr;
import org.makumba.*;

public class intHandler extends FieldHandler 
{
  static final Object empty= new Integer(0);
  public Object getEmptyValue(){ return empty; }
  public String getDataType() { return "int"; }
  public Class getJavaType() { return java.lang.Integer.class; }

  public int getDefaultInt(){ return ((Integer)getDefaultValue()).intValue(); }
  
  public Object getNull() { return Pointer.NullInteger; }

  public Object checkValueImpl(Object value) { return normalCheck(value); }
}
