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
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import org.makumba.CompositeValidationException;
import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.DataDefinition.MultipleUniqueKeyDefinition;
import org.makumba.FieldDefinition;
import org.makumba.FieldDefinition.FieldErrorMessageType;
import org.makumba.MakumbaError;
import org.makumba.NotUniqueException;
import org.makumba.Pointer;
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
        }
    }

    @Override
    public void close() {
        // FIXME we should maybe do more things here, for now, we only reset the primary key index
        resetPrimaryKey();

    }

    /** the SQL table opening. might call create() or alter() */
    protected void setTableAndFieldNames(NameResolver nr) {

        tbname = getSQLDatabase().getDBName(dd);

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

        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            if (fd.getType().startsWith("set")) {
                continue;
            }
            onStartup(fd, config, dbc);
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
                    st.executeUpdate(getSQLDatabase().indexCreateUniqueSyntax(dd, fieldNames));
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                        "INDEX ADDED on " + briefMulti);
                    st.close();
                    getSQLDatabase().indexCreated(dbc);
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
        fieldList(sb, dd.getFieldDefinitions().iterator());
        handlerList = sb.toString();

        // initialises list of fields without PK as we have auto-increment
        sb = new StringBuffer();
        Iterator<FieldDefinition> e = dd.getFieldDefinitions().iterator();
        e.next();
        fieldList(sb, e);
        handlerListAutoIncrement = sb.toString();

        indexField = dd.getIndexPointerFieldName();
        indexDBField = getSQLDatabase().getFieldDBName(dd.getFieldDefinition(indexField));
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

        boolean checkColumn(FieldDefinition fd) throws SQLException;

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

        @Override
        public boolean shouldCreate() {
            return columns == null;
        }

        @Override
        public boolean hasMoreColumns() throws SQLException {
            if (i < columns.size()) {
                column = columns.elementAt(i);
                i++;
                return true;
            }
            return false;
        }

        @Override
        public String columnName() throws SQLException {
            return (String) column.get("COLUMN_NAME");
        }

        @Override
        public int columnType() throws SQLException {
            return ((Integer) column.get("DATA_TYPE")).intValue();
        }

        public int columnSize() throws SQLException {
            return ((Integer) column.get("COLUMN_SIZE")).intValue();
        }

        @Override
        public String columnTypeName() throws SQLException {
            return (String) column.get("TYPE_NAME");
        }

        @Override
        public boolean checkColumn(FieldDefinition fd) throws SQLException {
            boolean unmod = getSQLDatabase().unmodified(fd, columnType(), columnSize(), columns, i);
            if (fd.getIntegerType() == FieldDefinition._ptrIndex) {
                autoIncrementAlter = !unmod;
            }
            return unmod;
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
        getSQLDatabase().onAlter(dd, dbc, alter);
        Vector<String> present = new Vector<String>();
        Vector<String> add = new Vector<String>();
        Vector<String> modify = new Vector<String>();
        Vector<String> drop = new Vector<String>();
        Object withness = new Object();

        while (cs.hasMoreColumns()) {
            String dbfn = cs.columnName();
            boolean found = false;
            for (FieldDefinition fd : dd.getFieldDefinitions()) {
                if (fd.getType().startsWith("set")) {
                    continue;
                }
                if (getSQLDatabase().getFieldDBName(fd).toLowerCase().equals(dbfn.toLowerCase())) {
                    handlerExist.put(fd.getName(), withness);
                    /*
                     * We only need to know the size of present later, doens't matter which values are inside
                     */
                    present.addElement(fd.getName());
                    if (!cs.checkColumn(fd) && !(alter && alter(dbc, fd, getSQLDatabase().getColumnAlterKeyword()))) {
                        java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                            "should modify: " + fd.getName() + " " + getSQLDatabase().getFieldDBName(fd) + " "
                                    + getSQLDatabase().getFieldDBType(fd) + " " + cs.columnType() + " "
                                    + cs.columnName());
                        modify.addElement(fd.getName());
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
        // keyIndex = new Hashtable<String, Integer>();

        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            if (fd.getType().startsWith("set")) {
                continue;
            }

            if (handlerExist.get(fd.getName()) == null && !(alter && alter(dbc, fd, "ADD"))) {
                add.addElement(fd.getName());
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                    "should add " + fd.getName() + " " + getSQLDatabase().getFieldDBName(fd) + " "
                            + getSQLDatabase().getFieldDBType(fd));
            } else {
                // keyIndex.put(fieldName, new Integer(v.size()));
                v.addElement(fd.getName());
            }
        }

        doAlter(dbc, drop, present, add, modify);
    }

    boolean alter(SQLDBConnection dbc, FieldDefinition fd, String op) throws SQLException {
        Statement st = dbc.createStatement();
        String command = null;
        if (!autoIncrementAlter) {
            try {
                command = "DROP INDEX " + getSQLDatabase().getFieldDBIndexName(fd) + " ON " + getDBName();
                st.executeUpdate(command);
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info("SUCCESS: " + command);
            } catch (SQLException e) {
                treatIndexException(e, command, dbc);
            }
        }
        autoIncrementAlter = false;
        String s = "ALTER TABLE " + getDBName() + " " + op + " " + getSQLDatabase().inCreate(fd);
        java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
            getSQLDatabase().getName() + ": " + s);
        st.executeUpdate(s);
        handlerExist.put(fd.getName(), "");
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
                getSQLDatabase().checkState(e, getSQLDatabase().getTableMissingStateName(dbc));
            }
        }

        /* TODO: concatAll() */

        String command = getSQLDatabase().createStatement(tblname, dd);

        command = getSQLDatabase().createDbSpecific(command);
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
    protected void fieldList(StringBuffer command, Iterator<FieldDefinition> e) {
        String comma = "";

        while (e.hasNext()) {
            FieldDefinition fd = e.next();
            if (fd.getType().startsWith("set")) {
                continue;
            }
            command.append(comma);
            comma = ", ";
            command.append(getSQLDatabase().getFieldDBName(fd));
        }
    }

    protected String prepareInsert(boolean autoIncrement) {

        /* TODO: concatAll() */
        StringBuffer ret = new StringBuffer();
        String sep = "";

        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            if (fd.getType().startsWith("set") || fd.getIntegerType() == FieldDefinition._ptrIndex && autoIncrement) {
                continue;
            }
            ret.append(sep).append(getSQLDatabase().inPreparedInsert(fd));
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
            for (FieldDefinition fd : dd.getFieldDefinitions()) {
                if (fd.getType().startsWith("set")) {
                    continue;
                }
                if (fd.getIntegerType() == FieldDefinition._ptrIndex && !wasIndex && getSQLDatabase().isAutoIncrement()) {
                    continue;
                }
                n++;
                try {
                    setInsertArgument(fd, ps, n, d);
                } catch (Throwable ex) {
                    // throw new DBError(ex, (getRecordInfo().getName())+"
                    // "+(fm.getName())+" "+(d.get(fm.getName())));
                    throw new org.makumba.DBError(ex, "insert into \""
                            + getDataDefinition().getName()
                            + "\" at field \""
                            + fd.getName()
                            + "\" could not assign value \""
                            + d.get(fd.getName())
                            + "\" "
                            + (d.get(fd.getName()) != null ? "of type \"" + d.get(fd.getName()).getClass().getName()
                                    + "\"" : ""));

                }
            }
            // exec closes ps
            // here if we have an error, we enrich it by finding the duplicates (which we assume is the only error one
            // can get at this stage of the insert)
            if (getSQLDatabase().exec(ps) == -1) {
                findDuplicates(dbc, d);
            }

            if (!wasIndex && getSQLDatabase().isAutoIncrement()) {
                ps = ((SQLDBConnection) dbc).getPreparedStatement(getSQLDatabase().getQueryAutoIncrementSyntax());
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
        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            Object val = d.get(fd.getName());
            if (fd.getType().startsWith("set")) {
                continue;
            }
            if (checkDuplicate(fd, dbc, d)) {
                if (fd.getErrorMessage(FieldErrorMessageType.NOT_UNIQUE) == null) {
                    notUnique.addException(new NotUniqueException(getFieldDefinition(fd.getName()), val));
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
        return "DELETE FROM " + tbname + " WHERE " + getSQLDatabase().inPreparedUpdate(getFieldDefinition(indexField));
    }

    // moved from FieldManager, adapted to dateCreateJavaManager,
    // dateModifyJavaManager and ptrIndexJavaManager
    /**
     * ask this field to write write its argumment value in a prepared INSERT SQL statement
     */
    public void setInsertArgument(FieldDefinition fd, PreparedStatement ps, int n, Dictionary<String, Object> d)
            throws SQLException {
        switch (fd.getIntegerType()) {
            case FieldDefinition._dateCreate:
            case FieldDefinition._dateModify:
                if (d.get(fd.getName()) == null) {
                    nxt(fd.getName(), d);
                }
                getSQLDatabase().set_timeStamp_InsertArgument(fd, ps, n, d);
                break;
            case FieldDefinition._ptrIndex:
                // this is not executed on autoIncrement
                org.makumba.Pointer p = (org.makumba.Pointer) d.get(fd.getName());
                if (p != null) {
                    getSQLDatabase().base_setInsertArgument(fd, ps, n, d);
                    if (p.getDbsv() == dbsv && p.longValue() > this.primaryKeyCurrentIndex) {
                        this.primaryKeyCurrentIndex = p.longValue();
                    }
                    return;
                }
                ps.setInt(n, (int) nxt_ptrIndex(fd.getName(), d).longValue());
                break;
            default:
                getSQLDatabase().base_setInsertArgument(fd, ps, n, d);
        }
    }

    // moved from FieldManager
    /**
     * ask this field to write its contribution in a SQL UPDATE statement should return "" if this field doesn't want to
     * take part in the update
     */
    public String inCondition(FieldDefinition fd, Dictionary<String, Object> d, String cond) {
        return getDBName() + cond + getSQLDatabase().writeConstant(fd, d.get(fd.getName()));
    }

    // moved from FieldManager
    /** ask this field to perform actions when the table is open */
    public void onStartup(FieldDefinition fd, Properties config, SQLDBConnection dbc) throws SQLException {
        if (alter && getSQLDatabase().shouldIndex(fd)) {
            manageIndexes(fd, dbc);
        }

        // getFieldDefinition(fieldName).is

        // getFieldDBName(fieldName)

        if (getSQLDatabase().shouldIndex(fd)) {
            extraIndexes.remove(getSQLDatabase().getFieldDBIndexName(fd).toLowerCase());
        }

        checkDuplicate.put(fd.getName(),
            "SELECT 1 FROM " + getDBName() + " WHERE " + getSQLDatabase().getFieldDBName(fd) + "=?");
        checkNullDuplicate.put(fd.getName(), "SELECT 1 FROM " + getDBName() + " WHERE "
                + getSQLDatabase().getFieldDBName(fd) + " is null");
        switch (fd.getIntegerType()) {
            case FieldDefinition._ptrIndex:
                if (!getSQLDatabase().isAutoIncrement()) {
                    dbsv = getSQLDatabase().getDbsv();
                    Statement st = dbc.createStatement();
                    // System.out.println("\t\t** Checking keys " + getDBName() + " " + fieldName);
                    resetPrimaryKey();
                    ResultSet rs = st.executeQuery("SELECT MAX(" + getSQLDatabase().getFieldDBName(fd) + "), COUNT("
                            + getSQLDatabase().getFieldDBName(fd) + ") FROM " + tbname + " WHERE "
                            + getSQLDatabase().getFieldDBName(fd) + ">=" + primaryKeyCurrentIndex + " AND "
                            + getSQLDatabase().getFieldDBName(fd) + "<=" + getSQLDatabase().getMaxPointerValue());
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

    // moved from FieldManager
    /** Examine DB indexes. */
    public boolean isIndexOk(FieldDefinition fd) {
        Boolean b = indexes.get(getSQLDatabase().getFieldDBIndexName(fd).toLowerCase());
        if (b != null) {
            return fd.isUnique() == !b.booleanValue();
        }
        return false;
    } // end isIndexOk()

    public boolean hasForeignKey(FieldDefinition fd) {
        return foreignKeys.get(getSQLDatabase().getFieldDBIndexName(fd).toLowerCase()) != null;
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
    public void manageIndexes(FieldDefinition fd, SQLDBConnection dbc) throws SQLException {
        // String keyName = getFieldDBIndexName(fieldName);
        String brief = getDataDefinition().getName() + "#" + fd + " (" + fd.getDescription() + ")";

        if (getDatabase().usesHibernateIndexes()) { // if we use hibernate and we are allowed to change the table
            // FIXME: this will have to be done in another step, before hibernate schema update
            dropIndex(fd, dbc, "RESIDUAL MAKUMBA INDEX DROPPED on " + brief); // we drop the index
            return;
        }

        if (!isIndexOk(fd)) {
            // org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
            // "ALTERING INDEX on field "+getName()+" of
            // "+rm.getRecordInfo().getName() );

            dropIndex(fd, dbc, "INDEX DROPPED on " + brief);

            boolean createNormalEvenIfUnique = false;

            if (fd.isUnique()) {
                try {
                    // try creating unique index
                    Statement st = dbc.createStatement();
                    st.executeUpdate(getSQLDatabase().indexCreateUniqueSyntax(fd));
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                        "UNIQUE INDEX ADDED on " + brief);
                    st.close();
                    getSQLDatabase().indexCreated(dbc);
                } catch (SQLException e) {
                    // log all errors
                    java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").warning(
                    // rm.getDatabase().getConfiguration()+": "+ //DB name
                        "Problem adding UNIQUE INDEX on " + brief + ": " + e.getMessage() + " [ErrorCode: "
                                + e.getErrorCode() + ", SQLstate:" + e.getSQLState() + "]");
                    createNormalEvenIfUnique = true;
                }
            }

            if (createNormalEvenIfUnique || !fd.isUnique()) {
                try {
                    // create normal index
                    Statement st = dbc.createStatement();
                    st.executeUpdate(getSQLDatabase().indexCreateSyntax(fd));
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
            manageForeignKeys(fd, dbc, brief);
        }
    }// method

    public void manageForeignKeys(FieldDefinition fd, SQLDBConnection dbc, String brief) throws DBError {
        // for foreign keys

        String type = fd.getType();
        if ((type.equals("ptr") || type.equals("ptrOne") || type.equals("ptrRel")) && !hasForeignKey(fd)) {
            // System.out.println("We need a foreign key for " + brief);

            try {
                // try creating foreign key index
                Statement st = dbc.createStatement();

                DataDefinition fkTable = fd.getPointedType();

                if (type.equals("ptrOne")) {
                    fkTable = fd.getSubtable();
                }

                FieldDefinition foreign = fkTable.getFieldDefinition(fkTable.getIndexPointerFieldName());

                // System.out.println("testing: "+foreignKeyCreateSyntax(fieldName,
                // getFieldDefinition(fieldName).getPointedType().getName(),
                // getFieldDefinition(fieldName).getPointedType().getIndexPointerFieldName()));
                st.executeUpdate(getSQLDatabase().foreignKeyCreateSyntax(fd, foreign));
                java.util.logging.Logger.getLogger("org.makumba.db.init.tablechecking").info(
                    "FOREIGN KEY ADDED on " + brief);
                st.close();
                getSQLDatabase().indexCreated(dbc);
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

    private void dropIndex(FieldDefinition fd, SQLDBConnection dbc, String message) {
        String syntax = getSQLDatabase().indexDropSyntax(fd);
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
    public boolean checkDuplicate(FieldDefinition fd, DBConnection dbc, Dictionary<String, Object> data) {
        if (!fd.isUnique()) {
            return false;
        }
        Object val = data.get(fd.getName());
        SQLDBConnection sqlDbc = (SQLDBConnection) dbc;
        PreparedStatement ps;
        if (val == null) {
            ps = sqlDbc.getPreparedStatement(checkNullDuplicate.get(fd.getName()));
        } else {
            ps = sqlDbc.getPreparedStatement(checkDuplicate.get(fd.getName()));
        }
        try {
            if (val != null) {
                getSQLDatabase().setUpdateArgument(fd, ps, 1, val);
            }
            return ps.executeQuery().next();
        } catch (SQLException se) {
            Database.logException(se, sqlDbc);
            throw new org.makumba.DBError(se, checkDuplicate.get(fd.getName()));
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
            query += getSQLDatabase().getFieldDBName(getFieldDefinition(fields[j]))
                    + (values[j] != null ? "=?" : " is null");
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
                    getSQLDatabase().setUpdateArgument(getFieldDefinition(fields[i]), ps, i + 1 - k, values[i]);
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
        Vector<String> alreadyAdded = new Vector<String>(); // we store which tables we already added to the projection
        for (int i = 0; i < fields.length; i++) {
            if (values[i] == null) {
                nullIndexes.add(i);
            }
            String projectionExpr;
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
                    where += projection
                            + "."
                            + getSQLDatabase().getFieldDBName(getFieldDefinition(subField))
                            + "="
                            + otherProjection
                            + "."
                            + otherTable.getSQLDatabase().getFieldDBName(
                                otherTable.getFieldDefinition(pointedType.getIndexPointerFieldName())) + " AND ";
                    alreadyAdded.add(subField);
                }
                projectionExpr = otherProjection + "."
                        + otherTable.getSQLDatabase().getFieldDBName(otherTable.getFieldDefinition(fieldName));
            } else { // simple case of having a field from the same MDD
                projectionExpr = projection + "." + getSQLDatabase().getFieldDBName(getFieldDefinition(fields[i]));
            }

            where += projectionExpr + (values[i] != null ? "=?" : " is null");
            if (i + 1 < fields.length) {
                where += " AND ";
            }

        }

        // if we have a pointer, we are in editing mode --> we make the query to not consider our record
        if (pointer != null) {
            where += " AND " + projection + "."
                    + getSQLDatabase().getFieldDBName(dd.getFieldDefinition(dd.getIndexPointerFieldName())) + "<>"
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
                        otherTable.getSQLDatabase().setUpdateArgument(otherTable.getFieldDefinition(fieldName), ps,
                            n - k, values[i]);
                    }

                } else { // otherwise we use this table manager
                    if (values[i] != null) {
                        getSQLDatabase().setUpdateArgument(getFieldDefinition(fields[i]), ps, n, values[i]);
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
        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            if (fieldsToIgnore.get(fd.getName()) == null) {
                Object o = fieldsToCheck.get(fd.getName());
                if (o != null) {

                    boolean isDateCreate = fd.getIntegerType() == FieldDefinition._dateCreate;
                    boolean isDataModify = fd.getIntegerType() == FieldDefinition._dateModify;
                    boolean isPtrIndex = fd.getIntegerType() == FieldDefinition._ptrIndex;

                    if (isDateCreate || isDataModify || isPtrIndex) {
                        checkCopyRights(fd.getName());
                    } else {
                        fd.checkInsert(fieldsToCheck);
                    }

                    fieldsToCheck.put(fd.getName(), fd.checkValue(o));
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

}
