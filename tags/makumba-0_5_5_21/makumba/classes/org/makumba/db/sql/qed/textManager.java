package org.makumba.db.sql.qed;

/** this deals with SQL uncontrolled-length fields */
public class textManager extends org.makumba.db.sql.textManager
{
  /** msql needs an 'approximative size' for text fields. */
  public String inCreate(Database d){ return super.inCreate(d)+"(1024000)";}

  }
