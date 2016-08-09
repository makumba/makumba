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
import java.sql.SQLException;
import java.util.Properties;

//this file was based in the Claudspace... so it needs to be

/** the database adapter for Hsqldb */
public class HsqldbDatabase extends org.makumba.db.makumba.sql.Database
{
	
	protected String getJdbcUrl(Properties p) {
		// makumba hsqldb implementation accepts stuff like localhost_hsql_path_to_some_dir.properties
		String s=super.getJdbcUrl(p);
		String dbn= p.getProperty("#database");
		dbn=dbn.replace('_','/');
		int n = s.lastIndexOf(':');
		return s.substring(0,n+1)+dbn;
	}
	
  /** simply calls super */
  public HsqldbDatabase(Properties p) 
    { 
	  super(addShutdown(p));
	}

	private static Properties addShutdown(Properties p) {
		// we make sure that shutdown=true is sent to Hsqldb in the connection properties
		// this ensures db files cleanup when makumba goes down
		p.put("sql.shutdown", "true");
		return p;
	}

	public boolean isDuplicateException(SQLException e) {
		return e.getMessage().toLowerCase().indexOf("violation of unique index") != -1;
	}

	public String getLimitSyntax() {
		return 	" LIMIT ? OFFSET ?";
	}

	public boolean isLimitOffsetFirst() {
		return false;
	}
	
}
