package org.makumba.abstr;

public class ptrOneParser extends subtableParser
{
  RecordParser parser;

  public FieldParser parse(FieldCursor fc) 
  {
    makeSubtable(fc);
    parser= new RecordParser(subtable, fc.rp);
    return this;
  }
  
  String addText(String nm, String origNm, String val)
  { 
    if(parser.text.putLast(nm, origNm, val)!=null)
      return "field already exists";
    return null;
  }  
  
  public void parseSubfields() 
  { 
    parser.parse();
    fi.extra2= parser.ri.title;
  }
}
