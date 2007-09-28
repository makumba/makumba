package org.makumba.commons;

/**
 * This class knows how to read Makumba configuration and is used internally by different classes that need specifc services.
 * It can be seen as a service dispatcher in a way.
 * 
 * @author Manuel Gay
 * @version $Id: Configuration.java,v 1.1 28.09.2007 11:15:00 Manuel Exp $
 */
public class Configuration {
    
    private String defaultDataDefinitionProvider = "org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory";
    
    public String getDefaultDataDefinitionProviderClass() {
        return this.defaultDataDefinitionProvider;
    }
    
    /**
     * Gives the data definition provider implementation to use
     * @return a String containing the class name of the data definition provider implementation
     */
    public String getDataDefinitionProviderClass() {
        
        // FIXME this should lookup a configuration file and return whatever is specified there
        return getDefaultDataDefinitionProviderClass();
    }

}
