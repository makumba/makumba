package org.makumba.abstr;
import java.util.Vector;

public class charEnumParser extends FieldParser
{
  public FieldParser parse(FieldCursor fc) 
  {
    fc.expectCharEnum(fi);
    fi.description= fc.lookupDescription();
    return this;
  }
}
