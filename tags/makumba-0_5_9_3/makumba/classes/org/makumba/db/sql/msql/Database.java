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

package org.makumba.db.sql.msql;
import org.makumba.abstr.*;
import java.sql.*;
import java.util.*;

/** The msql particularities of the database
 */
public class Database extends org.makumba.db.sql.Database
{
  /** simply calls super */
  public Database(Properties p) 
    { super(p); }

  /** msql doesn't accept underscores as first char of table name */
  protected String getTableName(String s)
  {
    s= super.getTableName(s);
    if(s.charAt(0)=='_')
      s= "x"+s.substring(1);
    return s;
  }

  /** the imaginary jdbc driver does not return sql states... 
   * we just let every state pass, but print the exception */
  protected void checkState(SQLException e, String state)
  {
    System.out.println(e); 
  }
  
  /** returns org.makumba.db.sql.msql.RecordManager */
  protected Class getTableClass()
  { return org.makumba.db.sql.msql.RecordManager.class; } 

}
