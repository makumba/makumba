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

package org.makumba.db.makumba.sql;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.makumba.FieldDefinition;
import org.makumba.db.makumba.sql.SQLDBConnection;

public class MysqlTableManager extends org.makumba.db.makumba.sql.TableManager
{
  @Override
protected String createDbSpecific(String command){return command+" type=InnoDB"; }

  /** checks if an alteration is needed, and calls doAlter if so */
  @Override
protected void alter(org.makumba.db.makumba.sql.SQLDBConnection dbc, CheckingStrategy cs, boolean alter) throws SQLException
  {
    Statement st= dbc.createStatement();
    ResultSet rs=st.executeQuery("SHOW CREATE TABLE "+getDBName());
    rs.next();
    String def=rs.getString(2).trim();
    if(def.lastIndexOf(')') > def.lastIndexOf(" TYPE=InnoDB") &&
			def.lastIndexOf(')') > def.lastIndexOf(" ENGINE=InnoDB")){
      String s="ALTER TABLE "+getDBName()+" TYPE=InnoDB";
      if(alter) {
          java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(getSQLDatabase().getName()+": "+s);
          st.executeUpdate(s);
      } else {
          java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning("should alter: " + s);
      }

      }
    rs.close();
    st.close();
    super.alter(dbc, cs, alter);
  }

  /** mysql needs to have it adjustable, depending on version (see bug 512) */
  @Override
protected String getTableMissingStateName(SQLDBConnection dbc)
  {
   try{
     //version:"3.0.5-gamma" has major:3, minor:0
     String version=dbc.getMetaData().getDriverVersion();
     int major=dbc.getMetaData().getDriverMajorVersion();
     int minor=dbc.getMetaData().getDriverMinorVersion();
     String mark=""+major+"."+minor+".";
     String minorStr=version.substring(version.indexOf(mark)+mark.length());
     if(minorStr.indexOf('-') == -1)
     	minorStr=minorStr.substring(0,minorStr.indexOf(' '));
     else
       minorStr=minorStr.substring(0,minorStr.indexOf('-'));
     int minor2=Integer.parseInt(minorStr);

     if(major>3 || major==3 && minor>0 || major==3 && minor==0 && minor2>=8)
       return "tableMissing";
     else
       return "tableMissing-before308";
   }catch(Exception e) {
       //e.printStackTrace();
	return "tableMissing";
   }
  }
  
  @Override
protected int getSQLType(String fieldName) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
        case FieldDefinition._text:
            return -1;
        default:
            return super.getSQLType(fieldName);
        }
    }


}
