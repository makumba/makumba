package org.makumba.abstr;

public class FieldParser extends FieldHandler
{
  FieldParser(){}
  
  FieldParser(FieldInfo fi)
  {
    this.fi= fi;
  }
 
  FieldParser parse(FieldCursor fc) throws org.makumba.DataDefinitionParseError
  { 
    while(true)
    { 
      if(fc.lookup("not"))
      {
        if(fi.notNull)
          throw fc.fail("too many not null");
        fc.expect("null ");
        fi.notNull= true;
        continue;
      }
      
      if(fc.lookup("fixed "))
      {
        if(fi.fixed)
          throw fc.fail("too many fixed");
        fi.fixed= true;
        continue;
      }
      
      break;
    }
    
    
    FieldParser ret= setType(fc.expectTypeLiteral(), fc);
    
    if(ret == null) 
      {
        String s=fc.rp.definedTypes.getProperty(fi.type);
        if(s==null)
          throw fc.fail("unknown type: "+fi.type);
        fc.substitute(fi.type.length(), s);
        
        ret= setType(fc.expectTypeLiteral(), fc);

        if(ret== null)
          throw fc.fail("unknown type: "+fi.type);
      }
    fi.description= fi.description==null?fi.name:fi.description;
    return ret;
  }

  FieldParser setType(String s, FieldCursor fc) throws org.makumba.DataDefinitionParseError
  {
     fi.type=s;
     FieldParser ret;
     
     ret= (FieldParser)fc.rp.makeHandler(fi.type);
     if(ret==null)
        return null;
     ret.fi= fi;        
     return ret.parse(fc);        
  }

  String addText(String nm, String origNm, String val)  
  { return "subfield not allowed"; }  
  
  String acceptTitle(String nm, String origNm, String val, Object o) 
  { 
    val= val.trim();
    if(nm.equals("!title"))
    {
        RecordInfo ri=(RecordInfo)o;
        if(ri.fields.get(val)==null)
	  return ri.getName()+ " has no field called "+ val;
        fi.extra2=val;
        return null;
    }
    return addText(nm, origNm, val);
  }  
  
  public void parseSubfields() { }
}
