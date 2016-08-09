package org.makumba.abstr;

public class setParser extends subtableParser
{
  RecordInfo settbl;

  public FieldParser parse(FieldCursor fc) 
  {
    RecordInfo ori= fc.lookupTableSpecifier();
    if(ori==null)
    {
      String word= fc.lookupTypeLiteral();
      if(word==null)
      {
        try{
          fi.description= fc.lookupDescription();
          } catch(org.makumba.DataDefinitionParseError pe)
          { throw fc.fail("table specifier, enumeration type, or nothing expected"); }
        return setType("setComplex", fc);
      }
      FieldParser fp= enumSet(fc, word);
      if(fp!=null)
	return fp;
      
      String s= fc.rp.definedTypes.getProperty(word);
      if(s==null)
	throw fc.fail("table, char{}, int{} or macro type expected after set");
      
      fc.substitute(word.length(), s);
      
      fp= enumSet(fc, fc.expectTypeLiteral());
      
      if(fp!= null)
	return fp;

      throw fc.fail("int{} or char{} macro expected after set");
     }

    makeSubtable(fc);
    subtable.mainPtr=addPtrHere();

    settbl= ori;

    subtable.foreignPtr=addPtr(settbl.getBaseName(), ori);

    return this;
  }

  String addText(String nm, String origNm, String val)
  {
    String s=acceptTitle(nm, origNm, val, settbl);
    if(s==null)
      subtable.title=val.trim();
    return s;
  }

  FieldParser enumSet(FieldCursor fc, String word) 
  {
    FieldParser fp;
    if(fc.lookup("{"))
      {
        fp= setType("set"+word+"Enum", fc);
        if(fp!= null)
           return fp;
        fc.fail("int{} or char{} expected after set");
      }
    return null;

  }

  public void parseSubfields()
  {
    if(fi.extra2==null)
    {
        fi.extra2=subtable.title= settbl.getTitleField();
    }
  }

}
