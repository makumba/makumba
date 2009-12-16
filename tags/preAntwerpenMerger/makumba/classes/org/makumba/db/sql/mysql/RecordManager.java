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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.makumba.MakumbaSystem;
import org.makumba.db.sql.SQLDBConnection;


public class RecordManager extends org.makumba.db.sql.RecordManager
{
  protected String createDbSpecific(String command){return command+" type=InnoDB"; }

  /** checks if an alteration is needed, and calls doAlter if so */
  protected void alter(org.makumba.db.sql.SQLDBConnection dbc, CheckingStrategy cs) throws SQLException
  {
    Statement st= dbc.createStatement();
    ResultSet rs=st.executeQuery("SHOW CREATE TABLE "+getDBName());
    rs.next();
    String def=rs.getString(2).trim();
    if(def.lastIndexOf(')') > Math.max(def.lastIndexOf(" TYPE=InnoDB"), def.lastIndexOf(" ENGINE=InnoDB"))){
      String s="ALTER TABLE "+getDBName()+" TYPE=InnoDB";
      MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(getSQLDatabase().getConfiguration()+": "+s);
      st.executeUpdate(s);
    }
    rs.close();
    st.close();
    super.alter(dbc, cs);
  }

  /** mysql needs to have it adjustable, depending on version (see bug 512) */
  protected String getTableMissingStateName(SQLDBConnection dbc)
  {
   try{
     //version:"3.0.5-gamma" has major:3, minor:0
     String version=dbc.getMetaData().getDriverVersion();
     int major=dbc.getMetaData().getDriverMajorVersion();
     int minor=dbc.getMetaData().getDriverMinorVersion();

     String pattern = ".*"+major+"\\."+minor+"\\.(\\d+).*";
     Matcher matcher = Pattern.compile(pattern).matcher(version);
     matcher.matches();
	 int minor2 = Integer.parseInt(matcher.group(1));	
     MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info("Determined MySQL JDBC driver version: " +major+"."+minor+"."+minor2);

     if(major>3 || major==3 && minor>0 || major==3 && minor==0 && minor2>=8)
       return "tableMissing";
     else
       return "tableMissing-before308";
   }catch(Exception e) {
    MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning("Could not determine MySQL JDBC driver version, assuming 3.0.8 or later.");
	e.printStackTrace();
	return "tableMissing";
   }
  }


}