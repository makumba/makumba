package org.makumba.db.sql.informix;

public class dateTimeManager extends org.makumba.db.sql.dateTimeManager
{
  protected String getDBType()
  {
    return "DATETIME YEAR TO FRACTION";
  }
}
