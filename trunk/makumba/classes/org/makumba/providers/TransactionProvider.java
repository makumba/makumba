package org.makumba.providers;

import java.util.Enumeration;
import java.util.Properties;

import org.makumba.Transaction;
import org.makumba.commons.ClassResource;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.commons.RuntimeWrappedException;

/**
 * This class is a facade for creating different kinds of TransactionProviders. Its constructor knows from a
 * Configuration (or in the future maybe through other means) which implementation to use, and provides this
 * implementation methods to its client, without revealing the implementation used.
 * 
 * TODO this is not the best way of doing things, needs refactoring (TransactionProvider as superclass for the other guys)

 * @author Manuel Gay
 * @version $Id: TransactionProvider.java,v 1.1 28.09.2007 15:49:55 Manuel Exp $
 */
public class TransactionProvider implements TransactionProviderInterface {
    
    private TransactionProviderInterface transactionProviderImplementation;    
    
    private static TransactionProvider singleton;
    
    private TransactionProvider() {
        this(new Configuration());
    }
    
    public static TransactionProvider getInstance() {
        if(singleton == null) {
            singleton = new TransactionProvider();
        }
        return singleton;
    }
    
    public TransactionProvider(TransactionProviderInterface tpi){
        this.transactionProviderImplementation=tpi;
    }
    
    public TransactionProvider(Configuration config) {
        try {
            this.transactionProviderImplementation = (TransactionProviderInterface) Class.forName(config.getDefaultTransactionProviderClass()).newInstance();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public Transaction getConnectionTo(String name) {
        return transactionProviderImplementation.getConnectionTo(name);
    }

    public String getDefaultDataSourceName() {
        return transactionProviderImplementation.getDefaultDataSourceName();
    }


    public String getDatabaseProperty(String name, String propName) {
        return transactionProviderImplementation.getDatabaseProperty(name, propName);
    }

    public void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv) {
        transactionProviderImplementation._copy(sourceDB, destinationDB, typeNames, ignoreDbsv);
    }

    public void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv) {
        transactionProviderImplementation._delete(whereDB, provenienceDB, typeNames, ignoreDbsv);
    }

    public String getDataSourceName(String lookupFile) {
        return transactionProviderImplementation.getDataSourceName(lookupFile);
    }

    public boolean supportsUTF8() {
        return transactionProviderImplementation.supportsUTF8();
    }
    
    public CRUDOperationProvider getCRUD() {
        return transactionProviderImplementation.getCRUD();
    }
    
    public static String findInHostProperties(Properties p, String str){
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
                (n= TransactionProvider.findInHostProperties(p, userDir))!=null 
                ||
                wbp!=null &&((n= TransactionProvider.findInHostProperties(p, wbp))!=null)
                ||
                (n= TransactionProvider.findInHostProperties(p, "default"))!=null
                )
          
            return n;
        
        return p.getProperty("default");
    }

    public static String findDatabaseName(String s) {
        try {
            return TransactionProvider.findDatabaseName((Properties) NamedResources.getStaticCache(TransactionProvider.dbsel).getResource(s));
        } catch (RuntimeWrappedException e) {
            if (e.getCause() instanceof org.makumba.MakumbaError)
                throw (org.makumba.MakumbaError) e.getCause();
            throw e;
        }
    }
    
    public static int dbsel = NamedResources.makeStaticCache("Database selection files", new NamedResourceFactory() {
        
        private static final long serialVersionUID = 1L;
    
        protected Object makeResource(Object nm) {
            Properties p = new Properties();
            try {
                java.io.InputStream input = org.makumba.commons.ClassResource.get((String) nm).openStream();
                p.load(input);
                input.close();
            } catch (Exception e) {
                throw new org.makumba.ConfigFileError((String) nm);
            }
            return p;
        }
    });

}
