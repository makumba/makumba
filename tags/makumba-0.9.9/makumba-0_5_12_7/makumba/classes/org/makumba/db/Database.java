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

package org.makumba.db;
import java.util.*;
import java.lang.reflect.*;
import org.makumba.util.*;
import org.makumba.abstr.*;
import org.makumba.*;


/** 
  ptrOne...
  Object[] args={base};
	Vector v= prepareQuery("SELECT p."+field+" FROM "+base.getType()+" p WHERE p=$1").execute(args);
	Pointer p=(Pointer)((Dictionary)v.elementAt(0)).get("col1");
	if(p!=null)
	  delete(p.getType(), p);
	Dictionary d= new Hashtable();
	d.put(field, p=insert(base.getType()+"->"+field, data));
	update(base.getType(), base, d);
	return p;
	*/

/** a generic database, that maps RecordInfos to tables */
public abstract class Database 
{
  public String getName(){ return configName; }

  NamedResources queries;
  NamedResources updates;

  int nconn=0;  
  int initConnections=1;

  protected ResourcePool connections= new ResourcePool(){
      public Object create()
	{
	    nconn++;
	    config.put("jdbc_connections", ""+nconn);
	    return makeDBConnection();
	}
      
      // preventing stale connections
      public void renew(Object o){
	((DBConnection)o).commit();
      }

      public void close(Object o){
	((DBConnection)o).close();
      }
    };
  
  public void initConnections()
  {
    try{
      // resourcePool should have limits...
      connections.init(initConnections);
    }catch(Exception e){ throw new DBError(e); }
  }
  
  protected void closeConnections() 
  { 
    connections.close();
  } 

  public void close() 
  {
    MakumbaSystem.getMakumbaLogger("db.init").info("closing  "+getConfiguration()+"\n\tat "+org.makumba.view.dateFormatter.debugTime.format(new java.util.Date()));
    tables.close();
    queries.close();
    updates.close();
    closeConnections();
  }

  public DBConnection getDBConnection() 
  {
    try{
	return new DBConnectionWrapper((DBConnection)connections.get());
    }catch(Exception e){ throw new DBError(e); }
  }

  protected abstract DBConnection makeDBConnection();

  //---------------------------------------------------

  int dbsv;
  Properties config=null;
  Class tableclass;
  String configName;
  Hashtable queryCache= new Hashtable();

  /** return the unique index of this database */
  public int getDbsv(){ return dbsv; }

  public abstract Pointer getPointer(String type, int uid);

  static Class[] theProp={ java.util.Properties.class };

  public String getConfiguration(){ return configName; }
  public String getConfiguration(String v){ return config.getProperty(v); }

    static int dbs= NamedResources.makeStaticCache("Databases open",
						 new NamedResourceFactory()
   {
     protected Object makeResource(Object nm) 
       {
	 Properties p= new Properties();
	 String name= (String)nm;

	 try{
	   p.load(org.makumba.util.ClassResource.get(name+".properties").openStream());
	 }catch(Exception e)
	   { throw new org.makumba.ConfigFileError(name+".properties");}

	 try{
	   name= name.substring(name.lastIndexOf('/')+1);
	   int n= name.indexOf('_');
	   String host;
	   p.put("#host", host=name.substring(0, n));
	   int m= name.indexOf('_', n+1);
	   if(Character.isDigit(name.charAt(n+1)))
	     {
	       p.put("#host", host+":"+name.substring(n+1, m));
	       n=m;
	       m= name.indexOf('_', n+1);
	     }
	     
	   p.put("#sqlEngine", name.substring(n+1, m));
	   p.put("#database", name.substring(m+1));

	   String dbclass= (String)p.get("dbclass");
	   if(dbclass==null)
	     {
	       dbclass=org.makumba.db.sql.Database.getEngineProperty(p.getProperty("#sqlEngine")+".dbclass");
	       if(dbclass==null)
		 dbclass="org.makumba.db.sql.Database";
	     }
	   p.put("db.name", (String)name);
	   Object pr[]= {p};
	   
	   try{
	     Database d= (Database)Class.forName(dbclass)
	       .getConstructor(theProp)
	       .newInstance(pr);
	     d.configName= (String)name;
	     d.tables= new NamedResources("Database tables for "+name,
					  d.tableFactory);
	     return d;
	   }
	   catch(InvocationTargetException ite)
	     {throw new org.makumba.MakumbaError(ite.getTargetException()); }
	 }
	 catch(Exception e){ throw new org.makumba.MakumbaError(e); }
       }
   }
     );

  /** finds the database name of the server according to the host name and current directory.
   * If none is specified, a default is used, if available */
  public static String findDatabaseName(Properties p)
  {
    for(Enumeration e= p.keys(); e.hasMoreElements();)
      {
	String s= (String)e.nextElement();
	int i= s.indexOf('#');
	if(i==-1)
	  continue;
	try{
	  if(java.net.InetAddress.getByName(s.substring(0, i)).equals(java.net.InetAddress.getLocalHost()) &&
	   s.substring(i+1).equals(System.getProperty("user.dir")))
	    return p.getProperty(s);
	}catch(java.net.UnknownHostException uhe){}
      }

    for(Enumeration e= p.keys(); e.hasMoreElements();)
      {
	String s= (String)e.nextElement();
	int i= s.indexOf('#');
	if(i==-1)
	  continue;
	try{
	  if(java.net.InetAddress.getByName(s.substring(0, i)).equals(java.net.InetAddress.getLocalHost()) &&
	     s.substring(i+1).equals("default"))
	    return p.getProperty(s);
	}catch(java.net.UnknownHostException uhe){}
      }

    return p.getProperty("default");
  }

  public static Database findDatabase(Properties p) 
  {
    return getDatabase(findDatabaseName(p));
  }

  public static Database findDatabase(String s) 
  {
    return getDatabase(findDatabaseName(s));
  }

  static int dbsel= NamedResources.makeStaticCache
  ("Database selection files",
   new NamedResourceFactory()
   {
     protected Object makeResource(Object nm) 
       {
	 Properties p= new Properties();
	 try{
	   java.io.InputStream input=org.makumba.util.ClassResource.get((String)nm).openStream();
	   p.load(input);
	   input.close();
	 }catch(Exception e){ throw new org.makumba.ConfigFileError((String)nm); }
	 return p;
       }
   });

  public static String findDatabaseName(String s) 
  {
    try{
      return findDatabaseName((Properties)NamedResources.getStaticCache(dbsel)
			      .getResource(s));
        }catch(RuntimeWrappedException e)
	  {
	    if(e.getReason() instanceof org.makumba.MakumbaError)
	      throw (org.makumba.MakumbaError)e.getReason();
	    throw e;
	  }
  }

  /** Initializes a Database object from the given properties.
   * The following properties are expected: <dl>
   * <dt> dbclass<dd> the class of the concrete database, e.g.  org.makumba.db.sql.SQLDatabase. An object of this class will be instantiated, and the properties will be passed on to it for further initialization
   * <dt> tableclass<dd> the class of the concrete database table, e.g. org.makumba.db.sql.odbc.RecordManager. If this is not present, the class returned by the method getTableClass() is used
   * <dt> dbsv<dd> the unique id of the database. Can be any integer 0-255
   * </dl>
   */
  public static Database getDatabase(String name) 
  {
    try{
      return (Database)NamedResources.getStaticCache(dbs).getResource(name);
    }catch(RuntimeWrappedException e)
      {
	if(e.getReason() instanceof org.makumba.MakumbaError)
	  throw (org.makumba.MakumbaError)e.getReason();
	throw e;
      }
  }
  
  protected Database(Properties config) 
  {
    this.config=config;
    this.configName=config.getProperty("db.name");
    String s=config.getProperty("initConnections");
    if(s!=null)
      initConnections= Integer.parseInt(s.trim());

    config.put("jdbc_connections", "0");
    try{
      dbsv=new Integer((String)config.get("dbsv")).intValue();
      
      tableclass= getTableClassConfigured();
      
      config.put("alter#org.makumba.db.Catalog", "true");
      config.put("alter#org.makumba.db.Lock", "true");
    }catch(Exception e){ throw new org.makumba.MakumbaError(e); }

    
    queries= new SoftNamedResources
      ("Database "+getName()+" query objects",
       new NamedResourceFactory(){
	public Object makeResource(Object name) 
	  {
	    return prepareQueryImpl((String)name);
	  }
      });

    updates= new SoftNamedResources
      ("Database "+getName()+" update objects",
       new NamedResourceFactory(){
	public Object makeResource(Object o) 
	  {
	    Object[] multi=(Object[])o;
	    
	    return prepareUpdateImpl((String)multi[0], (String)multi[1], (String)multi[2]);
	  }
	protected Object getHashObject(Object name)
	  {
	    Object[] multi=(Object[])name;
	    return ""+multi[0]+"####"+multi[1]+"######"+multi[2];
	  }
      });	    
  }

  /** this method should be redefined by database classes that have a default table class. this returns null */
  protected Class getTableClassConfigured(){ return null; }
  
  /** write a date constant in OQL */
  public abstract String OQLDate(java.util.Date d);
  
  //--------------------------- bunch of utility methods -------------------------------
  // these methods are added 20001030 from the experience we had with this incipient form of
  // Database API. Cristi wrote them initially for Minerva, Toto used to have them static in
  // org.eu.best.Metadata, but their place really is here. The utility methods getKeyFromPointer
  // and getPointerFromKey in that class should probably go to the presentation level
  // -----------------------------------------------------------------------------------
  /** this method will return a table by macumba name. Subtables (->) are returned as well (for now)
   * maybe we could write Table.getSubtable(ptr, field) to operate with subsets? how about 1:1 pointers? need to think of that API further*/
  public Table getTable(String name) 
  {
    // OLDSUPPORT >>
    if(name.indexOf('/')!=-1)
      {
	name=name.replace('/', '.');
	if(name.charAt(0)=='.')
	  name=name.substring(1);
      }
    // <<
    
    int n= name.indexOf("->");
    if (n==-1)
      return getTable(org.makumba.abstr.RecordInfo.getRecordInfo(name));
    // the abstract level doesn't return recordInfo for subtables (->) since it is supposed they are managed via their parent tables. the current DB api doesn't provide that.
    
    Table t= getTable(name.substring(0, n));
    while(true)
      {
	name= name.substring(n+2);
	n= name.indexOf("->");
	if(n==-1)
	  break;
	t= t.getRelatedTable(name.substring(0, n));
      }
    t=t.getRelatedTable(name);
    return t;
  }
  
  
  /** get the table from this database associated with the given RecordInfo */
  public Table getTable(RecordInfo ri)
  {
    return (Table)tables.getResource(ri);
  }
  
  /** finds the longest configuration string that matches the pattern and returns the associated property*/
  public static String findConfig(Properties cnf, String pattern)
  {
    String ret= null;
    for(Enumeration e= cnf.keys();e.hasMoreElements();)
      {
	String key= (String)e.nextElement();
	if(pattern.startsWith(key) && (ret==null || ret.length()<key.length()))
	  ret=key;
      }
    return ret;
  }

  public abstract Query prepareQueryImpl(String query);
  public abstract Update prepareUpdateImpl(String type, String set, String where);

  public abstract int getMinPointerValue();
  public abstract int getMaxPointerValue();

  public void deleteFrom(DBConnection c, String table, DBConnection sourceDB)
  {
    DataDefinition dd= MakumbaSystem.getDataDefinition(table);
    MakumbaSystem.getMakumbaLogger("db.admin.delete").info("deleted "+getTable(table).deleteFrom(c, sourceDB)+" old objects from "+table);

    for(Enumeration e= dd.getFieldNames().elements(); e.hasMoreElements(); )
      {
	FieldDefinition fi= dd.getFieldDefinition((String)e.nextElement());
	if(fi.getType().startsWith("set") || fi.getType().equals("ptrOne"))
	  deleteFrom(c, fi.getSubtype().getName(), sourceDB);
      }
  }

  public void deleteFrom(String sourceDB, String table)
  {
    String[]tables = {table};
    deleteFrom(sourceDB, tables);
  }

  public void deleteFrom(String source, String[] tables)
  {
    DBConnection c= getDBConnection();    
    DBConnection sourceDBc=getDatabase(source).getDBConnection();
    try{
      deleteFrom(c, tables, sourceDBc);
    }finally{ c.close(); sourceDBc.close(); }
  }

  public void deleteFrom(DBConnection c, String[] tables, DBConnection sourceDB)
  {
    for (int i=0;i<tables.length;i++) 
      deleteFrom(c, tables[i], sourceDB);
  }


  public void copyFrom(String sourceDB, String table)
  {
    String[]tables = {table};
    copyFrom(sourceDB, tables);
  }

  public void copyFrom(String source, String[] tables)
  {
    DBConnection c= getDBConnection();    
    DBConnection sourceDBc=getDatabase(source).getDBConnection();
    try{
      copyFrom(c, tables, sourceDBc);
    }finally{ c.close(); sourceDBc.close(); }
  }

  public void copyFrom(DBConnection c, String[] tables, DBConnection sourceDB)
  {
    deleteFrom(c, tables, sourceDB);
    for (int i=0;i<tables.length;i++) 
      copyFrom(c, tables[i], sourceDB);
  }

  public void copyFrom(DBConnection c, String table, DBConnection sourceDB)
  {
    DataDefinition dd= MakumbaSystem.getDataDefinition(table);
    getTable(table).copyFrom(c, c.getHostDatabase().getTable(table), sourceDB);

    for(Enumeration e= dd.getFieldNames().elements(); e.hasMoreElements(); )
      {
	FieldDefinition fi= dd.getFieldDefinition((String)e.nextElement());
	if(fi.getType().startsWith("set") || fi.getType().equals("ptrOne"))
	  copyFrom(c, fi.getSubtype().getName(),sourceDB);
      }
  }

  public void copyFrom(String source)
  {
    DBConnection c= getDBConnection();    
    DBConnection sourceDB=findDatabase(source).getDBConnection();
    try{
      Vector v=sourceDB.executeQuery("SELECT c.name AS name FROM org.makumba.db.Catalog c", null);
      String[] tables= new String[v.size()];
      
      for (int i=0; i<tables.length; i++)
	{
	  String nm=(String)((Dictionary)v.elementAt(i)).get("name");
	  MakumbaSystem.getMakumbaLogger("db.admin.copy").info(nm);
	  tables[i]=nm;
	}
      copyFrom(c, tables, sourceDB);
    }
    finally{c.close(); sourceDB.close(); }
  }
  
  public void openTables(String [] tables) 
  {
    for (int i=0;i<tables.length;i++) {
      openTable(tables[i]);
    }
  }
  
  public void openTable(String table) 
  {
    getTable(table);
    DataDefinition dd= MakumbaSystem.getDataDefinition(table);

    for(Enumeration e= dd.getFieldNames().elements(); e.hasMoreElements(); )
      {
	FieldDefinition fi= dd.getFieldDefinition((String)e.nextElement());
	if(fi.getType().startsWith("set") || fi.getType().equals("ptrOne"))
	  openTable(fi.getSubtype().getName());
      }
  }

  protected void finalize() throws Throwable
  {
    close();
  }

    NamedResources tables;

  synchronized void addTable(String s) 
  {
    if(s.equals("org.makumba.db.Catalog"))
      return;
    DBConnection c= getDBConnection();
    try{
	Enumeration e= c.executeQuery("SELECT c FROM org.makumba.db.Catalog c WHERE c.name=$1", s).elements();
	if(!e.hasMoreElements())
	    {
		Dictionary h= new Hashtable(3); h.put("name", s);
		getTable("org.makumba.db.Catalog").insertRecord(c, h);
	    }
    }finally{ c.close(); }
  }
  
  public Table makePseudoTable(RecordInfo ri)
  {
    Table ret= null;
    try{
      ret=(Table)tableclass.newInstance();
    }catch(Throwable t){throw new MakumbaError(t); }
    configureTable(ret, ri);
    return ret;
  }

  void configureTable(Table tbl, RecordInfo ri){
    tbl.db= Database.this;
    tbl.setRecordInfo(ri);
    tbl.open(config);
  }

    NamedResourceFactory tableFactory= new NamedResourceFactory()
    {
	public Object getHashObject(Object name)
	{
	    return ((RecordInfo)name).getName();
	}
	
	public Object makeResource(Object name, Object hashName)
	    throws Throwable
	{
	    return tableclass.newInstance();
	}
	
	public void configureResource(Object name, Object hashName, Object resource)
	{
	  configureTable((Table)resource, (RecordInfo)name);
	  addTable(((Table)resource).getRecordInfo().getName());
	}
    };

}


