package org.makumba.db.sql;
import java.sql.*;
import java.util.Dictionary;
import java.util.Calendar;

/** standard SQL drivers have to break the org.makumba date in two. This represents a strictly SQL time (i.e. only the time part)
  *@see org.makumba.db.sql.dateSQLManager
  */
public  class timeManager extends FieldManager
{
  /** returns the normal name, postfixed by "_t" */
  public String getName(){ return super.getName()+"_t"; }

  /** returns time */
  public String getDataType(){ return "time"; }
  
  /** returns TIME */
  protected String getDBType()
  {
    return "TIME";
  }
  public int getSQLType()
  {
    return java.sql.Types.TIME;
  }

    /** set the java value in a data chunk. it will take what the dateOnlyManager previously written, and combine them */
  public void setValue(Dictionary d, ResultSet rs, int i) 
       throws SQLException
  {
    Calendar date= Calendar.getInstance();
    Calendar time=Calendar.getInstance();
    
    date.setTime((Date)d.get(getDataName()));
    time.setTime((java.util.Date)getValue(rs, i));
    date.set(Calendar.HOUR, time.get(Calendar.HOUR));
    date.set(Calendar.MINUTE, time.get(Calendar.MINUTE));	
    date.set(Calendar.SECOND, time.get(Calendar.SECOND));
    date.set(Calendar.AM_PM, time.get(Calendar.AM_PM));
    d.put(getDataName(), date.getTime());
  }
  
  public Object toSQLObject(Object o)
  {
    return new Time(((java.util.Date)o).getTime());
  }

  public String writeConstant(Object o)
  { 
    return "\'"+super.writeConstant(o)+"\'";
  }

  public Object getDefaultValue()
  {
    return new Time(((Date)super.getDefaultValue()).getTime());
  }
}
