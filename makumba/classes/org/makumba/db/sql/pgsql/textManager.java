package org.makumba.db.sql.pgsql;
import java.sql.*;
import org.makumba.*;

/** the Postgres jdbc driver seems to be dum about the width of character fields */
public class textManager extends org.makumba.db.sql.textManager
{
  /** returns Postgres TEXT */
  protected String getDBType()
  {
    return "TEXT";
  }
  
  protected int getSQLType()
  {
    return Types.VARCHAR;
  }

  public void setArgument(PreparedStatement ps, int n, Object o)
       throws SQLException
  {
    Text t= Text.getText(o);
    ps.setString(n, t.toString());
  }
}
