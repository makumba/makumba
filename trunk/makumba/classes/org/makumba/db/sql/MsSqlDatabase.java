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
import java.util.Properties;


/** the database adapter for Microsoft SQL Server */
public class MsSqlDatabase extends org.makumba.db.sql.Database
{

  /** calls super and adds DB-specific properties */
  public MsSqlDatabase(Properties p) 
    { 
	  super(addProperties(p));
	}

	private static Properties addProperties(Properties p) {
		//set the database name - mssql jdbc driver does not use it in jdbc url, but as additional property: 
		//DatabaseName=myAppDbName
		p.put("sql.DatabaseName", p.getProperty("#database"));

		// we make sure that sql.SelectMethod=Cursor is sent to mssql in the connection properties
		p.put("sql.SelectMethod", "Cursor");
		return p;
	}

	
	protected String getJdbcUrl(Properties p) {
		// makumba mssql implementation accepts stuff like localhost_mssql_makumba.properties
		//which needs to be converted to JDBC url for MS jdbc driver: 
		//jdbc:microsoft:sqlserver://server_name:1433
		//or in case with named instances: servername!instancename_mssql_makumba.properties
		//which needs to give url:
		//jdbc:microsoft:sqlserver://server_name\\instance_name
		super.getJdbcUrl(p); //needed to set eng field
		String url="jdbc:microsoft:sqlserver://";
		String host = p.getProperty("#host");
		host = host.replaceAll("!","\\\\");
		url +=host;
		return url;
	}

	/*
	protected boolean isDuplicateException(SQLException e) {
		return e.getMessage().toLowerCase().indexOf("violation of unique index") != -1;
	}
	*/


	/** MS SQL Server uses incompatible sytax, see http://blog.daemon.com.au/archives/000301.html */
	public boolean supportsLimitInQuery() {
		return false;
	}

/*	
	public String getLimitSyntax() {
		//return 	" LIMIT ? OFFSET ?";
		return 	" TOP (?) ";
	}
	
	public boolean isLimitOffsetFirst() {
		return true;
	}
*/

	
}
