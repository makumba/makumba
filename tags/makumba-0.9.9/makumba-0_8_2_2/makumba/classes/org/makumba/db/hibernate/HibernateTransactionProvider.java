package org.makumba.db.hibernate;

import org.makumba.HibernateSFManager;
import org.makumba.Transaction;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.Configuration.DataSourceType;

/**
 * This class is a Hibernate-specific implementation of a {@link TransactionProvider}.
 * 
 * FIXME see what to do with the old _copy, ... etc. methods
 * 
 * @author Manuel Gay
 * @version $Id: HibernateTransactionProvider.java,v 1.1 06.11.2007 11:01:32 Manuel Exp $
 */
public class HibernateTransactionProvider extends TransactionProvider {
   
    private static class SingletonHolder implements org.makumba.commons.SingletonHolder {
        private static TransactionProvider singleton = new HibernateTransactionProvider();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }
    
    private static class CRUDOperationProviderSingletonHolder implements org.makumba.commons.SingletonHolder {
        private static CRUDOperationProvider singleton = new HibernateCRUDOperationProvider();
        
        public void release() {
            singleton = null;
        }

        public CRUDOperationProviderSingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }

    }
    
    public static TransactionProvider getInstance() {
        return SingletonHolder.singleton;
    }
    
    private HibernateTransactionProvider() {
        
    }

    public CRUDOperationProvider getCRUD() {
        return super.getCRUD(this);
    }

    public Transaction getConnectionTo(String name) {
        return super.getConnectionTo(name, this);
    }
    
    public String getQueryLanguage() {
        return super.getQueryLanguage(this);
    }

    private DataSourceType lastConnectionType = DataSourceType.hibernate;

    @Override
    protected DataSourceType getLastConnectionType() {
        return this.lastConnectionType;
    }

    @Override
    protected void setLastConnectionType(DataSourceType type) {
        this.lastConnectionType = type;
    }

    @Override
    protected CRUDOperationProvider getCRUDInternal() {
        return CRUDOperationProviderSingletonHolder.singleton;
    }

    @Override
    protected String getQueryLanguageInternal() {
        return "hql";
    }
    
    @Override
    protected Transaction getTransaction(String name) {
        return new HibernateTransaction(name, DataDefinitionProvider.getInstance(), this);
    }

    
    public Object getHibernateSessionFactory(String name) {
        return HibernateSFManager.getSF(name);
    }


}
