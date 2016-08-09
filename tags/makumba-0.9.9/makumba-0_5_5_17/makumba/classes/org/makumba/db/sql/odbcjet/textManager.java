package org.makumba.db.sql.odbcjet;
import java.sql.*;
import org.makumba.*;
import java.io.*;

/** the odbc text is called "longbinary" and works with streams instead of large byte arrays */
public class textManager extends org.makumba.db.sql.textManager
{
  protected String getDBType()
  {
    return "LONGBINARY";
  }

  /** get the java value of the recordSet column corresponding to this field. This method should return null if the SQL field is null */
  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    InputStream in=rs.getBinaryStream(i);
    if(rs.wasNull())
      return null;
    return new Text(in);
  }

  public void setArgument(PreparedStatement ps, int n, Object o)
       throws SQLException
  {    
    Text t= (Text)o;
    if(t.length()==0)
      ps.setBytes(n, new byte[0]);
    else
      ps.setBinaryStream(n, t.toBinaryStream(), t.length());
  }
}

