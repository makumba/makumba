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

    fi.unique=true;
    return setType("ptrOne", fc);
  }
  
  String addText(String nm, String origNm, String val) 
  { 
        return acceptTitle(nm, origNm, val, fi.extra1);
  }  
  
}
