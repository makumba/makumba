package org.makumba.db.sql;
import java.sql.*;
import java.io.*;
import org.makumba.*;

/** this deals with SQL uncontrolled-length fields */
public class textManager extends FieldManager
{
  /** returns text */
  protected String getDBType()
  {
      return "LONG VARBINARY";
  }
  
  protected int getSQLType()
  {
    return java.sql.Types.LONGVARBINARY;
  }

  /** does apostrophe escape */
  public String writeConstant(Object o)
  { return org.makumba.db.sql.Database.SQLEscape(o.toString()); }

  /** get the java value of the recordSet column corresponding to this field. This method should return null if the SQL field is null */
  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    Object o= super.getValue(rs, i);
    if(o==null )
      return o;
    return Text.getText(o);

    /*
    InputStream is= rs.getBinaryStream(i);
    if(is==null )
      return null;
    return new Text(is);
    */
  }

  public void setArgument(PreparedStatement ps, int n, Object o)
       throws SQLException
  {
    Text t= Text.getText(o);
    ps.setBinaryStream(n, t.toBinaryStream(), t.length());
    //ps.setBytes(n, t.getBytes());
  }

  public boolean shouldIndex(){ return false; }

}
