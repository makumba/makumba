package org.makumba.db.sql.odbcjet;

/** this deals with dates in ODBC */
public class dateTimeManager extends org.makumba.db.sql.dateTimeManager
{
  /** stupdid odbc needs a {ts 'date'} format when writing date constants */
	public String writeConstant(Object o)
  { return "{ts "+super.writeConstant(o)+"}"; }
}