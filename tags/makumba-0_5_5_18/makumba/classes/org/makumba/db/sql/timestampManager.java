package org.makumba.db.sql;
import java.sql.*;

/** Represents a timestamp. Used by dateCreate and dateModify for their DB representation */
public class timestampManager extends FieldManager
{
  /** returns timestamp */
  protected String getDBType()
  {
    return "TIMESTAMP";
  }

  /** writes the date between apostrophes */
  public String writeConstant(Object o)
  { 
    return "\'"+super.writeConstant(o)+"\'"; 
  }

  public int getSQLType()
  {
    return java.sql.Types.TIMESTAMP;
  }

  public void setInsertArgument(PreparedStatement ps, int n, java.util.Dictionary d) throws SQLException
  {
    Object o= d.get(getName());
    if(o instanceof java.util.Date && !(o instanceof Timestamp))
      d.put(getName(), new Timestamp(((java.util.Date)o).getTime()));
    super.setInsertArgument(ps, n,d);
  }

  public Object checkValueImpl(Object value)
    {
	Object o=super.checkValueImpl(value);	
	if(o instanceof java.util.Date && !(o instanceof Timestamp))
	   o= new Timestamp(((java.util.Date)o).getTime());
	return o;
    }

}
