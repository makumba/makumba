package org.makumba.db.sql.qed;
import java.sql.*;

/** this deals with SQL chars */
public class charManager extends org.makumba.db.sql.charManager
{
  /** returns char */
   protected String getDBType()
   {
     return "VARCHAR";
   }

 /** Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this returns true. Some SQL drivers allocate more anyway. */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return rsm.getColumnType(index)==java.sql.Types.VARCHAR;
  }
}
