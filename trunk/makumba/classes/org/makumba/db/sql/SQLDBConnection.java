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

  SQLDBConnection(org.makumba.db.Database db, java.sql.Connection c){ super(db); conn=c; n=nconn++;}

  public String toString(){ return "connection "+n; }
  public DatabaseMetaData getMetaData()throws SQLException{return conn.getMetaData(); }
  public Statement createStatement()throws SQLException { return conn.createStatement(); }

  PreparedStatement getPreparedStatement(String s)
  {
    try{
       return conn.prepareStatement(s);      
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

