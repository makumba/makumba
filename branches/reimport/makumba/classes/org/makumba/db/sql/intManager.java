package org.makumba.db.sql;
import java.sql.*;

/** deals with SQL ints */
public class intManager extends FieldManager
{
  /** returns int */
  protected String getDBType()
  {
    return "INT";
  }

  protected int getSQLType()
  {
    return java.sql.Types.INTEGER;
  }

  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    int n= rs.getInt(i);
    if(rs.wasNull())
      return null;
    return new Integer(n);
  }


}
