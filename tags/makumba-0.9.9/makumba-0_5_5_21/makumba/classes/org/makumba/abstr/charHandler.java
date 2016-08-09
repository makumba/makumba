package org.makumba.abstr;
import org.makumba.*;

public class charHandler extends FieldHandler implements stringTypeFixed
{
  public Object getEmptyValue(){ return ""; }
  public String getDataType() { return "char"; }
  public Class getJavaType() { return java.lang.String.class; }

  public String getDefaultString(){ return (String)getDefaultValue(); }
  
  public int getWidth(){ return ((Integer)fi.extra2).intValue(); } 

  public Object getNull() { return Pointer.NullString; }

  public Object checkValueImpl(Object value) 
  { 
    normalCheck(value); 
    String s= (String)value;
    if(s.length()> getWidth())
      throw new InvalidValueException(getFieldInfo(), "String too long for char[] field. Maximum width: "+getWidth()+" given width "+s.length()+".\n\tGiven value <"+s+">");
    return value;
  }
}

