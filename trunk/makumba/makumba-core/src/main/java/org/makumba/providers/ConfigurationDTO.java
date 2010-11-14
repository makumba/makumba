package org.makumba.providers;

import java.util.HashMap;
import java.util.Map;

/**
 * Temporary holder for Configuration data, should be merged with Configuration when a better provider mechanism will
 * exist. The fields have package visibility for now to avoid loads of getter / setter code.
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id$
 */
public class ConfigurationDTO {

    // providers
    String dataDefinitionProvider;

    String queryInliner;

    String pointerUIDStrategyClass;

    // temporary stuff
    boolean generateEntityClasses;

    // db stuff
    String defaultDatabaseLayer;

    String defaultClientSideValidation = "live";

    boolean defaultReloadFormOnError = true;

    String defaultFormAnnotation = "after";

    boolean defaultCalendarEditor;

    String calendarEditorLink;

    // dev tools
    String repositoryURL;

    String repositoryLinkText;

    boolean errorLog;

    String makumbaToolsLocation;

    String makumbaValueEditorLocation;

    String makumbaDownloadLocation;

    String makumbaResourcesLocation;

    String makumbaAutoCompleteLocation;

    String makumbaUniqueLocation;

    Map<DeveloperTool, String> developerToolsLocations = new HashMap<DeveloperTool, String>();

    Map<MakumbaServlet, String> servletLocations = new HashMap<MakumbaServlet, String>();

    Map<String, String> javaViewerSyntaxStyles;

    Map<String, String> jspViewerSyntaxStyles;

    Map<String, String> jspViewerSyntaxStylesTags;

    Map<String, Map<String, String>> internalCodeGeneratorTemplates;

    Map<String, Map<String, String>> applicationSpecificCodeGeneratorTemplates;

    Map<String, String> logicPackages;

    Map<String, String> authorizationDefinitions;

}
