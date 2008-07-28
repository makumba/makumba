package org.makumba.providers;

import org.makumba.Transaction;

/**
 * This is an interface for a Transaction provider, useful to perform data operations against a datastore.
 * 
 * TODO to be refactored and Transformed into a superclass for the different kind of TPs out there
 * 
 * @author Manuel Gay
 * @version $Id: TransactionProviderInterface.java,v 1.1 28.09.2007 15:43:42 Manuel Exp $
 */
public interface TransactionProviderInterface {
    
    public Transaction getConnectionTo(String name);
    
    public String getDefaultDataSourceName();
    
    public String getDataSourceName(String lookupFile);
    
    public CRUDOperationProvider getCRUD();
    
    public String getQueryLanguage();
    
    public boolean supportsUTF8();
    
    // FIXME moved from MakumbaSystem
    public String getDatabaseProperty(String name, String propName);
    
    //FIXME should not be generic, very specific, still needed?
    public void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv);
    
    //FIXME should not be generic, very specific, still needed?
    public void _delete(String whereDB, String provenienceDB, String[] typeNames, boolean ignoreDbsv);
    
}
