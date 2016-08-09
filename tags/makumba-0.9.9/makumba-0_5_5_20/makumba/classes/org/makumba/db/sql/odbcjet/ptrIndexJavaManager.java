package org.makumba.db.sql.odbcjet;

/** odbcjet might represent int as double */
public class ptrIndexJavaManager extends org.makumba.db.sql.ptrIndexJavaManager
{
  protected boolean unmodified(java.sql.ResultSetMetaData rsm, int index)
       throws java.sql.SQLException
  {
    return super.unmodified(rsm, index) ||
      rsm.getColumnType(index)==java.sql.Types.DOUBLE;
  }
}
