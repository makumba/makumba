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
import java.sql.SQLException;
import java.util.Properties;

/** the database adapter for PostgreSQL */
public class DbcpDatabase extends org.makumba.db.sql.Database
{
  /** simply calls super */
  public DbcpDatabase(Properties p) 
    { super(p); }

  /** column names are case-insensitive */
  protected String getFieldName(String s)
  { //FIXME needs a new nameresolver
    return s.toLowerCase();//nr.getFieldNameInSource(s).toLowerCase();
  }


  /** the postgres jdbc driver does not return sql states...
   * we just let every state pass, but print the exception */
  protected void checkState(SQLException e, String state)
  {
    System.out.println(e+" "+e.getSQLState());
  }

//TODO now in sqlEngines.properties -->OK?  
//  /** returns org.makumba.db.sql.pgsql.RecordManager */
//  protected Class getTableClass()
//  
//  { return org.makumba.db.sql.dbcp.RecordManager.class; }

  protected String getJdbcUrl(Properties p)
  {
     return "jdbc:dbcp://local";
  }

}
