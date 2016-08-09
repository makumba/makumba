package org.makumba.db.sql.pgsql.old;
import java.sql.*;

public class charManager extends org.makumba.db.sql.pgsql.charManager
{
  /** allows CHAR type, not only varchar. for old databases */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return super.unmodified(rsm, index) || rsm.getColumnType(index)==Types.CHAR;
  }

  /** trims the data at the left. for old databases */
  public Object getValue(ResultSet rs, int i)
    throws SQLException
    {
      Object o=super.getValue(rs, i);
      if(o==null)
	return null;
      String s= o.toString();
      i= s.length()-1;
      while(i>=0 && s.charAt(i)==' ')
	i--;
      return s.substring(0, i+1);
    } 
}
