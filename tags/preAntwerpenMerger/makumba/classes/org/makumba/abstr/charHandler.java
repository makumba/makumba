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
import org.makumba.InvalidValueException;
import org.makumba.Pointer;

public class charHandler extends FieldHandler implements stringTypeFixed
{
  public Object getEmptyValue(){ return ""; }
  public String getDataType() { return "char"; }
  public Class getJavaType() { return java.lang.String.class; }

  public String getDefaultString(){ return (String)getDefaultValue(); }
  
  public int getWidth(){ return ((Integer)fi.extra2).intValue(); } 

  public Object getNull() { return Pointer.NullString; }

  public Object checkValueImpl(Object value) 
  { 
    normalCheck(value); 
    String s= (String)value;
    if(s.length()> getWidth())
      throw new InvalidValueException(getFieldDefinition(), "String too long for char[] field. Maximum width: "+getWidth()+" given width "+s.length()+".\n\tGiven value <"+s+">");
    return value;
  }
}

