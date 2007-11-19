package org.makumba.db.hibernate;

import java.util.Properties;

import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.ClassResource;
import org.makumba.commons.Configuration;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProviderInterface;

/**
 * FIXME reimplement the hierarchy of this provider, it should extend some TransactionProvider
 * 
 * @author Manuel Gay
 * @version $Id: HibernateTransactionProvider.java,v 1.1 06.11.2007 11:01:32 Manuel Exp $
 */
public class HibernateTransactionProvider implements TransactionProviderInterface {
    
    private static HibernateCRUDOperationProvider singleton;
    
    private Configuration config = new Configuration();
    
    private DataDefinitionProvider ddp = new DataDefinitionProvider(config);
    
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
        return new HibernateTransaction(name, ddp, this);
    }

    public String getDataSourceName(String lookupFile) {
        return Configuration.findDatabaseName(lookupFile);
    }

    public String getDatabaseProperty(String name, String propName) {
        throw new MakumbaError("Not implemented");
    }

    public String getDefaultDataSourceName() {
        return Configuration.findDatabaseName("MakumbaDatabase.properties");
    }

    private Object sf;
    
    public Object getHibernateSessionFactory(String name) {
            if(sf==null && ClassResource.get(name+".cfg.xml")!=null){
                sf= HibernateSFManager.getSF(name+".cfg.xml", true);
            }
            return sf;
        
    }

    public boolean supportsUTF8() {
        throw new MakumbaError("Not implemented");
    }

    public Properties getDataSourceConfiguration(String name) {
        throw new MakumbaError("Not implemented");
    }

}
