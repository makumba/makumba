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
 * @version $Id: Configuration.java,v 1.1 28.09.2007 11:15:00 Manuel Exp $
 */
public class Configuration implements Serializable {

    public static final String PROPERTY_NOT_SET = "PROPERTY_NOT_SET";

    public static final String KEY_CLIENT_SIDE_VALIDATION = "clientSideValidation";

    public static final String KEY_RELOAD_FORM_ON_ERROR = "reloadFormOnError";

    public static final String KEY_DEFAULT_DATABASE_LAYER = "defaultDatabaseLayer";

    public static final String MAKUMBA_CONF = "Makumba.conf";

    private static final String MAKUMBA_CONF_DEFAULT = MAKUMBA_CONF + ".default";

    public static final String PLACEHOLDER_CONTEXT_PATH = "_CONTEXT_PATH_";

    private static String defaultClientSideValidation = "live";

    private static boolean defaultReloadFormOnError = true;

    private static final long serialVersionUID = 1L;

    private static final String defaultDataDefinitionProvider = "org.makumba.providers.datadefinition.makumba.MakumbaDataDefinitionFactory";

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

    public static void setContextPath(String path) {
        contextPath = path;
    }

    private static String contextPath = null;

    private static Map<String, ConfiguredDataSource> configuredDataSources = new HashMap<String, ConfiguredDataSource>();

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
            }

            defaultClientSideValidation = applicationConfig.getStringProperty("controllerConfig",
                KEY_CLIENT_SIDE_VALIDATION, defaultConfig);
            defaultReloadFormOnError = applicationConfig.getBooleanProperty("controllerConfig",
                KEY_RELOAD_FORM_ON_ERROR, defaultConfig);

            buildConfiguredDataSources();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** builds the data sources for the configuration. **/
    private static void buildConfiguredDataSources() {
        for (Iterator<String> iterator = applicationConfig.sectionNames().iterator(); iterator.hasNext();) {
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
        String name = null, host = null, path = null, webapp = null;
        StringTokenizer st = new StringTokenizer(section, " ");
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            int n = token.indexOf(":");
            if (n > -1) {
                String tokenName = token.substring(0, n);
                String tokenValue = token.substring(n + 1);

                if (org.makumba.commons.StringUtils.equalsAny(tokenName, "dataSource", "host", "path", "web-app")) {

                    if (StringUtils.isBlank(tokenValue)) {
                        throw new ConfigurationError("Property " + tokenName + " has no value");
                    }

                    if (tokenName.equals("dataSource")) {
                        name = tokenValue;
                    } else if (tokenName.equals("host")) {
                        host = tokenValue;
                    } else if (tokenName.equals("path")) {
                        path = tokenValue;
                    } else if (tokenName.equals("web-app")) {
                        webapp = tokenValue;
                    }
                } else {
                    throw new ConfigurationError("Invalid property '" + token + "' in dataSource section [" + section
                            + "]. Correct syntax is [dataSource:<name> host:<host> path:<path> web-app:<context>}]");
                }

            } else {
                throw new ConfigurationError("Invalid property '" + token + "' in dataSource section [" + section
                        + "]. Correct syntax is [dataSource:<name> host:<host> path:<path> web-app:<context>}]");
            }

        }

        // figure type of data source. if none provided, we use the default database layer type
        String type = applicationConfig.getProperty(section, "databaseLayer");
        if (type.equals(PROPERTY_NOT_SET)) {
            type = applicationConfig.getStringProperty("dataSourceConfig", "defaultDatabaseLayer", defaultConfig);
            Logger.getLogger("org.makumba.config").warning(
                "Using default databaseLayer " + type + " for dataSource " + name
                        + ". To get rid of this message, set a databaseLayer property for this dataSource.");
        }

        // populate with properties

        String[] globalDatabaseConfigurationProperties = { "foreignKeys", "defaultDataSource" };

        Map<String, String> globalProperties = applicationConfig.getProperties("dataSourceConfig");

        Map<String, String> dataSourceConfiguration = new Hashtable<String, String>();
        for (String globalProperty : globalDatabaseConfigurationProperties) {
            if (globalProperties.get(globalProperty) != null) {
                dataSourceConfiguration.put(globalProperty, globalProperties.get(globalProperty));
            }
        }
        try {
            dataSourceConfiguration.putAll(applicationConfig.getProperties(section));
        } catch (ConfigurationError ce) {
            throw new ConfigurationError("DataSource [" + section + "] is not configured in Makumba.conf");
        }

        ConfiguredDataSource c = new ConfiguredDataSource(host, name, path, DataSourceType.valueOf(type), webapp);
        c.setProperties(dataSourceConfiguration);
        return c;
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
     * Gives the default transaction provider implementation to use
     * 
     * @return a String containing the class name of the transaction provider implementation
     */
    public static String getDefaultTransactionProviderClass() {

        if (getDefaultDatabaseLayer().equals(DataSourceType.makumba.name())) {
            return "org.makumba.db.makumba.MakumbaTransactionProvider";
        } else if (getDefaultDatabaseLayer().equals(DataSourceType.hibernate.name())) {
            return "org.makumba.db.hibernate.HibernateTransactionProvider";
        } else {
            throw new ConfigurationError("databaseLayer must be either 'makumba' or 'hibernate'");
        }
    }

    /**
     * Gives the default database layer to use
     * 
     * @return "makumba" or "hibernate"
     */
    public static String getDefaultDatabaseLayer() {
        return applicationConfig.getStringProperty("dataSourceConfig", KEY_DEFAULT_DATABASE_LAYER, defaultConfig);
    }

    public static String getClientSideValidationDefault() {
        return defaultClientSideValidation;
    }

    public static boolean getReloadFormOnErrorDefault() {
        return defaultReloadFormOnError;
    }

    public static boolean getUseDefaultResponseStyles() {
        return applicationConfig.getBooleanProperty("controllerConfig", KEY_USE_DEFAULT_RESPONSE_STYLES, defaultConfig);
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
            Map<String, String> globalProperties = applicationConfig.getProperties("dataSourceConfig");
            String defaulDataSourceName = globalProperties.get("defaultDataSource");

            // first we check if there is maybe only one dataSource, in that case we take it as default
            int count = 0;
            String lastSection = "";
            boolean doLookup = true;
            for (Object sectionObject : applicationConfig.sectionNames()) {
                String section = (String) sectionObject;
                if (section.startsWith("dataSource:")) {
                    count++;
                    doLookup = lastSection.indexOf(" ") > -1 && section.indexOf(" ") > -1 && lastSection.substring(0, lastSection.indexOf(" ")).equals(
                        section.substring(0, section.indexOf(" ")));
                    lastSection = section;
                }
            }
            if (count == 1) {
                defaultDataSource = buildConfiguredDataSource(lastSection);
                return defaultDataSource;
            } else if (count == 0) {
                throw new ConfigurationError("You must configure at least one dataSource for Makumba to work properly");
            }

            // now we treat the case when there are two or more dataSources that have the same name, but different host
            // etc properties
            // i.e. run a lookup and decide accordingly
            // but do this only if there are only dataSources that have the same name
            if (doLookup && (lastSection.indexOf("host:") > -1 || lastSection.indexOf("path:") > -1 || lastSection.indexOf("web-app:") > -1)) {
                // we have dataSources with the same name, try to figure default through lookup
                // first we fetch the same name
                String dataSourceName = lastSection.substring("dataSource:".length(), lastSection.indexOf(" "));
                defaultDataSource = lookupDataSource(dataSourceName);
                return defaultDataSource;
            }

            // now we can't really tell which one to use by ourselves so we see if there is a default one
            if (defaulDataSourceName == null) {
                throw new ConfigurationError(
                        "Since there is more than one configured dataSource, Makumba needs to know which one to use. Please specify a defaultDataSource in section dataSourceConfig.");
            }

            // we fetch the default one
            for (String c : configuredDataSources.keySet()) {
                if (c.startsWith("dataSource:" + defaulDataSourceName)) {
                    defaultDataSource = configuredDataSources.get(c);
                    return defaultDataSource;
                }
            }

            // nothing found?
            throw new ConfigurationError("Default dataSource " + defaulDataSourceName + " not found in Makumba.conf");

        }

        return defaultDataSource;

    }

    private static Map<String, ConfiguredDataSource> resolvedConfiguredDataSources = new HashMap<String, ConfiguredDataSource>();

    /**
     * Looks up the right {@link ConfiguredDataSource} based on host and path. FIXME the host name may looks weirdish
     */
    private static ConfiguredDataSource lookupDataSource(String dataSource) {

        ConfiguredDataSource result = resolvedConfiguredDataSources.get(dataSource);
        if (result == null) {

            try {

                String host = InetAddress.getLocalHost().toString();
                String path = System.getProperty("user.dir");
                java.net.URL u = ClassResource.get("/");
                String alternatePath = u != null ? u.toString() : null;

                String thisConfiguration1 = "dataSource:" + dataSource + " host:" + host + " path:" + path
                        + " web-app:" + contextPath;
                String thisConfiguration2 = "dataSource:" + dataSource + " host:" + host + " path:" + alternatePath
                        + " web-app:" + contextPath;

                // we go over all the data sources and compare them to those we have
                String maxKey = "";

                for (String k : configuredDataSources.keySet()) {
                    if (thisConfiguration1.startsWith(k) && k.length() > maxKey.length()
                            && thisConfiguration1.startsWith("dataSource:" + dataSource + " ")) {
                        maxKey = k;
                        result = configuredDataSources.get(k);
                    }
                }

                for (String k : configuredDataSources.keySet()) {
                    if (thisConfiguration2.startsWith(k) && k.length() > maxKey.length()
                            && thisConfiguration2.startsWith("dataSource:" + dataSource + " ")) {
                        maxKey = k;
                        result = configuredDataSources.get(k);
                    }
                }

                // nothing like our data source was found
                if (result == null) {
                    throw new ConfigurationError("No DataSource " + dataSource + " configured in Makumba.conf");
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            Logger.getLogger("org.makumba.config").info(
                "Resolved dataSource " + dataSource + " to " + result.toString());
            resolvedConfiguredDataSources.put(result.getName(), result);
        }
        return result;

    }

    enum DataSourceType {
        makumba, hibernate;
    }

}
