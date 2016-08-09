package org.makumba.db.sql;
import org.makumba.abstr.*;
import java.sql.*;
import java.util.*;

/** this field manager wraps around another field manager and constructs wrappers of whatever it wants to replace itself with */
public class wrapperManager extends FieldManager
{
  /** the wrapped object */
  FieldManager wrapped;

  public wrapperManager(){}
  
  public void setFieldInfo(FieldInfo fi)
  {
    super.setFieldInfo(fi);
    try{
      wrapped.setFieldInfo(fi);
    }catch(NullPointerException e){}
  }

  /** called at replaceIn() */
  protected FieldManager makeWrapped(RecordHandler rh)
  { throw new RuntimeException("should be redefined! "); }

  protected wrapperManager makeWrapper(FieldManager fm, RecordHandler rh)
  {
    try{
      wrapperManager wm= (wrapperManager)getClass().newInstance();
      wm.wrapped= fm;
      wm.setRecord(rh);
      return wm;
    }
    catch(Exception e){ throw new org.makumba.util.RuntimeWrappedException(e); }
  }

  protected void setRecord(org.makumba.abstr.RecordHandler rh){}

  public void setDBName(RecordManager rm, Properties config) 
  {
    wrapped.setDBName(rm, config);
  }

  public Object replaceIn(RecordHandler rh)
  {
    try{
      Object o= wrapped.replaceIn(rh);
      if(o== wrapped)
	{
	  return this;
	}
      if(o instanceof FieldHandler)
	return makeWrapper((FieldManager)o, rh);
      FieldHandler[] fha= (FieldHandler[])o;
      fha[0]= makeWrapper((FieldManager)fha[0], rh);
      return fha;
    }catch(NullPointerException e)
      {
	return makeWrapper(makeWrapped(rh), rh);
      }
  }

  protected String getDBType(){ return wrapped.getDBType(); }

  protected int getSQL(){ return wrapped.getSQLType(); }

  /** check if the column from the SQL database still coresponds with the abstract definition of this field */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return wrapped.unmodified(rsm, index);
  }


  /** check if the column from the SQL database still coresponds with the abstract definition of this field */
  protected boolean unmodified(int type, int size, java.util.Vector v, int index)
       throws SQLException
  {
    return wrapped.unmodified(type, size, v, index);
  }

  public Object toSQLObject(Object o){ return wrapped.toSQLObject(o); }

  //  public String inPreparedInsert(){ return wrapped.inPreparedInsert(); }

  public void setInsertArgument(PreparedStatement ps, int n, Dictionary d) 
       throws SQLException
  {
    wrapped.setInsertArgument(ps, n, d);
  }

  public void setNullArgument(PreparedStatement ps, int n)
       throws SQLException
  {
    wrapped.setNullArgument(ps, n);
  }

  /** the database-level name of the field */
  public String getDBName()
  { return wrapped.getDBName(); }

  /** ask this field to write its contribution in a SQL CREATE statement */
  public String inCreate(Database d){ return wrapped.inCreate(d); }
  
  /** ask this field to write a value of its type in a SQL statement */
  public String writeConstant(Object o)
  { 
    return wrapped.writeConstant(o);
  }

  /** ask this field to write its contribution in a SQL INSERT statement */
  public String inInsert(Dictionary d)
  {
    return wrapped.inInsert(d);
  }

  /** ask this field to write its contribution in a SQL UPDATE statement */
  public String inUpdate(Dictionary d)
  {
    return wrapped.inUpdate(d);
  }

  /** ask this field to perform actions when the table is open */
  public void onStartup(RecordManager rm, java.util.Properties p, SQLDBConnection dbc) 
       throws SQLException
  {
    this.rm=rm;
    wrapped.onStartup(rm, p, dbc);
  }


  /** set the java value in a data chunk */
  public void setValue(Dictionary d, ResultSet rs, int i) 
    throws SQLException
  {
    wrapped.setValue(d, rs, i);
  }

  /** get the java value of the recordSet column corresponding to this field */
  public Object getValue(ResultSet rs, int i)
    throws SQLException
    {
      return wrapped.getValue(rs, i); 
    }

  public Object checkValueImpl(Object value)
    {
	return wrapped.checkValueImpl(value);
    }
}
