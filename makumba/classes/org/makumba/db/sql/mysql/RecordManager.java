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

package org.makumba.db.sql.mysql;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.makumba.MakumbaSystem;

public class RecordManager extends org.makumba.db.sql.RecordManager
{
  protected String createDbSpecific(String command){return command+" type=InnoDB"; }

  /** checks if an alteration is needed, and calls doAlter if so */
  protected void alter(Statement st, CheckingStrategy cs) throws SQLException
  {
    ResultSet rs=st.executeQuery("SHOW CREATE TABLE "+getDBName());
    rs.next();
    if(!rs.getString(2).trim().endsWith("TYPE=InnoDB")){
      String s="ALTER TABLE "+getDBName()+" TYPE=InnoDB";
      MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(getSQLDatabase().getConfiguration()+": "+s);
      st.executeUpdate(s);
    }
    rs.close();
    super.alter(st, cs);
  }

}
