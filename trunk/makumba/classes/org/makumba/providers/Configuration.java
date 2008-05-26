package org.makumba.providers;

import java.io.Serializable;
import java.net.URL;
import java.util.Properties;

import org.makumba.commons.MakumbaResourceServlet;

/**
 * This class knows how to read Makumba configuration and is used internally by different classes that need specifc
 * services. It can be seen as a service dispatcher in a way.
 * 
 * @author Manuel Gay
 * @version $Id: Configuration.java,v 1.1 28.09.2007 11:15:00 Manuel Exp $
 */
public class Configuration implements Serializable {

    private static final String PLACEHOLDER_CONTEXT_PATH = "_CONTEXT_PATH_";

    public static String defaultClientSideValidation = "live";

    public static boolean defaultReloadFormOnError = true;

    private static boolean defaultCalendarEditor = true;

    private static String defaultCalendarEditorLink = "<img border=\"0\" src=\"" + PLACEHOLDER_CONTEXT_PATH + "/"
            + MakumbaResourceServlet.resourceDirectory + "/" + MakumbaResourceServlet.RESOURCE_PATH_IMAGES
            + "calendar.gif\">";

    private static final long serialVersionUID = 1L;

    private static final String defaultDataDefinitionProvider = "org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory";

    private String defaultTransactionProvider = "org.makumba.db.makumba.MakumbaTransactionProvider";

    private static Properties controllerConfig;

    static {
        controllerConfig = new Properties();
        try {
            URL controllerURL = org.makumba.commons.ClassResource.get("MakumbaController.properties");
            controllerConfig.load(controllerURL.openStream());

            // FIXME: these other config details should most likely be loaded from a different config file
            defaultClientSideValidation = controllerConfig.getProperty("defaultClientSideValidation",
                defaultClientSideValidation);
            defaultReloadFormOnError = Boolean.parseBoolean(controllerConfig.getProperty("defaultReloadFormOnError",
                String.valueOf(defaultReloadFormOnError)));

            defaultCalendarEditorLink = controllerConfig.getProperty("defaultCalendarEditorLink",
                defaultCalendarEditorLink);
            defaultCalendarEditor = Boolean.parseBoolean(controllerConfig.getProperty("defaultCalendarEditor",
                String.valueOf(defaultCalendarEditor)));
            
        } catch (Exception e) {
            controllerConfig = null;
        }
    }

    public Configuration() {
        if (controllerConfig != null) {
            defaultTransactionProvider = controllerConfig.getProperty("defaultTransactionProvider",
                defaultTransactionProvider);
        }
    }

    private String getDefaultDataDefinitionProviderClass() {
        return defaultDataDefinitionProvider;
    }

    private String dataDefinitionProvider = null;

    /**
     * Gives the data definition provider implementation to use
     * 
     * @return a String containing the class name of the data definition provider implementation
     */
    public String getDataDefinitionProviderClass() {

        // FIXME this should lookup a configuration file and return whatever is specified there
        return (dataDefinitionProvider == null) ? getDefaultDataDefinitionProviderClass() : dataDefinitionProvider;
    }

    /**
     * Gives the transaction provider implementation to use
     * 
     * @return a String containing the class name of the transaction provider implementation
     */
    public String getDefaultTransactionProviderClass() {
        return defaultTransactionProvider;
    }

    public void setDataDefinitionProvider(String ddp) {
        this.dataDefinitionProvider = ddp;
    }

    public void setDefaultTransactionProvider(String defaultTransactionProvider) {
        this.defaultTransactionProvider = defaultTransactionProvider;
    }

    public static String getClientSideValidationDefault() {
        return defaultClientSideValidation;
    }

    public static boolean getReloadFormOnErrorDefault() {
        return defaultReloadFormOnError;
    }

    public static boolean getCalendarEditorDefault() {
        return defaultCalendarEditor;
    }

    public static String getDefaultCalendarEditorLink(String contextPath) {
        return defaultCalendarEditorLink.replaceAll(PLACEHOLDER_CONTEXT_PATH, contextPath);
    }

}
