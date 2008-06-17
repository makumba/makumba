package org.makumba.db.makumba;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.makumba.Transaction;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.TransactionProviderInterface;

/**
 * Makumba-specific implementation of the {@link TransactionProviderInterface}.
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaTransactionProvider.java,v 1.1 21.11.2007 17:06:25 Manuel Exp $
 */
public class MakumbaTransactionProvider implements TransactionProviderInterface {
    
    private static CRUDOperationProvider singleton;
    
    static Class[] theProp = { java.util.Properties.class };
    
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
            if (e.getCause() instanceof org.makumba.MakumbaError)
                throw (org.makumba.MakumbaError) e.getCause();
            throw e;
        }
    }

    static int dbs = NamedResources.makeStaticCache("Databases open", new NamedResourceFactory() {

        private static final long serialVersionUID = 1L;

        protected Object makeResource(Object nm) {
            Properties p = new Properties();
            String name = (String) nm;

            try {
                p.load(org.makumba.commons.ClassResource.get(name + ".properties").openStream());
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
                    dbclass = org.makumba.db.makumba.sql.Database.getEngineProperty(p.getProperty("#sqlEngine") + ".dbclass");
                    if (dbclass == null)
                        dbclass = "org.makumba.db.makumba.sql.Database";
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
    
    public Transaction getConnectionTo(String name) {
        return (DBConnectionWrapper) getDatabase(name).getDBConnection(name);
    }

    public String getDefaultDataSourceName() {
        return TransactionProvider.findDatabaseName("MakumbaDatabase.properties");
    }



    public String getDatabaseProperty(String name, String propName) {
        return getDatabase(name).getConfiguration(propName);
    }
    
    public void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv) {
        getDatabase(destinationDB).copyFrom(sourceDB, typeNames, ignoreDbsv);
    }

    public void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv) {
        getDatabase(whereDB).deleteFrom(provenienceDB, typeNames, ignoreDbsv);
    }

    public String getDataSourceName(String lookupFile) {
        return TransactionProvider.findDatabaseName(lookupFile);
    }

    public boolean supportsUTF8() {
        return Database.supportsUTF8();
    }

    public String getQueryLanguage() {
        return "oql";
    }
    
    public CRUDOperationProvider getCRUD() {
        if(singleton == null) {
            singleton = new MakumbaCRUDOperationProvider();
        }
        return singleton;
        
    }

    public static Database findDatabase(String s) {
        return getDatabase(TransactionProvider.findDatabaseName(s));
    }

}
