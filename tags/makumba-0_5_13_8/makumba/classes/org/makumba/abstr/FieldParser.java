///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

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
        fc.expect("null");
        fc.expectWhitespace();
        fi.notNull= true;
        continue;
      }
      
      if(fc.lookup("fixed"))
      {
        fc.expectWhitespace();
        if(fi.fixed)
          throw fc.fail("too many fixed");
        fi.fixed= true;
        continue;
      }

      if(fc.lookup("unique"))
      {
        fc.expectWhitespace();
        if(fi.unique)
          throw fc.fail("already unique");
        fi.unique= true;
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
