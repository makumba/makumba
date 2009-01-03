package org.makumba.providers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import org.makumba.Transaction;

/**
 * This class is a facade for creating different kinds of TransactionProviders. Its constructor knows from a
 * Configuration (or in the future maybe through other means) which implementation to use, and provides this
 * implementation methods to its client, without revealing the implementation used.
 *
 * @author Manuel Gay
 * @version $Id: TransactionProvider.java,v 1.1 28.09.2007 15:49:55 Manuel Exp $
 */
public abstract class TransactionProvider {
    
    public static final String CONNECTION_PREFIX = "connection.";
    
    public static final String CONNECTION_URL = "url";
    
    public static final String CONNECTION_USERNAME = "username";

    public static final String CONNECTION_PASSWORD = "password";
    

    private static class SingletonHolder {
        
        private static TransactionProvider singleton;
        
        static {
            try {
                Logger.getLogger("org.makumba.providers").info(
                    "Instantiating TransactionProvider '" + Configuration.getDefaultTransactionProviderClass() + "'");
                Method getInstance = Class.forName(Configuration.getDefaultTransactionProviderClass()).getDeclaredMethod("getInstance", null);
                 singleton = (TransactionProvider) getInstance.invoke(null, null);
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
            
    }

    public static TransactionProvider getInstance() {
        return SingletonHolder.singleton;
    }

    public abstract Transaction getConnectionTo(String name);

    public String getDefaultDataSourceName() {
        return Configuration.getDefaultDataSourceName();
    }
    
    public abstract String getDatabaseProperty(String name, String propName);

    public abstract void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv);

    public abstract void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv);

    public abstract CRUDOperationProvider getCRUD();

    public abstract String getQueryLanguage();
    
}
