package org.makumba.abstr;

public class intParser extends FieldParser
{
  public FieldParser parse(FieldCursor fc) 
  {
    if(!fc.lookup("{"))
      {
        fi.description= fc.lookupDescription(); 
        return this;
      }
    return setType("intEnum", fc);
  }
  
  
}
