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
import org.makumba.*;

public class ptrIndexHandler extends FieldHandler
{
  public String getDataType() { return "pointer"; }
  public Class getJavaType() { return Pointer.class; }
  public Object getNull() { return Pointer.Null; }

  public Object checkValueImpl(Object value) 
  {
    if(value instanceof Pointer)
      {
	if(!((Pointer)value).getType().equals(getPointedType().getName()))
	  throw new InvalidValueException(getFieldInfo(), getPointedType().getName(), (Pointer)value);
	return value;
      }
    if(value instanceof String)
      return new Pointer(getPointedType().getName(), (String)value);
    throw new InvalidValueException(getFieldInfo(), "Only java.lang.String and org.makumba.Pointer are assignable to makumba pointers, given value <"+value+"> is of type "+value.getClass().getName());
  }

  public RecordInfo getPointedType() 
  {
    return getFieldInfo().getRecordInfo(); 
  }
}
