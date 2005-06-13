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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.makumba.Pointer;

public class dateHandler extends FieldHandler
{
  static final Date empty ;
  public Object getEmptyValue() { return empty; }
  public Date getDefaultDate(){ return (Date)getDefaultValue(); }
  public String getDataType() { return "datetime"; }
  public Class getJavaType() { return java.util.Date.class; }
  public Object getNull() { return Pointer.NullDate; }
  public Object checkValueImpl(Object value) { return normalCheck(value); }

    static{
	Calendar c=  new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
	c.clear();
	c.set(1900, 0, 1);
	empty=c.getTime();
    }

}



