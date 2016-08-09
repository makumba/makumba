package org.makumba.db.sql.msql;
import org.makumba.abstr.*;
import java.sql.*;
import java.util.*;

/** The msql particularities of the database
 */
public class Database extends org.makumba.db.sql.Database
{
  /** simply calls super */
  public Database(Properties p) 
    { super(p); }

  /** msql doesn't accept underscores as first char of table name */
  protected String getTableName(String s)
  {
    s= super.getTableName(s);
    if(s.charAt(0)=='_')
      s= "x"+s.substring(1);
    return s;
  }

  /** the imaginary jdbc driver does not return sql states... 
   * we just let every state pass, but print the exception */
  protected void checkState(SQLException e, String state)
  {
    System.out.println(e); 
  }
  
  /** returns org.makumba.db.sql.msql.RecordManager */
  protected Class getTableClass()
  { return org.makumba.db.sql.msql.RecordManager.class; } 

}
