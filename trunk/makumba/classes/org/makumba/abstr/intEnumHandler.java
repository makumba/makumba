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
import java.util.*;

public class intEnumHandler extends intHandler implements intEnumerator
{
  public Enumeration getValues() { return ((Vector)fi.extra1).elements(); }
  
  public Enumeration getNames() { return ((Vector) fi.extra2).elements(); }
    
  public Vector getDeprecatedValues() { return (Vector)fi.extra3; }
  
  public int getEnumeratorSize(){ return ((Vector)fi.extra1).size(); }
  
  public String getStringAt(int i){ return ((Vector)fi.extra1).elementAt(i).toString(); }
  
  public String getNameAt(int i){ return (String)((Vector)fi.extra2).elementAt(i); }

  public boolean isAssignableFrom(FieldInfo fi){ return super.isAssignableFrom(fi) || fi.getType().equals("int") || fi.getType().equals("char"); }

  public String getNameFor(int n){ 
    Vector names=(Vector)fi.extra2;
    Vector values=(Vector)fi.extra1;
    for(int i=0; i<values.size(); i++)
	if(values.elementAt(i).equals(new Integer(n)))
	   return (String)names.elementAt(i); 
    throw new org.makumba.InvalidValueException(getFieldInfo(), "Can't find a name for "+n+" in "+values+" with names "+names);
  }

  public int getIntAt(int i){ return ((Integer)((Vector)fi.extra1).elementAt(i)).intValue(); }

  public Object checkValueImpl(Object value) 
  {
    Vector names=(Vector)fi.extra2; 
    Vector values=(Vector)fi.extra1;
    if(value instanceof Integer)
      {
	for(int i=0; i<values.size(); i++)
	  if(values.elementAt(i).equals(value))
	    return value;
	throw new org.makumba.InvalidValueException(getFieldInfo(), "int value set to int enumerator ("+value+") is not a member of "+values);
      }
    if(!(value instanceof String))
      throw new org.makumba.InvalidValueException(getFieldInfo(), "int enumerators only accept values of type Integer or String. Value supplied ("+value+") is of type "+value.getClass().getName());

    for(int i=0; i<names.size(); i++)
      if(names.elementAt(i).equals(value))
	return values.elementAt(i);
    
    throw new org.makumba.InvalidValueException(getFieldInfo(), "string value set to int enumerator ("+value+") is neither a member of "+names+" nor amember of "+values);
  }
}
