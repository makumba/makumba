package org.makumba.db.sql;
import org.makumba.*;
import java.sql.*;

/** this deals with pointers in SQL */
public class ptrDBManager extends FieldManager
{
  /** returns INT */
  protected String getDBType()
  {
    return "INT";
  }

  public int getSQLType()
  {
    return Types.INTEGER;
  }

  /** return the value as a Pointer */
  public Object getValue(ResultSet rs, int i) throws SQLException
  {
    Object o= super.getValue(rs, i);
    if(o==null )
      return o;
    return new SQLPointer(getPointedType().getName(), ((Number)o).longValue());
  }

  /** ask this field to write a value of its type in a SQL statement */
  public Object toSQLObject(Object o)
  {
    return new Integer((int)((Pointer)o).longValue());
  }
  
  /*
  public void onStartup(RecordManager rm, java.util.Properties p)
       throws SQLException
  {
    super.onStartup(rm, p);
    try{
      Statement st= rm.getSQLDatabase().getConnection().createStatement();
      st.executeUpdate("CREATE INDEX "+ rm.getDBName()+"_"+getDBName()+ " ON "+ rm.getDBName()+"("+getDBName()+")");
    }catch(SQLException e) { }
  }*/
}
