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

package org.makumba.db.makumba;

import java.sql.SQLException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.makumba.ConfigurationError;
import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.SoftNamedResources;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 FIXME what is this comment?
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

/**
 * A generic Makumba database (used by the {@link MakumbaTransactionProvider}, that maps RecordInfos to tables. It also
 * takes care of creating and maintaining the schema. FIXME methods in here should get some documentation
 * 
 * @author Cristian Bogdan
 * @author Thomas Laroche
 * @author Rudolf Mayer
 * @author Marius Andra
 * @author Manuel Gay
 * @author please add if more
 */
public abstract class Database {

    public String getName() {
        return dataSourceName;
    }

    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    protected TransactionProvider tp = TransactionProvider.getInstance();

    NamedResources queries;

    NamedResources updates;

    int nconn = 0;

    int initConnections = 1;

    protected static boolean requestUTF8 = false;

    protected static boolean requestForeignKeys = false;

    static protected boolean supportsForeignKeys() {
        return requestForeignKeys;
    }

    protected abstract void closeResourcePool();

    protected abstract int getResourcePoolSize();

    protected abstract int getOpenedConnections();

    protected abstract int getIdleConnections();

    protected abstract DBConnection getPooledDBConnection();

    public void close() {
        java.util.logging.Logger.getLogger("org.makumba.db.init").info(
            "closing  " + getName() + "\n\tat "
                    + org.makumba.commons.formatters.dateFormatter.debugTime.format(new java.util.Date()));
        tables.close();
        queries.close();
        updates.close();
        closeResourcePool();
    }

    public DBConnection getDBConnection(String dataSource) {
        try {
            return new DBConnectionWrapper(getPooledDBConnection(), dataSource, tp);
        } catch (Exception e) {
            throw new DBError(e);
        }
    }

    public DBConnection getDBConnection() {
        try {
            return new DBConnectionWrapper(getPooledDBConnection(), getName(), tp);
        } catch (Exception e) {
            throw new DBError(e);
        }
    }

    protected abstract DBConnection makeDBConnection();

    public abstract boolean isDuplicateException(SQLException e);

    public abstract Map<String, String> getDuplicateFields(SQLException e);

    // ---------------------------------------------------

    private int dbsv = 0;

    private boolean autoIncrement;

    Properties config = null;

    Class<?> tableclass;

    /** the name of the configured dataSource **/
    String dataSourceName;

    protected NameResolver nr;

    /** return the unique index of this database */
    public int getDbsv() {
        return dbsv;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public abstract Pointer getPointer(String type, int uid);

    public String getConfiguration(String v) {
        if (v.equals("resource_pool_size")) {
            return String.valueOf(getResourcePoolSize());
        }
        if (v.equals("idle_connections")) {
            return String.valueOf(getIdleConnections());
        }
        if (v.equals("jdbc_connections")) {
            return String.valueOf(getOpenedConnections());
        }
        return config.getProperty(v);
    }

    protected Database(Properties config) {
        this.config = config;
        this.nr = new NameResolver(config);
        this.dataSourceName = config.getProperty("db.name");
        String s = config.getProperty("initConnections");
        if (s != null) {
            initConnections = Integer.parseInt(s.trim());
        }

        config.put("jdbc_connections", "0");
        try {
            if (config.get("dbsv") != null && config.get("autoIncrement") != null) {
                throw new org.makumba.ConfigurationError("Either dbsv or autoIncrement can be specified");
            }
            if (config.get("dbsv") != null) {

                Long dbsvTest = new Long(config.getProperty("dbsv"));
                if (dbsvTest > 127) {
                    throw new ConfigurationError("The DBSV cannot be larger than 127, provided value is "
                            + config.getProperty("dbsv"));
                }

                dbsv = new Integer(config.getProperty("dbsv")).intValue();
            } else if (config.get("autoIncrement") != null) {
                autoIncrement = true;
            } else {
                throw new org.makumba.ConfigurationError("dbsv or autoIncrement must be specified");
            }

            tableclass = getTableClassConfigured();

            // always allow altering/creating/.. of makumba internal tables
            config.put("alter#org.makumba.db.makumba.Catalog", "true");
            config.put("alter#org.makumba.db.makumba.Lock", "true");
            config.put("alter#org.makumba.controller.MultipleSubmit", "true");
            config.put("alter#org.makumba.controller.ErrorLog", "true");
            config.put("alter#org.makumba.devel.relations.Relation", "true");
            config.put("alter#org.makumba.devel.relations.WebappDatabase", "true");
            config.put("alter#org.makumba.devel.relations.RelationOrigin", "true");

        } catch (Exception e) {
            throw new org.makumba.MakumbaError(e);
        }

        queries = new SoftNamedResources("Database " + getName() + " query objects", new NamedResourceFactory() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public Object makeResource(Object o) {
                Object[] multi = (Object[]) o;

                return prepareQueryImpl((String) multi[0], (String) multi[1]);
            }

            @Override
            protected Object getHashObject(Object name) {
                Object[] multi = (Object[]) name;
                return "" + multi[0] + "####" + multi[1];
            }
        });

        updates = new SoftNamedResources("Database " + getName() + " update objects", new NamedResourceFactory() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
            public Object makeResource(Object o) {
                Object[] multi = (Object[]) o;

                return prepareUpdateImpl((String) multi[0], (String) multi[1], (String) multi[2]);
            }

            @Override
            protected Object getHashObject(Object name) {
                Object[] multi = (Object[]) name;
                return "" + multi[0] + "####" + multi[1] + "######" + multi[2];
            }
        });
    }

    /** this method should be redefined by database classes that have a default table class. this returns null */
    protected Class<?> getTableClassConfigured() {
        return null;
    }

    /** write a date constant in OQL */
    public abstract String OQLDate(java.util.Date d);

    // --------------------------- bunch of utility methods -------------------------------
    // these methods are added 20001030 from the experience we had with this incipient form of
    // Database API. Cristi wrote them initially for Minerva, Toto used to have them static in
    // org.eu.best.Metadata, but their place really is here. The utility methods getKeyFromPointer
    // and getPointerFromKey in that class should probably go to the presentation level
    // -----------------------------------------------------------------------------------
    /**
     * this method will return a table by macumba name. Subtables (->) are returned as well (for now) maybe we could
     * write Table.getSubtable(ptr, field) to operate with subsets? how about 1:1 pointers? need to think of that API
     * further
     */
    public Table getTable(String name) {
        // OLDSUPPORT >>
        if (name.indexOf('/') != -1) {
            name = name.replace('/', '.');
            if (name.charAt(0) == '.') {
                name = name.substring(1);
            }
        }
        // <<

        int n = name.indexOf("->");
        if (n == -1) {
            return getTable(ddp.getDataDefinition(name));
            // the abstract level doesn't return recordInfo for subtables (->) since it is supposed they are managed via
            // their parent tables. the current DB api doesn't provide that.
        }

        Table t = getTable(name.substring(0, n));
        while (true) {
            name = name.substring(n + 2);
            n = name.indexOf("->");
            if (n == -1) {
                break;
            }
            t = t.getRelatedTable(name.substring(0, n));
        }
        t = t.getRelatedTable(name);
        return t;
    }

    /** get the table from this database associated with the given RecordInfo */
    public Table getTable(DataDefinition ri) {
        return (Table) tables.getResource(ri);
    }

    /** finds the longest configuration string that matches the pattern and returns the associated property */
    public static String findConfig(Properties cnf, String pattern) {
        String ret = null;
        for (Enumeration<Object> e = cnf.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (pattern.startsWith(key) && (ret == null || ret.length() < key.length())) {
                ret = key;
            }
        }
        return ret;
    }

    public abstract Query prepareQueryImpl(String query, String insertIn);

    public abstract Update prepareUpdateImpl(String type, String set, String where);

    public abstract int getMinPointerValue();

    public abstract int getMaxPointerValue();

    public void deleteFrom(DBConnection c, String table, DBConnection sourceDB, boolean ignoreDbsv) {
        DataDefinition dd = ddp.getDataDefinition(table);
        java.util.logging.Logger.getLogger("org.makumba.db.admin.delete").info(
            "deleted " + getTable(table).deleteFrom(c, sourceDB, ignoreDbsv) + " old objects from " + table);

        for (String string : dd.getFieldNames()) {
            FieldDefinition fi = dd.getFieldDefinition(string);
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne")) {
                deleteFrom(c, fi.getSubtable().getName(), sourceDB, ignoreDbsv);
            }
        }
    }

    public void deleteFrom(String sourceDB, String table, boolean ignoreDbsv) {
        String[] _tables = { table };
        deleteFrom(sourceDB, _tables, ignoreDbsv);
    }

    public void deleteFrom(String source, String[] tables, boolean ignoreDbsv) {
        DBConnection c = getDBConnection();
        DBConnection sourceDBc = MakumbaTransactionProvider.getDatabase(source).getDBConnection();
        try {
            deleteFrom(c, tables, sourceDBc, ignoreDbsv);
        } finally {
            c.close();
            sourceDBc.close();
        }
    }

    public void deleteFrom(DBConnection c, String[] tables, DBConnection sourceDB, boolean ignoreDbsv) {
        for (String table : tables) {
            deleteFrom(c, table, sourceDB, ignoreDbsv);
        }
    }

    public void copyFrom(String sourceDB, String table, boolean ignoreDbsv) {
        String[] _tables = { table };
        copyFrom(sourceDB, _tables, ignoreDbsv);
    }

    public void copyFrom(String source, String[] tables, boolean ignoreDbsv) {
        DBConnection c = getDBConnection();
        DBConnection sourceDBc = MakumbaTransactionProvider.getDatabase(source).getDBConnection();
        try {
            copyFrom(c, tables, sourceDBc, ignoreDbsv);
        } finally {
            c.close();
            sourceDBc.close();
        }
    }

    public void copyFrom(DBConnection c, String[] tables, DBConnection sourceDB, boolean ignoreDbsv) {
        deleteFrom(c, tables, sourceDB, ignoreDbsv);
        for (String table : tables) {
            copyFrom(c, table, sourceDB, ignoreDbsv);
        }
    }

    public void copyFrom(DBConnection c, String table, DBConnection sourceDB, boolean ignoreDbsv) {
        DataDefinition dd = ddp.getDataDefinition(table);
        getTable(table).copyFrom(c, c.getHostDatabase().getTable(table), sourceDB, ignoreDbsv);

        for (String string : dd.getFieldNames()) {
            FieldDefinition fi = dd.getFieldDefinition(string);
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne")) {
                copyFrom(c, fi.getSubtable().getName(), sourceDB, ignoreDbsv);
            }
        }
    }

    public void copyFrom(String source, boolean ignoreDbsv) {
        DBConnection c = getDBConnection();
        DBConnection sourceDB = MakumbaTransactionProvider.getDatabase(source).getDBConnection();
        try {
            Vector<Dictionary<String, Object>> v = sourceDB.executeQuery(
                "SELECT c.name AS name FROM org.makumba.db.Catalog c", null);
            String[] _tables = new String[v.size()];

            for (int i = 0; i < _tables.length; i++) {
                String nm = (String) v.elementAt(i).get("name");
                java.util.logging.Logger.getLogger("org.makumba.db.admin.copy").info(nm);
                _tables[i] = nm;
            }
            copyFrom(c, _tables, sourceDB, ignoreDbsv);
        } finally {
            c.close();
            sourceDB.close();
        }
    }

    public void openTables(String[] _tables) {
        for (String _table : _tables) {
            openTable(_table);
        }
    }

    public void openTable(String table) {
        getTable(table);
        DataDefinition dd = ddp.getDataDefinition(table);

        for (String string : dd.getFieldNames()) {
            FieldDefinition fi = dd.getFieldDefinition(string);
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne")) {
                openTable(fi.getSubtable().getName());
            }
        }
    }

    public void checkForeignKeys(String table) {
        getTable(table);
        DataDefinition dd = ddp.getDataDefinition(table);

        for (String string : dd.getFieldNames()) {
            FieldDefinition fi = dd.getFieldDefinition(string);
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne")) {
                openTable(fi.getSubtable().getName());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        close();
    }

    NamedResources tables;

    synchronized void addTable(String s) {
        if (s.equals("org.makumba.db.makumba.Catalog")) {
            return;
        }
        DBConnection c = getDBConnection();
        try {
            Enumeration<Dictionary<String, Object>> e = c.executeQuery(
                "SELECT c FROM org.makumba.db.makumba.Catalog c WHERE c.name=$1", s).elements();
            if (!e.hasMoreElements()) {
                Dictionary<String, Object> h = new Hashtable<String, Object>(3);
                h.put("name", s);
                getTable("org.makumba.db.makumba.Catalog").insertRecord(c, h);
            }
        } finally {
            c.close();
        }
    }

    public Table makePseudoTable(DataDefinition ri) {
        Table ret = null;
        try {
            ret = (Table) tableclass.newInstance();
        } catch (Throwable t) {
            throw new MakumbaError(t);
        }
        configureTable(ret, ri);
        return ret;
    }

    void configureTable(Table tbl, DataDefinition ri) {
        tbl.db = Database.this;
        tbl.setDataDefinition(ri);
        tbl.open(config, nr);
    }

    NamedResourceFactory tableFactory = new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        @Override
        public Object getHashObject(Object name) {
            return ((DataDefinition) name).getName();
        }

        @Override
        public Object makeResource(Object name, Object hashName) throws Throwable {
            return tableclass.newInstance();
        }

        @Override
        public void configureResource(Object name, Object hashName, Object resource) {
            configureTable((Table) resource, (DataDefinition) name);
            addTable(((Table) resource).getDataDefinition().getName());
        }
    };

    /**
     * If this is true, i.e. hibernate is used, makumba will not take care of creating indexes itself FIXME this should
     * somehow be more separated, e.g. through some schema configuration mechanism
     */
    public boolean usesHibernateIndexes() {
        // TODO: add hibernate schema update authorization
        return false;
    }

    public String getTypeNameInSource(DataDefinition dd) {
        String nameInSource = ((org.makumba.db.makumba.sql.TableManager) this.getTable(dd)).getDBName();
        return nameInSource;
    }

    public String getFieldNameInSource(DataDefinition dd, String field) {
        String nameInSource = ((org.makumba.db.makumba.sql.TableManager) this.getTable(dd)).getFieldDBName(field);
        return nameInSource;
    }

    public Properties getConfigurationProperties() {
        return config;
    }

    public NameResolver getNameResolver() {
        return nr;
    }

    public void initialiseTables(String name) {
        tables = new NamedResources("Database tables for " + name, tableFactory);
    }

}
