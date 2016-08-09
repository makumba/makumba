package org.makumba.db.sql.odbcjet;
import java.sql.*;
import java.util.Dictionary;


public  class timeManager extends org.makumba.db.sql.timeManager
{
  /** ODBC seems to represent TIME as TIMESTAMP...
   */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return super.unmodified(rsm, index) || rsm.getColumnType(index)==java.sql.Types.TIMESTAMP;
  }
}
