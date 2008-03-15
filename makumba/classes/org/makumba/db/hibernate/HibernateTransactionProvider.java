package org.makumba.db.hibernate;

import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.Transaction;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.TransactionProviderInterface;

/**
 * This class is a Hibernate-specific implementation of a {@link TransactionProviderInterface}.
 * 
 * FIXME reimplement the hierarchy of this provider, i.e. fix the Configuration problem.
 * FIXME see what to do with the old _copy, ... etc. methods
 * 
 * @author Manuel Gay
 * @version $Id: HibernateTransactionProvider.java,v 1.1 06.11.2007 11:01:32 Manuel Exp $
 */
public class HibernateTransactionProvider implements TransactionProviderInterface {
    
    private static HibernateCRUDOperationProvider singleton;
    
    public void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv) {
        throw new MakumbaError("Not implemented");
    }

    public void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv) {
        throw new MakumbaError("Not implemented");

    }

    public CRUDOperationProvider getCRUD() {
        if(singleton == null) {
            singleton = new HibernateCRUDOperationProvider();
        }
        return singleton;
    }

    public Transaction getConnectionTo(String name) {
        return new HibernateTransaction(name, DataDefinitionProvider.getInstance(), this);
    }

    public String getDataSourceName(String lookupFile) {
        return TransactionProvider.findDatabaseName(lookupFile);
    }

    public String getDatabaseProperty(String name, String propName) {
        throw new MakumbaError("Not implemented");
    }

    public String getDefaultDataSourceName() {
        return TransactionProvider.findDatabaseName("MakumbaDatabase.properties");
    }
    
    public Object getHibernateSessionFactory(String name) {
        return HibernateSFManager.getSF(name);
    }

    public boolean supportsUTF8() {
        throw new MakumbaError("Not implemented");
    }
}
