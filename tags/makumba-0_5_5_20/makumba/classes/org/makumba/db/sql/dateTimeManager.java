package org.makumba.db.sql;
import java.sql.*;

/** Represents a org.makumba date for the drivers that support the DATETIME type (notably ODBC). Normally this class is not used, a handler family that would use it, has to redirect the date type to a dateTime type */
public class dateTimeManager extends FieldManager
{
  /** returns datetime */
  protected String getDBType()
  {
    return "DATETIME";
  }

  public int getSQLType()
  {
    return java.sql.Types.TIMESTAMP;
  }

  /** writes the date between apostrophes */
  public String writeConstant(Object o)
  { 
    return "\'"+new Timestamp(((java.util.Date)o).getTime())+"\'";
      //"\'"+super.writeConstant(o)+"\'"; 
  }

  public Object toSQLObject(Object o)
  {
    return new Timestamp(((java.util.Date)o).getTime());
  }

  /** get the java value of the recordSet column corresponding to this field. This method should return null if the SQL field is null 
   */
  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    Object o= rs.getObject(i);
    if(rs.wasNull())
      return null;
    return o;
  }

}
