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

package org.makumba.db.sql.odbcjet.v4;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/** Because of odbc4 bug with ts literals
	*	we use format without tenths of seconds for timestamp 
	*	"dd-MM-yyyy hh:mm:ss"
	**/
public class timestampManager extends org.makumba.db.sql.odbcjet.timestampManager
{
  static DateFormat odbcDate
  =new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.UK);
 
  public String writeConstant(Object o)
  { return "\'"+odbcDate.format( new Timestamp(((java.util.Date)o).getTime() )) +"\'"; }

}
