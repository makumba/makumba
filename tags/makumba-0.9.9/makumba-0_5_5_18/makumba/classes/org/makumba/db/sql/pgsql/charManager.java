package org.makumba.db.sql.pgsql;
import java.sql.*;

/** the Postgres jdbc driver seems to be dum about the width of character fields */
public class charManager extends org.makumba.db.sql.charManager
{
  /** postgres can't check char width, we return true */
  protected boolean checkWidth(ResultSetMetaData rsm, int index)
  {
    return true;
  }
}
