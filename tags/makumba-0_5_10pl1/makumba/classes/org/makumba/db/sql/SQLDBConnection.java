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
import org.makumba.*;
import org.makumba.db.*;
import java.sql.*;
import org.makumba.util.*;

public class SQLDBConnection extends DBConnection
{
  static int nconn=0;
  int n;

  private Connection conn;

  SQLDBConnection(org.makumba.db.Database db) throws SQLException
  { 
    super(db);
    n=nconn++;
    makeConnection();
  }
  
  private void makeConnection() throws SQLException
  {
    conn=DriverManager.getConnection(((org.makumba.db.sql.Database)db).url, ((org.makumba.db.sql.Database)db).connectionConfig);
    if(conn.getMetaData().supportsTransactions())
      conn.setAutoCommit(false);
    if(conn.getMetaData().supportsTransactionIsolationLevel(Database.DESIRED_TRANSACTION_LEVEL))
      conn.setTransactionIsolation(Database.DESIRED_TRANSACTION_LEVEL);
  }

  public void commit()
  {
    try{
      conn.commit();
    }catch(SQLException e) 
      {
	Database.logException(e, this);
	throw new DBError(e);
      } 
  }


  public void rollback()
  {
    try{
      conn.rollback();
    }catch(SQLException e) 
      {
	Database.logException(e, this);
	throw new DBError(e);
      } 
  }

  private Connection getConnection() throws SQLException
  {
      /* if(conn.isClosed())
      {
	MakumbaSystem.getMakumbaLogger("db.exception").warning("reconnecting connection "+n);
	makeConnection();
	}*/

    return conn;
  }
  public String toString(){ return "connection "+n; }

  public DatabaseMetaData getMetaData()throws SQLException
  {return getConnection().getMetaData(); }

  public Statement createStatement()throws SQLException 
  { return getConnection().createStatement(); }

  PreparedStatement getPreparedStatement(String s)
  {
    try{
       return getConnection().prepareStatement(s);      
    }catch(SQLException e) 
      {
	org.makumba.db.sql.Database.logException(e);
	throw new DBError(e); 
      }
  }

    /*
    try{
      return (PreparedStatement)preparedStatements.getResource(s);
    }catch(RuntimeWrappedException e)
      {
	if(e.getReason() instanceof SQLException)
	  {
	    org.makumba.db.sql.Database.logException((SQLException)e.getReason());
	    throw new DBError(e.getReason()); 
	  }
	throw e;
      }
  }
  
  NamedResources preparedStatements= new NamedResources(new NamedResourceFactory()
   {
     protected Object makeResource(Object nm) throws SQLException {
       return conn.prepareStatement((String)nm);
     }
  });
  */
}

