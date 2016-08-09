package org.makumba.db.sql.cloudscape;
import java.sql.*;

/** this deals with SQL uncontrolled-length fields */
public class textManager extends org.makumba.db.sql.textManager
{
  /** ask this field to write its contribution in a SQL CREATE statement */
  public String inCreate(Database d){ return getDBName()+" "+getDBType(d)+"(1024000)";}

}
