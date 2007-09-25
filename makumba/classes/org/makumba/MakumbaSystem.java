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

package org.makumba;


import org.makumba.controller.html.CalendarEditorProvider;
import org.makumba.controller.html.KruseCalendarEditor;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory;
import org.makumba.util.NamedResources;
import org.makumba.util.wiki.JspWikiFormatter;
import org.makumba.util.wiki.WikiFormatter;
import org.makumba.view.validation.ClientsideValidationProvider;
import org.makumba.view.validation.LiveValidationProvider;

/** The makumba runtime system. Provides starter methods to obtain {@link Transaction} and {@link DataDefinition} objects */
public class MakumbaSystem {
    /** DataDefinition provider * */
    private static DataDefinitionProvider MDDFactory = MakumbaDataDefinitionFactory.getInstance();

    /** The date at which makumba is loaded */
    static public final java.util.Date loadingTime = new java.util.Date();

    /** cleanup all the makumba resources at the end of a makumba program or at e.g. servlet context reload */
    public static void close() {
        org.makumba.MakumbaSystem.getMakumbaLogger("system").info("destroying makumba caches");
        NamedResources.cleanup();
    }

    /**
     * Get information about the makumba cache sizes.
     * 
     * @return a {@link java.util.Map} with cache categories as keys and cache sizes as values
     * @since makumba-0.5.5.13
     */
    public static java.util.Map getCacheInfo() {
        return org.makumba.util.NamedResources.getCacheInfo();
    }

    /**
     * The name of the default database according to the lookup file "MakumbaDatabase.properties"
     * 
     * @since makumba-0.5.4
     * @deprecated Use {@link #getDefaultDataSourceName()} instead
     */
    public static String getDefaultDatabaseName() {
        return getDefaultDataSourceName();
    }

    /**
     * The name of the default datasource according to the Makumba configuration
     * 
     * TODO use DataSource provider
     * 
     */
    public static String getDefaultDataSourceName() {
        return org.makumba.db.Database.findDatabaseName("MakumbaDatabase.properties");
    }

    /**
     * The name of the default database according to the database lookup file indicated
     * 
     * @param dbLookupFile
     *            the name of the database lookup file, including ".properties", or any other extension. The file should
     *            be in CLASSPATH.
     * @since makumba-0.5.4
     */
    public static String getDefaultDatabaseName(String dbLookupFile) {
        return org.makumba.db.Database.findDatabaseName(dbLookupFile);
    }

    /**
     * Get a connection to the database described in the .properties file with the given name.
     * 
     * @param name
     *            the database name, the same as the db description file but without ".properties". for example
     *            "localhost_mysql_databasename". The file should be in CLASSPATH. The operations carried out during
     *            database initialization are logged (see {@link java.util.logging.Logger},
     *            {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.init"</code></b>
     *            and <b><code>"db.init.tablechecking"</code></b> loggers, with {@link java.util.logging.Level#INFO}
     *            (connections, checkings), {@link java.util.logging.Level#SEVERE} (fatal errors) and with
     *            {@link java.util.logging.Level#WARNING} logging levels.
     * @since makumba-0.5.4
     */
    public static Transaction getConnectionTo(String name) {
        return org.makumba.db.Database.getDatabase(name).getDBConnection();
    }

    /**
     * Get a connection to the default database found according to MakumbaDatabase.properties
     * 
     * @since makumba-0.5
     * @deprecated This method name is misleading since it returns a connection, not a database. Use
     *             getConnectionTo(getDefaultDatabaseName()) instead
     */
    public static Transaction findDatabase() {
        return getConnectionTo(getDefaultDataSourceName());
    }

    /**
     * Find the Database according to the given lookup file from the CLASSPATH. The file name will include the
     * .properties extension
     * 
     * @since makumba-0.5
     * @deprecated This method name is misleading since it returns a connection, not a database. Use
     *             getConnectionTo(getDefaultDatabaseName(dbLookupFile)) instead
     */
    public static Transaction findDatabase(String dbLookupFile) {
        return getConnectionTo(getDefaultDatabaseName(dbLookupFile));
    }

    /**
     * Get the Database defined by the given connection file from the CLASSPATH. The file name should not include the
     * .properties extension
     * 
     * @since makumba-0.5
     * @deprecated This method name is misleading since it returns a connection, not a database. Use
     *             getConnectionTo(connectionFile) instead
     */
    public static Transaction getDatabase(String connectionFile) {
        return getConnectionTo(connectionFile);
    }

    /**
     * Access the properties of a database. Besides the properties defined in the database connection file, the
     * following are available <table border =1>
     * <tr>
     * <td><code>sql_engine.name</code>
     * <td>name of the SQL engine used
     * <tr>
     * <td><code>sql_engine.version</code>
     * <td>version of the SQL engine used
     * <tr>
     * <td><code>sql.jdbc_driver.name</code>
     * <td>name of the JDBC driver used
     * <tr>
     * <td><code>jdbc_driver.name</code>
     * <td>name of the JDBC driver used
     * <tr>
     * <td><code>jdbc_driver.version</code>
     * <td>version of the JDBC driver used
     * <tr>
     * <td><code>jdbc_url</code>
     * <td>JDBC url connected to
     * <tr>
     * <td><code>jdbc_connections</code>
     * <td>number of jdbc connections open </table>
     * 
     * @since makumba-0.5.5.7
     */
    public static String getDatabaseProperty(String name, String propName) {
        return org.makumba.db.Database.getDatabase(name).getConfiguration(propName);
    }

    /**
     * Get the DataDefinition defined by the given type. The type a.b.C will generate a lookup for the file
     * CLASSPATH/a/b/C.mdd and then for CLASSPATH/dataDefinitions/a/b/C.mdd
     */
    public static DataDefinition getDataDefinition(String typeName) {
        return MDDFactory.getDataDefinition(typeName);
    }

    public static DataDefinition getTemporaryDataDefinition(String name) {
        return MDDFactory.getVirtualDataDefinition(name);
    }

    /** Make a field definition from the indicated string */
    public static FieldDefinition makeFieldDefinition(String name, String definition) {
        return MDDFactory.makeFieldDefinition(name, definition);
    }

    /** Make a field definition with the elementary type */
    public static FieldDefinition makeFieldOfType(String name, String type) {
        return MDDFactory.makeFieldOfType(name, type);
    }

    /** Make a field definition identical with the given one, except for the name */
    public static FieldDefinition makeFieldWithName(String name, FieldDefinition type) {
        return MDDFactory.makeFieldWithName(name, type);
    }

    /** Make a field definition with the elementary type */
    public static FieldDefinition makeFieldOfType(String name, String type, String description) {
        return MDDFactory.makeFieldOfType(name, type, description);
    }

    /** Make a field definition identical with the given one, except for the name */
    public static FieldDefinition makeFieldWithName(String name, FieldDefinition type, String description) {
        return MDDFactory.makeFieldWithName(name, type, description);
    }

    /**
     * Get the DataDefinition of the records returned by the given OQL query
     * 
     * @deprecated use {@link OQLQueryProvider#getOQLAnalyzer} for better OQL functionality
     
    public static DataDefinition getResultDataDefinition(String OQL) {
        return OQLQueryProvider.getOQLAnalyzer(OQL).getProjectionType();
    }
    */

    /**
     * Deletes the records of certain types that originate from a certain database. Useful for failed imports or copies.
     * The database configuration must have admin# confirmations that match each of the indicated types. Use _delete(d,
     * d, ...) for databases that need re-import of data of certain types. Deletion is logged (see
     * {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the
     * <b><code>"db.admin.delete"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
     */
    public static void _delete(String whereDB, String provenienceDB, String[] typeNames) {
        org.makumba.db.Database.getDatabase(whereDB).deleteFrom(provenienceDB, typeNames, false);
    }

    /**
     * Deletes the records of certain types. Useful for failed imports or copies. The database configuration must have
     * admin# confirmations that match each of the indicated types. Use _delete(d, d, ...) for databases that need
     * re-import of data of certain types. Deletion is logged (see {@link java.util.logging.Logger},
     * {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.delete"</code></b>
     * logger, with {@link java.util.logging.Level#INFO} logging level.
     */
    public static void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv) {
        org.makumba.db.Database.getDatabase(whereDB).deleteFrom(provenienceDB, typeNames, ignoreDbsv);
    }

    /**
     * Copies records of certain types (and their subtypes) from a database to another. Only data having the dbsv of the
     * first database is copied. The destination database must have admin# confirmations that match each of the
     * indicated types Copying is logged (see {@link java.util.logging.Logger},
     * {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.copy"</code></b>
     * logger, with {@link java.util.logging.Level#INFO} logging level.
     */
    public static void _copy(String sourceDB, String destinationDB, String[] typeNames) {
        org.makumba.db.Database.getDatabase(destinationDB).copyFrom(sourceDB, typeNames, false);
    }

    /**
     * Copies records of certain types (and their subtypes) from a database to another. The destination database must
     * have admin# confirmations that match each of the indicated types Copying is logged (see
     * {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the
     * <b><code>"db.admin.copy"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
     */
    public static void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv) {
        org.makumba.db.Database.getDatabase(destinationDB).copyFrom(sourceDB, typeNames, ignoreDbsv);
    }

    /** Returns a Makumba version (derived from a CVS tag) */
    public static String getVersion() {
        return org.makumba.version.getVersion();
    }

    /** Returns build date (as recorded during building) */
    public static java.util.Date getBuildDate() {
        return org.makumba.version.getBuildDate();
    }

    static String loggingRoot = "org.makumba";

    /**
     * Get a logger for logging during makumba operations. See {@link java.util.logging.Logger},
     * {@link #setLoggingRoot(java.lang.String)}. This method is mostly used by makumba code. From application code,
     * use {@link #getLogger(java.lang.String)} or {@link #getLogger() }.
     * <p>
     * The table below describes when makumba logging occurs and at what logging {@link java.util.logging.Level} (note
     * also that {@link java.util.logging.Level#SEVERE} and {@link java.util.logging.Level#WARNING} logging is done in
     * makumba when fatal errors or warnings occur).
     * <p>
     * The logging levels below tell the programmer how to configure logging so that some parts of the makumba logging
     * become visible. For example, details on database update performance are not normally visible on the log, since
     * they are at {@link java.util.logging.Level#FINE} logging level, and {@link java.util.logging.Level#INFO} is
     * default. To view them, one needs to add the following line in <code>logging.properties</code>(see
     * {@link java.util.logging.LogManager} for explanations of logger configuration)<br>
     * <blockquote> <code>org.makumba.update.performance.level=FINE</code><br>
     * </blockquote> The programmer could just as well decide that all makumba logging at or over the level FINE should
     * be visible, except for the one on taglib performance:<br>
     * <blockquote> <code>org.makumba.level=FINE</code><br>
     * <code>org.makumba.taglib.performance.level=INFO</code> </blockquote>
     * <p>
     * <table border=1>
     * <tr>
     * <td>Operation
     * <td>Log name
     * <td>Logging details
     * <td>Logging level
     * <tr>
     * <td>application operations
     * <td>loggingRoot + <code>apps</code>+ the parameter to {@link #getLogger(java.lang.String)}
     * <td>logging level used by applications that call {@link #getLogger(java.lang.String)} and {@link #getLogger()}
     * <td> any, as required by the application
     * <tr>
     * <td>database opening
     * <td>loggingRoot + <code>db.init</code>, <code>db.init.tablechecking</code>
     * <td>see {@link #getConnectionTo(java.lang.String)}
     * <td>{@link java.util.logging.Level#INFO}
     * <tr>
     * <td>connection pooling
     * <td>loggingRoot + <code>util.pool
     <td>infomrmation on the size of the database connection pool
     <td>{@link java.util.logging.Level#FINE}

     <tr><td>database administration
     <td>loggingRoot + <code>db.admin.copy</code>, <code>db.admin.delete
     <td>see {@link #_copy}, {@link #_delete}
     <td>{@link java.util.logging.Level#INFO}

     <tr><td>database querying
     <td>loggingRoot + <code>db.query.compilation</code>,
     <code>db.query.execution</code>, <code>db.query.performance
     <td>see {@link org.makumba.Transaction#executeQuery(java.lang.String, java.lang.Object)}, 
     {@link org.makumba.Transaction#read(org.makumba.Pointer, java.lang.Object)}
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
     <td>see {@link org.makumba.Transaction}, all insert, delete, and update operations
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
    public static java.util.logging.Logger getMakumbaLogger(String suffix) {
        return java.util.logging.Logger.getLogger(loggingRoot + "." + suffix);
    }

    /**
     * Return a logger for use by an application.
     * 
     * @return the logging root + "apps" + the indicated suffix
     * @see org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)
     * @since makumba-0.5.5.3
     */
    public static java.util.logging.Logger getLogger(String suffix) {
        return getMakumbaLogger("apps." + suffix);
    }

    /**
     * Return a logger for use by an application.
     * 
     * @return the logging root plus "apps"
     * @see org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)
     * @since makumba-0.5.5.3
     */
    public static java.util.logging.Logger getLogger() {
        return getMakumbaLogger("apps");
    }

    /**
     * Change the makumba logging root in this classloader. The default logging root is <b><code>"org.makumba"</code></b>
     * so loggers would look like <b><code>org.makumba.db.init.tableckecking</code></b>
     * 
     * @since makumba-0.5.5.3
     */
    public static void setLoggingRoot(String root) {
        loggingRoot = root;
    }

    /**
     * Returns the timezone in which makumba should read/display dates from/to users, configurable by the system
     * variable makumba.displayTimeZone
     */
    public static java.util.TimeZone getTimeZone() {
        String s = null;
        try {
            s = System.getProperty("makumba.displayTimeZone");
        } catch (SecurityException e) {
        } // for applets
        if (s != null)
            return java.util.TimeZone.getTimeZone(s);
        return java.util.TimeZone.getDefault();

    }

    /**
     * @return the system's default wiki formatter.
     */
    public static WikiFormatter getWikiFormatter() {
        return JspWikiFormatter.getInstance();
    }

    /** Returns the makumba system locale */
    // later: should be configurable by config files
    public static java.util.Locale getLocale() {
        return java.util.Locale.UK;
    }

    /**
     * Discover mdds in a directory in classpath.
     * 
     * @return filenames as Vector of Strings.
     */
    public static java.util.Vector mddsInDirectory(String dirInClasspath) {
        java.net.URL u = org.makumba.util.ClassResource.get(dirInClasspath);
        java.io.File dir = new java.io.File(u.getFile());
        java.util.Vector<String> mdds = new java.util.Vector<String>();
        fillMdds(dir.toString().length() + 1, dir, mdds);
        return mdds;
    }

    static void fillMdds(int baselength, java.io.File dir, java.util.Vector<String> mdds) {
        if (dir.isDirectory()) {
            String[] list = dir.list();
            for (int i = 0; i < list.length; i++) {
                String s = list[i];
                if (s.endsWith(".mdd")) {
                    s = dir.toString() + java.io.File.separatorChar + s;
                    s = s.substring(baselength, s.length() - 4); // cut off the ".mdd"
                    s = s.replace(java.io.File.separatorChar, '.');
                    mdds.add(s);
                } else {
                    java.io.File f = new java.io.File(dir, s);
                    if (f.isDirectory())
                        fillMdds(baselength, f, mdds);
                }
            }
        }
    }

    /** Get the default calendar editor. FIXME: read this from some config, or so. */
    public static CalendarEditorProvider getCalendarProvider() {
        return KruseCalendarEditor.getInstance();
    }

    /** Get the default client-side validation provider. FIXME: read this from some config, or so. */
    public static ClientsideValidationProvider getClientsideValidationProvider() {
        return new LiveValidationProvider();
    }
}
