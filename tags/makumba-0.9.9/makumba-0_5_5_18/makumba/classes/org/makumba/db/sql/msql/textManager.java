package org.makumba.db.sql.msql;

/** this deals with SQL uncontrolled-length fields */
public class textManager extends org.makumba.db.sql.textManager
{
  /** what is the database level type of this field? */
  protected String getDBType()
  {
    return "TEXT";
  }

  /** msql needs an 'approximative size' for text fields. */
  public String inCreate(Database d){ return super.inCreate(d)+"(255)";}
}
