package org.makumba.db.sql;
import java.sql.*;

/** this deals with SQL chars */
public class charManager extends FieldManager
{
  /** returns char */
  protected String getDBType()
  {
    return "VARCHAR";
  }

  protected int getSQLType()
  {
    return java.sql.Types.VARCHAR;
  }

  /** Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this returns true. Some SQL drivers allocate more anyway. */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return (super.unmodified(rsm, index) || rsm.getColumnType(index)== java.sql.Types.CHAR)&& checkWidth(rsm, index);
  }

  /** check the char width */
  protected boolean checkWidth(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    // some drivers might allocate more, it's their business
    return rsm.getColumnDisplaySize(index)>=getWidth();
  }

  /** Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this returns true. Some SQL drivers allocate more anyway. */
  protected boolean unmodified(int type, int size, java.util.Vector columns, int index)
       throws SQLException
  {
    return (super.unmodified(type, size, columns, index) 
	    || type== java.sql.Types.CHAR)&& checkWidth(size);
  }

  /** check the char width */
  protected boolean checkWidth(int width)
       throws SQLException
  {
    // some drivers might allocate more, it's their business
    return width>=getWidth();
  }

  /** write in CREATE, in the form name char[size] */
  public String inCreate(Database d)
    {
        String s= Database.getEngineProperty(d.getEngine()+"."+"charBinary");
        if(s!=null && s.equals("true"))
            s=" BINARY";
        else
            s="";
        return super.inCreate(d)+"("+getWidth()+")"+s;
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
    if(o instanceof byte[])
        return new String((byte[])o);
    return o;
  }

}
