///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.db.sql;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Properties;

import org.makumba.abstr.FieldHandler;
import org.makumba.abstr.FieldInfo;
import org.makumba.abstr.RecordHandler;

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
