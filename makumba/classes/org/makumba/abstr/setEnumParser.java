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

public abstract class setEnumParser extends subtableParser
{
  FieldInfo _enum= new FieldInfo(subtable, "enum");
  
  public FieldParser parse(FieldCursor fc) 
  {
    makeSubtable(fc);
    subtable.mainPtr=addPtrHere();
    subtable.addField1(_enum);
    subtable.title=_enum.name;
    subtable.setField=_enum.name;
    parseEnum(fc);
    fi.description= fc.lookupDescription(); 
    _enum.description=fi.getDescription()==null?_enum.name:fi.getDescription();
    return this;
  }
  
  abstract void parseEnum(FieldCursor fc);
}
