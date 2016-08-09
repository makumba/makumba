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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.makumba.Pointer;

/** this deals with pointers in SQL */
public class ptrDBManager extends FieldManager
{
  //should it just extend intManager instead?

  /** returns INT */
  protected String getDBType()
  {
    return "INTEGER";
  }

  public int getSQLType()
  {
    return Types.INTEGER;
  }

  /** return the value as a Pointer */
  public Object getValue(ResultSet rs, int i) throws SQLException
  {
    Object o= super.getValue(rs, i);
    if(o==null )
      return o;
    return new SQLPointer(getPointedType().getName(), ((Number)o).longValue());
  }

  /** ask this field to write a value of its type in a SQL statement */
  public Object toSQLObject(Object o)
  {
    return new Integer((int)((Pointer)o).longValue());
  }
  
  /*
  public void onStartup(RecordManager rm, java.util.Properties p)
       throws SQLException
  {
    super.onStartup(rm, p);
    try{
      Statement st= rm.getSQLDatabase().getConnection().createStatement();
      st.executeUpdate("CREATE INDEX "+ rm.getDBName()+"_"+getDBName()+ " ON "+ rm.getDBName()+"("+getDBName()+")");
    }catch(SQLException e) { }
  }*/
}
