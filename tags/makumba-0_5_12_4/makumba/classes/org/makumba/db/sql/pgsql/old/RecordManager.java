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

package org.makumba.db.sql.pgsql.old;

/** just a surogate class to indicate there is a small handler sub-family here
 * There is one type redirectation in this family: charEnum=char 
 * RecordHandler should have loaded org.makumba.db.sql.pgsql.charManager when it saw the similar redirection in the upper family, so there is a small bug here */
public class RecordManager extends org.makumba.db.sql.pgsql.RecordManager
{
  static{
    System.out.println(""+(char)7+"old Postgres driver in use");
  }
}
