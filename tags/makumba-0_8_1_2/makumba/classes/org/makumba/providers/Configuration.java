///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2008  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: SourceViewControllerHandler.java 3224 2008-10-05 22:32:17Z rosso_nero $
//  $Name$
/////////////////////////////////////

package org.makumba.providers;

import java.io.Serializable;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class knows how to read Makumba configuration and is used internally by different classes that need specific
 * services. It can be seen as a service dispatcher in a way.
 * 
 * @author Manuel Gay
 * @version $Id: Configuration.java,v 1.1 28.09.2007 11:15:00 Manuel Exp $
 */
public class Configuration implements Serializable {

    public static final String KEY_CLIENT_SIDE_VALIDATION = "clientSideValidation";

    public static final String KEY_RELOAD_FORM_ON_ERROR = "reloadFormOnError";

    public static final String KEY_DEFAULT_TRANSACTION_PROVIDER = "defaultTransactionProvider";

    public static final String MAKUMBA_CONF = "Makumba.conf";

    private static final String MAKUMBA_CONF_DEFAULT = MAKUMBA_CONF + ".default";

    public static final String PLACEHOLDER_CONTEXT_PATH = "_CONTEXT_PATH_";

    private static String defaultClientSideValidation = "live";

    private static boolean defaultReloadFormOnError = true;

    private static final long serialVersionUID = 1L;

    private static final String defaultDataDefinitionProvider = "org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory";

    // calendar editor
    private static final String KEY_CALENDAR_EDITOR = "calendarEditor";

    private static final String KEY_CALENDAR_EDITOR_LINK = "calendarEditorLink";

    // developer tools
    public static final String KEY_MAKUMBA_TOOLS = "path";

    public static final String KEY_MDD_VIEWER = "mddViewer";

    public static final String KEY_JAVA_VIEWER = "javaViewer";

    public static final String KEY_LOGIC_DISCOVERY = "logicDiscovery";

    public static final String KEY_CODE_GENERATOR = "codeGenerator";

    public static final String KEY_DATA_QUERY_TOOL = "dataQueryTool";

    public static final String KEY_DATA_OBJECT_VIEWER = "dataObjectViewer";

    public static final String KEY_DATA_LISTER = "dataLister";

    public static final String KEY_OBJECT_ID_CONVERTER = "objectIdConverter";

    public static final String KEY_REFERENCE_CHECKER = "referenceChecker";

    public static final String KEY_RELATION_CRAWLER = "relationCrawler";

    public static final String KEY_REPOSITORY_URL = "repositoryURL";

    public static final String KEY_REPOSITORY_LINK_TEXT = "repositoryLinkText";

    // makumba servlets
    public static final String KEY_MAKUMBA_VALUE_EDITOR = "makumbaValueEditor";

    public static final String KEY_MAKUMBA_UNIQUENESS_VALIDATOR = "makumbaUniquenessValidator";

    public static final String KEY_MAKUMBA_RESOURCES = "makumbaResources";

    public static final String KEY_MAKUMBA_DOWNLOAD = "makumbaDownload";

    private static final Map<String, String> allGenericDeveloperToolsMap = ArrayUtils.toMap(new String[][] {
            { KEY_MDD_VIEWER, "Mdd viewer" }, { KEY_JAVA_VIEWER, "Business logics viewer" },
            { KEY_DATA_LISTER, "Data browser" }, { KEY_DATA_QUERY_TOOL, "Data query" },
            { KEY_OBJECT_ID_CONVERTER, "Pointer value converter" }, { KEY_REFERENCE_CHECKER, "Reference checker" },
            { KEY_RELATION_CRAWLER, "Relation crawler" } });

    public static Map<String, String> getAllGenericDeveloperToolsMap() {
        return allGenericDeveloperToolsMap;
    }

    private static MakumbaINIFileReader defaultConfig;

    private static MakumbaINIFileReader applicationConfig;
    static {
        try {
            // the internal default configuration
            URL path = org.makumba.commons.ClassResource.get(MAKUMBA_CONF_DEFAULT);
            Logger.getLogger("org.makumba.config").info("Loading internal default configuration from " + path);
            defaultConfig = new MakumbaINIFileReader(path);

            // application-specific configuration
            URL url = org.makumba.commons.ClassResource.get(MAKUMBA_CONF);
            if (url != null) {
                Logger.getLogger("org.makumba.config").info("Loading application configuration from " + url);
                applicationConfig = new MakumbaINIFileReader(url);
            } else { // if we did not find any configuration, we use the default one
                Logger.getLogger("org.makumba.config").severe(
                    "No application configuration found -> using internal default configuration!");
                applicationConfig = defaultConfig;
                System.out.println(applicationConfig.sectionNames());
                System.out.println(applicationConfig.optionNames("controllerConfig"));
            }

            defaultClientSideValidation = applicationConfig.getStringProperty("controllerConfig",
                KEY_CLIENT_SIDE_VALIDATION, defaultConfig);
            defaultReloadFormOnError = applicationConfig.getBooleanProperty("controllerConfig",
                KEY_RELOAD_FORM_ON_ERROR, defaultConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gives the data definition provider implementation to use
     * 
     * @return a String containing the class name of the data definition provider implementation
     */
    public static String getDataDefinitionProviderClass() {
        // FIXME this should lookup a configuration file and return whatever is specified there
        return defaultDataDefinitionProvider;
    }

    /**
     * Gives the transaction provider implementation to use
     * 
     * @return a String containing the class name of the transaction provider implementation
     */
    public static String getDefaultTransactionProviderClass() {
        return applicationConfig.getStringProperty("controllerConfig", KEY_DEFAULT_TRANSACTION_PROVIDER, defaultConfig);
    }

    public static String getClientSideValidationDefault() {
        return defaultClientSideValidation;
    }

    public static boolean getReloadFormOnErrorDefault() {
        return defaultReloadFormOnError;
    }

    public static boolean getCalendarEditorDefault() {
        return applicationConfig.getBooleanProperty("inputStyleConfig", KEY_CALENDAR_EDITOR, defaultConfig);
    }

    public static String getDefaultCalendarEditorLink(String contextPath) {
        return applicationConfig.getStringProperty("inputStyleConfig", KEY_CALENDAR_EDITOR_LINK, defaultConfig).replaceAll(
            PLACEHOLDER_CONTEXT_PATH, contextPath);
    }

    public static String getRepositoryURL() {
        return applicationConfig.getProperty("makumbaToolConfig", KEY_REPOSITORY_URL);
    }

    public static String getRepositoryLinkText() {
        return applicationConfig.getProperty("makumbaToolConfig", KEY_REPOSITORY_LINK_TEXT);
    }

    public static String getMakumbaToolsLocation() {
        final String property = applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_TOOLS);
        return property.endsWith("/") ? property.substring(0, property.length() - 1) : property;
    }

    public static String getMddViewerLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_MDD_VIEWER);
    }

    public static String getJavaViewerLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_JAVA_VIEWER);
    }

    public static String getLogicDiscoveryViewerLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_LOGIC_DISCOVERY);
    }

    public static String getDataViewerLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_DATA_OBJECT_VIEWER);
    }

    public static String getDataListerLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_DATA_LISTER);
    }

    public static String getDataQueryLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_DATA_QUERY_TOOL);
    }

    public static String getObjectIdConverterLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_OBJECT_ID_CONVERTER);
    }

    public static String getReferenceCheckerLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_REFERENCE_CHECKER);
    }

    public static String getCodeGeneratorLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_CODE_GENERATOR);
    }

    public static String getMakumbaValueEditorLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_VALUE_EDITOR);
    }

    public static String getMakumbaRelationCrawlerLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_RELATION_CRAWLER);
    }

    public static String getMakumbaUniqueLocation() {
        return getMakumbaToolsLocation()
                + applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_UNIQUENESS_VALIDATOR);
    }

    public static String getMakumbaResourcesLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_RESOURCES);
    }

    public static String getMakumbaDownloadLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_DOWNLOAD);
    }

    public static String getConfigProperty(String key) {
        return applicationConfig.getProperty("makumbaToolPaths", key);
    }

    public static Map<String, String> getLogicPackages() {
        return applicationConfig.getProperties("businessLogicPackages");
    }

    public static Map<String, String> getAuthorizationDefinitions() {
        return applicationConfig.getProperties("authorization");
    }

    public static String getApplicationConfigurationSource() {
        return applicationConfig != null ? applicationConfig.getSource() : null;
    }
}
