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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Dictionary;

/** This deals with unique indexes in SQL, ensuring the uniqueness at the java level. Most SQL drivers provide internal mechanisms for this.
 * This class contains a simple syncronized incrementor*/
public class ptrIndexJavaManager extends ptrDBManager
{
  long n;
  int dbsv;

  protected void reset()
  {
    n= rm.getSQLDatabase().getMinPointerValue();
  }

  protected String getPtrType() { return getFieldDefinition().getDataDefinition().getName(); }

  /** called at table open. determines the maximum index with this database's  dbsv */
  public void onStartup(RecordManager rm, java.util.Properties p, SQLDBConnection dbc) 
       throws SQLException
  {
    super.onStartup(rm, p, dbc);
    dbsv= rm.getSQLDatabase().getDbsv();

    Statement st= dbc.createStatement();
    
    reset();
    
    ResultSet rs= st.executeQuery("SELECT MAX("+getDBName()+"), COUNT("+getDBName()+") FROM "+rm.tbname+" WHERE "+
				  getDBName()+">="+n+" AND "+getDBName()+"<="+rm.getSQLDatabase().getMaxPointerValue());
    //    long n2=n;
    rs.next();
    if(rs.getLong(2)>0)
      n=rs.getLong(1);
    
    rs.close();

/*    rs=st.executeQuery("SELECT "+getDBName()+" FROM "+rm.tbname);

    while(rs.next())
      {
  	long i= rs.getLong(getDBName());
  	if(i>>nbit== dbsv && i>n2)
  	  n2=i;
      }
    if(n2!=n)
      throw new RuntimeException(""+n2+"<>"+n);
    rs.close();
    */
      st.close();
  }

  SQLPointer nxt(Dictionary d)
  {
    SQLPointer i= new SQLPointer(getPtrType(), nextId());
    d.put(getName(), i);
    return i;
  }

  public void checkInsert(Dictionary d)
  {
    Object o=d.get(getName());
    if(o!=null)
      {
	checkCopy("index");
	d.put(getName(), checkValue(o));
      }
  }

  public void checkUpdate(Dictionary d)
  {
    Object o=d.get(getName());
    if(o!=null)
      throw new org.makumba.InvalidValueException(getFieldDefinition(), "you cannot update an index pointer");
  }

  public void setInsertArgument(PreparedStatement ps, int n, Dictionary d) 
       throws SQLException
  {
    org.makumba.Pointer p=(org.makumba.Pointer)d.get(getName());
    if(p!=null)
      {
	super.setInsertArgument(ps, n, d);
	if(p.getDbsv()==dbsv&& p.longValue()>this.n)
	  this.n=p.longValue();
	return;
      }
    ps.setInt(n, (int)nxt(d).longValue());
  }

  /** writes a unique index in the data */
  public String inInsert(Dictionary d)
  {
    org.makumba.Pointer p=(org.makumba.Pointer)d.get(getName());
    if(p!=null)
       {
	 if(p.getDbsv()==dbsv&& p.longValue()>this.n)
	   this.n=p.longValue();
	 return super.inInsert(d);
       }
    return ""+nxt(d).longValue();
  }

  public void setCopyArgument(PreparedStatement ps, int n, Dictionary d) throws SQLException
  {
    super.setCopyArgument(ps, n, d);
  }

  /** copies index from the data */
  public String inCopy(Dictionary d)
  {
    return super.inInsert(d);
  }

  /** writes a unique index in the data */
  public String inUpdate(java.util.Dictionary d)
  {
    throw new RuntimeException("shouldn't be called");
  }

  public void setUpdateArgument(PreparedStatement ps, int n, Dictionary d) 
  {
    throw new RuntimeException("shouldn't be called");
  }

  /** determines the unique index by incrementing a counter */
  protected synchronized long nextId()
  {
    return ++n;
  }
}
