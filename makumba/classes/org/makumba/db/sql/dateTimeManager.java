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
import java.sql.*;

/** Represents a org.makumba date for the drivers that support the DATETIME type (notably ODBC). Normally this class is not used, a handler family that would use it, has to redirect the date type to a dateTime type */
public class dateTimeManager extends FieldManager
{
  /** returns datetime */
  protected String getDBType()
  {
    return "DATETIME";
  }

  public int getSQLType()
  {
    return java.sql.Types.TIMESTAMP;
  }

  /** writes the date between apostrophes */
  public String writeConstant(Object o)
  { 
    return "\'"+new Timestamp(((java.util.Date)o).getTime())+"\'";
      //"\'"+super.writeConstant(o)+"\'"; 
  }

  public Object toSQLObject(Object o)
  {
    return new Timestamp(((java.util.Date)o).getTime());
  }

  /** get the java value of the recordSet column corresponding to this field. This method should return null if the SQL field is null 
   */
  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    Object o= rs.getObject(i);
    if(rs.wasNull())
      return null;
    return o;
  }

}
