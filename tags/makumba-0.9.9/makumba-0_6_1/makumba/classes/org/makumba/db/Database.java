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

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.hibernate.SessionFactory;
import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.util.ClassResource;
import org.makumba.util.NamedResourceFactory;
import org.makumba.util.NamedResources;
import org.makumba.util.ResourcePool;
import org.makumba.util.RuntimeWrappedException;
import org.makumba.util.SoftNamedResources;

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
public abstract class Database {
    public String getName() {
        return configName;
    }

    NamedResources queries;

    NamedResources updates;

    int nconn = 0;

    int initConnections = 1;

    protected ResourcePool connections = new ResourcePool() {
        public Object create() {
            nconn++;
            config.put("jdbc_connections", "" + nconn);
            return makeDBConnection();
        }

        // preventing stale connections
        public void renew(Object o) {
            ((DBConnection) o).commit();
        }

        public void close(Object o) {
            ((DBConnection) o).close();
        }
    };

    public void initConnections() {
        try {
            // resourcePool should have limits...
            connections.init(initConnections);
        } catch (Exception e) {
            throw new DBError(e);
        }
    }

    protected void closeConnections() {
        connections.close();
    }

    public void close() {
        MakumbaSystem.getMakumbaLogger("db.init").info(
                "closing  " + getConfiguration() + "\n\tat "
                        + org.makumba.view.dateFormatter.debugTime.format(new java.util.Date()));
        tables.close();
        queries.close();
        updates.close();
        closeConnections();
        if(sf!=null)
            sf.close();
    }

    public DBConnection getDBConnection() {
        try {
            return new DBConnectionWrapper((DBConnection) connections.get());
        } catch (Exception e) {
            throw new DBError(e);
        }
    }

    protected abstract DBConnection makeDBConnection();

    // ---------------------------------------------------

    private int dbsv=0;

    private boolean autoIncrement;
    
    Properties config = null;

    Class tableclass;

    String configName;

    Hashtable queryCache = new Hashtable();

    String fullName;

    /** return the unique index of this database */
    public int getDbsv() {
        return dbsv;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public abstract Pointer getPointer(String type, int uid);

    static Class[] theProp = { java.util.Properties.class };

    public String getConfiguration() {
        return fullName;
    }

    public String getConfiguration(String v) {
        if (v.equals("resource_pool_size")) {
            return String.valueOf(connections.getSize());
        }
        return config.getProperty(v);
    }

    static int dbs = NamedResources.makeStaticCache("Databases open", new NamedResourceFactory() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected Object makeResource(Object nm) {
            Properties p = new Properties();
            String name = (String) nm;

            try {
                p.load(org.makumba.util.ClassResource.get(name + ".properties").openStream());
            } catch (Exception e) {
                throw new org.makumba.ConfigFileError(name + ".properties");
            }

            try {
                String origName= name;
                name = name.substring(name.lastIndexOf('/') + 1);
                int n = name.indexOf('_');
                String host;
                p.put("#host", host = name.substring(0, n));
                int m = name.indexOf('_', n + 1);
                if (Character.isDigit(name.charAt(n + 1))) {
                    p.put("#host", host + ":" + name.substring(n + 1, m));
                    n = m;
                    m = name.indexOf('_', n + 1);
                }

                p.put("#sqlEngine", name.substring(n + 1, m));
                p.put("#database", name.substring(m + 1));

                String dbclass = (String) p.get("dbclass");
                if (dbclass == null) {
                    dbclass = org.makumba.db.sql.Database.getEngineProperty(p.getProperty("#sqlEngine") + ".dbclass");
                    if (dbclass == null)
                        dbclass = "org.makumba.db.sql.Database";
                }
                p.put("db.name", (String) name);
                Object pr[] = { p };

                try {
                    Database d = (Database) Class.forName(dbclass).getConstructor(theProp).newInstance(pr);
                    d.configName = (String) name;
                    d.fullName=origName;
                    d.tables = new NamedResources("Database tables for " + name, d.tableFactory);
     
                    // TODO: need to check if hibernate schema update is authorized. If yes, drop/adjust indexes from all tabbles so that hibernate can create its foreign keys at schema update.
                    
                    return d;
                } catch (InvocationTargetException ite) {
                    throw new org.makumba.MakumbaError(ite.getTargetException());
                }
            } catch (Exception e) {
                throw new org.makumba.MakumbaError(e);
            }
        }
    });

    static String findInHostProperties(Properties p, String str){
        for (Enumeration e = p.keys(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            int i = s.indexOf('#');
            try {
                if ((i==-1|| java.net.InetAddress.getByName(s.substring(0, i)).equals(java.net.InetAddress.getLocalHost()))
                        && str.endsWith(s.substring(i + 1)))
                    return p.getProperty(s);
            } catch (java.net.UnknownHostException uhe) {
            }
        }
        return null;
    }
    
    /**
     * finds the database name of the server according to the host name and current directory. If none is specified, a
     * default is used, if available
     */
    public static String findDatabaseName(Properties p) {
        String userDir= System.getProperty("user.dir");
        String n;
        java.net.URL u= ClassResource.get("/"); 
        String wbp= u!=null?u.toString():null;
        
        if(
                (n= findInHostProperties(p, userDir))!=null 
                ||
                wbp!=null &&((n= findInHostProperties(p, wbp))!=null)
                ||
                (n= findInHostProperties(p, "default"))!=null
                )
          
            return n;
        
        return p.getProperty("default");
    }

    public static Database findDatabase(Properties p) {
        return getDatabase(findDatabaseName(p));
    }

    public static Database findDatabase(String s) {
        return getDatabase(findDatabaseName(s));
    }

    static int dbsel = NamedResources.makeStaticCache("Database selection files", new NamedResourceFactory() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        protected Object makeResource(Object nm) {
            Properties p = new Properties();
            try {
                java.io.InputStream input = org.makumba.util.ClassResource.get((String) nm).openStream();
                p.load(input);
                input.close();
            } catch (Exception e) {
                throw new org.makumba.ConfigFileError((String) nm);
            }
            return p;
        }
    });

    public static String findDatabaseName(String s) {
        try {
            return findDatabaseName((Properties) NamedResources.getStaticCache(dbsel).getResource(s));
        } catch (RuntimeWrappedException e) {
            if (e.getReason() instanceof org.makumba.MakumbaError)
                throw (org.makumba.MakumbaError) e.getReason();
            throw e;
        }
    }

    /**
     * Initializes a Database object from the given properties. The following properties are expected:
     * <dl>
     * <dt> dbclass
     * <dd> the class of the concrete database, e.g. org.makumba.db.sql.SQLDatabase. An object of this class will be
     * instantiated, and the properties will be passed on to it for further initialization
     * <dt> tableclass
     * <dd> the class of the concrete database table, e.g. org.makumba.db.sql.odbc.RecordManager. If this is not
     * present, the class returned by the method getTableClass() is used
     * <dt> dbsv
     * <dd> the unique id of the database. Can be any integer 0-255
     * </dl>
     */
    public static Database getDatabase(String name) {
        try {
            return (Database) NamedResources.getStaticCache(dbs).getResource(name);
        } catch (RuntimeWrappedException e) {
            if (e.getReason() instanceof org.makumba.MakumbaError)
                throw (org.makumba.MakumbaError) e.getReason();
            throw e;
        }
    }

    protected Database(Properties config) {
        this.config = config;
        this.configName = config.getProperty("db.name");
        String s = config.getProperty("initConnections");
        if (s != null)
            initConnections = Integer.parseInt(s.trim());

        config.put("jdbc_connections", "0");
        try {
        	if(config.get("dbsv")!=null && config.get("autoIncrement")!=null)
	        	throw new org.makumba.ConfigFileError("only one of dbsv and autoIncrement can be specified");
            if(config.get("dbsv")!=null)
                dbsv = new Integer((String) config.get("dbsv")).intValue();
            else 
                if(config.get("autoIncrement")!=null)
                    autoIncrement=true;
                else throw new org.makumba.ConfigFileError("either dbsv or autoIncrement must be specified");
            
            tableclass = getTableClassConfigured();

            config.put("alter#org.makumba.db.Catalog", "true");
            config.put("alter#org.makumba.db.Lock", "true");
            config.put("alter#org.makumba.controller.MultipleSubmit", "true");
        } catch (Exception e) {
            throw new org.makumba.MakumbaError(e);
        }

        queries = new SoftNamedResources("Database " + getName() + " query objects", new NamedResourceFactory() {
            /**
             * 
             */
            private static final long serialVersionUID = 1L;

            public Object makeResource(Object o) {
                Object[] multi = (Object[]) o;

                return prepareQueryImpl((String) multi[0], (String) multi[1]);
            }

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

            public Object makeResource(Object o) {
                Object[] multi = (Object[]) o;

                return prepareUpdateImpl((String) multi[0], (String) multi[1], (String) multi[2]);
            }

            protected Object getHashObject(Object name) {
                Object[] multi = (Object[]) name;
                return "" + multi[0] + "####" + multi[1] + "######" + multi[2];
            }
        });
    }

    /** this method should be redefined by database classes that have a default table class. this returns null */
    protected Class getTableClassConfigured() {
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
            if (name.charAt(0) == '.')
                name = name.substring(1);
        }
        // <<

        int n = name.indexOf("->");
        if (n == -1)
            return getTable(MakumbaSystem.getDataDefinition(name));
        // the abstract level doesn't return recordInfo for subtables (->) since it is supposed they are managed via
        // their parent tables. the current DB api doesn't provide that.

        Table t = getTable(name.substring(0, n));
        while (true) {
            name = name.substring(n + 2);
            n = name.indexOf("->");
            if (n == -1)
                break;
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
        for (Enumeration e = cnf.keys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            if (pattern.startsWith(key) && (ret == null || ret.length() < key.length()))
                ret = key;
        }
        return ret;
    }

    public abstract Query prepareQueryImpl(String query, String insertIn);

    public abstract Update prepareUpdateImpl(String type, String set, String where);

    public abstract int getMinPointerValue();

    public abstract int getMaxPointerValue();

    public void deleteFrom(DBConnection c, String table, DBConnection sourceDB, boolean ignoreDbsv) {
        DataDefinition dd = MakumbaSystem.getDataDefinition(table);
        MakumbaSystem.getMakumbaLogger("db.admin.delete").info(
                "deleted " + getTable(table).deleteFrom(c, sourceDB, ignoreDbsv) + " old objects from " + table);

        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            FieldDefinition fi = dd.getFieldDefinition((String) e.nextElement());
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne"))
                deleteFrom(c, fi.getSubtable().getName(), sourceDB, ignoreDbsv);
        }
    }

    public void deleteFrom(String sourceDB, String table, boolean ignoreDbsv) {
        String[] _tables = { table };
        deleteFrom(sourceDB, _tables, ignoreDbsv);
    }

    public void deleteFrom(String source, String[] tables, boolean ignoreDbsv) {
        DBConnection c = getDBConnection();
        DBConnection sourceDBc = getDatabase(source).getDBConnection();
        try {
            deleteFrom(c, tables, sourceDBc, ignoreDbsv);
        } finally {
            c.close();
            sourceDBc.close();
        }
    }

    public void deleteFrom(DBConnection c, String[] tables, DBConnection sourceDB, boolean ignoreDbsv) {
        for (int i = 0; i < tables.length; i++)
            deleteFrom(c, tables[i], sourceDB, ignoreDbsv);
    }

    public void copyFrom(String sourceDB, String table, boolean ignoreDbsv) {
        String[] _tables = { table };
        copyFrom(sourceDB, _tables, ignoreDbsv);
    }

    public void copyFrom(String source, String[] tables, boolean ignoreDbsv) {
        DBConnection c = getDBConnection();
        DBConnection sourceDBc = getDatabase(source).getDBConnection();
        try {
            copyFrom(c, tables, sourceDBc, ignoreDbsv);
        } finally {
            c.close();
            sourceDBc.close();
        }
    }

    public void copyFrom(DBConnection c, String[] tables, DBConnection sourceDB, boolean ignoreDbsv) {
        deleteFrom(c, tables, sourceDB, ignoreDbsv);
        for (int i = 0; i < tables.length; i++)
            copyFrom(c, tables[i], sourceDB, ignoreDbsv);
    }

    public void copyFrom(DBConnection c, String table, DBConnection sourceDB, boolean ignoreDbsv) {
        DataDefinition dd = MakumbaSystem.getDataDefinition(table);
        getTable(table).copyFrom(c, c.getHostDatabase().getTable(table), sourceDB, ignoreDbsv);

        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            FieldDefinition fi = dd.getFieldDefinition((String) e.nextElement());
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne"))
                copyFrom(c, fi.getSubtable().getName(), sourceDB, ignoreDbsv);
        }
    }

    public void copyFrom(String source, boolean ignoreDbsv) {
        DBConnection c = getDBConnection();
        DBConnection sourceDB = findDatabase(source).getDBConnection();
        try {
            Vector v = sourceDB.executeQuery("SELECT c.name AS name FROM org.makumba.db.Catalog c", null);
            String[] _tables = new String[v.size()];

            for (int i = 0; i < _tables.length; i++) {
                String nm = (String) ((Dictionary) v.elementAt(i)).get("name");
                MakumbaSystem.getMakumbaLogger("db.admin.copy").info(nm);
                _tables[i] = nm;
            }
            copyFrom(c, _tables, sourceDB, ignoreDbsv);
        } finally {
            c.close();
            sourceDB.close();
        }
    }

    public void openTables(String[] _tables) {
        for (int i = 0; i < _tables.length; i++) {
            openTable(_tables[i]);
        }
    }

    public void openTable(String table) {
        getTable(table);
        DataDefinition dd = MakumbaSystem.getDataDefinition(table);

        for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
            FieldDefinition fi = dd.getFieldDefinition((String) e.nextElement());
            if (fi.getType().startsWith("set") || fi.getType().equals("ptrOne"))
                openTable(fi.getSubtable().getName());
        }
    }

    protected void finalize() throws Throwable {
        close();
    }

    NamedResources tables;

    synchronized void addTable(String s) {
        if (s.equals("org.makumba.db.Catalog"))
            return;
        DBConnection c = getDBConnection();
        try {
            Enumeration e = c.executeQuery("SELECT c FROM org.makumba.db.Catalog c WHERE c.name=$1", s).elements();
            if (!e.hasMoreElements()) {
                Dictionary h = new Hashtable(3);
                h.put("name", s);
                getTable("org.makumba.db.Catalog").insertRecord(c, h);
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
        tbl.open(config);
    }

    NamedResourceFactory tableFactory = new NamedResourceFactory() {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        public Object getHashObject(Object name) {
            return ((DataDefinition) name).getName();
        }

        public Object makeResource(Object name, Object hashName) throws Throwable {
            return tableclass.newInstance();
        }

        public void configureResource(Object name, Object hashName, Object resource) {
            configureTable((Table) resource, (DataDefinition) name);
            addTable(((Table) resource).getDataDefinition().getName());
        }
    };

    private SessionFactory sf;

    public synchronized SessionFactory getHibernateSessionFactory() {
        if(sf==null && ClassResource.get(getConfiguration()+".cfg.xml")!=null){
            sf= HibernateSFManager.getSF(getConfiguration()+".cfg.xml", false);
        }
        return sf;
    }

    /**
     * If this is true, i.e. hibernate is used, makumba will not take care of creating indexes itself
     */
    public boolean usesHibernateIndexes() {
        // TODO: add hibernate schema update authorization
        return false;
    }

}
