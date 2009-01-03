package org.makumba.db.hibernate;

import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.Transaction;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * This class is a Hibernate-specific implementation of a {@link TransactionProvider}.
 * 
 * FIXME see what to do with the old _copy, ... etc. methods
 * 
 * @author Manuel Gay
 * @version $Id: HibernateTransactionProvider.java,v 1.1 06.11.2007 11:01:32 Manuel Exp $
 */
public class HibernateTransactionProvider extends TransactionProvider {
   
    private static class SingletonHolder {
        private final static HibernateTransactionProvider singleton = new HibernateTransactionProvider();  
    }
    
    private static class CRUDOperationProviderSingletonHolder {
        private static CRUDOperationProvider singleton = new HibernateCRUDOperationProvider();

    }
    
    public static HibernateTransactionProvider getInstance() {
        return SingletonHolder.singleton;
    }
    
    private HibernateTransactionProvider() {
        
    }
    
    public void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv) {
        throw new MakumbaError("Not implemented");
    }

    public void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv) {
        throw new MakumbaError("Not implemented");

    }

    public CRUDOperationProvider getCRUD() {
        return CRUDOperationProviderSingletonHolder.singleton;
    }

    public Transaction getConnectionTo(String name) {
        return new HibernateTransaction(name, DataDefinitionProvider.getInstance(), this);
    }

    public String getDatabaseProperty(String name, String propName) {
        throw new MakumbaError("Not implemented");
    }
    
    public Object getHibernateSessionFactory(String name) {
        return HibernateSFManager.getSF(name);
    }

    public String getQueryLanguage() {
        return "hql";
    }
}
