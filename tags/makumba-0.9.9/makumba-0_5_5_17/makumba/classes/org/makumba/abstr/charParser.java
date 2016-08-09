package org.makumba.abstr;

public class charParser extends FieldParser
{
  public FieldParser parse(FieldCursor fc) 
  {
    if(!fc.lookup("{"))
    {
      fc.expect("[");
      fi.extra2= fc.expectInteger();
      fc.expect("]");
      fi.description= fc.lookupDescription();
      return this;
    }
      
    return setType("charEnum", fc);
  }
}
