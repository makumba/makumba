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
import org.makumba.abstr.*;
import java.sql.*;
import java.util.*;

/** the general SQL field handler */
public abstract class FieldManager extends FieldHandler
{
  String dbname;
  RecordManager rm;

  /** what is the database level type of this field? */
  protected String getDBType(Database d)
  {
    String s= Database.getEngineProperty(d.getEngine()+"."+getDataType());
    if(s==null)
      return getDBType();
    return s;
  }


  /** what is the database level type of this field? */
  protected String getDBType()
  {
    throw new RuntimeException(""+getClass()+"should be redefined");
  }

  /** what is the SQL type of this field? */
  protected int getSQLType()
  {
    throw new RuntimeException(""+getClass()+"should be redefined");
  }

  /** check if the column from the SQL database still coresponds with the abstract definition of this field */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return rsm.getColumnType(index)==getSQLType();
  }

  /** check if the column from the SQL database (read from the catalog) still coresponds with the abstract definition of this field */
  protected boolean unmodified(int type, int size, Vector columns, int index)
       throws SQLException
  {
    return type==getSQLType();
  }

  /** the database-level name of the field */
  public String getDBName(){ return dbname; }

  /** ask this field to write its contribution in a SQL CREATE statement */
  public String inCreate(Database d){ return getDBName()+" "+getDBType(d);}

  /** ask this field to write its contribution in a SQL CREATE statement */
    //  public String inCreate(){ return getDBName()+" "+getDBType(null);}
 
  /** ask this field to write a value of its type in a SQL statement*/
  public String writeConstant(Object o)
  { 
    if(o==getNull())
      return "null";
    return toSQLObject(o).toString();
  }

  /** transform the object for a SQL insert or update */
  public Object toSQLObject(Object o)
  { 
    return o;
  }

  /** ask this field to write write its argument placeholder ('?')  in a prepared INSERT SQL statement*/
  public String inPreparedInsert(){ return "?"; }

  /** ask this field to write write its argumment placeholder in a prepared UPDATE SQL statement*/
  public String inPreparedUpdate(){ return getDBName()+"=?"; }

  /** ask this field to write write its argumment value in a prepared INSERT SQL statement*/
  public void setInsertArgument(PreparedStatement ps, int n, Dictionary d) 
       throws SQLException
  {
    Object o= d.get(getDataName());
    if(o==null ||o.equals(getNull()))
      setNullArgument(ps, n);
    else
      setArgument(ps, n, o);
  }

  /** ask this field to write write its argumment value in a prepared SQL statement for copying*/
  public void setCopyArgument(PreparedStatement ps, int n, Dictionary d) 
       throws SQLException
  {
    try{
      Object o= d.get(getDataName());
      if(o==null ||o.equals(getNull()))
	setNullArgument(ps, n);
      else
	setArgument(ps, n, o);
    }catch(Exception e){ throw new RuntimeException(getName()+ " "+e.getMessage()); }
  }

  /** ask this field to write write its argumment value in a prepared UPDATE SQL statement*/
  public void setUpdateArgument(PreparedStatement ps, int n, Dictionary d) 
       throws SQLException
  {
    setUpdateArgument(ps, n, d.get(getDataName()));
  }

  /** ask this field to write write its argumment value in a prepared UPDATE SQL statement*/
  public void setUpdateArgument(PreparedStatement ps, int n, Object o) 
       throws SQLException
  {
    if(o==getNull())
      setNullArgument(ps, n);
    else
      try{
      setArgument(ps, n, o);
    }catch(SQLException e) { org.makumba.MakumbaSystem.getMakumbaLogger("db.update.execution").log(java.util.logging.Level.SEVERE, getDataName()+"  "+o.getClass(), e); throw e; }
  }

  /** set a non-null argument of this type in a prepared SQL statement */
  public void setArgument(PreparedStatement ps, int n, Object o)
       throws SQLException
  {
    ps.setObject(n, toSQLObject(o));
  }

  /** set a null argument of this type in a prepared SQL statement */
  public void setNullArgument(PreparedStatement ps, int n)
       throws SQLException
  {
    ps.setNull(n, getSQLType());
  }

  /** OLDCODE ask this field to write its value in a SQL INSERT statement */
  public String inInsert(Dictionary d)
  {
    try{
      return writeConstant(d.get(getDataName()));
    }catch(NullPointerException e){return "null";}
  }

  /** OLDCODE ask this field to write its value in a SQL INSERT statement */
  public String inCopy(Dictionary d)
  {
    return inInsert(d);
  }

  /** OLDCODE ask this field to write its contribution in a SQL UPDATE statement 
   * should return "" if this field doesn't want to take part in the update
   */
  public String inUpdate(Dictionary d)
  {
    return getDBName()+"="+writeConstant(d.get(getDataName()));
  }

  /** ask this field to write its contribution in a SQL UPDATE statement 
   * should return "" if this field doesn't want to take part in the update
   */
  public String inCondition(Dictionary d, String cond)
  {
    return getDBName()+cond+writeConstant(d.get(getDataName()));
  }

  /** what is the property of the current engine? */
  protected String getEngineProperty(String s)
  {
    Database d=rm.getSQLDatabase();
    return d.getEngineProperty(d.getEngine()+"."+s);
  }


  /** ask this field to perform actions when the table is open 
   */
  public void onStartup(RecordManager rm, Properties config, SQLDBConnection dbc) 
       throws SQLException
  {
    this.rm=rm;
    if(rm.alter && shouldIndex())
	manageIndexes(dbc);
  }


  /** Ask this field how to name the index on this field. Normally called from manageIndexes().  */
  public String getDBIndexName() 
  {
     return rm.getDBName()+"_"+getDBName();  
  }


  /** Examine DB indexes. */
  public boolean isIndexOk(SQLDBConnection dbc)
  {
    Boolean b= (Boolean)rm.indexes.get(getDBIndexName().toLowerCase());
    if(b!=null)
      return (isUnique()==!b.booleanValue()); 
    return false;
  } //end isIndexOk()



  /** Ask this field to add/remove indexes as needed, normally called from onStartup().
   */
  public void manageIndexes(SQLDBConnection dbc) 
       throws SQLException
  {
     String keyName=getDBIndexName();
     String brief=rm.getRecordInfo().getName()+"#"+getName()+" ("+getDescription()+")";

     if(!isIndexOk(dbc)) 
     {
       //org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
       //	"ALTERING INDEX on field "+getName()+" of "+rm.getRecordInfo().getName() );

       try{	//drop the old, wrong index if it exists
		Statement st= dbc.createStatement();
		st.executeUpdate(indexDropSyntax());
		org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
			"INDEX DROPPED on "+brief );
		st.close();

       }catch(SQLException e) {}

       boolean createNormalEvenIfUnique=false;

       if(isUnique())
	{
	   try{
		//try creating unique index
		Statement st= dbc.createStatement();
		st.executeUpdate(indexCreateUniqueSyntax());
		org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
			"UNIQUE INDEX ADDED on "+brief );
		st.close();
		rm.indexCreated(dbc);
	   }catch(SQLException e) 
	   { 
		//log all errors 
		org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning(
			//rm.getDatabase().getConfiguration()+": "+ //DB name
			"Problem adding UNIQUE INDEX on "+brief
			+": "+e.getMessage() );
		createNormalEvenIfUnique=true;
	   }
	}

       if(createNormalEvenIfUnique || !isUnique()) 
        {
	   try{
		//create normal index
		Statement st= dbc.createStatement();
		st.executeUpdate(indexCreateSyntax());
		org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
			"INDEX ADDED on "+brief );
		st.close();
		rm.indexCreated(dbc);
	   }catch(SQLException e) 
	   { 
		   org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning(
			//rm.getDatabase().getConfiguration()+": "+ //DB name
			"Problem adding INDEX on "+brief
			+": "+e.getMessage() );
	   }
	} 

     }//isIndexOk

  }//method

  /** Syntax for index creation. */
  public String indexCreateSyntax() {
	return "CREATE INDEX "+getDBIndexName()+" ON "+rm.getDBName()+" ("+getDBName()+")";
  }

  /** Syntax for unique index creation. */
  public String indexCreateUniqueSyntax() {
	return "CREATE UNIQUE INDEX "+getDBIndexName()+" ON "+rm.getDBName()+" ("+getDBName()+")";
  }

  /** Syntax for dropping index. */
  public String indexDropSyntax() {
	return "DROP INDEX "+getDBIndexName()+" ON "+rm.getDBName();
  }


  /** Tell whether this type of field should be indexed. */
  public boolean shouldIndex() {return true; }

  /* sets the database-level name of this field, normally identical with its abstract-level name, unless the database has some restrictions, or the configuration indicates that the field exists in the table with another name */
  public void setDBName(RecordManager rm, Properties config) 
  {
    String dbname1=null;
    dbname1= config.getProperty(rm.getRecordInfo().getName()+"#"+getName());
    if(dbname1==null)
      {
	dbname1= rm.getSQLDatabase().getFieldName(getName());
	while(rm.checkDBName(dbname1))
	  dbname1=dbname1+"_";
      }
    dbname=dbname1;
  }


  /** set the java value in a data chunk. If the value in the recordset is SQL null, a NullPointerException is thrown*/
  public void setValue(Dictionary d, ResultSet rs, int i) 
    throws SQLException
  {
    Object o= getValue(rs, i);
    if(o!=null)
      d.put(getDataName(), o);
    else
      d.remove(getDataName());
  }

  /** set the java value in a data chunk. If the value in the recordset is SQL null, a NullPointerException is thrown*/
  public void setValue(Object []data, ResultSet rs, int i) 
    throws SQLException
  {
    data[i]=getValue(rs, i);
  }

  /** get the java value of the recordSet column corresponding to this field. This method should return null if the SQL field is null 
   */
  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    Object o= rs.getObject(i);
    if(rs.wasNull())
      return null;
    //	return getDefaultValue();
    return o;
  }

  protected void checkCopy(String s)
  {
    if(!rm.admin)
      throw new org.makumba.InvalidValueException(getFieldInfo(), "you cannot insert an "+s+" field unless the type "+rm.getRecordInfo().getName()+" has administration approval in the database connection file");
  }

}
