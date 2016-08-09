package org.makumba.abstr;

public class simpleParser extends FieldParser
{
  public FieldParser parse(FieldCursor fc)
  {
    fi.description= fc.lookupDescription(); 
    return this;
  }
}
