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
