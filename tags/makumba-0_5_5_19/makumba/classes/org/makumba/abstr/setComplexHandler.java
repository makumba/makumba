package org.makumba.abstr;

public class setComplexHandler extends ptrOneHandler
{
  public String getDataType() { return "null"; }
  public Class getJavaType() { return null; }

  public Object checkValueImpl(Object value)
  {
    throw new org.makumba.InvalidValueException(getFieldInfo(), "subsets cannot be assigned directly");
  }
}
