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

package org.makumba.db.sql;
import org.makumba.abstr.FieldHandler;

/** This handler just replaces itself with two handlers, one for the date part, the other for the time part
  */
public class dateManager extends FieldManager
{
  /** returns a dateSQL and a time */
  public Object replaceIn(org.makumba.abstr.RecordHandler rh)
  { 
    FieldHandler[] ret;
    Database d= ((RecordManager)rh).getSQLDatabase();
    if(d.getConfiguration("separateDate")!=null)
      {
	ret= new FieldHandler[1];
    	ret[0]= rh.makeHandler("dateTime");
      }
    else
      {
	ret= new FieldHandler[2];
	ret[0]= rh.makeHandler("dateSQL");
	ret[1]= rh.makeHandler("time");
      }
    return ret;

  }
}
