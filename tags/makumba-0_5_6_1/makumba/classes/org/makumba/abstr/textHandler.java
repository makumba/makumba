package org.makumba.abstr;
import org.makumba.*;

public class textHandler extends FieldHandler implements stringType
{
  public Object getEmptyValue(){ return ""; }

  public String getDataType() { return "text"; }
  public Class getJavaType() { return org.makumba.Text.class; }

  public String getDefaultString(){ return (String)getDefaultValue(); }

  public Object getNull() { return Pointer.NullText; }

  public Object checkValueImpl(Object value) 
  { 
    try{
      return Text.getText(value);
    }catch(InvalidValueException e) {throw new InvalidValueException(getFieldInfo(), e.getMessage()); }
    
  }
}
