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

/** standard SQL drivers have to break the org.makumba date in two. This represents a strictly SQL date (i.e. only the date part)
  *@see org.makumba.db.sql.timeManager
  */
public  class dateSQLManager extends dateManager
{
  /** returns DATE */
  protected String getDBType()
  {
    return "DATE";
  }
  /** returns date */
  public String getDataType(){ return "date"; }

  public int getSQLType()
  {
    return java.sql.Types.DATE;
  }

  /** checks if the column type is java.sql.Types.DATE 
   * TIMESTAMP is accepted as well
   */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    int tp=rsm.getColumnType(index);
    return tp==java.sql.Types.DATE || tp== java.sql.Types.TIMESTAMP;
  }

  public Object getValue(ResultSet rs, int i)
    throws SQLException
  {
    Date d= rs.getDate(i);
    if(rs.wasNull())
      return null;
    return d;
  }
  
  public Object toSQLObject(Object o)
  {
    return new Date(((java.util.Date)o).getTime());
  }
  public String writeConstant(Object o)
  { 
    return "\'"+super.writeConstant(o)+"\'";
  }

  
}
