package org.makumba;

import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;

/**
 * This class is used in order to configure the Makumba system.
 * It provides configuration data and providers. 
 * 
 * @author Manuel Gay
 * @version $Id: Configuration.java,v 1.1 2007/08/28 17:08:08 manuel_gay Exp $
 */
public class Configuration {
    
    // here will come all the providers, they will get injected by Spring
    
    private DataDefinitionProvider dataDefinitionProvider;

    public Configuration() {
        //TODO do this with spring
    }
    
    // FIXME this should read in some configuration file
    public static boolean UTF8Enabled = true;

    public static boolean isUTF8Enabled() {
        return UTF8Enabled;
    }

    public DataDefinitionProvider getDataDefinitionProvider() {
        return dataDefinitionProvider;
    }

    public void setDataDefinitionProvider(DataDefinitionProvider dataDefinitionProvider) {
        this.dataDefinitionProvider = dataDefinitionProvider;
    }
    
}
