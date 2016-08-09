package org.makumba.db.sql.dbcp;
import java.sql.*;
import java.util.*;

/** the database adapter for PostgreSQL */
public class Database extends org.makumba.db.sql.Database
{
  /** simply calls super */
  public Database(Properties p) 
    { super(p); }

  /** column names are case-insensitive */
  protected String getFieldName(String s)
  {
    return super.getFieldName(s).toLowerCase();
  }


  /** the postgres jdbc driver does not return sql states...
   * we just let every state pass, but print the exception */
  protected void checkState(SQLException e, String state)
  {
    System.out.println(e+" "+e.getSQLState());
  }

  /** returns org.makumba.db.sql.pgsql.RecordManager */
  protected Class getTableClass()
  { return org.makumba.db.sql.dbcp.RecordManager.class; }

  
  /** get a jdbc connection 
  protected Connection getConnection() throws SQLException
  {
	return makeConnection();
  }

  protected void initConnections() throws SQLException{ }
  protected void closeConnections() throws SQLException{ }

  protected void releaseConnection(Connection conn) throws SQLException
  {
	  conn.close();
  }
  */

  protected String getJdbcUrl(Properties p)
  {
     return "jdbc:dbcp://local";
  }

}
