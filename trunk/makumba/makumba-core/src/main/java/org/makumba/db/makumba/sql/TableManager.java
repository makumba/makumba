// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

package org.makumba.db.makumba.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.logging.Level;

import org.makumba.CompositeValidationException;
import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.NotUniqueException;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.DataDefinition.MultipleUniqueKeyDefinition;
import org.makumba.FieldDefinition.FieldErrorMessageType;
import org.makumba.commons.NameResolver;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.SQLPointer;
import org.makumba.commons.StringUtils;
import org.makumba.db.DataHolder;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.Table;

public class TableManager extends Table {
    protected String tbname;

    protected String handlerList, handlerListAutoIncrement;

    protected String indexDBField;

    protected String indexField;

    protected String modTable;

    protected long primaryKeyCurrentIndex;

    protected int dbsv;

    boolean alter;

    boolean exists_;

    Hashtable<String, Object> handlerExist = new Hashtable<String, Object>();

    Dictionary<String, Integer> keyIndex;

    String preparedInsertString, preparedInsertAutoIncrementString, preparedDeleteString, preparedDeleteFromString,
            preparedDeleteFromIgnoreDbsvString;

    /** The query that searches for duplicates on this field */
    Hashtable<String, String> checkDuplicate = new Hashtable<String, String>();

    Hashtable<String, String> checkNullDuplicate = new Hashtable<String, String>();

    @Override
    public boolean exists() {
        return exists_;
    }

    @Override
    public boolean exists(String s) {
        return handlerExist.get(s) != null;
    }

    public String getDBName() {
        return tbname;
    }

    protected org.makumba.db.makumba.sql.Database getSQLDatabase() {
        return (org.makumba.db.makumba.sql.Database) getDatabase();
    }

    protected boolean usesHidden() {
        return true;
    }

    void makeKeyIndex() {
        if (keyIndex == null) {
            keyIndex = new Hashtable<String, Integer>();

            for (int i = 0; i < getDataDefinition().getFieldNames().size(); i++) {
                FieldDefinition fi = getDataDefinition().getFieldDefinition(i);
                if (!fi.getType().startsWith("set")) {
                    keyIndex.put(fi.getName(), new Integer(i));
                }
            }
        }
    }

    /** the SQL table opening. might call create() or alter() */
    @Override
    protected void open(Properties config, NameResolver nr) {
        setTableAndFieldNames(nr);
        if (!getDataDefinition().isTemporary()) {
            // System.out.println("\n\n** Opening table " + getDBName());
            DBConnectionWrapper dbcw = (DBConnectionWrapper) getSQLDatabase().getDBConnection();
            SQLDBConnection dbc = (SQLDBConnection) dbcw.getWrapped();
            try {
                checkStructure(dbc, config);
                initFields(dbc, config);
                preparedInsertString = prepareInsert(false);
                preparedInsertAutoIncrementString = prepareInsert(true);
                preparedDeleteString = prepareDelete();
                preparedDeleteFromIgnoreDbsvString = "DELETE FROM " + getDBName();
                preparedDeleteFromString = "DELETE FROM " + getDBName() + " WHERE " + indexDBField + " >= ? AND "
                        + indexDBField + " <= ?";
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally {
                dbcw.close();
            }
        } else {
            makeKeyIndex();
        }

    }

    @Override
    public void close() {
        // FIXME we should maybe do more things here, for now, we only reset the primary key index
        resetPrimaryKey();

    }

    /** the SQL table opening. might call create() or alter() */
    protected void setTableAndFieldNames(NameResolver nr) {

        tbname = nr.resolveTypeName(dd.getName());

    }

    boolean admin;

    @Override
    public boolean canAdmin() {
        return admin;
    }

    protected void checkStructure(SQLDBConnection dbc, Properties config) {
        String s = org.makumba.db.makumba.Database.findConfig(config, "admin#" + getDataDefinition().getName());
        admin = s != null && config.getProperty(s).trim().equals("true");

        s = org.makumba.db.makumba.Database.findConfig(config, "alter#" + getDataDefinition().getName());
        alter = s != null && config.getProperty(s).trim().equals("true");

        java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
            getDatabase().getName() + ": checking " + getDataDefinition().getName() + " as " + tbname);

        try {
            CheckingStrategy cs = null;
            if (getSQLDatabase().catalog != null) {
                cs = new CatalogChecker(getSQLDatabase().catalog);
            } else {
                throw new MakumbaError(getDatabase().getName() + ": could not open catalog");
            }

            if (cs.shouldCreate()) {
                create(dbc, tbname, alter);

                exists_ = alter;
                config.put("makumba.wasCreated", "");
                makeKeyIndex();
            } else {
                exists_ = true;
                alter(dbc, cs, alter);
            }
        } catch (SQLException sq) {
            sq.printStackTrace();
            throw new org.makumba.DBError(sq);
        }
    }

    Map<String, Boolean> indexes = new HashMap<String, Boolean>();

    Boolean parsedForeignKeys = false;

    Hashtable<String, String[]> foreignKeys = new Hashtable<String, String[]>();

    Set<String> extraIndexes;

    private boolean autoIncrementAlter;

    // private boolean primaryKeyOK;

    protected void initFields(SQLDBConnection dbc, Properties config) throws SQLException {
        try {
            // System.out.println("\t** init fields " + getDBName());
            ResultSet rs = dbc.getMetaData().getIndexInfo(null, null, getDBName(), false, false);
            while (rs.next()) {
                String iname = rs.getString("INDEX_NAME");
                boolean non_unique = rs.getBoolean("NON_UNIQUE");
                if (iname != null) {
                    indexes.put(iname.toLowerCase(), new Boolean(non_unique));
                }

            }
            rs.close();
            // http://osdir.com/ml/db.postgresql.jdbc/2002-11/msg00089.html for fieldnames
            ResultSet rs2 = dbc.getMetaData().getImportedKeys(null, null, getDBName());

            while (rs2.next()) {
                String[] temp_foreign = new String[2];
                temp_foreign[0] = rs2.getString("PKTABLE_NAME");
                temp_foreign[1] = rs2.getString("PKCOLUMN_NAME");

                if (foreignKeys.get(rs2.getString("FKCOLUMN_NAME")) != null) {
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                        "WARNING: duplicate foreign keys for table `" + rs2.getString("FKTABLE_NAME") + "`, field `"
                                + rs2.getString("FKCOLUMN_NAME") + "`");
                } else {
                    foreignKeys.put(rs2.getString("FKCOLUMN_NAME").toLowerCase(), temp_foreign);
                }
            }
            rs2.close();

        } catch (SQLException e) {
            Database.logException(e, dbc);
            throw new DBError(e);
        }

        extraIndexes = new HashSet<String>(indexes.keySet());

        for (String string : dd.getFieldNames()) {
            String fieldName = string;
            if (getFieldDefinition(fieldName).getType().startsWith("set")) {
                continue;
            }
            onStartup(fieldName, config, dbc);
        }

        // now process multi-field indices
        DataDefinition.MultipleUniqueKeyDefinition[] multiFieldUniqueKeys = getDataDefinition().getMultiFieldUniqueKeys();
        for (int i = 0; i < multiFieldUniqueKeys.length; i++) {
            String[] fieldNames = multiFieldUniqueKeys[i].getFields();
            if (!multiFieldUniqueKeys[i].isKeyOverSubfield() && !isIndexOk(fieldNames)) {
                String fields = StringUtils.toString(fieldNames);
                String briefMulti = getDataDefinition().getName() + "#" + fields.toLowerCase();
                try {
                    Statement st = dbc.createStatement();
                    st.executeUpdate(indexCreateUniqueSyntax(fieldNames));
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                        "INDEX ADDED on " + briefMulti);
                    st.close();
                    indexCreated(dbc);
                } catch (SQLException e) {
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                        "Problem adding multi-field INDEX on " + briefMulti + ": " + e.getMessage() + " [ErrorCode: "
                                + e.getErrorCode() + ", SQLstate:" + e.getSQLState() + "]");
                    if (getDatabase().isDuplicateException(e)) {
                        throw new DBError("Error adding unique key for " + getDataDefinition().getName()
                                + " on fields " + fields + ": " + e.getMessage());
                    } else {
                        throw new DBError(e);
                    }
                }
            }
            extraIndexes.remove(StringUtils.concatAsString(fieldNames).toLowerCase());
        }

        if (!getDatabase().usesHibernateIndexes()) {
            if (alter) {
                for (String indexName : extraIndexes) {
                    String syntax = "DROP INDEX " + indexName + " ON " + getDBName();
                    try {
                        Statement st = dbc.createStatement();
                        st.executeUpdate(syntax);
                        java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                            "INDEX DROPPED on " + getDataDefinition().getName() + "#" + indexName);
                        st.close();
                    } catch (SQLException e) {
                        treatIndexException(e, syntax, dbc);
                    }
                }
            } else {
                StringBuffer extraList = new StringBuffer();
                String separator = "";
                for (String x : extraIndexes) {
                    extraList.append(separator).append(x);
                    separator = ", ";
                }
                if (extraList.length() > 0) {
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                        "Extra indexes on " + getDataDefinition().getName() + ": " + extraList);
                }
            }
        }

        // initialises list of fields with primary key (pointer index)
        StringBuffer sb = new StringBuffer();
        fieldList(sb, dd.getFieldNames().elements());
        handlerList = sb.toString();

        // initialises list of fields without PK as we have auto-increment
        sb = new StringBuffer();
        Enumeration<String> e = dd.getFieldNames().elements();
        e.nextElement();
        fieldList(sb, e);
        handlerListAutoIncrement = sb.toString();

        indexField = dd.getIndexPointerFieldName();
        indexDBField = getFieldDBName(indexField);
    }

    private void treatIndexException(SQLException e, String command, SQLDBConnection dbc) {
        Level lev = Level.FINE;
        if (e.getMessage().indexOf("check that column/key exists") != -1) {
            // dropping an index that doesn't exist, this is expected since we don't check for existence first, maybe we
            // should...
            lev = Level.FINEST;
        }
        if (command.indexOf("fk") != -1) {
            // dropping a hibernate foreign key, this is serious
            lev = Level.WARNING;
        }
        if (!java.util.logging.Logger.getLogger("org.makumba.db.exception").isLoggable(lev)) {
            return;
        }
        java.util.logging.Logger.getLogger("org.makumba.db.exception").log(lev, "Unsuccessful: " + command);
        Database.logException(e, dbc, lev);
    }

    protected interface CheckingStrategy {
        boolean hasMoreColumns() throws SQLException;

        String columnName() throws SQLException;

        int columnType() throws SQLException;

        String columnTypeName() throws SQLException;

        boolean checkColumn(String fieldName) throws SQLException;

        boolean shouldCreate() throws SQLException;
    }

    class CatalogChecker implements CheckingStrategy {
        Vector<Hashtable<String, Object>> columns;

        Hashtable<String, Object> column;

        int i = 0;

        CatalogChecker(Hashtable<String, Vector<Hashtable<String, Object>>> catalog) throws SQLException {
            columns = catalog.get(tbname);
            if (columns == null) {
                columns = catalog.get(tbname.toLowerCase());
                if (columns == null) {
                    columns = catalog.get(tbname.toUpperCase());
                    if (columns != null) {
                        tbname = tbname.toUpperCase();
                    }
                } else {
                    tbname = tbname.toLowerCase();
                }

            }
        }

        public boolean shouldCreate() {
            return columns == null;
        }

        public boolean hasMoreColumns() throws SQLException {
            if (i < columns.size()) {
                column = columns.elementAt(i);
                i++;
                return true;
            }
            return false;
        }

        public String columnName() throws SQLException {
            return (String) column.get("COLUMN_NAME");
        }

        public int columnType() throws SQLException {
            return ((Integer) column.get("DATA_TYPE")).intValue();
        }

        public int columnSize() throws SQLException {
            return ((Integer) column.get("COLUMN_SIZE")).intValue();
        }

        public String columnTypeName() throws SQLException {
            return (String) column.get("TYPE_NAME");
        }

        public boolean checkColumn(String fieldName) throws SQLException {
            return unmodified(fieldName, columnType(), columnSize(), columns, i);
        }
    }

    @Override
    public int deleteFrom(DBConnection here, DBConnection source, boolean ignoreDbsv) {
        if (!exists()) {
            return 0;
        }
        if (!canAdmin()) {
            throw new MakumbaError("no administration approval for " + getDataDefinition().getName());
        }

        if (here instanceof DBConnectionWrapper) {
            here = ((DBConnectionWrapper) here).getWrapped();
        }
        PreparedStatement ps = null;
        if (ignoreDbsv) {
            ps = ((SQLDBConnection) here).getPreparedStatement(preparedDeleteFromIgnoreDbsvString);
        } else {
            ps = ((SQLDBConnection) here).getPreparedStatement(preparedDeleteFromString);
            try {
                ps.setInt(1, source.getHostDatabase().getMinPointerValue());
                ps.setInt(2, source.getHostDatabase().getMaxPointerValue());
            } catch (SQLException e) {
                org.makumba.db.makumba.sql.Database.logException(e);
                throw new DBError(e);
            }
        }
        // exec closes the ps
        int n = getSQLDatabase().exec(ps);

        if (!getSQLDatabase().isAutoIncrement()) {
            resetPrimaryKey();
        }
        return n;
    }

    /**
     * checks if an alteration is needed, and calls doAlter if so
     * 
     * @param alter
     *            TODO
     */
    protected void alter(SQLDBConnection dbc, CheckingStrategy cs, boolean alter) throws SQLException {
        Vector<String> present = new Vector<String>();
        Vector<String> add = new Vector<String>();
        Vector<String> modify = new Vector<String>();
        Vector<String> drop = new Vector<String>();
        Object withness = new Object();

        while (cs.hasMoreColumns()) {
            String dbfn = cs.columnName();
            boolean found = false;
            for (String string : dd.getFieldNames()) {
                String fieldName = string;
                if (getFieldDefinition(fieldName).getType().startsWith("set")) {
                    continue;
                }
                if (getFieldDBName(fieldName).toLowerCase().equals(dbfn.toLowerCase())) {
                    handlerExist.put(fieldName, withness);
                    /*
                     * We only need to know the size of present later, doens't matter which values are inside
                     */
                    present.addElement(fieldName);
                    if (!cs.checkColumn(fieldName) && !(alter && alter(dbc, fieldName, getColumnAlterKeyword()))) {
                        java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                            "should modify: " + fieldName + " " + getFieldDBName(fieldName) + " "
                                    + getFieldDBType(fieldName) + " " + cs.columnType() + " " + cs.columnName());
                        modify.addElement(fieldName);
                    }
                    found = true;
                }
            }
            if (found) {
                continue;
            }
            drop.addElement(dbfn);
            java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                "extra field: " + cs.columnName() + " " + cs.columnType() + " " + cs.columnTypeName());
        }

        Vector<String> v = new Vector<String>();
        keyIndex = new Hashtable<String, Integer>();

        for (String fieldName : dd.getFieldNames()) {
            if (getFieldDefinition(fieldName).getType().startsWith("set")) {
                continue;
            }
            if (handlerExist.get(fieldName) == null && !(alter && alter(dbc, fieldName, "ADD"))) {
                add.addElement(fieldName);
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                    "should add " + fieldName + " " + getFieldDBName(fieldName) + " " + getFieldDBType(fieldName));
            } else {
                keyIndex.put(fieldName, new Integer(v.size()));
                v.addElement(fieldName);
            }
        }

        doAlter(dbc, drop, present, add, modify);
    }

    protected String getColumnAlterKeyword() {
        return "MODIFY";
    }

    boolean alter(SQLDBConnection dbc, String fieldName, String op) throws SQLException {
        Statement st = dbc.createStatement();
        String command = null;
        if (!autoIncrementAlter) {
            try {
                command = "DROP INDEX " + getFieldDBIndexName(fieldName) + " ON " + getDBName();
                st.executeUpdate(command);
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info("SUCCESS: " + command);
            } catch (SQLException e) {
                treatIndexException(e, command, dbc);
            }
        }
        autoIncrementAlter = false;
        String s = "ALTER TABLE " + getDBName() + " " + op + " " + inCreate(fieldName, getSQLDatabase());
        java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
            getSQLDatabase().getName() + ": " + s);
        st.executeUpdate(s);
        handlerExist.put(fieldName, "");
        dbc.commit();
        st.close();
        return true;
    }

    /**
     * do the needed alterations after examining the data definition of the existing table. a temporary copy table is
     * created, and the fields are copied from it to the re-CREATEd table. ALTER TABLE might be used instead, and
     * drivers that don't support it will have their own RecordManager, extending this one.
     * 
     * @param drop
     *            the names of the db fields that should be dropped (they might not be)
     * @param present
     *            the abstract fields that exist in the DB, in DB order
     * @param add
     *            the abstract fields that are not present in the db and need to be added
     * @param modify
     *            the abstract fields that exist in the db but need to be modified to the new abstract definition
     */
    protected void doAlter(SQLDBConnection dbc, Vector<String> drop, Vector<String> present, Vector<String> add,
            Vector<String> modify) throws SQLException {
        // MakumbaSystem.getLogger("debug.db").severe(drop);
        // MakumbaSystem.getLogger("debug.db").severe(present);
        // MakumbaSystem.getLogger("debug.db").severe(add);
        // MakumbaSystem.getLogger("debug.db").severe(modify);

        if (add.size() == 0 && modify.size() == 0) {
            return;
        }

        if (present.size() == 0) {
            create(dbc, tbname, alter);
        }
    }

    /** a table creation, from this table's RecordInfo */
    protected void create(SQLDBConnection dbc, String tblname, boolean really) throws SQLException {
        Statement st = dbc.createStatement();
        // Object[] dbArg = { getSQLDatabase() };
        if (really && !tblname.startsWith("temp")) {
            try {
                st.executeUpdate("DROP TABLE " + tblname);
            } catch (SQLException e) {
                getSQLDatabase().checkState(e, getTableMissingStateName(dbc));
            }
        }

        /* TODO: concatAll() */

        StringBuffer ret = new StringBuffer();
        String sep = "";
        for (String fieldName : dd.getFieldNames()) {
            if (getFieldDefinition(fieldName).getType().startsWith("set")) {
                continue;
            }
            ret.append(sep).append(inCreate(fieldName, getSQLDatabase()));
            sep = ",";
        }
        String command = "CREATE TABLE " + tblname + "(" + ret + ")";

        command = createDbSpecific(command);
        if (!really) {
            java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning("would be:\n" + command);
            return;
        }
        if (!tblname.startsWith("temp")) {
            java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(command);
        }
        st.executeUpdate(command);
        if (!tblname.startsWith("temp")) {
            dbc.commit();
        }
        st.close();
    }

    /* TODO: move to initFields */
    /** list the given fields in a command field1, field2 ... */
    protected void fieldList(StringBuffer command, Enumeration<String> e) {
        String comma = "";

        while (e.hasMoreElements()) {
            String fieldName = e.nextElement();
            if (getFieldDefinition(fieldName).getType().startsWith("set")) {
                continue;
            }
            command.append(comma);
            comma = ", ";
            command.append(getFieldDBName(fieldName));
        }
    }

    protected String prepareInsert(boolean autoIncrement) {

        /* TODO: concatAll() */
        StringBuffer ret = new StringBuffer();
        String sep = "";

        for (String fieldName : dd.getFieldNames()) {
            if (getFieldDefinition(fieldName).getType().startsWith("set")
                    || getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._ptrIndex && autoIncrement) {
                continue;
            }
            ret.append(sep).append(inPreparedInsert(fieldName));
            sep = ",";
        }

        return "INSERT INTO " + tbname + " (" + (autoIncrement ? handlerListAutoIncrement : handlerList) + ") VALUES ("
                + ret + ")";
    }

    @Override
    public Pointer insertRecordImpl(DBConnection dbc, Dictionary<String, Object> d) {
        boolean wasIndex = d.get(indexField) != null;
        boolean wasCreate = d.get("TS_create") != null;
        boolean wasModify = d.get("TS_create") != null;

        // while(true)
        try {
            if (dbc instanceof DBConnectionWrapper) {
                dbc = ((DBConnectionWrapper) dbc).getWrapped();
            }

            PreparedStatement ps;
            if (wasIndex || !getSQLDatabase().isAutoIncrement()) {
                ps = ((SQLDBConnection) dbc).getPreparedStatement(preparedInsertString);
            } else {
                ps = ((SQLDBConnection) dbc).getPreparedStatement(preparedInsertAutoIncrementString);
            }
            int n = 0;
            for (String fieldName : dd.getFieldNames()) {
                if (getFieldDefinition(fieldName).getType().startsWith("set")) {
                    continue;
                }
                if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._ptrIndex && !wasIndex
                        && getSQLDatabase().isAutoIncrement()) {
                    continue;
                }
                n++;
                try {
                    setInsertArgument(fieldName, ps, n, d);
                } catch (Throwable ex) {
                    // throw new DBError(ex, (getRecordInfo().getName())+"
                    // "+(fm.getName())+" "+(d.get(fm.getName())));
                    throw new org.makumba.DBError(ex, "insert into \""
                            + getDataDefinition().getName()
                            + "\" at field \""
                            + fieldName
                            + "\" could not assign value \""
                            + d.get(fieldName)
                            + "\" "
                            + (d.get(fieldName) != null ? "of type \"" + d.get(fieldName).getClass().getName() + "\""
                                    : ""));

                }
            }
            // exec closes ps
            // here if we have an error, we enrich it by finding the duplicates (which we assume is the only error one
            // can get at this stage of the insert)
            if (getSQLDatabase().exec(ps) == -1) {
                findDuplicates(dbc, d);
            }

            if (!wasIndex && getSQLDatabase().isAutoIncrement()) {
                ps = ((SQLDBConnection) dbc).getPreparedStatement(getQueryAutoIncrementSyntax());
                ResultSet rs = ps.executeQuery();
                rs.next();
                d.put(indexField, new SQLPointer(getDataDefinition().getName(), rs.getInt(1)));
                rs.close();
                ps.close();
            }

            Pointer ret = (Pointer) d.get(indexField);

            if (!wasIndex) {
                d.remove(indexField);
            }
            if (!wasCreate) {
                d.remove("TS_create");
            }
            if (!wasModify) {
                d.remove("TS_modify");
            }
            return ret;
        }
        /*
          * catch(ReconnectedException re) { prepareStatements(); continue; }
          */

        // catch(SQLException e) { throw new org.makumba.DBError (e); }
        catch (Throwable t) {
            // we wrap errors into a DB error, except CompositeValidationException, which will be handled separately
            if (t instanceof CompositeValidationException) {
                throw (CompositeValidationException) t;
            } else {
                if (!(t instanceof DBError)) {
                    t = new org.makumba.DBError(t);
                }
                throw (DBError) t;
            }
        }
    }

    @Override
    public void findDuplicates(DBConnection dbc, Dictionary<String, Object> d) {
        CompositeValidationException notUnique = new CompositeValidationException();

        // first we check all fields of the data definition
        for (String string : dd.getFieldNames()) {
            String fieldName = string;
            Object val = d.get(fieldName);
            if (getFieldDefinition(fieldName).getType().startsWith("set")) {
                continue;
            }
            if (checkDuplicate(fieldName, dbc, d)) {
                FieldDefinition fd = dd.getFieldDefinition(fieldName);
                if (fd.getErrorMessage(FieldErrorMessageType.NOT_UNIQUE) == null) {
                    notUnique.addException(new NotUniqueException(getFieldDefinition(fieldName), val));
                } else {
                    notUnique.addException(new NotUniqueException(fd.getErrorMessage(FieldErrorMessageType.NOT_UNIQUE)));
                }
            }
        }

        // now we check all multi-field indices
        MultipleUniqueKeyDefinition[] multiFieldUniqueKeys = getDataDefinition().getMultiFieldUniqueKeys();
        for (int i = 0; i < multiFieldUniqueKeys.length; i++) {
            // we only need to check unique keys within the same data definition now
            // multi-field unique keys spanning over several data definitions are checked in
            // checkDuplicate(DataDefinition.MultipleUniqueKeyDefinition, Object[], SQLDBConnection)
            // and other keys would not cause an error here, as they are not mapped to the data base
            if (!multiFieldUniqueKeys[i].isKeyOverSubfield()) {
                String[] fields = multiFieldUniqueKeys[i].getFields();
                Object[] values = new Object[fields.length];
                for (int j = 0; j < fields.length; j++) {
                    values[j] = d.get(fields[j]);
                }
                if (checkDuplicate(fields, values, dbc)) {
                    notUnique.addException(new NotUniqueException(multiFieldUniqueKeys[i].getFields()[0],
                            multiFieldUniqueKeys[i].getErrorMessage(values)));
                }
            }
        }

        if (notUnique.getExceptions().size() > 0) {
            throw notUnique;
        }
    }

    protected String prepareDelete() {
        return "DELETE FROM " + tbname + " WHERE " + inPreparedUpdate(indexField);
    }

    public void deleteRecord(DBConnection dbc, Pointer uid) {
        if (dbc instanceof DBConnectionWrapper) {
            dbc = ((DBConnectionWrapper) dbc).getWrapped();
        }

        PreparedStatement ps = ((SQLDBConnection) dbc).getPreparedStatement(preparedDeleteString);

        // while(true)
        try {
            setUpdateArgument(getDBName(), ps, 1, uid);
            // exec closes the ps
            getSQLDatabase().exec(ps);
            // break;
        }// catch(ReconnectedException e) { continue; }
        catch (SQLException f) {
            org.makumba.db.makumba.sql.Database.logException(f);
            throw new DBError(f);
        }
    }

    @Deprecated
    public void updateRecord(DBConnection dbc, Pointer uid, Dictionary<String, Object> d) {
        if (dbc instanceof DBConnectionWrapper) {
            dbc = ((DBConnectionWrapper) dbc).getWrapped();
        }
        d.remove(indexField);
        d.remove("TS_create");

        // d.put("TS_modify", "");
        d.put("TS_modify", new java.util.Date());

        StringBuffer command = new StringBuffer("UPDATE ").append(tbname).append(" SET ");

        String s = "";
        for (Enumeration<String> e = d.keys(); e.hasMoreElements();) {
            if (s.length() > 0) {
                command.append(",");
            }
            String fieldName = e.nextElement();
            String fieldDBName = getFieldDBName(fieldName);
            if (fieldDBName == null) {
                throw new org.makumba.DBError(new Exception("no such field " + fieldDBName + " in " + this.getDBName()));
            }
            command.append(s = inPreparedUpdate(fieldName));

        }

        command.append(" WHERE " + inPreparedUpdate(indexField));
        // System.out.println("UTFcommand: "+command.toString());

        // while(true)
        try {
            PreparedStatement st = ((SQLDBConnection) dbc).getPreparedStatement(command.toString());

            int n = 1;
            for (Enumeration<String> e = d.keys(); e.hasMoreElements(); n++) {
                String ss = e.nextElement();
                setUpdateArgument(ss/* (String) e.nextElement() */, st, n, d);
            }

            setUpdateArgument(getDBName(), st, n, uid);

            // exec closes the st
            if (getSQLDatabase().exec(st) == -1) {
                findDuplicates(dbc, d);
            }
            return;
        }// catch(ReconnectedException re) { continue; }
        catch (SQLException se) {
            throw new org.makumba.DBError(se);
        }
    }

    protected void fillResult(ResultSet rs, Dictionary<String, Object> p) throws java.sql.SQLException {
        int n = dd.getFieldNames().size();
        for (int i = 0; i < n;) {
            if (dd.getFieldDefinition(i).getType().startsWith("set")) {
                continue;
            }
            setValue(dd.getFieldDefinition(i).getName(), p, rs, ++i);
        }
    }

    protected void fillResult(ResultSet rs, Object[] data) throws java.sql.SQLException {
        int n = dd.getFieldNames().size();
        for (int i = 0; i < n; i++) {
            if (dd.getFieldDefinition(i).getType().startsWith("set")) {
                continue;
            }
            try {
                data[i] = getValue(dd.getFieldDefinition(i).getName(), rs, i + 1);
            } catch (ArrayIndexOutOfBoundsException e) {
                java.util.logging.Logger.getLogger("org.makumba.db.query.execution").log(
                    java.util.logging.Level.SEVERE,
                    "" + i + " " + dd.getName() + " " + keyIndex + " " + dd.getFieldNames(), e);
                throw e;
            }
        }
    }

    public Object getValue(ResultSet rs, String fieldName, int i) {
        try {
            return getValue(fieldName, rs, i);
        } catch (SQLException e) {
            throw new org.makumba.DBError(e);
        }
    }

    // moved from FieldManager
    /**
     * get the java value of the recordSet column corresponding to this field. This method should return null if the SQL
     * field is null
     */
    public Object getValue(String fieldName, ResultSet rs, int i) throws SQLException {
        if (!getFieldDefinition(fieldName).getType().startsWith("set")) {
            switch (getFieldDefinition(fieldName).getIntegerType()) {
                case FieldDefinition._ptr:
                case FieldDefinition._ptrRel:
                case FieldDefinition._ptrOne:
                case FieldDefinition._ptrIndex:
                    return get_ptrDB_Value(fieldName, rs, i);
                case FieldDefinition._int:
                case FieldDefinition._intEnum:
                    return get_int_Value(fieldName, rs, i);
                case FieldDefinition._char:
                case FieldDefinition._charEnum:
                    return get_char_Value(fieldName, rs, i);
                case FieldDefinition._text:
                    return get_text_Value(fieldName, rs, i);
                case FieldDefinition._binary:
                    return get_binary_Value(fieldName, rs, i);
                case FieldDefinition._boolean:
                    return get_boolean_Value(fieldName, rs, i);
                case FieldDefinition._date:
                    return get_dateTime_Value(fieldName, rs, i);
                case FieldDefinition._dateCreate:
                case FieldDefinition._dateModify:
                    return get_timeStamp_Value(fieldName, rs, i);
                case FieldDefinition._nil:
                    return get_nil_Value(fieldName, rs, i);
                case FieldDefinition._real:
                    return get_real_Value(fieldName, rs, i);
                default:
                    return base_getValue(fieldName, rs, i);
            }
        } else {
            throw new RuntimeException("shouldn't be here");
        }
    }

    private Object get_real_Value(String fieldName, ResultSet rs, int i) throws SQLException {
        double n = rs.getDouble(i);
        if (rs.wasNull()) {
            return null;
        }
        return new Double(n);
    }

    // original getValue() from FieldManager
    public Object base_getValue(String fieldName, ResultSet rs, int i) throws SQLException {
        Object o = rs.getObject(i);
        if (rs.wasNull()) {
            return null;
        }
        // return getDefaultValue();
        return o;
    }

    // moved from ptrDBManager
    /** return the value as a Pointer */
    public Object get_ptrDB_Value(String fieldName, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fieldName, rs, i);
        if (o == null) {
            return o;
        }
        return new SQLPointer(dd.getFieldDefinition(fieldName).getPointedType().getName(), ((Number) o).longValue());
    }

    // moved from intManager
    public Object get_int_Value(String fieldName, ResultSet rs, int i) throws SQLException {
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
    public Object get_char_Value(String fieldName, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fieldName, rs, i);

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
    public Object get_text_Value(String fieldName, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fieldName, rs, i);
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
    public Object get_binary_Value(String fieldName, ResultSet rs, int i) throws SQLException {
        Object o = base_getValue(fieldName, rs, i);

        if (o == null) {
            return o;
        }
        return Text.getText(o);

        /*
         * InputStream is= rs.getBinaryStream(i); if(is==null ) return null; return new Text(is);
         */
    }

    public Object get_boolean_Value(String fieldName, ResultSet rs, int i) throws SQLException {
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
    public Object get_dateTime_Value(String fieldName, ResultSet rs, int i) throws SQLException {
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
    public Object get_nil_Value(String fieldName, ResultSet rs, int i) {
        return null;
    }

    // moved from timeStampManager
    public Object get_timeStamp_Value(String fieldName, ResultSet rs, int i) throws SQLException {
        Object o = rs.getTimestamp(i);
        if (rs.wasNull()) {
            return null;
        }
        return o;
    }

    // moved from FieldManager
    /**
     * ask this field to write write its argument value in a prepared UPDATE SQL statement
     */
    public void setUpdateArgument(String fieldName, PreparedStatement ps, int n, Object o) throws SQLException {
        if (o == getFieldDefinition(fieldName).getNull()) {
            setNullArgument(fieldName, ps, n);
        } else {
            try {
                // System.out.println("UTF: setUpdateArgument");
                setArgument(fieldName, ps, n, o);
            } catch (SQLException e) {
                java.util.logging.Logger.getLogger("org.makumba.db.update.execution").log(
                    java.util.logging.Level.SEVERE, getDBName() + "  " + o.getClass(), e);
                throw e;
            }
        }
    }

    // moved from FieldManager
    /**
     * ask this field to write write its argumment value in a prepared UPDATE SQL statement
     */
    public void setUpdateArgument(String fieldName, PreparedStatement ps, int n, Dictionary<String, Object> d)
            throws SQLException {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._dateCreate:
            case FieldDefinition._ptrIndex:
                throw new RuntimeException("shouldn't be called"); // doesn't go to
                // return in this
                // case
            case FieldDefinition._dateModify:
                nxt(fieldName, d);
                break;
        }
        setUpdateArgument(fieldName, ps, n, d.get(fieldName));
    }

    // moved from FieldManager
    /**
     * Sets a null argument of this type in a prepared SQL statement.<br>
     * Do NOT use this method in queries, only in assignements (INSERT, UPDATE) as some DB types will not support
     * queries with null arguments and instead require an explicit null check via "is null".
     **/
    public void setNullArgument(String fieldName, PreparedStatement ps, int n) throws SQLException {
        ps.setNull(n, getSQLType(fieldName));
    }

    // moved from FieldManager
    /** set a non-null argument of this type in a prepared SQL statement */
    public void setArgument(String fieldName, PreparedStatement ps, int n, Object o) throws SQLException {

        if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._binary) {
            set_binary_Argument(fieldName, ps, n, o);
        } else if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._text
                || getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._char
                || getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._charEnum) {
            // set_binary_Argument(fieldName, ps, n, o);
            set_text_Argument(fieldName, ps, n, o);
        } else {
            ps.setObject(n, toSQLObject(fieldName, o));
        }
    }

    // moved from textManager
    public void set_binary_Argument(String fieldName, PreparedStatement ps, int n, Object o) throws SQLException {

        Text t = Text.getText(o);
        ps.setBinaryStream(n, t.toBinaryStream(), t.length());

    }

    public void set_text_Argument(String fieldName, PreparedStatement ps, int n, Object o) throws SQLException {

        Text t = Text.getText(o);
        ps.setString(n, t.getString());

    }

    // moved from FieldManager
    /** what is the SQL type of this field? */
    protected int getSQLType(String fieldName) {

        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptrOne:
            case FieldDefinition._ptrIndex:
                return get_ptrDB_SQLType(fieldName);
            case FieldDefinition._int:
            case FieldDefinition._intEnum:
                return get_int_SQLType(fieldName);
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
            case FieldDefinition._text:
                return get_char_SQLType(fieldName);
            case FieldDefinition._binary:
                return get_binary_SQLType(fieldName);
            case FieldDefinition._boolean:
                return get_boolean_SQLType(fieldName);
            case FieldDefinition._date:
                return get_dateTime_SQLType(fieldName);
            case FieldDefinition._real:
                return get_real_SQLType(fieldName);
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return get_timeStamp_SQLType(fieldName);
            default:
                throw new RuntimeException("" + fieldName + " should be redefined");
        }
    }

    // moved from ptrDBManager
    public int get_ptrDB_SQLType(String fieldName) {
        return Types.INTEGER;
    }

    // moved from intManager
    protected int get_int_SQLType(String fieldName) {
        return java.sql.Types.INTEGER;
    }

    // moved from charManager
    protected int get_char_SQLType(String fieldName) {
        return java.sql.Types.VARCHAR;
    }

    protected int get_binary_SQLType(String fieldName) {
        return java.sql.Types.LONGVARBINARY;
    }

    protected int get_boolean_SQLType(String fieldName) {
        return java.sql.Types.BIT;
    }

    // moved from dateTimeManager
    public int get_dateTime_SQLType(String fieldName) {
        return java.sql.Types.TIMESTAMP;
    }

    // moved from realManager
    protected int get_real_SQLType(String fieldName) {
        return java.sql.Types.DOUBLE;
    }

    // moved from timeStampManager
    public int get_timeStamp_SQLType(String fieldName) {
        return java.sql.Types.TIMESTAMP;
    }

    // moved from FieldManager
    /** transform the object for a SQL insert or update */
    public Object toSQLObject(String fieldName, Object o) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptrOne:
            case FieldDefinition._ptrIndex:
                return toSQL_ptrDB_Object(fieldName, o);
            case FieldDefinition._date:
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return toSQL_dateTime_Object(fieldName, o);
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
    public Object toSQL_ptrDB_Object(String fieldName, Object o) {
        return new Integer((int) ((Pointer) o).longValue());
    }

    // moved from dateTimeManager
    public Object toSQL_dateTime_Object(String fieldName, Object o) {
        return new Timestamp(((java.util.Date) o).getTime());
    }

    /** the database-level name of the field */
    public String getFieldDBName(String fieldName) {
        return getDatabase().getNameResolver().resolveFieldName(dd, fieldName);
    }

    // moved from FieldManager
    /** ask this field to write its contribution in a SQL CREATE statement */
    public String inCreate(String fieldName, Database d) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return in_char_Create(fieldName, d);
            case FieldDefinition._boolean:
                return in_boolean_Create(fieldName, d);
            case FieldDefinition._ptrIndex:
                return in_primaryKeyCreate(fieldName, d);
            default:
                return base_inCreate(fieldName, d);
        }
    }

    // original inCreate() from FieldManager
    public String base_inCreate(String fieldName, Database d) {
        return getFieldDBName(fieldName) + " " + this.getFieldDBType(fieldName, d);
    }

    // moved from charManager
    /** write in CREATE, in the form name char[size] */
    public String in_char_Create(String fieldName, Database d) {
        String s = Database.getEngineProperty(d.getEngine() + ".charBinary");
        if (s != null && s.equals("true")) {
            s = " BINARY";
        } else {
            s = "";
        }
        // should width be computed by getDBType() instead?
        return getFieldDBName(fieldName) + " " + getFieldDBType(fieldName, d) + "("
                + getFieldDefinition(fieldName).getWidth() + ")" + s;
        // return
        // super.inCreate(d)+"("+getFieldDefinition(fieldName).getWidth()()+")"+s;
    }

    /** write in CREATE, in the form name BIT(1) */
    public String in_boolean_Create(String fieldName, Database d) {
        return getFieldDBName(fieldName) + " " + getFieldDBType(fieldName, d) + "(1)";
    }

    // moved from FieldManager
    /**
     * ask this field to write its argument placeholder in a prepared UPDATE SQL statement
     */
    public String inPreparedUpdate(String fieldName) {
        return getFieldDBName(fieldName) + "=?";
    }

    // moved from FieldManager
    /** what is the database level type of this field? */
    protected String getFieldDBType(String fieldName) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._ptr:
            case FieldDefinition._ptrRel:
            case FieldDefinition._ptrOne:
            case FieldDefinition._ptrIndex:
                return get_ptrDB_FieldDBType(fieldName);
            case FieldDefinition._int:
            case FieldDefinition._intEnum:
                return get_int_FieldDBType(fieldName);
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return get_char_FieldDBType(fieldName);
            case FieldDefinition._text:
                return get_text_FieldDBType(fieldName);
            case FieldDefinition._binary:
                return get_binary_FieldDBType(fieldName);
            case FieldDefinition._boolean:
                return get_boolean_FieldDBType(fieldName);
            case FieldDefinition._date:
                return get_dateTime_FieldDBType(fieldName);
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return get_timeStamp_FieldDBType(fieldName);
            case FieldDefinition._real:
                return get_real_FieldDBType(fieldName);
            default:
                throw new RuntimeException("" + fieldName + " should be redefined");
        }
    }

    // moved from ptrDBManager
    /** returns INT */
    protected String get_ptrDB_FieldDBType(String fieldName) {
        return "INTEGER";
    }

    // moved from intManager
    /** Use standard SQL name, unless defined otherwise in sqlEngines.properties. */
    protected String get_int_FieldDBType(String fieldName) {
        return "INTEGER"; // standard name
    }

    // moved from charManager
    /** returns char */
    protected String get_char_FieldDBType(String fieldName) {
        return "VARCHAR";
    }

    // moved from textManager
    /** returns text */
    protected String get_text_FieldDBType(String fieldName) {
        return "LONGTEXT";
    }

    /** returns text */
    protected String get_binary_FieldDBType(String fieldName) {
        return "LONG VARBINARY";
    }

    protected String get_boolean_FieldDBType(String fieldName) {
        return "BIT";
    }

    // moved from dateTimeManager
    /** returns datetime */
    protected String get_dateTime_FieldDBType(String fieldName) {
        return "DATETIME";
    }

    // moved from realManager
    /** Use standard SQL name, unless defined otherwise in sqlEngines.properties. */
    protected String get_real_FieldDBType(String fieldName) {
        return "DOUBLE PRECISION"; // standard name
    }

    // moved from timeStampManager
    /** returns timestamp */
    protected String get_timeStamp_FieldDBType(String fieldName) {
        return "TIMESTAMP";
    }

    // moved from FieldManager
    /** what is the database level type of this field? */
    protected String getFieldDBType(String fieldName, Database d) {
        String s = Database.getEngineProperty(d.getEngine() + "." + getFieldDefinition(fieldName).getDataType());
        if (s == null) {
            return getFieldDBType(fieldName);
        }
        return s;
    }

    // moved from FieldManager
    /**
     * Ask this field how to name the index on this field. Normally called from manageIndexes().
     */
    public String getFieldDBIndexName(String fieldName) {
        // return rm.getDBName()+"_"+getDBName();
        return getFieldDBName(fieldName);
    }

    // moved from FieldManager
    /**
     * ask this field to write write its argument placeholder ('?') in a prepared INSERT SQL statement
     */
    public String inPreparedInsert(String fieldName) {
        return "?";
    }

    // moved from FieldManager, adapted to dateCreateJavaManager,
    // dateModifyJavaManager and ptrIndexJavaManager
    /**
     * ask this field to write write its argumment value in a prepared INSERT SQL statement
     */
    public void setInsertArgument(String fieldName, PreparedStatement ps, int n, Dictionary<String, Object> d)
            throws SQLException {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                if (d.get(fieldName) == null) {
                    nxt(fieldName, d);
                }
                set_timeStamp_InsertArgument(fieldName, ps, n, d);
                break;
            case FieldDefinition._ptrIndex:
                // this is not executed on autoIncrement
                org.makumba.Pointer p = (org.makumba.Pointer) d.get(fieldName);
                if (p != null) {
                    base_setInsertArgument(fieldName, ps, n, d);
                    if (p.getDbsv() == dbsv && p.longValue() > this.primaryKeyCurrentIndex) {
                        this.primaryKeyCurrentIndex = p.longValue();
                    }
                    return;
                }
                ps.setInt(n, (int) nxt_ptrIndex(fieldName, d).longValue());
                break;
            default:
                base_setInsertArgument(fieldName, ps, n, d);
        }
    }

    // original setInsertArgument from FieldManager
    public void base_setInsertArgument(String fieldName, PreparedStatement ps, int n, Dictionary<String, Object> d)
            throws SQLException {
        Object o = d.get(fieldName);
        if (o == null || o.equals(getFieldDefinition(fieldName).getNull())) {
            setNullArgument(fieldName, ps, n);
        } else {
            setArgument(fieldName, ps, n, o);
        }
    }

    // moved from timeStampManager
    public void set_timeStamp_InsertArgument(String fieldName, PreparedStatement ps, int n,
            java.util.Dictionary<String, Object> d) throws SQLException {
        Object o = d.get(fieldName);
        if (o instanceof java.util.Date && !(o instanceof Timestamp)) {
            d.put(fieldName, new Timestamp(((java.util.Date) o).getTime()));
        }
        base_setInsertArgument(fieldName, ps, n, d);
    }

    // moved from FieldManager
    /**
     * ask this field to write write its argument value in a prepared SQL statement for copying
     */
    public void setCopyArgument(String fieldName, PreparedStatement ps, int n, Dictionary<String, Object> d)
            throws SQLException {
        try {
            Object o = d.get(fieldName);
            if (o == null || o.equals(getFieldDefinition(fieldName).getNull())) {
                setNullArgument(fieldName, ps, n);
            } else {
                setArgument(fieldName, ps, n, o);
            }
        } catch (Exception e) {
            throw new RuntimeException(fieldName + " " + e.getMessage());
        }
    }

    // moved from FieldManager
    /**
     * ask this field to write its contribution in a SQL UPDATE statement should return "" if this field doesn't want to
     * take part in the update
     */
    public String inCondition(String fieldName, Dictionary<String, Object> d, String cond) {
        return getDBName() + cond + writeConstant(fieldName, d.get(fieldName));
    }

    // moved from FieldManager
    /** ask this field to write its contribution in a SQL CREATE statement */
    // public String inCreate(){ return getDBName()+" "+getDBType(null);}
    // moved from FieldManager
    /** ask this field to write a value of its type in a SQL statement */
    public String writeConstant(String fieldName, Object o) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return write_char_Constant(fieldName, o);
            case FieldDefinition._text:
                return write_text_Constant(fieldName, o);
            case FieldDefinition._binary:
                return write_binary_Constant(fieldName, o);
            case FieldDefinition._boolean:
                return write_boolean_Constant(fieldName, o);
            case FieldDefinition._date:
                return write_dateTime_Constant(fieldName, o);
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                return write_timeStamp_Constant(fieldName, o);
            default:
                if (o == getFieldDefinition(fieldName).getNull()) {
                    return "null";
                }
                return toSQLObject(fieldName, o).toString();
        }
    }

    // original writeConstant from FieldManager
    public String base_writeConstant(String fieldName, Object o) {
        if (o == getFieldDefinition(fieldName).getNull()) {
            return "null";
        }
        return toSQLObject(fieldName, o).toString();
    }

    // moved from charHandler
    /** does apostrophe escape */
    public String write_char_Constant(String fieldName, Object o) {
        return org.makumba.db.makumba.sql.Database.SQLEscape(o.toString());
    }

    // moved from textManager
    /** does apostrophe escape */
    public String write_text_Constant(String fieldName, Object o) {
        return org.makumba.db.makumba.sql.Database.SQLEscape(o.toString());
    }

    /** does apostrophe escape */
    public String write_binary_Constant(String fieldName, Object o) {
        return org.makumba.db.makumba.sql.Database.SQLEscape(o.toString());
    }

    public String write_boolean_Constant(String fieldName, Object o) {
        return (Boolean) o ? "1" : "0";
    }

    // moved from dateTimeManager
    /** writes the date between apostrophes */
    public String write_dateTime_Constant(String fieldName, Object o) {
        return "\'" + new Timestamp(((java.util.Date) o).getTime()) + "\'";
        // "\'"+super.writeConstant(o)+"\'";
    }

    // moved from timeStampManager
    /** writes the date between apostrophes */
    public String write_timeStamp_Constant(String fieldName, Object o) {
        return "\'" + base_writeConstant(fieldName, o) + "\'";
    }

    // moved from FieldManager
    /** what is the property of the current engine? */
    protected String getEngineProperty(String fieldName, String s) {
        Database d = getSQLDatabase();
        return Database.getEngineProperty(d.getEngine() + "." + s);
    }

    // moved from FieldManager
    /** ask this field to perform actions when the table is open */
    public void onStartup(String fieldName, Properties config, SQLDBConnection dbc) throws SQLException {
        if (alter && shouldIndex(fieldName)) {
            manageIndexes(fieldName, dbc);
        }

        // getFieldDefinition(fieldName).is

        // getFieldDBName(fieldName)

        if (shouldIndex(fieldName)) {
            extraIndexes.remove(getFieldDBIndexName(fieldName).toLowerCase());
        }

        checkDuplicate.put(fieldName, "SELECT 1 FROM " + getDBName() + " WHERE " + getFieldDBName(fieldName) + "=?");
        checkNullDuplicate.put(fieldName, "SELECT 1 FROM " + getDBName() + " WHERE " + getFieldDBName(fieldName)
                + " is null");
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._ptrIndex:
                if (!getSQLDatabase().isAutoIncrement()) {
                    dbsv = getSQLDatabase().getDbsv();
                    Statement st = dbc.createStatement();
                    // System.out.println("\t\t** Checking keys " + getDBName() + " " + fieldName);
                    resetPrimaryKey();
                    ResultSet rs = st.executeQuery("SELECT MAX(" + getFieldDBName(fieldName) + "), COUNT("
                            + getFieldDBName(fieldName) + ") FROM " + tbname + " WHERE " + getFieldDBName(fieldName)
                            + ">=" + primaryKeyCurrentIndex + " AND " + getFieldDBName(fieldName) + "<="
                            + getSQLDatabase().getMaxPointerValue());
                    rs.next();
                    if (rs.getLong(2) > 0) {
                        primaryKeyCurrentIndex = rs.getLong(1);
                    }
                    // System.out.println("\t\t\tprimaryKeyCurrentIndex: " + primaryKeyCurrentIndex);
                    rs.close();
                    st.close();
                }
        }
    }

    /**
     * called at table open. determines the maximum index with this database's dbsv public void onStartup(String
     * fieldName, TableManager rm, java.util.Properties p, SQLDBConnection dbc) throws SQLException {
     * super.onStartup(fieldName, p, dbc); }
     */

    // moved from FieldManager
    /** Tell whether this type of field should be indexed. */
    public boolean shouldIndex(String fieldName) {
        if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._text
                || getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._binary) {
            return should_text_Index(fieldName);
        } else {
            return true;
        }
    }

    // moved from textManager
    public boolean should_text_Index(String fieldName) {
        return false;
    }

    // moved from FieldManager
    /** Examine DB indexes. */
    public boolean isIndexOk(String fieldName) {
        Boolean b = indexes.get(getFieldDBIndexName(fieldName).toLowerCase());
        if (b != null) {
            return getFieldDefinition(fieldName).isUnique() == !b.booleanValue();
        }
        return false;
    } // end isIndexOk()

    public boolean hasForeignKey(String fieldName) {
        return foreignKeys.get(getFieldDBIndexName(fieldName).toLowerCase()) != null;
    }

    public boolean isIndexOk(String[] fieldNames) {
        Boolean b = indexes.get(StringUtils.concatAsString(fieldNames).toLowerCase());
        if (b != null) {
            return getDataDefinition().hasMultiUniqueKey(fieldNames);
        }
        return false;
    }

    // moved from FieldManager
    /**
     * Ask this field to add/remove indexes as needed, normally called from onStartup().
     */
    public void manageIndexes(String fieldName, SQLDBConnection dbc) throws SQLException {
        // String keyName = getFieldDBIndexName(fieldName);
        String brief = getDataDefinition().getName() + "#" + fieldName + " ("
                + getFieldDefinition(fieldName).getDescription() + ")";

        if (getDatabase().usesHibernateIndexes()) { // if we use hibernate and we are allowed to change the table
            // FIXME: this will have to be done in another step, before hibernate schema update
            dropIndex(fieldName, dbc, "RESIDUAL MAKUMBA INDEX DROPPED on " + brief); // we drop the index
            return;
        }

        if (!isIndexOk(fieldName)) {
            // org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
            // "ALTERING INDEX on field "+getName()+" of
            // "+rm.getRecordInfo().getName() );

            dropIndex(fieldName, dbc, "INDEX DROPPED on " + brief);

            boolean createNormalEvenIfUnique = false;

            if (getFieldDefinition(fieldName).isUnique()) {
                try {
                    // try creating unique index
                    Statement st = dbc.createStatement();
                    st.executeUpdate(indexCreateUniqueSyntax(fieldName));
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                        "UNIQUE INDEX ADDED on " + brief);
                    st.close();
                    indexCreated(dbc);
                } catch (SQLException e) {
                    // log all errors
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                    // rm.getDatabase().getConfiguration()+": "+ //DB name
                        "Problem adding UNIQUE INDEX on " + brief + ": " + e.getMessage() + " [ErrorCode: "
                                + e.getErrorCode() + ", SQLstate:" + e.getSQLState() + "]");
                    createNormalEvenIfUnique = true;
                }
            }

            if (createNormalEvenIfUnique || !getFieldDefinition(fieldName).isUnique()) {
                try {
                    // create normal index
                    Statement st = dbc.createStatement();
                    st.executeUpdate(indexCreateSyntax(fieldName));
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                        "INDEX ADDED on " + brief);
                    st.close();
                } catch (SQLException e) {
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                    // rm.getDatabase().getConfiguration()+": "+ //DB name
                        "Problem adding INDEX on " + brief + ": " + e.getMessage() + " [ErrorCode: " + e.getErrorCode()
                                + ", SQLstate:" + e.getSQLState() + "]");
                }
            }

        }// isIndexOk

        if (org.makumba.db.makumba.sql.Database.supportsForeignKeys()) {
            manageForeignKeys(fieldName, dbc, brief);
        }
    }// method

    public void manageForeignKeys(String fieldName, SQLDBConnection dbc, String brief) throws DBError {
        // for foreign keys

        String type = getFieldDefinition(fieldName).getType();
        if ((type.equals("ptr") || type.equals("ptrOne") || type.equals("ptrRel")) && !hasForeignKey(fieldName)) {
            // System.out.println("We need a foreign key for " + brief);

            try {
                // try creating foreign key index
                Statement st = dbc.createStatement();

                String fkTableName = getFieldDefinition(fieldName).getPointedType().getName();
                String fkFieldName = getFieldDefinition(fieldName).getPointedType().getIndexPointerFieldName();

                if (type.equals("ptrOne")) {
                    fkTableName = getFieldDefinition(fieldName).getSubtable().getName();
                    fkFieldName = getFieldDefinition(fieldName).getSubtable().getIndexPointerFieldName();
                }

                // System.out.println("testing: "+foreignKeyCreateSyntax(fieldName,
                // getFieldDefinition(fieldName).getPointedType().getName(),
                // getFieldDefinition(fieldName).getPointedType().getIndexPointerFieldName()));
                st.executeUpdate(foreignKeyCreateSyntax(fieldName, fkTableName, fkFieldName));
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                    "FOREIGN KEY ADDED on " + brief);
                st.close();
                indexCreated(dbc);
            } catch (SQLException e) {
                // log all errors
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                // rm.getDatabase().getConfiguration()+": "+ //DB name
                    "Problem adding FOREIGN KEY on " + brief + ": " + e.getMessage() + " [ErrorCode: "
                            + e.getErrorCode() + ", SQLstate:" + e.getSQLState() + "]");
                throw new DBError("Error adding foreign key for " + brief + ": " + e.getMessage());
            }

        }
    }

    private void dropIndex(String fieldName, SQLDBConnection dbc, String message) {
        String syntax = indexDropSyntax(fieldName);
        try { // drop the old, wrong index if it exists
            Statement st = dbc.createStatement();
            st.executeUpdate(syntax);
            java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(message);
            st.close();
        } catch (SQLException e) {
            treatIndexException(e, syntax, dbc);
        }

    }

    // moved from FieldManager
    /** Syntax for index creation. */
    public String indexCreateSyntax(String fieldName) {
        return "CREATE INDEX " + getFieldDBIndexName(fieldName) + " ON " + getDBName() + " ("
                + getFieldDBName(fieldName) + ")";
    }

    // moved from FieldManager
    /** Syntax for unique index creation. */
    public String indexCreateUniqueSyntax(String fieldName) {
        return "CREATE UNIQUE INDEX " + getFieldDBIndexName(fieldName) + " ON " + getDBName() + " ("
                + getFieldDBName(fieldName) + ")";
    }

    /** Syntax for unique index creation. */
    public String foreignKeyCreateSyntax(String fieldName, String fkTableName, String fkFieldName) {
        return "ALTER TABLE " + getDBName() + " ADD FOREIGN KEY "
                + shortIndexName(((TableManager) getDatabase().getTable(fkTableName)).getDBName(), fieldName) + " ("
                + getFieldDBName(fieldName) + ") REFERENCES "
                + ((TableManager) getDatabase().getTable(fkTableName)).getDBName() + " ("
                + ((TableManager) getDatabase().getTable(fkTableName)).getFieldDBName(fkFieldName) + ")";
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
    public String indexCreateUniqueSyntax(String[] fieldNames) {
        String[] dbs = new String[fieldNames.length];
        for (int i = 0; i < dbs.length; i++) {
            dbs[i] = getFieldDBName(fieldNames[i]);
        }
        String dbFieldNames = StringUtils.toString(dbs, false);
        return "CREATE UNIQUE INDEX " + StringUtils.concatAsString(fieldNames) + " ON " + getDBName() + " ("
                + dbFieldNames + ")";
    }

    // moved from FieldManager
    /** Syntax for dropping index. */
    public String indexDropSyntax(String fieldName) {
        return "DROP INDEX " + getFieldDBIndexName(fieldName) + " ON " + getDBName();
    }

    // moved from FieldManager
    /**
     * set the java value in a data chunk. If the value in the recordset is SQL null, a NullPointerException is thrown
     */
    public void setValue(String fieldName, Dictionary<String, Object> d, ResultSet rs, int i) throws SQLException {
        Object o = getValue("", rs, i);
        if (o != null) {
            d.put(fieldName, o);
        } else {
            d.remove(fieldName);
        }
    }

    // moved from FieldManager
    /**
     * set the java value in a data chunk. If the value in the recordset is SQL null, a NullPointerException is thrown
     */
    public void setValue(String fieldName, Object[] data, ResultSet rs, int i) throws SQLException {
        data[i] = getValue("", rs, i);
    }

    // moved from FieldManager
    protected void checkCopy(String fieldName, String s) {
        if (!admin) {
            throw new org.makumba.InvalidValueException(getFieldDefinition(fieldName), "you cannot insert an " + s
                    + " field unless the type " + getDataDefinition().getName()
                    + " has administration approval in the database connection file");
        }
    }

    // moved from FieldManager
    /**
     * return whether there was a duplicate for this field when inserting the given data
     */
    public boolean checkDuplicate(String fieldName, DBConnection dbc, Dictionary<String, Object> data) {
        if (!getFieldDefinition(fieldName).isUnique()) {
            return false;
        }
        Object val = data.get(fieldName);
        SQLDBConnection sqlDbc = (SQLDBConnection) dbc;
        PreparedStatement ps;
        if (val == null) {
            ps = sqlDbc.getPreparedStatement(checkNullDuplicate.get(fieldName));
        } else {
            ps = sqlDbc.getPreparedStatement(checkDuplicate.get(fieldName));
        }
        try {
            if (val != null) {
                setUpdateArgument(fieldName, ps, 1, val);
            }
            return ps.executeQuery().next();
        } catch (SQLException se) {
            Database.logException(se, sqlDbc);
            throw new org.makumba.DBError(se, checkDuplicate.get(fieldName));
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                throw new org.makumba.DBError(e);
            }
        }
    }

    /**
     * return whether there was a duplicate entry for this multi-field combination when inserting the given data
     */
    public boolean checkDuplicate(String[] fields, Object values[], DBConnection dbc) {

        SQLDBConnection sqlDbc = (SQLDBConnection) dbc;
        Vector<Integer> nullIndexes = new Vector<Integer>();

        String query = "SELECT 1 FROM " + getDBName() + " WHERE ";
        for (int j = 0; j < fields.length; j++) {
            if (values[j] == null) {
                nullIndexes.add(j);
            }
            query += getFieldDBName(fields[j]) + (values[j] != null ? "=?" : " is null");
            if (j + 1 < fields.length) {
                query += " AND ";
            }
        }

        PreparedStatement ps = sqlDbc.getPreparedStatement(query);
        try {
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    // shift the argument index according to how many arguments have been replaced with "is null"
                    int k = 0;
                    for (Integer a : nullIndexes) {
                        if (a < i) {
                            k++;
                        }
                    }
                    setUpdateArgument(fields[i], ps, i + 1 - k, values[i]);
                }
            }
            return ps.executeQuery().next();
        } catch (SQLException se) {
            Database.logException(se, sqlDbc);
            throw new org.makumba.DBError(se, StringUtils.toString(fields));
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                throw new org.makumba.DBError(e);
            }
        }

    }

    /**
     * Checks for potential duplicates for multi-field unique keys that span over more than one data definition. There
     * is no equivalent for this on the database level, so we need to check this before we insert, in
     * {@link #checkInsert(Dictionary, Dictionary, Dictionary)}.
     * 
     * @return true if an entry for the given key already exists with these values
     */
    public boolean findMultiFieldMultiTableDuplicates(Pointer pointer,
            DataDefinition.MultipleUniqueKeyDefinition definition, Object values[], SQLDBConnection dbc) {

        String[] fields = definition.getFields();
        String from = getDBName();
        String where = "";
        Vector<Integer> nullIndexes = new Vector<Integer>();

        // for unique keys that go over subfields, we need to construct a query with joins
        String projection = dd.getName().replace('.', '_'); // label of this table
        from += " " + projection; // we need to use the labels, as we might have fields with the same name
        Vector<String> alreadyAdded = new Vector<String>(); // we store what tables we already added to the projection
        for (int i = 0; i < fields.length; i++) {
            if (values[i] == null) {
                nullIndexes.add(i);
            }
            if (fields[i].indexOf(".") != -1) { // FIXME: we go only one level of "." here, there might be more
                // do the projection
                String subField = fields[i].substring(0, fields[i].indexOf("."));
                String fieldName = fields[i].substring(fields[i].indexOf(".") + 1);
                DataDefinition pointedType = dd.getFieldDefinition(subField).getPointedType();
                TableManager otherTable = (TableManager) getDatabase().getTable(pointedType);
                String otherProjection = pointedType.getName().replace('.', '_');

                if (!alreadyAdded.contains(subField)) { // if this is a new table
                    // we add the projection & make the join
                    from += ", " + otherTable.getDBName() + " " + otherProjection;
                    where += projection + "." + getFieldDBName(subField) + "=" + otherProjection + "."
                            + otherTable.getFieldDBName(pointedType.getIndexPointerFieldName()) + " AND ";
                    alreadyAdded.add(subField);
                }

                // in any case, we match the tables on the fields.
                where += otherProjection + "." + otherTable.getFieldDBName(fieldName)
                        + (values[i] != null ? "=?" : " is null");
                if (i + 1 < fields.length) {
                    where += " AND ";
                }
            }
        }

        // if we have a pointer, we are in editing mode --> we make the query to not consider our record
        if (pointer != null) {
            where += " AND " + projection + "." + getFieldDBName(dd.getIndexPointerFieldName()) + "<>"
                    + pointer.getUid();
        }

        String query = "SELECT 1 FROM " + from + " WHERE " + where;// put it all together
        PreparedStatement ps = dbc.getPreparedStatement(query);
        try {
            // now we need to set the parameters for the query
            for (int i = 0; i < fields.length; i++) {
                int n = i + 1;
                if (fields[i].indexOf(".") != -1) { // is it a field in a different table
                    String subField = fields[i].substring(0, fields[i].indexOf("."));
                    String fieldName = fields[i].substring(fields[i].indexOf(".") + 1);
                    DataDefinition pointedType = dd.getFieldDefinition(subField).getPointedType();
                    // then we use the table manager of that table to set the value
                    TableManager otherTable = (TableManager) getDatabase().getTable(pointedType);
                    if (values[i] != null) {
                        // shift the argument index according to how many arguments have been replaced with "is null"
                        int k = 0;
                        for (Integer a : nullIndexes) {
                            if (a < i) {
                                k++;
                            }
                        }
                        otherTable.setUpdateArgument(fieldName, ps, n - k, values[i]);
                    }

                } else { // otherwise we use this table manager
                    if (values[i] != null) {
                        setUpdateArgument(fields[i], ps, n, values[i]);
                    }
                }
            }
            return ps.executeQuery().next();
        } catch (SQLException se) {
            Database.logException(se, dbc);
            throw new org.makumba.DBError(se, StringUtils.toString(fields));
        } finally {
            try {
                ps.close();
            } catch (SQLException e) {
                throw new org.makumba.DBError(e);
            }
        }

    }

    // moved from FieldManager
    /**
     * check if the column from the SQL database (read from the catalog) still corresponds with the abstract definition
     * of this field
     */
    protected boolean unmodified(String fieldName, int type, int size, Vector<Hashtable<String, Object>> columns,
            int index) throws SQLException {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._char:
            case FieldDefinition._charEnum:
                return unmodified_char(fieldName, type, size, columns, index);
            case FieldDefinition._ptrIndex:
                return unmodified_primaryKey(fieldName, type, size, columns, index);
            default:
                return base_unmodified(fieldName, type, size, columns, index);
        }
    }

    // original unmodified() from FieldManager
    protected boolean base_unmodified(String fieldName, int type, int size, Vector<Hashtable<String, Object>> columns,
            int index) throws SQLException {
        return type == getSQLType(fieldName);
    }

    private boolean unmodified_primaryKey(String fieldName, int type, int size,
            Vector<Hashtable<String, Object>> columns, int index) throws SQLException {
        if (!base_unmodified(fieldName, type, size, columns, index)) {
            return false;
        }
        if (!getSQLDatabase().isAutoIncrement() && !getDatabase().usesHibernateIndexes()) {
            return true;
        }
        boolean unmod = unmodifiedAutoIncrement(columns.elementAt(index - 1));
        autoIncrementAlter = !unmod;
        return unmod;
    }

    private boolean unmodifiedAutoIncrement(Hashtable<String, Object> column) {
        // this is a hack. we know that auto_increment is always not null, and we take advantage of makumba having
        // created _nullable_ primary keys before.
        return "NO".equals(column.get("IS_NULLABLE"));

    }

    private String in_primaryKeyCreate(String fieldName, Database d) {
        // FIXME: primary keys will have to be made in another step, before hibernate schema update
        if (getSQLDatabase().isAutoIncrement() || getDatabase().usesHibernateIndexes()) {
            return base_inCreate(fieldName, d) + " " + getCreateAutoIncrementSyntax();
        } else {
            return base_inCreate(fieldName, d);
        }
    }

    // moved from charManager
    /**
     * Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this
     * returns true. Some SQL drivers allocate more anyway.
     */
    protected boolean unmodified_char(String fieldName, int type, int size,
            java.util.Vector<Hashtable<String, Object>> columns, int index) throws SQLException {
        return (base_unmodified(fieldName, type, size, columns, index) || type == java.sql.Types.CHAR)
                && check_char_Width(fieldName, size);
    }

    // moved from wrapperManager
    /**
     * check if the column from the SQL database still coresponds with the abstract definition of this field
     */
    protected boolean unmodified_wrapper(String fieldName, int type, int size,
            java.util.Vector<Hashtable<String, Object>> v, int index) throws SQLException {
        return base_unmodified(fieldName, type, size, v, index);
    }

    // moved from charManager
    /** check the char width */
    protected boolean check_char_Width(String fieldName, ResultSetMetaData rsm, int index) throws SQLException {
        // some drivers might allocate more, it's their business
        return rsm.getColumnDisplaySize(index) >= getFieldDefinition(fieldName).getWidth();
    }

    // moved from charManager
    /** check the char width */
    protected boolean check_char_Width(String fieldName, int width) throws SQLException {
        // some drivers might allocate more, it's their business
        return width >= getFieldDefinition(fieldName).getWidth();
    }

    // moved from ptrIndexJavaManager
    protected void resetPrimaryKey() {
        primaryKeyCurrentIndex = getSQLDatabase().getMinPointerValue();
    }

    // moved from dateCreateJavaManager and dateModifyJavaManager
    void nxt(String fieldName, Dictionary<String, Object> d) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._dateCreate:
                d.put(fieldName, d.get(dd.getLastModificationDateFieldName()));
                break;
            case FieldDefinition._dateModify:
                d.put(fieldName, new Timestamp(new java.util.Date().getTime()));
                break;
        }
    }

    // moved from ptrIndexJavaManager
    public SQLPointer nxt_ptrIndex(String fieldName, Dictionary<String, Object> d) {
        SQLPointer i = new SQLPointer(dd.getName(), nextId_ptrIndex());
        d.put(fieldName, i);
        return i;
    }

    // moved from ptrIndexJavaManager
    /** determines the unique index by incrementing a counter */
    protected synchronized long nextId_ptrIndex() {
        return ++primaryKeyCurrentIndex;
    }

    /**
     * Checks if a set of values can be inserted in the database
     * 
     * @param fieldsToCheck
     *            the values to be checked
     * @param fieldsToIgnore
     *            the values of toCheck not to be checked
     * @param allFields
     *            the entire data to be inserted
     */
    @Override
    public void checkInsert(Dictionary<String, Object> fieldsToCheck, Dictionary<String, DataHolder> fieldsToIgnore,
            Dictionary<String, Object> allFields) {
        for (String string : dd.getFieldNames()) {
            String name = string;
            if (fieldsToIgnore.get(name) == null) {
                Object o = fieldsToCheck.get(name);
                if (o != null) {

                    boolean isDateCreate = getFieldDefinition(name).getIntegerType() == FieldDefinition._dateCreate;
                    boolean isDataModify = getFieldDefinition(name).getIntegerType() == FieldDefinition._dateModify;
                    boolean isPtrIndex = getFieldDefinition(name).getIntegerType() == FieldDefinition._ptrIndex;

                    if (isDateCreate || isDataModify || isPtrIndex) {
                        checkCopyRights(name);
                    } else {
                        getFieldDefinition(name).checkInsert(fieldsToCheck);
                    }

                    fieldsToCheck.put(name, getFieldDefinition(name).checkValue(o));
                }
            }
        }

        // check multi-field multi-table uniqueness
        checkMultiFieldMultiTableUniqueness(null, allFields);
    }

    /**
     * Checks if a set of values can be updated in the database
     * 
     * @param pointer
     *            the pointer to the record to be updated
     * @param fieldsToCheck
     *            the values to be checked
     * @param fieldsToIgnore
     *            the values of toCheck not to be checked
     * @param allFields
     *            the entire data to be inserted
     */
    @Override
    public void checkUpdate(Pointer pointer, Dictionary<String, Object> allFields) {

        // check multi-field key uniqueness that span over more than one table
        checkMultiFieldMultiTableUniqueness(pointer, allFields);
    }

    private void checkCopyRights(String fieldName) {
        switch (getFieldDefinition(fieldName).getIntegerType()) {
            case FieldDefinition._dateCreate:
                checkCopy(fieldName, "creation date");
                break;
            case FieldDefinition._dateModify:
                checkCopy(fieldName, "modification date");
                break;
            case FieldDefinition._ptrIndex:
                checkCopy(fieldName, "index");
                break;
        }
    }

    // moved from timeStampManager
    public Object check_timeStamp_ValueImpl(String fieldName, Object value) {
        Object o = getFieldDefinition(fieldName).checkValue(value);
        if (o instanceof java.util.Date && !(o instanceof Timestamp)) {
            o = new Timestamp(((java.util.Date) o).getTime());
        }
        return o;
    }

    /**
     * Checks all mult-field unique indices that span over more than one table. Other unique indices will be checked by
     * the database, and we just need to find them if something fails
     * {@link #findDuplicates(SQLDBConnection, Dictionary)}.
     */
    private void checkMultiFieldMultiTableUniqueness(Pointer pointer, Dictionary<String, Object> fullData)
            throws CompositeValidationException {

        DBConnectionWrapper dbcw = (DBConnectionWrapper) getSQLDatabase().getDBConnection();
        SQLDBConnection dbc = (SQLDBConnection) dbcw.getWrapped();

        // we use a try to make sure that in any case in the end, our connection gets closed
        try {
            MultipleUniqueKeyDefinition[] multiFieldUniqueKeys = getDataDefinition().getMultiFieldUniqueKeys();
            // Hashtable<Object, Object> duplicates = new Hashtable<Object, Object>();
            CompositeValidationException notUnique = new CompositeValidationException();
            for (MultipleUniqueKeyDefinition key : multiFieldUniqueKeys) {
                String[] fields = key.getFields();
                Object[] values = new Object[fields.length];
                if (key.isKeyOverSubfield()) {
                    for (int j = 0; j < fields.length; j++) {
                        values[j] = fullData.get(fields[j]);
                    }
                    if (findMultiFieldMultiTableDuplicates(pointer, key, values, dbc)) {
                        notUnique.addException(new NotUniqueException(key.getFields()[0], key.getErrorMessage(values)));
                        // duplicates.put(fields, values);
                    }
                }
            }
            notUnique.throwCheck();

        } catch (Exception e) {
            if (e instanceof CompositeValidationException) {
                throw (CompositeValidationException) e;
            } else {
                throw new RuntimeWrappedException(e);
            }
        } finally {
            dbcw.close();
        }

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

    protected String getQueryAutoIncrementSyntax() {
        return null;
    }

    protected String getCreateAutoIncrementSyntax() {
        return null;
    }

}
