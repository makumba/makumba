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
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DBError;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.db.DBConnection;
import org.makumba.db.DBConnectionWrapper;
import org.makumba.db.Update;

/** An SQL database, using JDBC.
 * This class should comply with SQL-92
 */
public class Database extends org.makumba.db.Database
{
  Properties connectionConfig= new Properties();
  String url;
  String eng;  //
  Properties types= new Properties();
  boolean addUnderscore= true; 
  Hashtable catalog= null;
  
  static final int DESIRED_TRANSACTION_LEVEL= java.sql.Connection.TRANSACTION_REPEATABLE_READ;

  static Properties sqlDrivers;

  public String getEngine(){ return eng; }
  public static String getEngineProperty(String s){ return sqlDrivers.getProperty(s); }


  protected DBConnection makeDBConnection() 
  {
    try{
      return new SQLDBConnection(this);
    }catch(SQLException e){ 
      logException(e);
      throw new DBError(e); 
    }
  }

  static{
    sqlDrivers= new Properties();
    try{
      sqlDrivers.load(org.makumba.util.ClassResource.get("org/makumba/db/sql/sqlEngines.properties").openStream());
    }catch(Exception e){throw new org.makumba.MakumbaError(e); }
  }

  /** Initialize the database. Use Database.getDatabase to initilize a SQL database! This constructor will be invoked there (thus it should be hidden :). Besides the properties needed for the org.makumba.db.Database, the following are expected:
   * <dl>
   * <dt>sql.user<dd> the username needed to connect to the database
   * <dt>sql.password<dd> the password needed to connect to the database
   * </dl>

   * To accomodate existing tables:
   * <dl>
   * <dt>addUnderscore<dd> if "true", all table and field names have underscore appended to it, to prevent conflict with SQL keywords in some engines. Make it false for compatibility with older databases.
   * <dt>typename_root= replacement<dd> Specify a rule how to make table names shorter. E.g. a rule best.minerva.student=bms and a type best.minerva.student->fields will create table with shoter name _bms__fields_ instead of _best_minerva_student__fields_ (last underscore depends on the addUnderscore setting)
   * <dt>alter#typename_root<dd> if "true", allows alteration of the table(s) corresponding to the data definition, if the table structure does not correspond to the data definition. The longest indicated root for a type counts (e.g. the more generic root can have alteration on true and the more specific one on false)
   * <dt>typename<dd> can specify the name of the SQL table coresponding to the indicated type
   * <dt>typename#field<dd> can specify the name of the SQL field corresponding to the indicated field
   * </dl>
   */
  public Database(Properties p) 
  {
    super(p);

    try{
      url=getJdbcUrl(p);
      p.put("jdbc_url", url);

      String s;

      for(Enumeration e=p.keys(); e.hasMoreElements(); )
	{
	  s= (String)e.nextElement();
	  if(!s.startsWith("sql."))
	    continue;
	  connectionConfig.put(s.substring(4), p.getProperty(s).trim());
	}

      // maybe this should be done just for mysql...
      if(connectionConfig.get("autoReconnect")==null)
	connectionConfig.setProperty("autoReconnect", "true");

      String driver= p.getProperty("sql.driver");
      s= p.getProperty("addUnderscore");
      if(s!=null)
	addUnderscore=s.equals("true");

      if(driver==null)
	driver= sqlDrivers.getProperty(url.substring(5, url.indexOf(':', 6)));
      
      MakumbaSystem.getMakumbaLogger("db.init").info("Makumba "+MakumbaSystem.getVersion()+" INIT: "+url);
      Class.forName(driver);
      initConnections();
      String staleConn= sqlDrivers.getProperty(getConfiguration("#sqlEngine")+".staleConnectionTime");

      if(staleConn!=null){
	long l= Long.parseLong(staleConn)*60000l;
	connections.startStalePreventionThread(l/2, l);
      }

      DBConnectionWrapper dbcw=(DBConnectionWrapper)getDBConnection();
      SQLDBConnection dbc=(SQLDBConnection)dbcw.getWrapped();
      try
       {
        p.put("sql_engine.name", dbc.getMetaData().getDatabaseProductName().trim());
	p.put("sql_engine.version", dbc.getMetaData().getDatabaseProductVersion().trim());
	p.put("jdbc_driver.name", dbc.getMetaData().getDriverName().trim());
	p.put("jdbc_driver.version", dbc.getMetaData().getDriverVersion().trim());

	MakumbaSystem.getMakumbaLogger("db.init").
	  info("\tconnected to "+p.get("sql_engine.name")+" version: "+
	       p.get("sql_engine.version")+
	       "\n\tusing "+p.get("jdbc_driver.name") +" version: "
	       +p.get("jdbc_driver.version")
	       +"\n\tusing DBSV "+p.get("dbsv"));
	if(!dbc.getMetaData().supportsTransactionIsolationLevel(DESIRED_TRANSACTION_LEVEL))
	  {
	    MakumbaSystem.getMakumbaLogger("db.init").
	      warning("transaction isolation level "+DESIRED_TRANSACTION_LEVEL+" not supported, using "+dbc.getMetaData().getDefaultTransactionIsolation());
	  }

	readCatalog(dbc);
	
      }finally{ dbcw.close(); }
    }catch(Exception e)
      {
	throw new org.makumba.MakumbaError(e);
      }
  }

  protected void readCatalog(SQLDBConnection dbc) throws SQLException
  {
    Exception ex=null;
    Hashtable c= new Hashtable();
    boolean failed=false;
    try{
      ResultSet rs= dbc.getMetaData().getColumns(null, null, "%", null);
      if(rs==null)
	failed=true;
      else
	while(rs.next())
	  {
	    String tn= rs.getString("TABLE_NAME");
	    Vector v= (Vector)c.get(tn);
	    if(v==null)
	      c.put(tn, v=new Vector());
	    Hashtable h= new Hashtable(5);
	    h.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
	    h.put("DATA_TYPE", new Integer(rs.getInt("DATA_TYPE")));
	    h.put("TYPE_NAME", rs.getString("TYPE_NAME"));
	    h.put("COLUMN_SIZE", new Integer(rs.getInt("COLUMN_SIZE")));
	    v.addElement(h);
	  }
      rs.close();
    }catch(SQLException e){ failed=true; ex=e;}
    if(failed)
      {
	MakumbaSystem.getMakumbaLogger("db.init").severe("failed to read catalog "+ex);
      }
    else
      {
	catalog=c;
      }
  }

  protected String getJdbcUrl(Properties p)
  {
    String url="jdbc:";
    eng=p.getProperty("#sqlEngine");
    url+=eng+":";
    String local= getEngineProperty(eng+".localJDBC");
    if(local==null || !local.equals("true"))
      url+="//"+p.getProperty("#host")+"/";
    return url+p.getProperty("#database");
  }

  public org.makumba.db.Query prepareQueryImpl(DBConnection dbc, String oqlQuery)
  {
    return new Query(dbc, oqlQuery);
  }

  public Update prepareUpdateImpl(DBConnection dbc, String type, String set, String where)
  {
    return new SQLUpdate((SQLDBConnection)dbc, type, set, where);
  }

  public int getMinPointerValue(){ return getDbsv()<<SQLPointer.getMaskOrder(); }

  public int getMaxPointerValue() { return ((getDbsv()+1)<<SQLPointer.getMaskOrder()) -1; }

  protected Class getTableClassConfigured()
  {
    String tcs;
    try{
      if((tcs=getConfiguration("tableclass"))!=null ||
	 (tcs=sqlDrivers.getProperty(getConfiguration("#sqlEngine")+".tableclass"))!=null)
	return Class.forName(tcs);
      else
	return getTableClass();
    }catch(Exception e) {throw new org.makumba.MakumbaError(e); }
  }

  protected Class getTableClass()
  { 
    return org.makumba.db.sql.RecordManager.class; 
  }

  /** escapes all apostrophes from a string and puts the string into apostrophes to be added in a sql command */
   public static String SQLEscape(String s)
   {
     StringBuffer sb= new StringBuffer("\'");
     int n= s.length();
     for(int i=0; i<n; i++)
       {
 		char c= s.charAt(i);
 		if(c=='\'')
 		  sb.append('\\');
 		else if(c=='\\')
 		  sb.append('\\');
 		else if(c=='\"')
 		  sb.append('\\');
 		else if((int)c==0)
 		{
 		  	sb.append("\\0");
 		  	continue;
	  	}
 		sb.append(c);

       }
     sb.append('\'');
     return sb.toString();
   }

  protected int getMaxTableNameLength() {return 64; }
  protected int getMaxFieldNameLength() {return 64; }

  /** get the database-level name of a table with a certain abstract name
    * This just replaces strange signs like ., -> ( ) with underscores _
    * old names are lowercased
    */
  protected String getTableName(String s)
  {
    //return (addUnderscore?s:("."+s.toLowerCase())).replace('.', '_').replace('(', '_').replace(')', '_').replace('>', '_').replace('-', '_')+(addUnderscore?"_":"");
    String name=s;
    if (!addUnderscore)
       name=("."+name.toLowerCase()); //OLDSUPPORT "/general/Person"->"_general_person"
    name=name.replace('.', '_').replace('(', '_').replace(')', '_').replace('>', '_').replace('-', '_');  //why '(' and ')'?
    name=name+(addUnderscore?"_":"");
    if(name.length()<=getMaxTableNameLength())
	return name;
    else  //compose "startingpartoflongnam___HASH"
	{
	 String hash=Integer.toString(name.hashCode(),Character.MAX_RADIX).replace('-','_');
	 String shortname=name.substring(0,getMaxTableNameLength()-3-hash.length());
	 return (shortname+"___"+hash);
	}
  }

  /** get the database-level name of a field with the given abstract name. This simply returns the same name, but it can be otherwise for certain more restrictive SQL engines
   * old names have first letter lowercased
   */
  protected String getFieldName(String s)
  {
     //return (addUnderscore?s:(s.startsWith("TS_")?s:s.substring(0,1).toLowerCase()+s.substring(1))).replace('.','_')+(addUnderscore?"_":"");
     String name=s;
     if(!addUnderscore && !s.startsWith("TS_"))   //make it start with lowercase
	name=name.substring(0,1).toLowerCase()+name.substring(1);
     name=name.replace('.','_');   //should be tirrelevant for field names, OLDSUPPORT?
     name= name+(addUnderscore?"_":"");
     if(name.length()<=getMaxFieldNameLength())
	return name;
    else  //compose "startingpartoflongnam___HASH"
	{
	 String hash=Integer.toString(name.hashCode(),Character.MAX_RADIX).replace('-','_');
	 String shortname=name.substring(0,getMaxFieldNameLength()-3-hash.length());
	 return (shortname+"___"+hash);
	}
  }

  /** check the sql state of a SQL exception and throw a DBError if it is not equal with the given state */
  protected void checkState(SQLException e, String state)
  {
    checkState(e, state, null);
  }
  
  /** check the sql state of a SQL exception and throw a DBError if it is not equal with the given state */
  protected void checkState(SQLException e, String state, String command)
  {
    state= sqlDrivers.getProperty(getConfiguration("#sqlEngine")+"."+state);
    if(state!=null && e.getSQLState().equals(state))
      return;
    MakumbaSystem.getMakumbaLogger("db.init.tablechecking").log(java.util.logging.Level.SEVERE, ""+e.getSQLState(), e);
    throw new org.makumba.DBError(e, command);
  }

  /** execute a prepared statement and log it, and its exceptions. return the number of records affected, or -1 if a duplicate error appeared */
  protected int exec(PreparedStatement ps)
  {
    try{
      MakumbaSystem.getMakumbaLogger("db.update.execution").fine(ps.toString());
      ps.execute();
      int n=ps.getUpdateCount();
      return n;
    }catch(SQLException e) 
      {
	if(isDuplicateException(e))
	  return -1;
	logException(e);
	throw new DBError(e);
      } 
  }
  
  /** return whether the exception indicates a duplicate. 
    may need to be specified differently for certain database engines */
  static boolean isDuplicateException(SQLException e){
    return e.getMessage().toLowerCase().indexOf("duplicate")!=-1;
  }

  static void logException(SQLException e){logException(e, null); }
  static void logException(SQLException e, DBConnection dbc)
  {
    String log="";
    if(dbc!=null)
      log=dbc.toString()+" ";
    for(SQLException se1=e; se1!=null; se1=se1.getNextException())
      {
	log+=se1.getMessage()+" SQL state: "+se1.getSQLState()+" error code :"+se1.getErrorCode()+"\n";
      }
	  
    MakumbaSystem.getMakumbaLogger("db.exception").warning(""+log);

  }

  /** write a date into an OQL query */
  public String OQLDate(java.util.Date d)
  {
    return "date" +"\""+new Timestamp(d.getTime())+"\"";
  }
  
  public Pointer getPointer(String type, int uid)
  {
    return new SQLPointer(type, getDbsv(), uid);
  }
}
