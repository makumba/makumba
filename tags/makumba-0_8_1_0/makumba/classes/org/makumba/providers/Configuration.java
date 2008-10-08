package org.makumba.providers;

import java.io.Serializable;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * This class knows how to read Makumba configuration and is used internally by different classes that need specific
 * services. It can be seen as a service dispatcher in a way.
 * 
 * @author Manuel Gay
 * @version $Id: Configuration.java,v 1.1 28.09.2007 11:15:00 Manuel Exp $
 */
public class Configuration implements Serializable {

    public static final String PLACEHOLDER_CONTEXT_PATH = "_CONTEXT_PATH_";

    public static String defaultClientSideValidation = "live";

    public static boolean defaultReloadFormOnError = true;

    private static final long serialVersionUID = 1L;

    private static final String defaultDataDefinitionProvider = "org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory";

    private String defaultTransactionProvider = "org.makumba.db.makumba.MakumbaTransactionProvider";

    private static Properties controllerConfig;

    // calendar editor
    private static final String KEY_CALENDAR_EDITOR = "calendarEditor";

    private static final String KEY_CALENDAR_EDITOR_LINK = "calendarEditorLink";

    // developer tools
    public static final String KEY_MDD_VIEWER = "mddViewer";
    
    public static final String KEY_BL_METHODS = "blMethods";

    public static final String KEY_JAVA_VIEWER = "javaViewer";

    private static final String KEY_LOGIC_DISCOVERY = "logicDiscovery";

    private static final String KEY_CODE_GENERATOR = "codeGenerator";

    public static final String KEY_DATA_QUERY_TOOL = "dataQueryTool";

    private static final String KEY_DATA_OBJECT_VIEWER = "dataObjectViewer";

    public static final String KEY_DATA_LISTER = "dataLister";

    public static final String KEY_OBJECT_ID_CONVERTER = "objectIdConverter";

    public static final String KEY_REFERENCE_CHECKER = "referenceChecker";

    private static final String KEY_REPOSITORY_URL = "repositoryURL";

    private static final String KEY_REPOSITORY_LINK_TEXT = "repositoryLinkText";

    // makumba servlets
    private static final String KEY_MAKUMBA_VALUE_EDITOR = "makumbaValueEditor";

    private static final String KEY_MAKUMBA_UNIQUENESS_VALIDATOR = "makumbaUniquenessValidator";

    private static final String KEY_MAKUMBA_RESOURCES = "makumbaResources";

    private static final String KEY_MAKUMBA_DOWNLOAD = "makumbaDownload";

    private static Properties makumbaDefaults = new Properties();

    private static final Hashtable<String, String> allGenericDeveloperToolsMap = new Hashtable<String, String>();

    public static Hashtable<String, String> getAllGenericDeveloperToolsMap() {
        return allGenericDeveloperToolsMap;
    }

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

        } catch (Exception e) {
            controllerConfig = null;
        }
        try {
            URL urlMakumbaDefaults = org.makumba.commons.ClassResource.get("internalDefaultMakumbaDefaults.properties");
            makumbaDefaults.load(urlMakumbaDefaults.openStream());
            Properties appMakumbaDefaults = new Properties();
            URL urlAppMakumbaDefaults = org.makumba.commons.ClassResource.get("MakumbaDefaults.properties");
            if (urlAppMakumbaDefaults != null) {
                appMakumbaDefaults.load(urlAppMakumbaDefaults.openStream());
                for (Object keyObject : makumbaDefaults.keySet()) {
                    String key = (String) keyObject;
                    if (StringUtils.isNotBlank(appMakumbaDefaults.getProperty(key))) {
                        makumbaDefaults.setProperty(key, appMakumbaDefaults.getProperty(key));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        allGenericDeveloperToolsMap.put(KEY_MDD_VIEWER, "Mdd viewer");
        allGenericDeveloperToolsMap.put(KEY_JAVA_VIEWER, "Business logics viewer");
        allGenericDeveloperToolsMap.put(KEY_DATA_LISTER, "Data browser");
        allGenericDeveloperToolsMap.put(KEY_DATA_QUERY_TOOL, "Data query");
        allGenericDeveloperToolsMap.put(KEY_OBJECT_ID_CONVERTER, "Pointer value converter");
        allGenericDeveloperToolsMap.put(KEY_REFERENCE_CHECKER, "Reference checker");
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
        return makumbaDefaults.getProperty(KEY_CALENDAR_EDITOR).equals("true");
    }

    public static String getDefaultCalendarEditorLink(String contextPath) {
        return makumbaDefaults.getProperty(KEY_CALENDAR_EDITOR_LINK).replaceAll(PLACEHOLDER_CONTEXT_PATH, contextPath);
    }

    public static String getMddViewerLocation() {
        return makumbaDefaults.getProperty(KEY_MDD_VIEWER);
    }

    public static String getBLMethodsLocation() {
        return makumbaDefaults.getProperty(KEY_BL_METHODS);
    }

    public static String getJavaViewerLocation() {
        return makumbaDefaults.getProperty(KEY_JAVA_VIEWER);
    }

    public static String getLogicDiscoveryViewerLocation() {
        return makumbaDefaults.getProperty(KEY_LOGIC_DISCOVERY);
    }

    public static String getDataViewerLocation() {
        return makumbaDefaults.getProperty(KEY_DATA_OBJECT_VIEWER);
    }

    public static String getDataListerLocation() {
        return makumbaDefaults.getProperty(KEY_DATA_LISTER);
    }

    public static String getDataQueryLocation() {
        return makumbaDefaults.getProperty(KEY_DATA_QUERY_TOOL);
    }

    public static String getObjectIdConverterLocation() {
        return makumbaDefaults.getProperty(KEY_OBJECT_ID_CONVERTER);
    }

    public static String getReferenceCheckerLocation() {
        return makumbaDefaults.getProperty(KEY_REFERENCE_CHECKER);
    }

    public static String getCodeGeneratorLocation() {
        return makumbaDefaults.getProperty(KEY_CODE_GENERATOR);
    }

    public static String getRepositoryURL() {
        return makumbaDefaults.getProperty(KEY_REPOSITORY_URL);
    }

    public static String getRepositoryLinkText() {
        return makumbaDefaults.getProperty(KEY_REPOSITORY_LINK_TEXT);
    }

    public static String getMakumbaValueEditorLocation() {
        return makumbaDefaults.getProperty(KEY_MAKUMBA_VALUE_EDITOR);
    }

    public static String getMakumbaUniqueLocation() {
        return makumbaDefaults.getProperty(KEY_MAKUMBA_UNIQUENESS_VALIDATOR);
    }

    public static String getMakumbaResourcesLocation() {
        return makumbaDefaults.getProperty(KEY_MAKUMBA_RESOURCES);
    }

    public static String getMakumbaDownloadLocation() {
        return makumbaDefaults.getProperty(KEY_MAKUMBA_DOWNLOAD);
    }

    public static String getConfigProperty(String key) {
        return makumbaDefaults.getProperty(key);
    }

}
