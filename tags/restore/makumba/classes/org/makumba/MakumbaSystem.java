package org.makumba;
import org.makumba.util.*;

/** The makumba runtime system. Provides starter methods to obtain {@link Database} and {@link DataDefinition} objects */
public class MakumbaSystem 
{
  /** The date at which makumba is loaded */
  static public final java.util.Date loadingTime=new java.util.Date();

  /** Get information about the makumba cache sizes.
   * @return a {@link java.util.Map} with cache categories as keys and cache sizes as values
   * @since makumba-0.5.5.13
   */
    public static java.util.Map getCacheInfo()
  {
      return org.makumba.util.NamedResources.getCacheInfo();
  }

  /** The name of the default database according to the lookup file "MakumbaDatabase.properties"
   * @since makumba-0.5.4
   */
  public static String getDefaultDatabaseName()
  {
    return org.makumba.db.Database.findDatabaseName("MakumbaDatabase.properties"); 
  }

  /** The name of the default database according to the database lookup file indicated
   * @param dbLookupFile the name of the database lookup file, including ".properties", or any other extension. The file should be in CLASSPATH.
   * @since makumba-0.5.4
   */
  public static String getDefaultDatabaseName(String dbLookupFile)
  {
    return org.makumba.db.Database.findDatabaseName(dbLookupFile); 
  }

  /** Get a connection to the database described in the .properties file with the given name. 
   * @param name the database name, the same as the db description file but without ".properties". for example "localhost_mysql_databasename". The file should be in CLASSPATH.
   * The operations carried out during database initialization are logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.init"</code></b> and <b><code>"db.init.tablechecking"</code></b> loggers, with {@link java.util.logging.Level#INFO} (connections, checkings), {@link java.util.logging.Level#SEVERE} (fatal errors) and with {@link java.util.logging.Level#WARNING} logging levels.
    @since makumba-0.5.4
   */
  public static Database getConnectionTo(String name)
  {
    return org.makumba.db.Database.getDatabase(name).getDBConnection(); 
  }

  /** Get a connection to the default database found according to MakumbaDatabase.properties 
    @since makumba-0.5
    @deprecated This method name is misleading since it returns a connection, not a database. 
    Use getConnectionTo(getDefaultDatabaseName()) instead
   */
  public static Database findDatabase()
  { return getConnectionTo(getDefaultDatabaseName()); }
  
  /** Find the Database according to the given lookup file from the CLASSPATH. The file name will include the .properties extension 
    @since makumba-0.5
    @deprecated This method name is misleading since it returns a connection, not a database. 
    Use getConnectionTo(getDefaultDatabaseName(dbLookupFile)) instead
   */
  public static Database findDatabase(String dbLookupFile) 
  { return getConnectionTo(getDefaultDatabaseName(dbLookupFile)); }
  
  /** Get the Database defined by the given connection file from the CLASSPATH. The file name should not include the .properties extension 
    @since makumba-0.5
    @deprecated This method name is misleading since it returns a connection, not a database. 
    Use getConnectionTo(connectionFile) instead
   */
  public static Database getDatabase(String connectionFile) 
  { return getConnectionTo(connectionFile); }


    /** Access the properties of a database. 
	Besides the properties defined in the database connection file, the following are available
	<table border =1>
	<tr><td><code>sql_engine.name</code>
	<td>name of the SQL engine used
	<tr><td><code>sql_engine.version</code>
	<td>version of the SQL engine used
	<tr><td><code>sql.jdbc_driver.name</code>
	<td>name of the JDBC driver used
	<tr><td><code>jdbc_driver.name</code>
	<td>name of the JDBC driver used
	<tr><td><code>jdbc_driver.version</code>
	<td>version of the JDBC driver used
	<tr><td><code>jdbc_url</code>
	<td>JDBC url connected to
	<tr><td><code>jdbc_connections</code>
	<td>number of jdbc connections open
	</table>
	* @since makumba-0.5.5.7
    */
    public static String getDatabaseProperty(String name, String propName)
    {
	return org.makumba.db.Database.getDatabase(name).getConfiguration(propName);
    }

  /** Get the DataDefinition defined by the given type. The type a.b.C will generate a lookup for the file CLASSPATH/a/b/C.mdd and then for CLASSPATH/dataDefinitions/a/b/C.mdd */
  public static DataDefinition getDataDefinition(String typeName) 
  { return org.makumba.abstr.RecordInfo.getRecordInfo(typeName); }

  /** Get the DataDefinition of the records returned by the given OQL query 
   *@deprecated use {@link #getOQLAnalyzer} for better OQL functionality
   */
  public static DataDefinition getResultDataDefinition(String OQL) 
  { return getOQLAnalyzer(OQL).getProjectionType(); }

  /** Deletes the records of certain types that originate from a certain database. Useful for failed imports or copies. The database configuration must have admin# confirmations that match each of the indicated types. Use _delete(d, d, ...) for databases that need re-import of data of certain types. 
   * Deletion is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.delete"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
   */
  public static void _delete(String whereDB, String provenienceDB, String[] typeNames)
  {
    org.makumba.db.Database.getDatabase(whereDB).deleteFrom(provenienceDB, typeNames);
  }

  /** Copies records of certain types (and their subtypes) from a database to another. The destination database must have admin# confirmations that match each of the indicated types 
   * Copying is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.copy"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
   */
  public static void _copy(String sourceDB, String destinationDB, String[] typeNames)
  {
    org.makumba.db.Database.getDatabase(destinationDB).copyFrom(sourceDB, typeNames);
  }

  /** Returns a Makumba version (via CVS keyword substitution, eg: $Name$) */
  public static String getVersion() {
    return "$Name$".substring(7,"$Name$".length()-2); 
  }

  static String loggingRoot="org.makumba";

  /** Get a logger for logging during makumba operations. See {@link java.util.logging.Logger}, {@link #setLoggingRoot(java.lang.String)}. This method is mostly used by makumba code. From application code, use {@link #getLogger(java.lang.String)} or {@link #getLogger() }.   
    <p>The table below describes when makumba logging occurs and at what logging {@link java.util.logging.Level} (note also that {@link java.util.logging.Level#SEVERE} and {@link java.util.logging.Level#WARNING} logging is done in makumba when fatal errors or warnings occur). 
    <p>The logging levels below tell the programmer how to configure logging so that some parts of the makumba logging become visible. For example, details on database update performance are not normally visible on the log, since they are at {@link java.util.logging.Level#FINE} logging level, and {@link java.util.logging.Level#INFO} is default. To view them, one needs to add the following line in <code>logging.properties</code>(see {@link java.util.logging.LogManager} for explanations of logger configuration)<br>
    <blockquote>
<code>org.makumba.update.performance.level=FINE</code><br>
    </blockquote>
The programmer could just as well decide that all makumba logging at or over the level FINE should be visible, except for the one on taglib performance:<br>
    <blockquote>
<code>org.makumba.level=FINE</code><br>
<code>org.makumba.taglib.performance.level=INFO</code>
    </blockquote>
<p>
    <table border=1>

    <tr><td>Operation<td>Log name<td>Logging details<td>Logging level

    <tr><td>application operations
    <td>loggingRoot + <code>apps</code>+ the parameter to {@link #getLogger(java.lang.String)}
    <td>logging level used by applications that call {@link #getLogger(java.lang.String)} and {@link #getLogger()}
    <td> any, as required by the application

    <tr><td>database opening
    <td>loggingRoot + <code>db.init</code>, <code>db.init.tablechecking</code>
    <td>see {@link #getConnectionTo(java.lang.String)}
    <td>{@link java.util.logging.Level#INFO}

    <tr><td>connection pooling
    <td>loggingRoot + <code>util.pool
    <td>infomrmation on the size of the database connection pool
    <td>{@link java.util.logging.Level#FINE}

    <tr><td>database administration
    <td>loggingRoot + <code>db.admin.copy</code>, <code>db.admin.delete
    <td>see {@link #_copy}, {@link #_delete}
    <td>{@link java.util.logging.Level#INFO}

    <tr><td>database querying
    <td>loggingRoot + <code>db.query.compilation</code>,
    <code>db.query.execution</code>, <code>db.query.performance
    <td>see {@link org.makumba.Database#executeQuery(java.lang.String, java.lang.Object)}, 
    {@link org.makumba.Database#read(org.makumba.Pointer, java.lang.Object)}
    <td>{@link java.util.logging.Level#FINE}

    <tr><td>database query grouping
    <td>loggingRoot + <code>db.query.grouping
    <td>grouping used during simulated left joins, e.g. in executing embedded &lt;mak:list&gt; JSP tags
    <td>{@link java.util.logging.Level#FINE}

    <tr><td>taglib performace
    <td>loggingRoot + <code>taglib.performance
    <td>performance info needed to fine-tune &lt;mak:list&gt; and other makumba JSP tags
    <td>{@link java.util.logging.Level#FINE}

    <tr><td>database updating
    <td>loggingRoot + <code>db.update.execution</code>, <code>db.update.performance
    <td>see {@link org.makumba.Database}, all insert, delete, and update operations
    <td>{@link java.util.logging.Level#FINE}

    <tr><td>business logic discovery
    <td>loggingRoot + <code>controller
    <td>The steps taken when finding business logic classes
    <td>{@link java.util.logging.Level#INFO}

    <tr><td>errors during business logic calls
    <td>loggingRoot + <code>controller.logicError
    <td>Runtime (probably involuntary) errors produced by the business logic programmer
    <td>{@link java.util.logging.Level#INFO}

    <tr><td>swaping of large content to disk
    <td>loggingRoot + <code>util.longContent
    <td>tells when large content in {@link org.makumba.Text} or large content produced by a mak:list tag are swapped to disk
    <td>{@link java.util.logging.Level#FINE}

    </table>
   * @since makumba-0.5.5.3
   */
  public static java.util.logging.Logger getMakumbaLogger(String suffix)
  {
    return java.util.logging.Logger.getLogger(loggingRoot+"."+suffix);
  }

  /** Return a logger for use by an application. 
   * @return the logging root + "apps" + the indicated suffix
   * @see org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)
   * @since makumba-0.5.5.3
   */
  public static java.util.logging.Logger getLogger(String suffix)
  {
    return getMakumbaLogger("apps."+suffix);
  }

  /** Return a logger for use by an application. 
   * @return the logging root plus "apps" 
   * @see org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)
   * @since makumba-0.5.5.3
   */
  public static java.util.logging.Logger getLogger()
  {
    return getMakumbaLogger("apps");
  }

  /** Change the makumba logging root in this classloader. The default logging root is <b><code>"org.makumba"</code></b> so loggers would look like <b><code>org.makumba.db.init.tableckecking</code></b>
   * @since makumba-0.5.5.3
   */
  public static void setLoggingRoot(String root){ loggingRoot=root; }

    /** Returns the timezone in which makumba should read/display dates from/to users, configurable by the system variable makumba.displayTimeZone */
  public static java.util.TimeZone getTimeZone() 
    { 
	String s= System.getProperty("makumba.displayTimeZone");
	if(s!=null)
	    return java.util.TimeZone.getTimeZone(s);
	return java.util.TimeZone.getDefault();

    }

    /** Returns the makumba system locale */
  // later: should be configurable by config files
  public static java.util.Locale getLocale() { return java.util.Locale.UK;}

    /** Get the OQL analyzer for the indicated query */
  static public OQLAnalyzer getOQLAnalyzer(String oqlQuery) 
  {
    try{
      return (OQLAnalyzer)NamedResources.getStaticCache(parsedQueries).getResource(oqlQuery);
    }catch(RuntimeWrappedException e){
      if(e.getReason() instanceof antlr.RecognitionException)
	{
	  Exception f=(antlr.RecognitionException)e.getReason();
	  String s=f.getMessage();
	  if(s.startsWith("line"))
	    s=s.substring(s.indexOf(':')+1);
	  throw new OQLParseError(s+"\r\nin query:\r\n"+oqlQuery);
	}
      throw e;
    }
  }

  static int parsedQueries= NamedResources.makeStaticCache
      ("OQL parsed queries", 
       new NamedResourceFactory(){
    protected Object makeResource(Object nm, Object hashName) 
      throws Exception
      {
	return parseQueryFundamental((String)nm);
      }
  });

  static OQLAnalyzer parseQueryFundamental(String oqlQuery) throws antlr.RecognitionException
  {
    java.util.Date d= new java.util.Date();
    org.makumba.db.sql.oql.OQLLexer lexer =  
      new org.makumba.db.sql.oql.OQLLexer(new java.io.StringBufferInputStream(oqlQuery));
    org.makumba.db.sql.oql.OQLParser parser = 
      new org.makumba.db.sql.oql.OQLParser(lexer);
    // Parse the input expression
    org.makumba.db.sql.oql.QueryAST t= null;
    try{

      parser.setASTNodeClass("org.makumba.db.sql.oql.OQLAST");
      parser.queryProgram();
      t = (org.makumba.db.sql.oql.QueryAST)parser.getAST();
      t.setOQL(oqlQuery);
      // Print the resulting tree out in LISP notation
      //      MakumbaSystem.getLogger("debug.db").severe(t.toStringTree());
      
      // see the tree in a window
      /*
	if(t!=null)
	{
	ASTFrame frame = new ASTFrame("AST JTree Example", t);
	frame.setVisible(true);
	}
	*/
    }
    catch(antlr.TokenStreamException f){ throw new org.makumba.MakumbaError(f); }
    long diff = new java.util.Date().getTime()-d.getTime();
    MakumbaSystem.getMakumbaLogger("db.query.compilation").fine("OQL to SQL: "+ diff +" ms: "+oqlQuery);
    return t;
  }



}
