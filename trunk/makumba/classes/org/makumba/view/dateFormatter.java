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

package org.makumba.view;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.GregorianCalendar;

public class dateFormatter extends FieldFormatter
{
  static String[] params= { "default", "empty", "format" };
  static String[][] paramValues= { null, null, null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public String formatNotNull(Object o, Dictionary formatParams) 
  {
    DateFormat formatter=yearDate;
    String s=(String)formatParams.get("format");
    if(s!=null)
      {
	formatter= new SimpleDateFormat(s, org.makumba.MakumbaSystem.getLocale());
	formatter.setCalendar(calendar);
      }      

    return formatter.format((java.util.Date)o); 
  }

  public static final Calendar calendar;
  
  public static final DateFormat yearDate;
  public static final DateFormat debugTime;

  static {
    calendar= new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
    yearDate= new SimpleDateFormat("dd MMMM yyyy",org.makumba.MakumbaSystem.getLocale());
    debugTime= new SimpleDateFormat("d MMMM yyyy HH:mm:ss zzz",org.makumba.MakumbaSystem.getLocale());
    yearDate.setCalendar(calendar);
    debugTime.setCalendar(calendar);
  }
}
