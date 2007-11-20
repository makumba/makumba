package org.makumba.providers;

import java.util.Properties;

import org.makumba.Transaction;
import org.makumba.commons.Configuration;

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
    
    
    public TransactionProvider() {
        this(new Configuration());
    }
    
    public TransactionProvider(Configuration config) {
        try {
            this.transactionProviderImplementation = (TransactionProviderInterface) Class.forName(config.getTransactionProviderClass()).newInstance();
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
    
    public Properties getDataSourceConfiguration(String name) {
        return transactionProviderImplementation.getDataSourceConfiguration(name);
    }
    
}
