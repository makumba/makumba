package org.makumba.abstr;

public class setComplexParser extends ptrOneParser
{
  public FieldParser parse(FieldCursor fc) 
  {
    super.parse(fc);
    subtable.mainPtr=addPtrHere();
    return this;
  }
}
