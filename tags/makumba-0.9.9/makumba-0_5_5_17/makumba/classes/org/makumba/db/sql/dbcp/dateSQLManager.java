package org.makumba.db.sql.dbcp;
import java.sql.*;

/** should be corrected to accept dates like 2-11-30 */

/** Represents a timestamp. Used by dateCreate and dateModify for their DB representation */
public class dateSQLManager extends org.makumba.db.sql.dateSQLManager
{
  /** a timestamp is always sent as null to the database */
  public String writeConstant(Object o)
  { 
    return "DATE "+super.writeConstant(o);
  }
}
