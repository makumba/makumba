package org.makumba.db.sql.odbcjet;

/** this deals with dates in ODBC */
public class timestampManager extends org.makumba.db.sql.timestampManager
{
  /** stupdid odbc needs a {ts 'date'} format when writing date constants */
  public String writeConstant(Object o)
  { return "{ts "+super.writeConstant(o)+"}"; }

  /*  public void setInsertArgument(java.sql.PreparedStatement ps, int n, java.util.Dictionary d) 
       throws java.sql.SQLException
  {setNullArgument(ps, n);}
  */
}
