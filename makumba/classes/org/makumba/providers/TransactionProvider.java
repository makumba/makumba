package org.makumba.providers;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.makumba.Transaction;
import org.makumba.commons.SingletonHolder;
import org.makumba.db.hibernate.HibernateTransactionProvider;
import org.makumba.db.makumba.MakumbaTransactionProvider;
import org.makumba.providers.Configuration.DataSourceType;

/**
 * This class is a facade for creating different kinds of TransactionProviders.
 * 
 * @author Manuel Gay
 * @version $Id: TransactionProvider.java,v 1.1 28.09.2007 15:49:55 Manuel Exp $
 */
public abstract class TransactionProvider implements SingletonHolder {

    private static String[] transactionProviders = { "makumba", "org.makumba.db.makumba.MakumbaTransactionProvider",
            "hibernate", "org.makumba.db.hibernate.HibernateTransactionProvider" };

    static final Map<String, TransactionProvider> providerInstances = new HashMap<String, TransactionProvider>();

    /**
     * Puts the TransactionProviders into a Map
     */
    static {
        for (int i = 0; i < transactionProviders.length; i += 2)
            try {
                Method getInstance = Class.forName(transactionProviders[i + 1]).getDeclaredMethod("getInstance", null);
                TransactionProvider tp = (TransactionProvider) getInstance.invoke(null, null);
                providerInstances.put(transactionProviders[i], tp);
            } catch (Throwable t) {
                t.printStackTrace();
            }
    }

    public static final String CONNECTION_PREFIX = "connection.";

    public static final String CONNECTION_URL = "url";

    public static final String CONNECTION_USERNAME = "username";

    public static final String CONNECTION_PASSWORD = "password";

    /**
     * Gives an instance of a {@link TransactionProvider}.
     */
    public static TransactionProvider getInstance() {
        return providerInstances.get(Configuration.getDefaultDatabaseLayer());
    }

    /**
     * Opens a {@link Transaction} with the specified dataSource.
     * 
     * @param name
     *            the name of the dataSource to connect to
     * @return a {@link Transaction}
     */
    public Transaction getConnectionTo(String name) {
        switch (Configuration.getDataSourceType(name)) {
            case makumba:
                return providerInstances.get(DataSourceType.makumba.toString()).getTransaction(name);
            case hibernate:
                return providerInstances.get(DataSourceType.makumba.toString()).getTransaction(name);
        }
        return null;
    }

    /**
     * gets a connection from the TransactionProvider needed by the dataSource. if the concrete TransactionProvider the
     * method was called from is not of the right type, link it dynamically to the right one
     */
    protected Transaction getConnectionTo(String name, TransactionProvider instance) {
        switch (Configuration.getDataSourceType(name)) {
            case makumba:
                if (instance instanceof HibernateTransactionProvider) {
                    instance.setTransactionProvider(providerInstances.get(DataSourceType.makumba.toString()));
                }
                return providerInstances.get(DataSourceType.makumba.toString()).getTransaction(name);
            case hibernate:
                if (instance instanceof MakumbaTransactionProvider) {
                    instance.setTransactionProvider(providerInstances.get(DataSourceType.hibernate.toString()));
                }
                return providerInstances.get(DataSourceType.hibernate.toString()).getTransaction(name);

        }
        return null;
    }

    public String getDefaultDataSourceName() {
        return Configuration.getDefaultDataSourceName();
    }

    protected abstract Transaction getTransaction(String name);

    protected abstract void setTransactionProvider(TransactionProvider tp);

    public abstract CRUDOperationProvider getCRUD();

    public abstract String getQueryLanguage();
    
    public TransactionProvider() {
        org.makumba.commons.SingletonReleaser.register(this);
    }
    
    public void release() {
        providerInstances.clear();
    }

}
