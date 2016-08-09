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
