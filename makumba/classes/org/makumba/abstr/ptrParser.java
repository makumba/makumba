package org.makumba.abstr;

public class ptrParser extends FieldParser      
{
  public FieldParser parse(FieldCursor fc) 
  {
    Object o= fc.lookupTableSpecifier();
    
    if(o!=null)
      fi.extra1= o;
   try{
     fi.description= fc.lookupDescription(); 
    } catch(org.makumba.DataDefinitionParseError e) 
    { throw fc.fail("table specifier or nothing expected"); }

    if(o!=null)
      return this;
    return setType("ptrOne", fc);
  }
  
  String addText(String nm, String origNm, String val) 
  { 
        return acceptTitle(nm, origNm, val, fi.extra1);
  }  
  
}
