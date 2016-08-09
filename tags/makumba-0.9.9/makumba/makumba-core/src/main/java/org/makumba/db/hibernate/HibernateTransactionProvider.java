package org.makumba.db.hibernate;

import org.makumba.HibernateSFManager;
import org.makumba.Transaction;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.Configuration.DataSourceType;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * This class is a Hibernate-specific implementation of a {@link TransactionProvider}. FIXME see what to do with the old
 * _copy, ... etc. methods
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: HibernateTransactionProvider.java,v 1.1 06.11.2007 11:01:32 Manuel Exp $
 */
public class HibernateTransactionProvider extends TransactionProvider {

    private static class SingletonHolder {
        private static TransactionProvider singleton = new HibernateTransactionProvider();
    }

    private static class CRUDOperationProviderSingletonHolder {
        private static CRUDOperationProvider singleton = new HibernateCRUDOperationProvider();
    }

    public static TransactionProvider getInstance() {
        return SingletonHolder.singleton;
    }

    private HibernateTransactionProvider() {

    }

    @Override
    public CRUDOperationProvider getCRUD() {
        return super.getCRUD(this);
    }

    @Override
    public Transaction getConnectionTo(String name) {
        return super.getConnectionTo(name, this);
    }

    @Override
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

    @Override
    public void closeDataSource(String dataSourceName) {
    }

}
