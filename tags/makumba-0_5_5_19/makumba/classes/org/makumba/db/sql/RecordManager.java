package org.makumba.db.sql;
import org.makumba.*;
import org.makumba.abstr.*;
import org.makumba.db.*;
import java.sql.*;
import java.util.*;
import java.lang.reflect.*;
import org.makumba.util.*;

/** This is the SQL RecordHandler, corresponding to a SQL table. When building its field handlers, it uses the rules in org.makumba.db.sql/redirectManager.properties file:
<pre>
intEnum=int
charEnum=char

ptrRelDB=ptrDB
ptrOneDB=ptrDB

setDB=no
setcharEnumDB=no
setinteEnumDB=no
setComplexDB=no
</pre>
... where the noManager builds no field handler.
* @see org.makumba.db.sql.FieldManager
 */
public class RecordManager extends Table
{
  protected String tbname;
  protected String handlerList;
  protected String indexDBField;
  protected String indexField;
  boolean alter;
  boolean exists_;
  Hashtable handlerExist= new Hashtable();
  Dictionary keyIndex;
    String preparedInsertString, preparedDeleteString, preparedDeleteFromString;

  public boolean exists(){ return exists_; }
  public boolean exists(String s){ return handlerExist.get(s)!=null; }
  
  public String getDBName()
  {
    return tbname;
  }

  protected org.makumba.db.sql.Database getSQLDatabase()
  { return (org.makumba.db.sql.Database)getDatabase(); }

  protected boolean usesHidden(){ return true; }

  protected String getTmpName() { return "tmp"+tbname; }

  /** the SQL table opening. might call create() or alter() */
  protected void open(Properties config) 
  {
    setTableAndFieldNames(config);
    if(!getRecordInfo().isTemporary())
      {
	  DBConnectionWrapper dbcw=(DBConnectionWrapper)getSQLDatabase().getDBConnection();
	  SQLDBConnection dbc=(SQLDBConnection)dbcw.getWrapped();
	try{
	  checkStructure(dbc, config);
	  initFields(dbc, config);
	  preparedInsertString=prepareInsert();
	  preparedDeleteString=prepareDelete();
	  preparedDeleteFromString=
	      "DELETE FROM "+getDBName()+" WHERE "+indexDBField+" >= ?"+
	      " AND "+indexDBField+" <= ?";
	}finally{ dbcw.close(); }
      }
    else keyIndex=getRecordInfo().getKeyIndex();
    
  }

  /** the SQL table opening. might call create() or alter() */
  protected void setTableAndFieldNames(Properties config) 
  {
    Object a[]= { this, config};

    tbname= config.getProperty(getRecordInfo().getName());
    /* find the shortest possible table name, according to what is defined in config
       a config with rule and table:
       best.minerva.student=bms
       best.minerva.student->fields
       
       will create table _bms__fields_
	   instead of _best_minerva_student__fields_ as it did before
	   */
    if(tbname==null)
	{
	  String key= Database.findConfig(config, getRecordInfo().getName());
	  String shortname=getRecordInfo().getName();
	  if(key!=null)
	    shortname= config.getProperty(key)+getRecordInfo().getName().substring(key.length());

	  tbname= getSQLDatabase().getTableName(shortname);
	}
    else
      if(tbname.indexOf('.')!=-1)
	tbname= getSQLDatabase().getTableName(tbname);

    try{
      callAll(getHandlerMethod("setDBName"), a);
    }catch(InvocationTargetException e)
      {
	throw new org.makumba.MakumbaError(e.getTargetException());
      }
  }
  
  boolean admin;

  public boolean canAdmin(){ return admin; }

  protected void checkStructure(SQLDBConnection dbc, Properties config) 
  {
    String s= Database.findConfig(config, "admin#"+getRecordInfo().getName());
    admin= (s!=null && config.getProperty(s).trim().equals("true"));

    s= Database.findConfig(config, "alter#"+getRecordInfo().getName());
    alter= (s!=null && config.getProperty(s).trim().equals("true"));

    MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(getDatabase().getConfiguration()+": checking "+getRecordInfo().getName()+" as "+tbname);

    try{
      Statement st= dbc.createStatement();

      CheckingStrategy cs= null;
      if(getSQLDatabase().catalog!=null)
	cs= new CatalogChecker(getSQLDatabase().catalog);
      else
      	throw new MakumbaError(getDatabase().getName()+": could not open catalog");
      
      if(cs.shouldCreate())
	{
	  create(st, tbname, alter);
	  exists_=alter;
	  config.put("makumba.wasCreated", "");
	  keyIndex=getRecordInfo().getKeyIndex();
	}
      else
	{
	  exists_=true;
	  alter(st, cs);
	}
      cs.close();
      
      st.close();
    }catch(SQLException sq)
      { sq.printStackTrace(); throw new org.makumba.DBError(sq); }
  }

  protected void initFields(SQLDBConnection dbc, Properties config) 
  {  
    Object a[]= { this, config, dbc};
    try{
      callAll(getHandlerMethod("onStartup"), a);
    }catch(InvocationTargetException e)
      {
	//		e.getTargetException().printStackTrace();
	if(alter)
	  throw new org.makumba.MakumbaError(e.getTargetException());
	else
	  MakumbaSystem.getMakumbaLogger("db.init.tablechecking").severe("unusable table: "+getRecordInfo().getName());
      } 
    
    StringBuffer sb= new StringBuffer();
    fieldList(sb, handlerOrder.elements());
    handlerList=sb.toString();
    indexField=getRecordInfo().getIndexName();
    indexDBField=((FieldManager)handlers.get(indexField)).getDBName();
  }
  
  interface CheckingStrategy
  {
    void close()throws SQLException;
    boolean hasMoreColumns()throws SQLException;
    String columnName()throws SQLException;
    int columnType()throws SQLException;
    String columnTypeName()throws SQLException;
    boolean checkColumn(FieldManager fm) throws SQLException;
    boolean shouldCreate() throws SQLException;
  }
  
  class CatalogChecker implements CheckingStrategy
  {
    Vector columns;
    Hashtable column;
    int i=0;

    CatalogChecker(Hashtable catalog) throws SQLException
    { 
      columns= (Vector)catalog.get(tbname);
      if(columns==null){
	columns= (Vector)catalog.get(tbname.toLowerCase());
	if(columns==null)
	    {
		columns= (Vector)catalog.get(tbname.toUpperCase());
		if(columns!=null)
		    tbname=tbname.toUpperCase();
	    }
	else
	    tbname=tbname.toLowerCase();
	
      }
    }
    
    public boolean shouldCreate(){ return columns==null; }

    public boolean hasMoreColumns()throws SQLException
    {
      if(i<columns.size())
	{
	  column= (Hashtable)columns.elementAt(i);
	  i++;
	  return true;
	}
      return false;
    }
    
    public String columnName() throws SQLException 
    { return (String)column.get("COLUMN_NAME"); }

    public int columnType()throws SQLException
    { return ((Integer)column.get("DATA_TYPE")).intValue(); }

    public int columnSize()throws SQLException
    { return ((Integer)column.get("COLUMN_SIZE")).intValue(); }

    public String columnTypeName()throws SQLException 
    { return (String)column.get("TYPE_NAME"); }

    public boolean checkColumn(FieldManager fm)throws SQLException
    {
      return fm.unmodified(columnType(), columnSize(), columns, i);
    }

    public void close(){ }
  }

    public int deleteFrom(DBConnection here, DBConnection source)
    {
	if(!exists())
	    return 0;
	if(!canAdmin())
	    throw new MakumbaError("no administration approval for "+getRecordInfo().getName());
    
	if(here instanceof DBConnectionWrapper)
	    here=((DBConnectionWrapper)here).getWrapped();
	PreparedStatement ps= (PreparedStatement)((SQLDBConnection)here)
	    .getPreparedStatement(preparedDeleteFromString);
	try{
	    ps.setInt(1, source.getHostDatabase().getMinPointerValue());
	    ps.setInt(2, source.getHostDatabase().getMaxPointerValue());
	}catch(SQLException e){ 
	  org.makumba.db.sql.Database.logException(e);
	  throw new DBError(e); 
	}
	int n= getSQLDatabase().exec(ps);
	
	((ptrIndexJavaManager)handlers.get(indexField)).reset();
	return n;
    }

  /** checks if an alteration is needed, and calls doAlter if so */
  protected void alter(Statement st, CheckingStrategy cs) throws SQLException
  {
    Vector present= new Vector();
    Vector add= new Vector();
    Vector modify= new Vector();
    Vector drop= new Vector();
    Object withness= new Object();
    
    while(cs.hasMoreColumns())
      {
	String dbfn= cs.columnName();
	boolean found=false;
	for(Enumeration e= handlerOrder.elements(); e.hasMoreElements();)
	  {
	    FieldManager fm= (FieldManager)e.nextElement();
	    if(fm.getDBName().toLowerCase().equals(dbfn.toLowerCase()))
	      {
		handlerExist.put(fm.getName(), withness);
		present.addElement(fm);
		if(!cs.checkColumn(fm) && !(alter && alter(st, fm, "MODIFY")))
		  {
		    MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning("should modify: "+
				       fm.getDataName()+" "+
				       fm.getDBName()+" "+
				       fm.getDBType()+" "+
				       cs.columnType()+" "+
				       cs.columnName());
		    modify.addElement(fm);
		  }
		found=true;
	      }
	  }
	if(found)
	  continue;
	drop.addElement(dbfn);
	MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning("extra field: "+
			   cs.columnName()+" "+
			   cs.columnType()+" "+
			   cs.columnTypeName());
      }
    
    Vector v= new Vector();
    keyIndex=new Hashtable();

    for(Enumeration e= handlerOrder.elements(); e.hasMoreElements();)
      {
	FieldManager fm=(FieldManager)e.nextElement();
	if(handlerExist.get(fm.getName())==null && !(alter && alter(st, fm, "ADD")))
	  {
	    add.addElement(fm);
	    MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning("should add "+
			       fm.getDataName()+" "+
			       fm.getDBName()+" "+
			       fm.getDBType());
	  }
	else
	  {
	    keyIndex.put(fm.getName(), new Integer(v.size()));
	    v.addElement(fm);
	  }
      }
    handlerOrder=v; 

    doAlter(st, drop, present, add, modify);
  }

  boolean alter(Statement st, FieldManager fm, String op) throws SQLException
  {
    String s="ALTER TABLE "+getDBName()+" "+op+" "+fm.inCreate(getSQLDatabase());
    MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(getSQLDatabase().getConfiguration()+": "+s);
    try{
      st.executeUpdate("DROP INDEX "+ getDBName()+"_"+fm.getDBName()+" ON " +getDBName());
    }catch(SQLException e) {}
    st.executeUpdate(s);
    handlerExist.put(fm.getName(), "");
    return true;
  }


  /** do the needed alterations after examining the data definition of the
    existing table. a temporary copy table is created, and the fields are copied from it to the re-CREATEd table. ALTER TABLE might be used instead, and drivers that don't support it will have their own RecordManager, extending this one.

    *@param drop the names of the db fields that should be dropped (they might not be)
    *@param present the abstract fields that exist in the DB, in DB order
    *@param add the abstract fields that are not present in the db and need to be added
    *@param modify the abstract fields that exist in the db but need to be modified to the new abstract definition
    */
  protected void doAlter(Statement st, Vector drop,
			 Vector present, Vector add, Vector modify)
       throws SQLException
  {
    //  MakumbaSystem.getLogger("debug.db").severe(drop);
    // MakumbaSystem.getLogger("debug.db").severe(present);
    // MakumbaSystem.getLogger("debug.db").severe(add);
    // MakumbaSystem.getLogger("debug.db").severe(modify);

    if(add.size()==0 && modify.size()== 0)
      return;

    if(present.size()==0)
      create(st, tbname, alter);
  }

  /*  the automatic alteration is really risky, so we skip it 
 int n= present.size();

    String fields=null;

    if(n>0)
      {
	StringBuffer sb= new StringBuffer();
	fieldList(sb, present.elements());

	fields= sb.toString();
	if(alter)
	  create(st, getTmpName(), true);

	if(alter)
	  cp(st, tbname, getTmpName(), fields, present);
      }

    create(st, tbname, alter);

    if(n>0)
      if(alter)
	cp(st, "tmp"+tbname, tbname, fields, present);
  }
  */

  /** a table copy function. tables should have this table's RecordInfo */
  protected void cp(Statement st, String tb1, String tb2, String fields,
		    Vector fms)
       throws SQLException
  {
	  DBConnectionWrapper dbcw=(DBConnectionWrapper)getSQLDatabase().getDBConnection();
	  SQLDBConnection dbc=(SQLDBConnection)dbcw.getWrapped();
    try{
      ResultSet rs= st.executeQuery("SELECT "+fields+" FROM "+tb1);
      Statement st1= dbc.createStatement();
      int cols=rs.getMetaData().getColumnCount()+1;
      
      while(rs.next())
	{
	  StringBuffer cmd= new StringBuffer().append("INSERT INTO ")
	    .append(tb2)
	    .append("(").append(fields).append(") VALUES (");
	  
	  String sep= null;
	  for(int i=1; i<cols; i++)
	    {
	      try{cmd.append(sep.toString());}
	      catch(NullPointerException npe){ sep=", "; }
	      
	      try{
		FieldManager fm= ((FieldManager)fms.elementAt(i-1));
		cmd.append(fm.writeConstant(fm.getValue(rs, i)));
	      }
	      catch(NullPointerException npe){ cmd.append("null"); }
	      catch(ClassCastException cce){ cmd.append("null"); }
	    }
	  cmd.append(")");
	  MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(cmd.toString());
	  st1.executeUpdate(cmd.toString());
	}
      st1.close();
      rs.close();
    }finally{ dbcw.close(); }
  }

  /** a table creation, from this table's RecordInfo */
  protected void create(Statement st, String tblname, boolean really)
       throws SQLException
  {
    Object [] dbArg= { getSQLDatabase() };
    if(really)
      try
      {
	st.executeUpdate("DROP TABLE "+tblname);
      }catch(SQLException e){ getSQLDatabase().checkState(e, "tableMissing"); }

    try{
      String command="CREATE TABLE "+tblname+"("+
	concatAll(getHandlerMethod("inCreate"), dbArg, ",")+
	")";
      if(!really)
	{
	  MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning("would be:\n"+command);
	  return;
	}
      MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(command);
      st.executeUpdate(command);
    }catch(InvocationTargetException e)
      { throw new org.makumba.DBError(e.getTargetException()); }
  }

  /** list the given fields in a command field1, field2 ... */
  protected static void fieldList(StringBuffer command, Enumeration e)
  {
    String comma="";

    while(e.hasMoreElements())
      {
	command.append(comma);
	comma=", ";
	command.append(((FieldManager)e.nextElement()).getDBName());
      }
  }

  boolean checkDBName(String s)
  {
    for(Enumeration e= handlerOrder.elements(); e.hasMoreElements();)
      {
	FieldManager fm= (FieldManager)e.nextElement();
	if(fm.getDBName()!=null && fm.getDBName().toLowerCase().equals(s.toLowerCase()))
	  return true;
      }
    return false;
  }
  //---------------------------------------

  protected String prepareInsert()
  {
    Object a[]= new Object[0];
    try{
      return "INSERT INTO "+tbname+" ("+handlerList+") VALUES ("+concatAll(getHandlerMethod("inPreparedInsert"), a, ",")+")";
    }catch(InvocationTargetException e){throw new org.makumba.MakumbaError(e.getTargetException());}
  }

  public Pointer insertRecordImpl(DBConnection dbc, Dictionary d) 
  {
    boolean wasIndex= d.get(indexField)!=null;
    boolean wasCreate= d.get("TS_create")!=null;
    boolean wasModify= d.get("TS_create")!=null;

    //    while(true)
      try
      {
	if(dbc instanceof DBConnectionWrapper)
	    dbc=((DBConnectionWrapper)dbc).getWrapped();

	PreparedStatement ps= (PreparedStatement)((SQLDBConnection)dbc)
	  .getPreparedStatement(preparedInsertString);

	int n=0;
	for(Enumeration e= handlerOrder.elements(); e.hasMoreElements(); )
	  {
	    n++;
	    FieldManager fm= (FieldManager)e.nextElement();
	    try{
	      fm.setInsertArgument(ps, n, d);
	    }catch(Throwable ex){ 
	      throw new org.makumba.DBError(ex, 
					    "insert into "+ getRecordInfo().getName()+
					    " at field "+fm.getName()+
					    " could not assign value \""+d.get(fm.getName())+
					    "\" of type "+d.get(fm.getName())!=null?
					    d.get(fm.getName()).getClass().getName():""); 
	    }
	  }
	getSQLDatabase().exec(ps);
	Pointer ret= (Pointer)d.get(indexField);
	if(!wasIndex)
	  d.remove(indexField);
	if(!wasCreate)
	  d.remove("TS_create");
	if(!wasModify)
	  d.remove("TS_modify");
	return ret;
      }/*catch(ReconnectedException re)
	{
	  prepareStatements();
	  continue;
	}*/
      //      catch(SQLException e) { throw new org.makumba.DBError (e); }
      catch(Throwable t) { throw new org.makumba.DBError (t); }
  }

  protected String prepareDelete()
  {
    return "DELETE FROM "+tbname+" WHERE "+((FieldManager)handlers.get(indexField)).inPreparedUpdate();
  }


  public void deleteRecord(DBConnection dbc, Pointer uid) 
  {
    if(dbc instanceof DBConnectionWrapper)
      dbc=((DBConnectionWrapper)dbc).getWrapped();
    
    PreparedStatement ps= (PreparedStatement)((SQLDBConnection)dbc)
      .getPreparedStatement(preparedDeleteString);
    
    //    while(true)
      try{
      ((FieldManager)handlers.get(indexField)).setUpdateArgument(ps, 1, uid);
      getSQLDatabase().exec(ps);
      //break;
      }//catch(ReconnectedException e) { continue; }
    catch(SQLException f) { 
      org.makumba.db.sql.Database.logException(f);
      throw new DBError(f); 
    }
  }

  public void updateRecord(DBConnection dbc, Pointer uid, Dictionary d) 
  {
    if(dbc instanceof DBConnectionWrapper)
      dbc=((DBConnectionWrapper)dbc).getWrapped();
    d.remove(indexField);
    d.remove("TS_create");

    //d.put("TS_modify", "");
    d.put("TS_modify", new java.util.Date());

    StringBuffer command=new StringBuffer("UPDATE ").
      append(tbname).
      append(" SET ");

    String s="";
    for(Enumeration e= d.keys(); e.hasMoreElements(); )
      {
	if(s.length()>0)
	  command.append(",");
	Object fld= e.nextElement();
	FieldManager fm= (FieldManager)handlers.get(fld);
	if(fm==null)
	  throw new org.makumba.DBError(new Exception("no such field "+ fld+ " in "+getRecordInfo().getName()));
	command.append(s=fm.inPreparedUpdate());
      }

    command.append(" WHERE "+((FieldManager)handlers.get(indexField))
		   .inPreparedUpdate());
    
    //    while(true)
    try
      {
	PreparedStatement st= ((SQLDBConnection)dbc).getPreparedStatement(command.toString());
	
	int n=1;
	for(Enumeration e= d.keys(); e.hasMoreElements(); n++ )
	  ((FieldManager)handlers.get(e.nextElement())).setUpdateArgument(st, n, d);
	
	((FieldManager)handlers.get(indexField)).setUpdateArgument(st, n, uid);
	
	getSQLDatabase().exec(st);
	return;
      }//catch(ReconnectedException re)	{ continue; }
      catch(SQLException se)
	{ throw new org.makumba.DBError(se); }
  }

  protected void fillResult(ResultSet rs, Dictionary p)
       throws java.sql.SQLException
  {
    int n= handlerOrder.size();
    for(int i=0; i<n; )
      {
	((FieldManager)handlerOrder.elementAt(i)).setValue(p, rs, ++i);
      }
  }

  protected void fillResult(ResultSet rs, Object[]data)
       throws java.sql.SQLException
  {
    int n= handlerOrder.size();
    for(int i=0; i<n; i++)
      {
	try{
	  data[i]=((FieldManager)handlerOrder.elementAt(i)).getValue(rs, i+1);
	}catch(ArrayIndexOutOfBoundsException e)
	  {
	    org.makumba.MakumbaSystem.getMakumbaLogger("db.query.execution").log
	      (java.util.logging.Level.SEVERE, ""+i+" "+getRecordInfo().getName()+" "+getRecordInfo().getKeyIndex()+" "+handlerOrder, e);
	    throw e;
	  }
      }
  }

  public Object getValue(ResultSet rs, String field, int i) 
  {
    try{
      return ((FieldManager)getFieldHandler(field)).getValue(rs, i);
    }catch(SQLException e){ throw new org.makumba.DBError(e); }
  }
}
