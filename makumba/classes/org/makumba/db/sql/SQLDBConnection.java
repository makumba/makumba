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

