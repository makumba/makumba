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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.makumba.ConfigurationError;
import org.makumba.commons.ClassResource;
import org.makumba.commons.CollectionUtils;

/**
 * This class knows how to read Makumba configuration and is used internally by different classes that need specific
 * services. It can be seen as a service dispatcher in a way.
 * 
 * @author Manuel Gay
 * @author Rudolf Mayer
 * @version $Id: Configuration.java,v 1.1 28.09.2007 11:15:00 Manuel Exp $
 */
public class Configuration implements Serializable {

    public static final String PROPERTY_NOT_SET = "PROPERTY_NOT_SET";

    public static final String KEY_CLIENT_SIDE_VALIDATION = "clientSideValidation";

    public static final String KEY_RELOAD_FORM_ON_ERROR = "reloadFormOnError";

    public static final String KEY_FORM_ANNOTATION = "formAnnotation";

    public static final String KEY_DEFAULT_DATABASE_LAYER = "defaultDatabaseLayer";

    public static final String MAKUMBA_CONF = "Makumba.conf";

    private static final String MAKUMBA_CONF_DEFAULT = MAKUMBA_CONF + ".default";

    public static final String PLACEHOLDER_CONTEXT_PATH = "_CONTEXT_PATH_";
    
    public static final String PLACEHOLDER_UNIQUENESS_SERVLET_PATH = "_UNIQUENESS_SERVLET_PATH_";

    private static String defaultClientSideValidation = "live";

    private static boolean defaultReloadFormOnError = true;

    private static String defaultFormAnnotation = "after";

    private static final long serialVersionUID = 1L;

    private static final String KEY_DATADEFINITIONPROVIDER = "dataDefinitionProvider";
    
    private static final String KEY_QUERYFUNCTIONINLINER = "queryFunctionInliner";
    
    public static final String MDD_DATADEFINITIONPROVIDER = "mdd";
    
    public static final String RECORDINFO_DATADEFINITIONPROVIDER = "recordinfo";
    
    // calendar editor
    public static final String KEY_CALENDAR_EDITOR = "calendarEditor";

    public static final String KEY_CALENDAR_EDITOR_LINK = "calendarEditorLink";

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

    // source code repository links
    public static final String KEY_REPOSITORY_URL = "repositoryURL";

    public static final String KEY_REPOSITORY_LINK_TEXT = "repositoryLinkText";

    public static final String KEY_USE_DEFAULT_RESPONSE_STYLES = "useDefaultResponseStyles";
    
    // error logging to the database
    
    public static final String KEY_DB_ERROR_LOG = "logErrors";
    
    // i18n
    public static final String KEY_DEFAULT_LANG = "defaultLanguage";
    public static final String KEY_LANG_PARAM = "languageParameterName";
    public static final String KEY_LANG_ATTRIBUTE = "languageAttributeName";

    // makumba servlets
    public static final String KEY_MAKUMBA_VALUE_EDITOR = "makumbaValueEditor";

    public static final String KEY_MAKUMBA_UNIQUENESS_VALIDATOR = "makumbaUniquenessValidator";

    public static final String KEY_MAKUMBA_AUTOCOMPLETE = "makumbaAutoComplete";

    public static final String KEY_MAKUMBA_RESOURCES = "makumbaResources";

    public static final String KEY_MAKUMBA_DOWNLOAD = "makumbaDownload";

    public static final String KEY_MAKUMBA_CACHE_CLEANER = "makumbaCacheCleaner";

    private static final Map<String, String> allGenericDeveloperToolsMap = CollectionUtils.toMap(new String[][] {
            { KEY_MDD_VIEWER, "Mdd viewer" }, { KEY_JAVA_VIEWER, "Business logics viewer" },
            { KEY_DATA_LISTER, "Data browser" }, { KEY_DATA_QUERY_TOOL, "Data query" },
            { KEY_OBJECT_ID_CONVERTER, "Pointer value converter" }, { KEY_REFERENCE_CHECKER, "Reference checker" },
            { KEY_RELATION_CRAWLER, "Relation crawler" } });

    public static Map<String, String> getAllGenericDeveloperToolsMap() {
        return allGenericDeveloperToolsMap;
    }

    private static Map<String, ConfiguredDataSource> configuredDataSources = new HashMap<String, ConfiguredDataSource>();

    private static MakumbaINIConfiguration defaultConfig;

    private static MakumbaINIConfiguration applicationConfig;
    
    private static Object loadLock = new Object();

    static Logger logger = Logger.getLogger("org.makumba.config");

    static {
        try {
            // the internal default configuration
            URL path = org.makumba.commons.ClassResource.get(MAKUMBA_CONF_DEFAULT);
            logger.info("Loading internal default configuration from " + path);
            defaultConfig = new MakumbaINIConfiguration(path);

            // application-specific configuration
            URL url = org.makumba.commons.ClassResource.get(MAKUMBA_CONF);
            if (url != null) {
                logger.info("Loading application configuration from " + url);
                synchronized (loadLock) {
                    applicationConfig = new MakumbaINIConfiguration(url, defaultConfig);
                }
                
            } else { // if we did not find any configuration, we shout. we need an application configuration for the
                // dataSource config.
                logger.severe("No application configuration found!");
                throw new ConfigurationError(
                        "Could not find application configuration file Makumba.conf in WEB-INF/classes!");
            }

            defaultReloadFormOnError = applicationConfig.getBooleanProperty("controllerConfig",
                KEY_RELOAD_FORM_ON_ERROR);
            // FIXME: check if the value in the file is ok, throw an exception otherwise
            defaultClientSideValidation = applicationConfig.getProperty("controllerConfig",
                KEY_CLIENT_SIDE_VALIDATION);
            // FIXME: check if the value in the file is ok, throw an exception otherwise
            defaultFormAnnotation = applicationConfig.getProperty("controllerConfig", KEY_FORM_ANNOTATION);

            buildConfiguredDataSources();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** builds the data sources for the configuration. **/
    private static void buildConfiguredDataSources() {
        for (Iterator<String> iterator = applicationConfig.getSections().iterator(); iterator.hasNext();) {
            String section = iterator.next();

            // expect something like
            // dataSource:<name> host:<host> path:<path> web-app:<web-app>

            if (section.startsWith("dataSource:")) {
                ConfiguredDataSource c = buildConfiguredDataSource(section);
                configuredDataSources.put(c.toString(), c);
            }

        }
    }

    /** builds a {@link ConfiguredDataSource} based on a dataSource section **/
    private static ConfiguredDataSource buildConfiguredDataSource(String section) throws ConfigurationError {
        String name = null, host = null, path = null;
        StringTokenizer st = new StringTokenizer(section, " ");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int n = token.indexOf(":");
            if (n > -1) {
                String tokenName = token.substring(0, n);
                String tokenValue = token.substring(n + 1);

                if (org.makumba.commons.StringUtils.equalsAny(tokenName, "dataSource", "host", "path")) {

                    if (StringUtils.isBlank(tokenValue)) {
                        throw new ConfigurationError("Property " + tokenName + " has no value");
                    }

                    if (tokenName.equals("dataSource")) {
                        name = tokenValue;
                    } else if (tokenName.equals("host")) {
                        host = tokenValue;
                    } else if (tokenName.equals("path")) {
                        path = tokenValue;
                    }
                } else {
                    throw new ConfigurationError("Invalid property '" + token + "' in dataSource section [" + section
                            + "]. Correct syntax is [dataSource:<name> host:<host> path:<path>}]");
                }

            } else {
                throw new ConfigurationError("Invalid property '" + token + "' in dataSource section [" + section
                        + "]. Correct syntax is [dataSource:<name> host:<host> path:<path>}]");
            }

        }

        // figure type of data source. if none is provided, we shout
        String type = applicationConfig.getProperty(section, "databaseLayer");
        if (type.equals(PROPERTY_NOT_SET)) {
            throw new ConfigurationError("Data source section [" + section
                    + "] misses the required property 'databaseLayer'.");
        }

        // populate with properties

        String[] globalDatabaseConfigurationProperties = { "foreignKeys", "defaultDataSource" };

        Map<String, String> globalProperties = applicationConfig.getPropertiesAsMap("dataSourceConfig");

        Map<String, String> dataSourceConfiguration = new Hashtable<String, String>();
        for (String globalProperty : globalDatabaseConfigurationProperties) {
            if (globalProperties.get(globalProperty) != null) {
                dataSourceConfiguration.put(globalProperty, globalProperties.get(globalProperty));
            }
        }
        try {
            dataSourceConfiguration.putAll(applicationConfig.getPropertiesAsMap(section));
        } catch (ConfigurationError ce) {
            throw new ConfigurationError("DataSource [" + section + "] is not configured in Makumba.conf");
        }

        ConfiguredDataSource c = new ConfiguredDataSource(host, name, path, DataSourceType.valueOf(type));
        c.setProperties(dataSourceConfiguration);
        return c;
    }
    
    /**
     * Sets a given property, for a specific section
     * @param section the name of the configuration section
     * @param key the key of the property
     * @param value the value of the property
     */
    public static void setPropery(String section, String key, String value) {
        applicationConfig.getSection(section).setProperty(key, value);
    }
    
    /**
     * Gives the data definition provider implementation to use
     * 
     * @return a String containing the class name of the data definition provider implementation
     */
    public static String getDataDefinitionProvider() {
        return applicationConfig.getProperty("dataSourceConfig", KEY_DATADEFINITIONPROVIDER);
    }
    
    public static String getQueryInliner() {
        return applicationConfig.getProperty("dataSourceConfig", KEY_QUERYFUNCTIONINLINER);
    }
    

    /**
     * Gives the default database layer to use
     * 
     * @return "makumba" or "hibernate"
     */
    public static String getDefaultDatabaseLayer() {
        synchronized (loadLock) {
            return applicationConfig.getProperty("dataSourceConfig", KEY_DEFAULT_DATABASE_LAYER);
        }
    }

    public static String getClientSideValidationDefault() {
        return defaultClientSideValidation;
    }

    public static boolean getReloadFormOnErrorDefault() {
        return defaultReloadFormOnError;
    }

    public static String getDefaultFormAnnotation() {
        return defaultFormAnnotation;
    }

    public static boolean getUseDefaultResponseStyles() {
        return applicationConfig.getBooleanProperty("controllerConfig", KEY_USE_DEFAULT_RESPONSE_STYLES);
    }

    public static boolean getCalendarEditorDefault() {
        return applicationConfig.getBooleanProperty("inputStyleConfig", KEY_CALENDAR_EDITOR);
    }

    public static String getDefaultCalendarEditorLink(String contextPath) {
        return applicationConfig.getProperty("inputStyleConfig", KEY_CALENDAR_EDITOR_LINK).replaceAll(
            PLACEHOLDER_CONTEXT_PATH, contextPath);
    }

    public static String getRepositoryURL() {
        return applicationConfig.getProperty("makumbaToolConfig", KEY_REPOSITORY_URL);
    }

    public static String getRepositoryLinkText() {
        return applicationConfig.getProperty("makumbaToolConfig", KEY_REPOSITORY_LINK_TEXT);
    }
    
    public static boolean getErrorLog() {
        return applicationConfig.getBooleanProperty("makumbaToolConfig", KEY_DB_ERROR_LOG);
    }
    
    /**
     * Returns the alternate location of a resource, PROPERTY_NOT_SET if there is none provide.
     * This makes it possible to configure alternate locations for e.g. javascript libs used by makumba.
     * @param res the name of the resource, e.g. "prototype.js"
     * @return the path starting from the context path to the library location, or PROPERTY_NOT_SET
     */
    public static String getResourceLocation(String res) {
        // we use getStringProperty to get null in case the property is not defined
        return applicationConfig.getProperty("makumbaToolConfig", res + "_location");
    }
    
    

    public static String getMakumbaToolsLocation() {
        final String property = applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_TOOLS);
        return property.endsWith("/") ? property.substring(0, property.length() - 1) : property;
    }

    public static String getMddViewerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_MDD_VIEWER));
    }

    public static String getJavaViewerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_JAVA_VIEWER));
    }

    public static String getLogicDiscoveryViewerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_LOGIC_DISCOVERY));
    }

    public static String getDataViewerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_DATA_OBJECT_VIEWER));
    }

    public static String getDataListerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_DATA_LISTER));
    }

    public static String getDataQueryLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_DATA_QUERY_TOOL));
    }

    public static String getObjectIdConverterLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_OBJECT_ID_CONVERTER));
    }

    public static String getReferenceCheckerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_REFERENCE_CHECKER));
    }

    public static String getCodeGeneratorLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_CODE_GENERATOR));
    }

    public static String getMakumbaValueEditorLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_VALUE_EDITOR));
    }

    public static String getMakumbaRelationCrawlerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_RELATION_CRAWLER));
    }

    public static String getMakumbaUniqueLocation() {
        return getMakumbaToolsLocation()
                + applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_UNIQUENESS_VALIDATOR);
    }

    public static String getMakumbaAutoCompleteLocation() {
        return getMakumbaToolsLocation() + applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_AUTOCOMPLETE);
    }

    public static String getMakumbaResourcesLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_RESOURCES));
    }

    public static String getMakumbaDownloadLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_DOWNLOAD));
    }

    public static String getMakumbaCacheCleanerLocation() {
        return getCompletePath(applicationConfig.getProperty("makumbaToolPaths", KEY_MAKUMBA_CACHE_CLEANER));
    }

    public static String getMakumbaToolsPathConfigProperty(String key) {
        return applicationConfig.getProperty("makumbaToolPaths", key);
    }

    public static Map<String, String> getJavaViewerSyntaxStyles() {
        return applicationConfig.getPropertiesAsMap("javaViewerSyntaxStyles", defaultConfig);
    }

    public static Map<String, String> getJspViewerSyntaxStyles() {
        return applicationConfig.getPropertiesAsMap("jspViewerSyntaxStyles", defaultConfig);
    }

    public static Map<String, String> getJspViewerSyntaxStylesTags() {
        return applicationConfig.getPropertiesAsMap("jspViewerSyntaxStylesTags", defaultConfig);
    }

    public static Map<String, Map<String, String>> getInternalCodeGeneratorTemplates() {
        return defaultConfig.getPropertiesStartingWith("codeGeneratorTemplate:");
    }

    public static Map<String, Map<String, String>> getApplicationSpecificCodeGeneratorTemplates() {
        return applicationConfig.getPropertiesStartingWith("codeGeneratorTemplate:");
    }

    public static Map<String, String> getLogicPackages() {
        return applicationConfig.getPropertiesAsMap("businessLogicPackages");
    }

    public static Map<String, String> getAuthorizationDefinitions() {
        return applicationConfig.getPropertiesAsMap("authorization");
    }

    public static String getApplicationConfigurationSource() {
        return applicationConfig != null ? applicationConfig.getSource() : null;
    }
    
    public static String getDefaultLanguage() {
        return applicationConfig.getProperty("internationalization", KEY_DEFAULT_LANG);
    }

    public static String getLanguageParameterName() {
        return applicationConfig.getProperty("internationalization", KEY_LANG_PARAM);
    }

    public static String getLanguageAttributeName() {
        return applicationConfig.getProperty("internationalization", KEY_LANG_ATTRIBUTE);
    }
    
    private static String getCompletePath(String path) {
        return StringUtils.isBlank(path) || path.equals(PROPERTY_NOT_SET) ? PROPERTY_NOT_SET
                : getMakumbaToolsLocation() + path;
    }

    /**
     * Returns the configuration for a specific dataSource. If more than one dataSource with the same name are found,
     * performs lookup.
     */
    public static Map<String, String> getDataSourceConfiguration(String dataSourceName) {

        ConfiguredDataSource conf = lookupDataSource(dataSourceName);

        return conf.getProperties();
    }

    /**
     * Gives the type of the data source (makumba or hibernate)
     */
    public static DataSourceType getDataSourceType(String dataSourceName) {
        return lookupDataSource(dataSourceName).getType();
    }

    private static ConfiguredDataSource defaultDataSource = null;

    /**
     * Gives the name of the default data source, according to following determination method:
     * <ol>
     * <li>If only one dataSource is configured, this one is used</li>
     * <li>If several dataSources of the same name are configured and contain lookup parameters (host, working
     * directory, ...), the one that matches the machine on which it runs is used</li>
     * <li>The defaultDataSource named in the dataSourceConfig section is used</li>
     * </ol>
     * 
     * @return the name of the dataSource to use by default
     */
    public static String getDefaultDataSourceName() {
        return getDefaultDataSource().getName();

    }

    /** the configuration properties of the default data source **/
    public static Map<String, String> getDefaultDataSourceConfiguration() {
        return getDefaultDataSource().getProperties();
    }

    /** the configuration of the default data source **/
    private static ConfiguredDataSource getDefaultDataSource() {

        if (defaultDataSource == null) {
            Map<String, String> globalProperties = applicationConfig.getPropertiesAsMap("dataSourceConfig");
            String defaultDataSourceName = globalProperties.get("defaultDataSource");

            if (defaultDataSourceName != null) {

                // we fetch the default one
                for (String c : configuredDataSources.keySet()) {
                    if (c.equals("dataSource:" + defaultDataSourceName)
                            || c.startsWith("dataSource:" + defaultDataSourceName + " ")) {
                        defaultDataSource = configuredDataSources.get(c);
                        return defaultDataSource;
                    }
                }

                // nothing found?
                throw new ConfigurationError("Default dataSource " + defaultDataSourceName
                        + " not found in Makumba.conf");

            }

            // first we check if there is maybe only one dataSource, in that case we take it as default
            int count = 0;
            String lastSection = "";
            HashMap<String, Boolean> toLookUp = new HashMap<String, Boolean>();
            for (Object sectionObject : applicationConfig.getSections()) {
                String section = (String) sectionObject;
                if (section.startsWith("dataSource:")) {
                    count++;
                    lastSection = section;

                    // we collect the sections we went through. if two sections start the same, we put them in a map to
                    // do a lookup
                    if (section.indexOf(" ") > -1 && toLookUp.get(section.substring(0, section.indexOf(" "))) != null) {
                        toLookUp.put(section.substring(0, section.indexOf(" ")), true);
                    } else if (section.indexOf(" ") > -1) {
                        toLookUp.put(section.substring(0, section.indexOf(" ")), false);
                    }
                }
            }
            if (count == 1) {
                defaultDataSource = buildConfiguredDataSource(lastSection);
                return defaultDataSource;
            } else if (count == 0) {
                throw new ConfigurationError("You must configure at least one dataSource for Makumba to work properly");
            }

            // now we treat the case when there are two or more dataSources that have the same name, but different host,
            // path properties
            // i.e. run a lookup and decide accordingly
            // but do this only if there are only dataSources that have the same name

            for (Entry<String, Boolean> entry : toLookUp.entrySet()) {
                if (entry.getValue()) {
                    ConfiguredDataSource c = lookupDataSource(entry.getKey().substring("dataSource:".length()));
                    if (c != null) {
                        defaultDataSource = c;
                    }
                }
            }

            // now we can't really tell which one to use by ourselves so we see if there is a default one
            if (defaultDataSourceName == null) {
                throw new ConfigurationError(
                        "Since there is more than one configured dataSource, Makumba needs to know which one to use. Please specify a defaultDataSource in section dataSourceConfig.");
            }

        }

        return defaultDataSource;

    }

    private static Map<String, ConfiguredDataSource> resolvedConfiguredDataSources = new HashMap<String, ConfiguredDataSource>();

    private static String remoteDataSourceConfigurationPath = "";
    
    public static String getRemoteDataSourceConfigurationPath() {
        return remoteDataSourceConfigurationPath;
    }
    
    /**
     * Looks up the right {@link ConfiguredDataSource} based on host and path.<br>
     * Tries to match all configured data sources against the local version of<br>
     * dataSource:<dataSourceName> host:<hostName> path:<workingDirPath> or of<br>
     * dataSource:<dataSourceName> host:<hostName> path:<webappPath><br>
     * If no match is found, tries to retrieve dataSource:<dataSourceName>
     * 
     * @throws ConfigurationError
     *             if no match is found
     */
    private static ConfiguredDataSource lookupDataSource(String dataSourceName) {

        ConfiguredDataSource result = resolvedConfiguredDataSources.get(dataSourceName);
        if (result == null) {

            try {

                String host = InetAddress.getLocalHost().getCanonicalHostName();
                String path = System.getProperty("user.dir");
                java.net.URL u = ClassResource.get("/");
                String alternatePath = u != null ? u.toString() : null;
                if(alternatePath != null && alternatePath.startsWith("file:")) {
                    alternatePath = alternatePath.substring("file:".length());
                    
                }

                String thisConfiguration1 = "dataSource:" + dataSourceName + " host:" + host + " path:" + path;
                String thisConfiguration2 = "dataSource:" + dataSourceName + " host:" + host + " path:" + alternatePath;
                
                remoteDataSourceConfigurationPath = thisConfiguration1;

                // we go over all the data sources and compare them to those we have
                String maxKey = "";

                for (String k : configuredDataSources.keySet()) {
                    if (thisConfiguration1.startsWith(k) && k.length() > maxKey.length()
                            && k.startsWith("dataSource:" + dataSourceName + " ")) {
                        maxKey = k;
                        result = configuredDataSources.get(k);
                    }
                }
                
                if(result == null) {
                    
                    for (String k : configuredDataSources.keySet()) {
                        if (thisConfiguration2.startsWith(k) && k.length() > maxKey.length()
                                && k.startsWith("dataSource:" + dataSourceName + " ")) {
                            maxKey = k;
                            result = configuredDataSources.get(k);
                        }
                    }

                    if(result == null) {
                        
                        // there was no dataSource:<name> path: ... found
                        // we fall back to the simple one
                        result = configuredDataSources.get("dataSource:" + dataSourceName);

                        if (result == null) {
                            throw new ConfigurationError("No DataSource " + dataSourceName + " configured in Makumba.conf");
                        }
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            logger.info("Resolved dataSource " + dataSourceName + " to " + result.toString());
            resolvedConfiguredDataSources.put(result.getName(), result);
        }
        return result;

    }

    public enum DataSourceType {
        makumba, hibernate;
    }

}
