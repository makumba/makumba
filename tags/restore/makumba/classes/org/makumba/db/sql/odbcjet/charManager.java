package org.makumba.db.sql.odbcjet;
import java.sql.*;

/** odbcjet might represent char fields as text fields */
public class charManager extends org.makumba.db.sql.charManager
{
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return super.unmodified(rsm, index) || 
      rsm.getColumnType(index)==java.sql.Types.VARCHAR;
  }
}
