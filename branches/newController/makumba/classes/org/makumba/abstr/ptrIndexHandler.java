package org.makumba.abstr;
import org.makumba.*;

public class ptrIndexHandler extends FieldHandler
{
  public String getDataType() { return "pointer"; }
  public Class getJavaType() { return Pointer.class; }
  public Object getNull() { return Pointer.Null; }

  public Object checkValueImpl(Object value) 
  {
    if(value instanceof Pointer)
      {
	if(!((Pointer)value).getType().equals(getPointedType().getName()))
	  throw new InvalidValueException(getFieldInfo(), getPointedType().getName(), (Pointer)value);
	return value;
      }
    if(value instanceof String)
      return new Pointer(getPointedType().getName(), (String)value);
    throw new InvalidValueException(getFieldInfo(), "Only java.lang.String and org.makumba.Pointer are assignable to makumba pointers, given value <"+value+"> is of type "+value.getClass().getName());
  }

  public RecordInfo getPointedType() 
  {
    return getFieldInfo().getRecordInfo(); 
  }
}
