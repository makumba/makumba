package org.makumba.abstr;
import java.util.Enumeration;

public abstract class subtableParser extends FieldParser
{
  RecordInfo subtable, here;

  void makeSubtable(FieldCursor fc)
  {
    here=fc.rp.ri;
    subtable= here.makeSubtable(fi.name);
    subtable.addStandardFields(subtable.subfield);
    fi.extra1= subtable;
  }

  String addPtr(String name, RecordInfo o)
  {
    int n= name.lastIndexOf('.');
    if(n!=-1)
        name= name.substring(n+1);
    while(subtable.fields.get(name)!= null)
        name= name+"_";

    FieldInfo ptr= new FieldInfo(subtable, name);
    subtable.addField1(ptr);
    ptr.fixed=true;
    ptr.notNull=true;
    ptr.type= "ptrRel";
    ptr.extra1= o;
    ptr.description= "relational pointer";
    return name;
  }

  String addPtrHere()
  {
    //    System.err.println(here.canonicalName()+" "+subtable.canonicalName());
    subtable.relations=1;
    if(here.isSubtable())
      return addPtr(here.subfield, here);
    else
      return addPtr(here.name, here);
  }
}
