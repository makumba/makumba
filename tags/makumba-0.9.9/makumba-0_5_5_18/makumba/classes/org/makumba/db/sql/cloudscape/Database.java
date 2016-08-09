package org.makumba.db.sql.cloudscape;
import java.sql.*;
import java.util.*;

/** the database adapter for PostgreSQL */
public class Database extends org.makumba.db.sql.Database
{
  /** simply calls super */
  public Database(Properties p) 
    { super(p); }

  /** Postgres column names are case-insensitive */
  protected String getFieldName(String s)
  {
    return super.getFieldName(s).toUpperCase();
  }

  /** the postgres jdbc driver does not return sql states...
   * we just let every state pass, but print the exception */
  protected void checkState(SQLException e, String state)
  {
    System.out.println(e);
  }

  /** returns org.makumba.db.sql.pgsql.RecordManager */
  protected Class getTableClass()
  { return org.makumba.db.sql.cloudscape.RecordManager.class; }


}
