package org.makumba.db.sql.dbcp;
import java.sql.*;

/** Represents a timestamp. Used by dateCreate and dateModify for their DB representation */
public class timestampManager extends org.makumba.db.sql.timestampManager
{
  /** a timestamp is always sent as null to the database */
  public String writeConstant(Object o)
  { 
    return "TIMESTAMP "+super.writeConstant(o);
  }
}
