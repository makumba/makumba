package org.makumba.abstr;

public class setintEnumParser extends setEnumParser
{
  void parseEnum(FieldCursor fc) 
  {
    enum.type= "intEnum";
    fc.expectIntEnum(enum);
  }
}
