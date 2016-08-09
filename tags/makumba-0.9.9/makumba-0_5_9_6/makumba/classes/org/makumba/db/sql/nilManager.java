package org.makumba.db.sql;
import java.sql.ResultSet;

/** only used for nil projections of the OQL queries */
public class nilManager extends FieldManager
{
  public Object getValue(ResultSet rs, int i)
  {
    return null;
  }
}
