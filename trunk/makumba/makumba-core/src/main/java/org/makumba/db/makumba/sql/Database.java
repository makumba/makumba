///////////////////////////////
//Makumba, Makumba tag library
//Copyright (C) 2000-2003  http://www.makumba.org
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//-------------
//$Id$
//$Name$
/////////////////////////////////////

package org.makumba.db.makumba.sql;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.commons.NameResolver;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.SQLPointer;
import org.makumba.commons.StringUtils;
import org.makumba.db.NativeQuery.ParameterHandler;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.MakumbaTransactionProvider;
import org.makumba.db.makumba.Update;

import com.mchange.v2.c3p0.C3P0ProxyStatement;
import com.mchange.v2.c3p0.ComboPooledDataSource;

/**
 * An SQL database, using JDBC. This class should comply with SQL-92
 */
public class Database extends org.makumba.db.makumba.Database {

    Properties connectionConfig = new Properties();

    String url;

    protected static String eng; //

    boolean addUnderscore = true;

    Hashtable<String, Vector<Hashtable<String, Object>>> catalog = null;

    private NameResolver nrh;

    static final int DESIRED_TRANSACTION_LEVEL = java.sql.Connection.TRANSACTION_REPEATABLE_READ;

    static Properties sqlDrivers;

    // static boolean requestUTF8 = false;

    protected ComboPooledDataSource pooledDataSource;

    public String getEngine() {
        return eng;
    }

    public static String getEngineProperty(String s) {
        return sqlDrivers.getProperty(s);
    }

    static public boolean supportsUTF8() {
        if (requestUTF8 == false) {
            return false;
        }
        if (sqlDrivers.getProperty(eng + ".utf8") == null) {
            return false;
        }
        if (sqlDrivers.getProperty(eng + ".utf8").equals("true")) {
            return requestUTF8;
        }
        return false;
    }

    static public boolean supportsForeignKeys() {
        if (requestForeignKeys == false) {
            return false;
        }
        if (sqlDrivers.getProperty(eng + ".foreignKeys") == null) {
            return false;
        }
        if (sqlDrivers.getProperty(eng + ".foreignKeys").equals("true")) {
            return requestForeignKeys;
        }
        return false;
    }

    @Override
    protected DBConnection makeDBConnection() {
        try {
            return new SQLDBConnection(this, tp, pooledDataSource);

            /*} FIXME this should be handled via SQL states and not the instance of the class 
             * catch(com.mysql.jdbc.CommunicationsException ce) {
                // it may be that a connection returned by the pooledDataSource is in fact stale
                // in that case we warn the user that there is a problem
                // TODO in fact we should try to get another connection or figure out if the connection given by the pool is valid
                // but this should be done by the pool...
                logException(ce);
                throw new DBError(ce, "Communications exception in database connection, make sure the timeout option 'MaxIdleTime' (current value: )" + pooledDataSource.getMaxIdleTime() + " s) is smaller than the connection timeout of your database server");
            */} catch (SQLException e) {
            logException(e);
            throw new DBError(e);
        }
    }

    static {
        sqlDrivers = new Properties();
        try {
            sqlDrivers.load(org.makumba.commons.ClassResource.get("sqlEngines.properties").openStream());
        } catch (Exception e) {
            throw new org.makumba.MakumbaError(e);
        }
    }

    /**
     * Initialize the database. Use Database.getDatabase to initilize a SQL database! This constructor will be invoked
     * there (thus it should be hidden :). Besides the properties needed for the org.makumba.db.Database, the following
     * are expected:
     * <dl>
     * <dt>sql.user
     * <dd>the username needed to connect to the database
     * <dt>sql.password
     * <dd>the password needed to connect to the database
     * </dl>
     * To accomodate existing tables:
     * <dl>
     * <dt>addUnderscore
     * <dd>if "true", all table and field names have underscore appended to it, to prevent conflict with SQL keywords in
     * some engines. Make it false for compatibility with older databases.
     * <dt>typename_root= replacement
     * <dd>Specify a rule how to make table names shorter. E.g. a rule best.minerva.student=bms and a type
     * best.minerva.student->fields will create table with shoter name _bms__fields_ instead of
     * _best_minerva_student__fields_ (last underscore depends on the addUnderscore setting)
     * <dt>alter#typename_root
     * <dd>if "true", allows alteration of the table(s) corresponding to the data definition, if the table structure
     * does not correspond to the data definition. The longest indicated root for a type counts (e.g. the more generic
     * root can have alteration on true and the more specific one on false)
     * <dt>typename
     * <dd>can specify the name of the SQL table coresponding to the indicated type
     * <dt>typename#field
     * <dd>can specify the name of the SQL field corresponding to the indicated field
     * <dt>encoding
     * <dd>if "utf8" the outputted html will be in the utf8 charset and the connection with mysql will also be made in
     * utf8
     * </dl>
     */
    public Database(Properties p) {
        super(p);
        nrh = new NameResolverHook(this);
        try {

            if (p.getProperty("encoding") != null && p.getProperty("encoding").equals("utf8")) {
                requestUTF8 = true;
            }

            // set engine
            eng = p.getProperty("#sqlEngine");

            // set JDBC Connection URL
            if (p.getProperty("jdbc_url") != null) {
                url = p.getProperty("jdbc_url");
            } else {
                url = getJdbcUrl(p);
            }

            p.put("jdbc_url", url);

            String s;

            for (Enumeration<Object> e = p.keys(); e.hasMoreElements();) {
                s = (String) e.nextElement();
                if (!s.startsWith("sql.")) {
                    continue;
                }
                connectionConfig.put(s.substring(4), p.getProperty(s).trim());
            }

            String driver = p.getProperty("sql.driver");

            if (p.getProperty("foreignKeys") != null && p.getProperty("foreignKeys").equals("true")) {
                requestForeignKeys = true;
            }

            if (driver == null) {
                driver = sqlDrivers.getProperty(getConfiguration("#sqlEngine"));
            }

            if (driver == null) {
                driver = sqlDrivers.getProperty(url.substring(5, url.indexOf(':', 6)));
            }

            java.util.logging.Logger.getLogger("org.makumba.db.init").info(
                "Makumba " + MakumbaSystem.getVersion() + " INIT: " + url);
            Class.forName(driver);

            // initialise the c3p0 pooled data source
            pooledDataSource = new ComboPooledDataSource();
            pooledDataSource.setDriverClass(driver);
            pooledDataSource.setJdbcUrl(url);

            // some default configuration that should fit any kind of makumba webapp
            // this can anyway be overriden in Makumba.conf
            pooledDataSource.setAcquireIncrement(5);
            pooledDataSource.setMaxIdleTime(1800); // 30 mins

            pooledDataSource.setProperties(connectionConfig);

            DBConnectionWrapper dbcw = (DBConnectionWrapper) getDBConnection();
            SQLDBConnection dbc = (SQLDBConnection) dbcw.getWrapped();
            try {
                p.put("sql_engine.name", dbc.getMetaData().getDatabaseProductName().trim());
                p.put("sql_engine.version", dbc.getMetaData().getDatabaseProductVersion().trim());
                p.put("jdbc_driver.name", dbc.getMetaData().getDriverName().trim());
                p.put("jdbc_driver.version", dbc.getMetaData().getDriverVersion().trim());

                java.util.logging.Logger.getLogger("org.makumba.db.init").info(
                    "\tconnected to "
                            + p.get("sql_engine.name")
                            + " version: "
                            + p.get("sql_engine.version")
                            + "\n\tusing "
                            + p.get("jdbc_driver.name")
                            + " version: "
                            + p.get("jdbc_driver.version")
                            + ("\n\tusing " + (isAutoIncrement() ? "auto increment (no DBSV)" : "DBSV " + p.get("dbsv"))));
                if (!dbc.getMetaData().supportsTransactionIsolationLevel(DESIRED_TRANSACTION_LEVEL)) {
                    java.util.logging.Logger.getLogger("org.makumba.db.init").warning(
                        "transaction isolation level " + DESIRED_TRANSACTION_LEVEL + " not supported, using "
                                + dbc.getMetaData().getDefaultTransactionIsolation());
                }

                readCatalog(dbc);

            } finally {
                dbcw.close();
            }
        } catch (Exception e) {
            throw new org.makumba.MakumbaError(e);
        }
    }

    @Override
    protected void closeResourcePool() {
        pooledDataSource.close();
    }

    @Override
    protected int getResourcePoolSize() {
        try {
            return pooledDataSource.getNumConnectionsDefaultUser();
        } catch (SQLException e) {
            logException(e);
            return -1;
        }
    }

    @Override
    protected int getIdleConnections() {
        try {
            return pooledDataSource.getNumIdleConnectionsDefaultUser();
        } catch (SQLException e) {
            logException(e);
            return -1;
        }
    }

    @Override
    protected int getOpenedConnections() {
        try {
            return pooledDataSource.getNumBusyConnectionsDefaultUser();
        } catch (SQLException e) {
            logException(e);
            return -1;
        }
    }

    @Override
    protected DBConnection getPooledDBConnection() {
        return makeDBConnection();
    }

    protected void readCatalog(SQLDBConnection dbc) throws SQLException {
        Exception ex = null;
        Hashtable<String, Vector<Hashtable<String, Object>>> c = new Hashtable<String, Vector<Hashtable<String, Object>>>();
        boolean failed = false;
        try {
            ResultSet rs = dbc.getMetaData().getColumns(null, null, "%", null);
            if (rs == null) {
                failed = true;
            } else {
                while (rs.next()) {
                    String tn = rs.getString("TABLE_NAME");
                    Vector<Hashtable<String, Object>> v = c.get(tn);
                    if (v == null) {
                        c.put(tn, v = new Vector<Hashtable<String, Object>>());
                    }
                    Hashtable<String, Object> h = new Hashtable<String, Object>(5);
                    h.put("COLUMN_NAME", rs.getString("COLUMN_NAME"));
                    h.put("DATA_TYPE", new Integer(rs.getInt("DATA_TYPE")));
                    h.put("TYPE_NAME", rs.getString("TYPE_NAME"));
                    h.put("COLUMN_SIZE", new Integer(rs.getInt("COLUMN_SIZE")));
                    h.put("IS_NULLABLE", rs.getString("IS_NULLABLE"));
                    v.addElement(h);
                }
            }
            rs.close();
        } catch (SQLException e) {
            failed = true;
            ex = e;
        }
        if (failed) {
            java.util.logging.Logger.getLogger("org.makumba.db.init").severe("failed to read catalog " + ex);
        } else {
            catalog = c;
        }
    }

    /**
     * builds a JDBC Connection URL given the host, sqlEngine and database properties
     */
    protected String getJdbcUrl(Properties p) {

        String _url = "jdbc:";
        _url += eng + ":";
        String local = getEngineProperty(eng + ".localJDBC");
        if (local == null || !local.equals("true")) {
            _url += "//" + p.getProperty("#host") + "/";
        }
        return _url + p.getProperty("#database") + (supportsUTF8() ? "?useEncoding=true&characterEncoding=UTF-8" : "");
    }

    @Override
    public org.makumba.db.makumba.Query prepareQueryImpl(String oqlQuery, String insertIn) {
        return new Query(this, oqlQuery, insertIn);
    }

    @Override
    public Update prepareUpdateImpl(String type, String set, String where) {
        return new SQLUpdate(this, type, set, where);
    }

    @Override
    public int getMinPointerValue() {
        return getDbsv() << SQLPointer.getMaskOrder();
    }

    @Override
    public int getMaxPointerValue() {
        return (getDbsv() + 1 << SQLPointer.getMaskOrder()) - 1;
    }

    @Override
    protected Class<?> getTableClassConfigured() {
        String tcs;
        try {
            if ((tcs = getConfiguration(MakumbaTransactionProvider.TABLE_CLASS)) != null
                    || (tcs = sqlDrivers.getProperty(getConfiguration("#sqlEngine") + "."
                            + MakumbaTransactionProvider.TABLE_CLASS)) != null) {
                return Class.forName(tcs);
            } else {
                return getTableClass();
            }
        } catch (Exception e) {
            throw new org.makumba.MakumbaError(e);
        }
    }

    protected Class<?> getTableClass() {
        return org.makumba.db.makumba.sql.TableManager.class;
    }

    /**
     * escapes all apostrophes from a string and puts the string into apostrophes to be added in a sql command
     */
    public static String SQLEscape(String s) {
        StringBuffer sb = new StringBuffer("\'");
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            if (c == '\'') {
                sb.append('\\');
            } else if (c == '\\') {
                sb.append('\\');
            } else if (c == '\"') {
                sb.append('\\');
            } else if (c == 0) {
                sb.append("\\0");
                continue;
            }
            sb.append(c);

        }
        sb.append('\'');
        return sb.toString();
    }

    /**
     * check the sql state of a SQL exception and throw a DBError if it is not equal with the given state
     */
    protected void checkState(SQLException e, String state) {
        checkState(e, state, null);
    }

    /**
     * check the sql state of a SQL exception and throw a DBError if it is not equal with the given state
     */
    protected void checkState(SQLException e, String state, String command) {
        state = sqlDrivers.getProperty(getConfiguration("#sqlEngine") + "." + state);
        if (state != null && e.getSQLState().equals(state)) {
            return;
        }
        java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").log(java.util.logging.Level.SEVERE,
            "" + e.getSQLState(), e);
        throw new org.makumba.DBError(e, command);
    }

    /**
     * execute a prepared statement and log it, and its exceptions. return the number of records affected, or -1 if a
     * duplicate error appeared
     */
    protected int exec(PreparedStatement ps) {
        try {
            java.util.logging.Logger.getLogger("org.makumba.db.update.execution").fine(getWrappedStatementToString(ps));
            ps.execute();
            int n = ps.getUpdateCount();
            ps.close();
            return n;
        } catch (SQLException e) {
            if (isDuplicateException(e)) {
                return -1;
            }
            logException(e);
            throw new DBError(e);
        }
    }

    /**
     * return whether the exception indicates a duplicate. may need to be specified differently for certain database
     * engines
     */
    @Override
    public boolean isDuplicateException(SQLException e) {
        return e.getMessage().toLowerCase().indexOf("duplicate") != -1;
    }

    @Override
    public Map<String, String> getDuplicateFields(SQLException e) {
        throw new MakumbaError("Method not implemented for this database driver, please contact the developers");
    }

    public boolean isForeignKeyViolationException(SQLException se) {
        return se.getMessage().toLowerCase().contains("a foreign key constraint fails");
    }

    static void logException(SQLException e) {
        logException(e, null);
    }

    static void logException(SQLException e, DBConnection dbc) {
        logException(e, dbc, Level.WARNING);
    }

    static void logException(SQLException e, DBConnection dbc, Level lev) {
        if (!java.util.logging.Logger.getLogger("org.makumba.db.exception").isLoggable(lev)) {
            return;
        }
        String log = "";
        if (dbc != null) {
            log = dbc.toString() + " ";
        }
        for (SQLException se1 = e; se1 != null; se1 = se1.getNextException()) {
            log += se1.getMessage() + " SQL state: " + se1.getSQLState() + " error code :" + se1.getErrorCode() + "\n";
        }

        java.util.logging.Logger.getLogger("org.makumba.db.exception").log(lev, "" + log);
    }

    /** write a date into an OQL query */
    @Override
    public String OQLDate(java.util.Date d) {
        return "date" + "\"" + new Timestamp(d.getTime()) + "\"";
    }

    @Override
    public Pointer getPointer(String type, int uid) {
        return new SQLPointer(type, getDbsv(), uid);
    }

    /** whether specific engine supports LIMIT & OFFSET extensions to the SQL-92 syntax */
    public boolean supportsLimitInQuery() {
        return true;
    }

    public String getLimitSyntax() {
        return "LIMIT ?, ?";
    }

    public boolean isLimitOffsetFirst() {
        return true;
    }

    /** Implementing classes can override this method to extract a more readable error message on foreign key errors. */
    public String parseReadableForeignKeyErrorMessage(SQLException se) {
        return se.getMessage();
    }

    public NameResolver getNameResolverHook() {
        return nrh;
    }

    /**
     * Since we use c3p0 for connection pooling we don't get the raw prepared statement anymore but a proxy. This method
     * fetches the toString() result of the wrapped prepared statement
     */
    public String getWrappedStatementToString(PreparedStatement ps) {
        String sql = "";
        try {
            C3P0ProxyStatement c3p0Stmt = (C3P0ProxyStatement) ps;
            Method toStringMethod = Object.class.getMethod("toString", new Class[] {});
            Object toStr = c3p0Stmt.rawStatementOperation(toStringMethod, C3P0ProxyStatement.RAW_STATEMENT,
                new Object[] {});
            if (toStr instanceof String) {
                sql = (String) toStr;
                sql = sql.substring(sql.indexOf('-') + 1).trim() + ";";
                return sql;
            }
        } catch (Throwable e) {
            return "Exception extracting SQL: " + e.getMessage();
        }
        return null;
    }

    // moved from FieldManager
    /**
     * get the java value of the recordSet column corresponding to this field. This method should return null if the SQL
     * field is null
     */
    public Object getValue(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        if (!fd.getType().startsWith("set")) {
            switch (fd.getIntegerType()) {
                case FieldDefinition._ptr:
                case FieldDefinition._ptrRel:
                case FieldDefinition._ptrOne:
                case FieldDefinition._ptrIndex:
                    return get_ptrDB_Value(fd, rs, i);
                case FieldDefinition._int:
                case FieldDefinition._intEnum:
                    return get_int_Value(fd, rs, i);
                case FieldDefinition._char:
                case FieldDefinition._charEnum:
                    return get_char_Value(fd, rs, i);
                case FieldDefinition._text:
                    return get_text_Value(fd, rs, i);
                case FieldDefinition._binary:
                    return get_binary_Value(fd, rs, i);
                case FieldDefinition._boolean:
                    return get_boolean_Value(fd, rs, i);
                case FieldDefinition._date:
                    return get_dateTime_Value(fd, rs, i);
                case FieldDefinition._dateCreate:
                case FieldDefinition._dateModify:
                    return get_timeStamp_Value(fd, rs, i);
                case FieldDefinition._nil:
                    return get_nil_Value(fd, rs, i);
                case FieldDefinition._real:
                    return get_real_Value(fd, rs, i);
                default:
                    return base_getValue(fd, rs, i);
            }
        } else {
            throw new RuntimeException("shouldn't be here");
        }
    }

    private Object get_real_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        double n = rs.getDouble(i);
        if (rs.wasNull()) {
            return null;
        }
        return new Double(n);
    }

    // original getValue() from FieldManager
    public Object base_getValue(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        Object o = rs.getObject(i);
        if (rs.wasNull()) {
            return null;
        }
        // return getDefaultValue();
        return o;
    }

    // moved from ptrDBManager
    /** return the value as a Pointer */
    public Object get_ptrDB_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fd, rs, i);
        if (o == null) {
            return o;
        }
        return new SQLPointer(fd.getPointedType().getName(), ((Number) o).longValue());
    }

    // moved from intManager
    public Object get_int_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        int n = rs.getInt(i);
        if (rs.wasNull()) {
            return null;
        }
        return new Integer(n);
    }

    // moved from charManager
    /**
     * get the java value of the recordSet column corresponding to this field. This method should return null if the SQL
     * field is null
     */
    public Object get_char_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fd, rs, i);

        if (o instanceof byte[]) {
            String a = new String((byte[]) o);
        }

        if (o == null) {
            return o;
        }
        if (o instanceof byte[]) {
            return new String((byte[]) o);
        }
        return o;
    }

    // moved from textManager
    /**
     * get the java value of the recordSet column corresponding to this field. This method should return null if the SQL
     * field is null
     */
    public Object get_text_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fd, rs, i);
        if (o == null) {
            return o;
        }
        if (o instanceof byte[]) {
            return new Text(new String((byte[]) o));
        }
        return o;

        /*
         * InputStream is= rs.getBinaryStream(i); if(is==null ) return null; return new Text(is);
         */
    }

    /**
     * get the java value of the recordSet column corresponding to this field. This method should return null if the SQL
     * field is null
     */
    public Object get_binary_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fd, rs, i);

        if (o == null) {
            return o;
        }
        return Text.getText(o);

        /*
         * InputStream is= rs.getBinaryStream(i); if(is==null ) return null; return new Text(is);
         */
    }

    public Object get_boolean_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        boolean b = rs.getBoolean(i);
        if (rs.wasNull()) {
            return null;
        }
        return b;
    }

    static SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // moved from dateTimeManager
    /**
     * get the java value of the recordSet column corresponding to this field. This method should return null if the SQL
     * field is null
     */
    public Object get_dateTime_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        // we try to use rs.getTimestamp first; if that fails, get it as an object, and proceed
        try {
            return rs.getTimestamp(i);
        } catch (Throwable t) {

        }

        Object o = rs.getObject(i);
        if (rs.wasNull()) {
            return null;
        }
        if (o instanceof byte[]) { // in some cases, the result might be in a byte array
            try { // then, try to convert it to a String
                o = new String((byte[]) o);
            } catch (Throwable t) {
                // FIXME: treat the exception ?
            }
        }
        if (o instanceof String) {
            try {
                o = sqlDateFormat.parse((String) o);
            } catch (ParseException e) {
                throw new RuntimeWrappedException(e);
            }
        }
        return o;
    }

    // moved from nilManager
    public Object get_nil_Value(FieldDefinition fd, ResultSet rs, int i) {
        return null;
    }

    // moved from timeStampManager
    public Object get_timeStamp_Value(FieldDefinition fd, ResultSet rs, int i) throws SQLException {
        Object o = rs.getTimestamp(i);
        if (rs.wasNull()) {
            return null;
        }
        return o;
    }

    public void setUpdateArgument(FieldDefinition fd, PreparedStatement ps, int n, Object o) throws SQLException {
        if (o == fd.getNull()) {
            setNullArgument(fd, ps, n);
        } else {
            try {
                // System.out.println("UTF: setUpdateArgument");
                setArgument(fd, ps, n, o);
            } catch (SQLException e) {
                java.util.logging.Logger.getLogger("org.makumba.db.update.execution").log(
                    java.util.logging.Level.SEVERE, fd.getDataDefinition() + "  " + o.getClass(), e);
                throw e;
            }
        }
    }

    // moved from FieldManager
    /**
     * Sets a null argument of this type in a prepared SQL statement.<br>
     * Do NOT use this method in queries, only in assignements (INSERT, UPDATE) as some DB types will not support
     * queries with null arguments and instead require an explicit null check via "is null".
     **/
    public void setNullArgument(FieldDefinition fd, PreparedStatement ps, int n) throws SQLException {
        ps.setNull(n, getSQLType(fd));
    }

    // moved from FieldManager
    /** set a non-null argument of this type in a prepared SQL statement */
    public void setArgument(FieldDefinition fd, PreparedStatement ps, int n, Object o) throws SQLException {

        if (fd.getIntegerType() == FieldDefinition._binary) {
            set_binary_Argument(fd, ps, n, o);
        } else if (fd.getIntegerType() == FieldDefinition._text || fd.getIntegerType() == FieldDefinition._char
                || fd.getIntegerType() == FieldDefinition._charEnum) {
            // set_binary_Argument(fieldName, ps, n, o);
            set_text_Argument(fd, ps, n, o);
        } else {
            ps.setObject(n, toSQLObject(fd, o));
        }
    }

    // moved from textManager
    public void set_binary_Argument(FieldDefinition fd, PreparedStatement ps, int n, Object o) throws SQLException {

        Text t = Text.getText(o);
        ps.setBinaryStream(n, t.toBinaryStream(), t.length());

    }

    public void set_text_Argument(FieldDefinition fd, PreparedStatement ps, int n, Object o) throws SQLException {

        Text t = Text.getText(o);
        ps.setString(n, t.getString());

    }

    // original setInsertArgument from FieldManager
    public void base_setInsertArgument(FieldDefinition fd, PreparedStatement ps, int n, Dictionary<String, Object> d)
            throws SQLException {
        Object o = d.get(fd.getName());
        if (o == null || o.equals(fd.getNull())) {
            setNullArgument(fd, ps, n);
        } else {
            setArgument(fd, ps, n, o);
        }
    }

    // moved from timeStampManager
    public void set_timeStamp_InsertArgument(FieldDefinition fd, PreparedStatement ps, int n,
            java.util.Dictionary<String, Object> d) throws SQLException {
        Object o = d.get(fd);
        if (o instanceof java.util.Date && !(o instanceof Timestamp)) {
            d.put(fd.getName(), new Timestamp(((java.util.Date) o).getTime()));
        }
        base_setInsertArgument(fd, ps, n, d);
    }

    // moved from FieldManager
    /**
     * ask this field to write write its argument value in a prepared SQL statement for copying
     */
    public void setCopyArgument(FieldDefinition fd, PreparedStatement ps, int n, Dictionary<String, Object> d)
            throws SQLException {
        try {
            Object o = d.get(fd.getName());
            if (o == null || o.equals(fd.getNull())) {
                setNullArgument(fd, ps, n);
            } else {
                setArgument(fd, ps, n, o);
            }
        } catch (Exception e) {
            throw new RuntimeException(fd.getName() + " " + e.getMessage());
        }
    }

    // moved from FieldManager
    /** what is the SQL type of this field? */
    protected int getSQLType(FieldDefinition fd) {

        switch (fd.getIntegerType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptrOne:
            case FieldDefinition._ptrIndex:
                return get_ptrDB_SQLType(fd);
            case FieldDefinition._int:
            case FieldDefinition._intEnum:
                return get_int_SQLType(fd);
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
            case FieldDefinition._text:
                return get_char_SQLType(fd);
            case FieldDefinition._binary:
                return get_binary_SQLType(fd);
            case FieldDefinition._boolean:
                return get_boolean_SQLType(fd);
            case FieldDefinition._date:
                return get_dateTime_SQLType(fd);
            case FieldDefinition._real:
                return get_real_SQLType(fd);
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return get_timeStamp_SQLType(fd);
            default:
                throw new RuntimeException("" + fd + " should be redefined");
        }
    }

    // moved from ptrDBManager
    public int get_ptrDB_SQLType(FieldDefinition fd) {
        return Types.INTEGER;
    }

    // moved from intManager
    protected int get_int_SQLType(FieldDefinition fd) {
        return java.sql.Types.INTEGER;
    }

    // moved from charManager
    protected int get_char_SQLType(FieldDefinition fd) {
        return java.sql.Types.VARCHAR;
    }

    protected int get_binary_SQLType(FieldDefinition fd) {
        return java.sql.Types.LONGVARBINARY;
    }

    protected int get_boolean_SQLType(FieldDefinition fd) {
        return java.sql.Types.BIT;
    }

    // moved from dateTimeManager
    public int get_dateTime_SQLType(FieldDefinition fd) {
        return java.sql.Types.TIMESTAMP;
    }

    // moved from realManager
    protected int get_real_SQLType(FieldDefinition fd) {
        return java.sql.Types.DOUBLE;
    }

    // moved from timeStampManager
    public int get_timeStamp_SQLType(FieldDefinition fd) {
        return java.sql.Types.TIMESTAMP;
    }

    // moved from FieldManager
    /** transform the object for a SQL insert or update */
    public Object toSQLObject(FieldDefinition fd, Object o) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptrOne:
            case FieldDefinition._ptrIndex:
                return toSQL_ptrDB_Object(fd, o);
            case FieldDefinition._date:
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return toSQL_dateTime_Object(fd, o);
            default:
                return o;

        }
    }

    // original toSQLObject() from FieldManager
    public Object base_toSQLObject(String fieldName, Object o) {
        return o;
    }

    // moved from ptrDBManager
    /** ask this field to write a value of its type in a SQL statement */
    public Object toSQL_ptrDB_Object(FieldDefinition fd, Object o) {
        return new Integer((int) ((Pointer) o).longValue());
    }

    // moved from dateTimeManager
    public Object toSQL_dateTime_Object(FieldDefinition fd, Object o) {
        return new Timestamp(((java.util.Date) o).getTime());
    }

    // moved from FieldManager
    /** ask this field to write its contribution in a SQL CREATE statement */
    // public String inCreate(){ return getDBName()+" "+getDBType(null);}
    // moved from FieldManager
    /** ask this field to write a value of its type in a SQL statement */
    public String writeConstant(FieldDefinition fd, Object o) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return write_char_Constant(fd, o);
            case FieldDefinition._text:
                return write_text_Constant(fd, o);
            case FieldDefinition._binary:
                return write_binary_Constant(fd, o);
            case FieldDefinition._boolean:
                return write_boolean_Constant(fd, o);
            case FieldDefinition._date:
                return write_dateTime_Constant(fd, o);
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return write_timeStamp_Constant(fd, o);
            default:
                if (o == fd.getNull()) {
                    return "null";
                }
                return toSQLObject(fd, o).toString();
        }
    }

    // original writeConstant from FieldManager
    public String base_writeConstant(FieldDefinition fd, Object o) {
        if (o == fd.getNull()) {
            return "null";
        }
        return toSQLObject(fd, o).toString();
    }

    // moved from charHandler
    /** does apostrophe escape */
    public String write_char_Constant(FieldDefinition fd, Object o) {
        return org.makumba.db.makumba.sql.Database.SQLEscape(o.toString());
    }

    // moved from textManager
    /** does apostrophe escape */
    public String write_text_Constant(FieldDefinition fd, Object o) {
        return org.makumba.db.makumba.sql.Database.SQLEscape(o.toString());
    }

    /** does apostrophe escape */
    public String write_binary_Constant(FieldDefinition fd, Object o) {
        return org.makumba.db.makumba.sql.Database.SQLEscape(o.toString());
    }

    public String write_boolean_Constant(FieldDefinition fd, Object o) {
        return (Boolean) o ? "1" : "0";
    }

    // moved from dateTimeManager
    /** writes the date between apostrophes */
    public String write_dateTime_Constant(FieldDefinition fd, Object o) {
        return "\'" + new Timestamp(((java.util.Date) o).getTime()) + "\'";
        // "\'"+super.writeConstant(o)+"\'";
    }

    // moved from timeStampManager
    /** writes the date between apostrophes */
    public String write_timeStamp_Constant(FieldDefinition fd, Object o) {
        return "\'" + base_writeConstant(fd, o) + "\'";
    }

    // moved from FieldManager
    /** ask this field to write its contribution in a SQL CREATE statement */
    public String inCreate(FieldDefinition fd) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return in_char_Create(fd);
            case FieldDefinition._boolean:
                return in_boolean_Create(fd);
            case FieldDefinition._ptrIndex:
                return in_primaryKeyCreate(fd);
            default:
                return base_inCreate(fd);
        }
    }

    // original inCreate() from FieldManager
    public String base_inCreate(FieldDefinition fd) {
        return getFieldDBName(fd) + " " + this.getFieldDBType(fd);
    }

    // moved from charManager
    /** write in CREATE, in the form name char[size] */
    public String in_char_Create(FieldDefinition fd) {
        String s = Database.getEngineProperty(getEngine() + ".charBinary");
        if (s != null && s.equals("true")) {
            s = " BINARY";
        } else {
            s = "";
        }
        // should width be computed by getDBType() instead?
        return getFieldDBName(fd) + " " + getFieldDBType(fd) + "(" + fd.getWidth() + ")" + s;
        // return
        // super.inCreate(d)+"("+getFieldDefinition(fieldName).getWidth()()+")"+s;
    }

    /** write in CREATE, in the form name BIT(1) */
    public String in_boolean_Create(FieldDefinition fd) {
        return getFieldDBName(fd) + " " + getFieldDBType(fd) + "(1)";
    }

    private String in_primaryKeyCreate(FieldDefinition fd) {
        // FIXME: primary keys will have to be made in another step, before hibernate schema update
        if (isAutoIncrement() || usesHibernateIndexes()) {
            return base_inCreate(fd) + " " + getCreateAutoIncrementSyntax();
        } else {
            return base_inCreate(fd);
        }
    }

    // moved from FieldManager
    /**
     * ask this field to write its argument placeholder in a prepared UPDATE SQL statement
     */
    public String inPreparedUpdate(FieldDefinition fd) {
        return getFieldDBName(fd) + "=?";
    }

    // moved from FieldManager
    /** what is the database level type of this field? */
    protected String getFieldDBType(FieldDefinition fd, Database d) {
        String s = Database.getEngineProperty(d.getEngine() + "." + fd.getDataType());
        if (s == null) {
            return getFieldDBType(fd);
        }
        return s;
    }

    // moved from FieldManager
    /** what is the database level type of this field? */
    protected String getFieldDBType(FieldDefinition fd) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptrOne:
            case FieldDefinition._ptrIndex:
                return get_ptrDB_FieldDBType(fd);
            case FieldDefinition._int:
            case FieldDefinition._intEnum:
                return get_int_FieldDBType(fd);
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return get_char_FieldDBType(fd);
            case FieldDefinition._text:
                return get_text_FieldDBType(fd);
            case FieldDefinition._binary:
                return get_binary_FieldDBType(fd);
            case FieldDefinition._boolean:
                return get_boolean_FieldDBType(fd);
            case FieldDefinition._date:
                return get_dateTime_FieldDBType(fd);
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return get_timeStamp_FieldDBType(fd);
            case FieldDefinition._real:
                return get_real_FieldDBType(fd);
            default:
                throw new RuntimeException("" + fd + " should be redefined");
        }
    }

    // moved from ptrDBManager
    /** returns INT */
    protected String get_ptrDB_FieldDBType(FieldDefinition fd) {
        return "INTEGER";
    }

    // moved from intManager
    /** Use standard SQL name, unless defined otherwise in sqlEngines.properties. */
    protected String get_int_FieldDBType(FieldDefinition fd) {
        return "INTEGER"; // standard name
    }

    // moved from charManager
    /** returns char */
    protected String get_char_FieldDBType(FieldDefinition fd) {
        return "VARCHAR";
    }

    // moved from textManager
    /** returns text */
    protected String get_text_FieldDBType(FieldDefinition fd) {
        return "LONGTEXT";
    }

    /** returns text */
    protected String get_binary_FieldDBType(FieldDefinition fd) {
        return "LONG VARBINARY";
    }

    protected String get_boolean_FieldDBType(FieldDefinition fd) {
        return "BIT";
    }

    // moved from dateTimeManager
    /** returns datetime */
    protected String get_dateTime_FieldDBType(FieldDefinition fd) {
        return "DATETIME";
    }

    // moved from realManager
    /** Use standard SQL name, unless defined otherwise in sqlEngines.properties. */
    protected String get_real_FieldDBType(FieldDefinition fd) {
        return "DOUBLE PRECISION"; // standard name
    }

    // moved from timeStampManager
    /** returns timestamp */
    protected String get_timeStamp_FieldDBType(FieldDefinition fd) {
        return "TIMESTAMP";
    }

    protected String getCreateAutoIncrementSyntax() {
        return null;
    }

    protected String getQueryAutoIncrementSyntax() {
        return null;
    }

    protected String getColumnAlterKeyword() {
        return "MODIFY";
    }

    // moved from FieldManager
    /**
     * check if the column from the SQL database (read from the catalog) still corresponds with the abstract definition
     * of this field
     */
    protected boolean unmodified(FieldDefinition fd, int type, int size, Vector<Hashtable<String, Object>> columns,
            int index) throws SQLException {
        switch (fd.getIntegerType()) {
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return unmodified_char(fd, type, size, columns, index);
            case FieldDefinition._ptrIndex:
                return unmodified_primaryKey(fd, type, size, columns, index);
            default:
                return base_unmodified(fd, type, size, columns, index);
        }
    }

    // original unmodified() from FieldManager
    protected boolean base_unmodified(FieldDefinition fd, int type, int size,
            Vector<Hashtable<String, Object>> columns, int index) throws SQLException {
        return type == getSQLType(fd);
    }

    private boolean unmodified_primaryKey(FieldDefinition fd, int type, int size,
            Vector<Hashtable<String, Object>> columns, int index) throws SQLException {
        if (!base_unmodified(fd, type, size, columns, index)) {
            return false;
        }
        if (!isAutoIncrement() && !usesHibernateIndexes()) {
            return true;
        }
        return unmodifiedAutoIncrement(columns.elementAt(index - 1));
    }

    private boolean unmodifiedAutoIncrement(Hashtable<String, Object> column) {
        // this is a hack. we know that auto_increment is always not null, and we take advantage of makumba having
        // created _nullable_ primary keys before.
        return "NO".equals(column.get("IS_NULLABLE"));

    }

    // moved from charManager
    /**
     * Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this
     * returns true. Some SQL drivers allocate more anyway.
     */
    protected boolean unmodified_char(FieldDefinition fd, int type, int size,
            java.util.Vector<Hashtable<String, Object>> columns, int index) throws SQLException {
        return (base_unmodified(fd, type, size, columns, index) || type == java.sql.Types.CHAR)
                && check_char_Width(fd, size);
    }

    // moved from wrapperManager
    /**
     * check if the column from the SQL database still coresponds with the abstract definition of this field
     */
    protected boolean unmodified_wrapper(FieldDefinition fd, int type, int size,
            java.util.Vector<Hashtable<String, Object>> v, int index) throws SQLException {
        return base_unmodified(fd, type, size, v, index);
    }

    // moved from charManager
    /** check the char width */
    protected boolean check_char_Width(FieldDefinition fd, int width) throws SQLException {
        // some drivers might allocate more, it's their business
        return width >= fd.getWidth();
    }

    // moved from FieldManager
    /**
     * Ask this field how to name the index on this field. Normally called from manageIndexes().
     */
    public String getFieldDBIndexName(FieldDefinition fd) {
        // return rm.getDBName()+"_"+getDBName();
        return getFieldDBName(fd);
    }

    // moved from FieldManager
    /**
     * ask this field to write write its argument placeholder ('?') in a prepared INSERT SQL statement
     */
    public String inPreparedInsert(FieldDefinition fd) {
        return "?";
    }

    // moved from FieldManager
    /** Syntax for index creation. */
    public String indexCreateSyntax(FieldDefinition fd) {
        return "CREATE INDEX " + getFieldDBIndexName(fd) + " ON " + getDBName(fd.getDataDefinition()) + " ("
                + getFieldDBName(fd) + ")";
    }

    // moved from FieldManager
    /** Syntax for unique index creation. */
    public String indexCreateUniqueSyntax(FieldDefinition fd) {
        return "CREATE UNIQUE INDEX " + getFieldDBIndexName(fd) + " ON " + getDBName(fd.getDataDefinition()) + " ("
                + getFieldDBName(fd) + ")";
    }

    // moved from FieldManager
    /** Tell whether this type of field should be indexed. */
    public boolean shouldIndex(FieldDefinition fd) {
        if (fd.getIntegerType() == FieldDefinition._text || fd.getIntegerType() == FieldDefinition._binary) {
            return should_text_Index(fd);
        } else {
            return true;
        }
    }

    // moved from textManager
    public boolean should_text_Index(FieldDefinition fd) {
        return false;
    }

    /** Syntax for unique index creation. */
    public String foreignKeyCreateSyntax(FieldDefinition fd, FieldDefinition foreign) {
        getTable(foreign.getDataDefinition());
        return "ALTER TABLE " + getDBName(fd.getDataDefinition()) + " ADD FOREIGN KEY "
                + shortIndexName(getDBName(foreign.getDataDefinition()), fd.getName()) + " (" + getFieldDBName(fd)
                + ") REFERENCES " + getDBName(foreign.getDataDefinition()) + " (" + getFieldDBName(foreign) + ")";
    }

    /** Makes a short index based on the table and field name, if needed **/
    protected String shortIndexName(String tableName, String constraintName) {
        // FIXME this may not be true for other DBMS than mysql
        String standardIndex = tableName + "__" + constraintName;
        if (standardIndex.length() + "__ibfk_XX".length() > 64) {
            // general_archive_Email__fromPerson --> g_a_E__fromPerson
            String shortIndex = "";
            StringTokenizer st = new StringTokenizer(tableName, "_");
            while (st.hasMoreTokens()) {
                shortIndex += st.nextToken().substring(0, 1);
                if (st.hasMoreTokens()) {
                    shortIndex += "_";
                }
            }
            shortIndex += "__" + constraintName;
            return shortIndex;
        } else {
            return standardIndex;
        }
    }

    // ALTER TABLE child ADD FOREIGN KEY (parent_id) REFERENCES parent(id)

    /** Syntax for multi-field index */
    public String indexCreateUniqueSyntax(DataDefinition dd, String[] fieldNames) {
        String[] dbs = new String[fieldNames.length];
        for (int i = 0; i < dbs.length; i++) {
            dbs[i] = getFieldDBName(dd.getFieldDefinition(fieldNames[i]));
        }
        String dbFieldNames = StringUtils.toString(dbs, false);
        return "CREATE UNIQUE INDEX " + StringUtils.concatAsString(fieldNames) + " ON " + getDBName(dd) + " ("
                + dbFieldNames + ")";
    }

    // moved from FieldManager
    /** Syntax for dropping index. */
    public String indexDropSyntax(FieldDefinition fd) {
        return "DROP INDEX " + getFieldDBIndexName(fd) + " ON " + getDBName(fd.getDataDefinition());
    }

    /**
     * DBMS-specific syntax useful resource: http://www.troels.arvin.dk/db/rdbms/
     **/

    /** for odbc */
    protected void indexCreated(SQLDBConnection dbc) {
    }

    /** for mysql */
    protected String createDbSpecific(String command) {
        return command;
    }

    /** mysql needs to have it adjustable */
    protected String getTableMissingStateName(SQLDBConnection dbc) {
        return "tableMissing";
    }

    protected void onAlter(DataDefinition dd, org.makumba.db.makumba.sql.SQLDBConnection dbc, boolean alter)
            throws SQLException {
    }

    public ParameterHandler makeParameterHandler(final PreparedStatement ps, final DBConnection dbc,
            final String command) {

        return new ParameterHandler() {

            @Override
            public void handle(int index, FieldDefinition fd, Object value) {
                try {
                    setUpdateArgument(fd, ps, index + 1, value);
                } catch (SQLException se) {
                    logException(se, dbc);
                    throw new DBError(se, command);
                }
            }
        };

    }

    public String createStatement(String tblname, DataDefinition dd) {
        StringBuffer ret = new StringBuffer();
        String sep = "";
        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            if (fd.getType().startsWith("set")) {
                continue;
            }
            ret.append(sep).append(inCreate(fd));
            sep = ",";
        }
        return "CREATE TABLE " + tblname + "(" + ret + ")";

    }
}
