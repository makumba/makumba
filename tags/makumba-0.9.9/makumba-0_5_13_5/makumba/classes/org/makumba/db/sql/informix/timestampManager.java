package org.makumba.db.sql.informix;

public class timestampManager extends org.makumba.db.sql.timestampManager
{
  protected String getDBType()
  {
    return "DATETIME YEAR TO FRACTION";
  }
}
