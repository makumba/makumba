package org.makumba.abstr;

public class setcharEnumParser extends setEnumParser
{
  void parseEnum(FieldCursor fc) 
  {
    enum.type="charEnum";
    fc.expectCharEnum(enum);
  }
}
