package org.makumba.commons;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Properties;

/**
 * This class knows how to read Makumba configuration and is used internally by different classes that need specifc services.
 * It can be seen as a service dispatcher in a way.
 * 
 * @author Manuel Gay
 * @version $Id: Configuration.java,v 1.1 28.09.2007 11:15:00 Manuel Exp $
 */
public class Configuration implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private static final String defaultDataDefinitionProvider = "org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory";
    
    private String defaultTransactionProvider = "org.makumba.db.makumba.MakumbaTransactionProvider";
    
    private String getDefaultDataDefinitionProviderClass() {
        return defaultDataDefinitionProvider;
    }
    
    private String getDefaultTransactionProviderClass() {
        return defaultTransactionProvider;
    }
    
    private String dataDefinitionProvider = null;
    
    private String transactionProvider = null;
    
    /**
     * Gives the data definition provider implementation to use
     * @return a String containing the class name of the data definition provider implementation
     */
    public String getDataDefinitionProviderClass() {
        
        // FIXME this should lookup a configuration file and return whatever is specified there
        return (dataDefinitionProvider == null) ? getDefaultDataDefinitionProviderClass() : dataDefinitionProvider;
    }
    
    /**
     * Gives the transaction provider implementation to use
     * @return a String containing the class name of the transaction provider implementation
     */
    public String getTransactionProviderClass() {
        
        // FIXME this should lookup a configuration file and return whatever is specified there
        return (transactionProvider == null) ? getDefaultTransactionProviderClass() : transactionProvider;
        
    }
    
    public void setDataDefinitionProvider(String ddp) {
        this.dataDefinitionProvider = ddp;
    }
    
    public void setTransactionProvider(String tp) {
        this.transactionProvider = tp;
    }

    //FIXME should be somewhere else
    static String findInHostProperties(Properties p, String str){
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
     * 
     * FIXME should be somewhere else
     * 
     */
    public static String findDatabaseName(Properties p) {
        String userDir= System.getProperty("user.dir");
        String n;
        java.net.URL u= ClassResource.get("/"); 
        String wbp= u!=null?u.toString():null;
        
        if(
                (n= findInHostProperties(p, userDir))!=null 
                ||
                wbp!=null &&((n= findInHostProperties(p, wbp))!=null)
                ||
                (n= findInHostProperties(p, "default"))!=null
                )
          
            return n;
        
        return p.getProperty("default");
    }
    
    //FIXME should be somewhere else
    static int dbsel = NamedResources.makeStaticCache("Database selection files", new NamedResourceFactory() {
        
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

    //FIXME should be somewhere else
    public static String findDatabaseName(String s) {
        try {
            return findDatabaseName((Properties) NamedResources.getStaticCache(dbsel).getResource(s));
        } catch (RuntimeWrappedException e) {
            if (e.getCause() instanceof org.makumba.MakumbaError)
                throw (org.makumba.MakumbaError) e.getCause();
            throw e;
        }
    }

    public void setDefaultTransactionProvider(String defaultTransactionProvider) {
        this.defaultTransactionProvider = defaultTransactionProvider;
    }

}
