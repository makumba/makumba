package org.makumba.abstr;
import java.util.Vector;

public class intEnumParser extends FieldParser
{
  public FieldParser parse(FieldCursor fc) 
  {
    fc.expectIntEnum(fi);
    fi.description= fc.lookupDescription(); 
    return this;
  }
}
