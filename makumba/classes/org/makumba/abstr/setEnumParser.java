package org.makumba.abstr;

public abstract class setEnumParser extends subtableParser
{
  FieldInfo enum= new FieldInfo(subtable, "enum");
  
  public FieldParser parse(FieldCursor fc) 
  {
    makeSubtable(fc);
    subtable.mainPtr=addPtrHere();
    subtable.addField1(enum);
    subtable.title=enum.name;
    subtable.foreignPtr=enum.name;
    parseEnum(fc);
    fi.description= fc.lookupDescription(); 
    enum.description=fi.description==null?enum.name:fi.description;
    return this;
  }
  
  abstract void parseEnum(FieldCursor fc);
}
