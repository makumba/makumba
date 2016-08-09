package org.makumba.db.sql.dbcp;
import java.sql.*;

/** Represents a timestamp. Used by dateCreate and dateModify for their DB representation */
public class timeManager extends org.makumba.db.sql.timeManager
{
  /** a timestamp is always sent as null to the database */
  public String writeConstant(Object o)
  { 
    return "TIME "+super.writeConstant(o);
  }
}
