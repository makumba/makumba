package org.makumba.db.sql;
import java.sql.*;

/** standard SQL drivers have to break the org.makumba date in two. This represents a strictly SQL date (i.e. only the date part)
  *@see org.makumba.db.sql.timeManager
  */
public  class dateSQLManager extends dateManager
{
  /** returns DATE */
  protected String getDBType()
  {
    return "DATE";
  }
  /** returns date */
  public String getDataType(){ return "date"; }

  public int getSQLType()
  {
    return java.sql.Types.DATE;
  }

  /** checks if the column type is java.sql.Types.DATE 
   * TIMESTAMP is accepted as well
   */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    int tp=rsm.getColumnType(index);
    return tp==java.sql.Types.DATE || tp== java.sql.Types.TIMESTAMP;
  }

  public Object getValue(ResultSet rs, int i)
    throws SQLException
  {
    Date d= rs.getDate(i);
    if(rs.wasNull())
      return null;
    return d;
  }
  
  public Object toSQLObject(Object o)
  {
    return new Date(((java.util.Date)o).getTime());
  }
  public String writeConstant(Object o)
  { 
    return "\'"+super.writeConstant(o)+"\'";
  }

  
}
