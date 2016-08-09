package org.makumba.db.makumba;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

import org.makumba.ConfigurationError;
import org.makumba.Transaction;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.Configuration;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.Configuration.DataSourceType;

/**
 * Makumba-specific implementation of the {@link TransactionProvider}.
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaTransactionProvider.java,v 1.1 21.11.2007 17:06:25 Manuel Exp $
 */
public class MakumbaTransactionProvider extends TransactionProvider {

    private static class SingletonHolder implements org.makumba.commons.SingletonHolder {
        private static TransactionProvider singleton = new MakumbaTransactionProvider();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    public static TransactionProvider getInstance() {
        return SingletonHolder.singleton;
    }

    public static final String DATABASE_CLASS = "database_class";

    public static final String TABLE_CLASS = "table_class";

    public static final String CONNECTION_HOST = "host";

    public static final String CONNECTION_PORT = "port";
    
    public static final String CONNECTION_ENGINE = "engine";
    
    public static final String CONNECTION_DATABASE = "database";

    private MakumbaTransactionProvider() {

    }

    private static class CRUDOperationProviderSingletonHolder implements org.makumba.commons.SingletonHolder {
        private static CRUDOperationProvider singleton = new MakumbaCRUDOperationProvider();
        
        public void release() {
            singleton = null;
        }

        public CRUDOperationProviderSingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    static Class<?>[] theProp = { java.util.Properties.class };

    /**
     * The Makumba database layer handles the following properties:
     * <dl>
     * <dt>sql.*</dt>
     * <dd>properties that are passed directly to the JDBC driver</dd>
     * <dt>dbsv</dt>
     * <dd>the DBSV will be used to set the most significant 8 bits of all primary keys generated for the new records
     * created by this makumba client. Using different DBSVs can help to ensure key uniqueness among multiple clients
     * using (or synchronizing with) the same database. Database inserts of records that have the same primary key as an
     * already existing record in that table (i.e. a record created by a client that used the same DBSV) will fail. You
     * cannot specify both dbsv and auto_increment in the same configuration file, but you can access the same database
     * with some clients using (different) dbsv-s, and others using autoIncrement.</dd>
     * <dt>autoIncrement</dt>
     * <dd>if autoIncrement is on, makumba will use the server-side primary key generation. You cannot specify both dbsv
     * and auto_increment in the same configuration file, but you can access the same database with some clients using
     * (different) dbsv-s, and others using autoIncrement.</dd>
     * <dt>initConnections</dt>
     * <dd>if autoIncrement is on, makumba will use the server-side primary key generation. You cannot specify both dbsv
     * and auto_increment in the same configuration file, but you can access the same database with some clients using
     * (different) dbsv-s, and others using autoIncrement.</dd>
     * <dt>database_class</dt>
     * <dd>The main class of the makumba database driver. This is normally read from
     * org/makumba/db/sql/sqlEngines.properties, but other, more powerful drivers can be plugged in.</dd>
     * <dt>table_class</dt>
     * <dd>Table handler of the database driver (optional, norlally known by the dbclass or read from
     * org/makumba/db/sql/sqlEngines.properties)</dd>
     * <dt>typename = SQLtableName</dt>
     * <dd>Sets the name of the SQL table representing the given type to SQLtableName (to inhibit automatic table name
     * generation)</dd>
     * <dt>typename->field=SQLfieldName</dt>
     * <dd>Sets the name of the SQL field representing the given object attribute to SQLfieldName (to inhibit automatic
     * field name generation)</dd>
     * <dt>typenamePrefix=typenameShorthand</dt>
     * <dd>Before automatic tablename generation; shortens the names of all the types that begin with the indicated
     * prefix by replacing it with the indicated shorthand. Useful for SQL engines that have severe limitations on
     * tablename length</dd>
     * <dt>alter#typenameShorthand=true|false</dt>
     * <dd>If true allows automatic alteration of the SQL table representing the type(s) when the SQL table doesn't
     * match the type definition. For example: <code>alter#=true</code><br>
     * <code>alter#general=false</code><br>
     * will allow alteration of all tables from the database except for tables whose names begin with "general"</dd>
     * <dt>admin#typenameShorthand=true|false</dt>
     * <dd>If true allows deletion of multiple records of the respective types during <code>org.makumba.delete</code>
     * and <code>org.makumba.copy</code> the type(s) when the SQL table doesn't match the type definition.</dd>
     * <dt>addUnderscore=true|false</dt>
     * <dd>Specifies whether to add an underscore at the end of the generated field and table names during automatic
     * name generation. It is true by default. (Introduced to avoid conflicts with reserved words on some SQL engines)</dd>
     * </dl>
     */
    public static Database getDatabase(String name) {
        try {
            return (Database) NamedResources.getStaticCache(dbs).getResource(name);
        } catch (RuntimeWrappedException e) {
            if (e.getCause() instanceof org.makumba.MakumbaError)
                throw (org.makumba.MakumbaError) e.getCause();
            throw e;
        }
    }

    static int dbs = NamedResources.makeStaticCache("Databases open", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        /**
         * Configure the Database. If a JDBC Connection URL is given, this one will be used, otherwise, it will be built
         * internally from host, port, engine and database.
         */
        protected Object makeResource(Object nm) {
            Properties p = new Properties();
            String name = (String) nm;

            // load properties from the configuration
            Map<String, String> properties = Configuration.getDataSourceConfiguration(name);

            for (String property : properties.keySet()) {
                String value = properties.get(property);

                if (property.startsWith(CONNECTION_PREFIX)) {
                    property = property.substring(CONNECTION_PREFIX.length());

                    // we need to adjust for the global connection.username format, Hibernate does use "username" instead of
                    // the JDBC "user"
                    if (property.equals(CONNECTION_USERNAME)) {
                        property = "sql.user";
                    }
                    
                    if(property.equals(CONNECTION_PASSWORD)) {
                        property = "sql.password";
                    }
                }
                p.put(property, value);
            }

            // first we see if there is a JDBC connection URL provided
            // jdbc:[subprotocol]:[node]/[databaseName]

            String jdbcURL = p.getProperty(TransactionProvider.CONNECTION_URL);
            if (jdbcURL != null) {
                if (!jdbcURL.startsWith("jdbc:")) {
                    throw new ConfigurationError("jdbcConnectionUrl of dataSource " + name
                            + " invalid, should start with jdbc:");
                }
                p.put("jdbc_url", jdbcURL);

                // fetch engine because we need it in order to make the database
                String sqlEngine = jdbcURL.substring(5);
                sqlEngine = sqlEngine.substring(0, sqlEngine.indexOf(":"));
                p.put("#sqlEngine", sqlEngine);

            } else {

                // check if the required properties are there
                String[] requiredProps = { CONNECTION_HOST, CONNECTION_DATABASE, CONNECTION_ENGINE};
                for (String prop : requiredProps) {
                    if (p.get(prop) == null) {
                        throw new ConfigurationError("Property " + prop + " not defined for dataSource " + name);
                    }
                }
                if(p.getProperty(CONNECTION_PORT) != null) {
                    p.put("#host", p.getProperty(CONNECTION_HOST) + ":" + p.getProperty(CONNECTION_PORT));
                } else {
                    p.put("#host", p.getProperty(CONNECTION_HOST));
                }
                p.put("#sqlEngine", p.getProperty(CONNECTION_ENGINE));
                p.put("#database", p.getProperty(CONNECTION_DATABASE));
            }

            try {

                String dbclass = (String) p.get(DATABASE_CLASS);
                if (dbclass == null) {
                    dbclass = org.makumba.db.makumba.sql.Database.getEngineProperty(p.getProperty("#sqlEngine") + "."
                            + DATABASE_CLASS);
                    if (dbclass == null)
                        dbclass = "org.makumba.db.makumba.sql.Database";
                }
                p.put("db.name", (String) name);

                Object pr[] = { p };

                try {
                    Database d = (Database) Class.forName(dbclass).getConstructor(theProp).newInstance(pr);
                    d.dataSourceName = (String) name;
                    d.initialiseTables(name);

                    // TODO: need to check if hibernate schema update is authorized. If yes, drop/adjust indexes from
                    // all tables so that hibernate can create its foreign keys at schema update.

                    return d;
                } catch (InvocationTargetException ite) {
                    throw new org.makumba.MakumbaError(ite.getTargetException());
                }
            } catch (Exception e) {
                throw new org.makumba.MakumbaError(e);
            }
        }
    });

    public Transaction getConnectionTo(String name) {
        return super.getConnectionTo(name, this);
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
        return getDatabase(name).getConfiguration(propName);
    }

    public void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv) {
        getDatabase(destinationDB).copyFrom(sourceDB, typeNames, ignoreDbsv);
    }

    public void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv) {
        getDatabase(whereDB).deleteFrom(provenienceDB, typeNames, ignoreDbsv);
    }

    public String getQueryLanguage() {
        return super.getQueryLanguage(this);
    }

    public CRUDOperationProvider getCRUD() {
        return super.getCRUD(this);
    }

    @Override
    protected Transaction getTransaction(String name) {
        return (DBConnectionWrapper) getDatabase(name).getDBConnection(name);
    }

    @Override
    protected CRUDOperationProvider getCRUDInternal() {
        return CRUDOperationProviderSingletonHolder.singleton;
    }


    @Override
    protected String getQueryLanguageInternal() {
        return "oql";
    }
    
    private DataSourceType lastConnectionType = DataSourceType.makumba;

    @Override
    protected DataSourceType getLastConnectionType() {
        return this.lastConnectionType;
    }

    @Override
    protected void setLastConnectionType(DataSourceType type) {
        this.lastConnectionType = type;
    }
    
    @Override
    public void closeDataSource(String dataSourceName) {
        getDatabase(dataSourceName);
        
    }

}
