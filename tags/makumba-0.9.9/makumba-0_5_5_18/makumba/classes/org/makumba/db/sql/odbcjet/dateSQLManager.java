package org.makumba.db.sql.odbcjet;

/** odbc can use normal date-time separation too. If that is desired, this class should be used */
public class dateSQLManager extends org.makumba.db.sql.dateSQLManager
{
  /** odbc needs a {d 'date'} format when writing date constants */
  public String writeConstant(Object o)
  { return "{d "+super.writeConstant(o)+"}"; }
}
