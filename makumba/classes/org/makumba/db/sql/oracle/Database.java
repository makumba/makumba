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

package org.makumba.db.sql.oracle;
import java.sql.*;
import java.util.*;

/** the database adapter for PostgreSQL */
public class Database extends org.makumba.db.sql.Database
{
  /** simply calls super */
  public Database(Properties p) 
    { super(p); }

  protected int getMaxTableNameLength() {return 30; }
  protected int getMaxFieldNameLength() {return 30; }


//  /** returns oracle RecordManager */
//  protected Class getTableClass()
//  { return org.makumba.db.sql.oracle.RecordManager.class; }

  protected String getJdbcUrl(Properties p)
  {
    String url="jdbc:oracle:thin:@//"+p.getProperty("#host")+":1521/";
    return url+p.getProperty("#database");
  }

}
