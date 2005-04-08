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
import java.util.Vector;

public class charEnumHandler extends charHandler implements Enumerator
{
  public Enumeration getValues() { return ((Vector)fi.extra1).elements(); }
  
  public Enumeration getNames() { return ((Vector) fi.extra1).elements(); }
    
  public int getEnumeratorSize(){ return ((Vector)fi.extra1).size(); }
  
  public String getStringAt(int i){ return ((Vector)fi.extra1).elementAt(i).toString(); }
  
  public String getNameAt(int i){ return (String)((Vector)fi.extra1).elementAt(i); }

  public Object checkValueImpl(Object value) 
  {
    super.checkValueImpl(value);

    Vector names=(Vector)fi.extra1; 

    for(int i=0; i<names.size(); i++)
      if(names.elementAt(i).equals(value))
	return value;
    throw new org.makumba.InvalidValueException(getFieldDefinition(), "value set to char enumerator ("+value+") is not a member of "+names);
  }
}
