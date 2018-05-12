package org.makumba.commons.tags;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.makumba.commons.http.MakumbaServlet;
import org.makumba.devel.DeveloperTool;
import org.makumba.forms.html.CalendarEditorProvider;
import org.makumba.forms.html.KruseCalendarEditor;
import org.makumba.forms.validation.ClientsideValidationProvider;
import org.makumba.forms.validation.LiveValidationProvider;
import org.makumba.providers.Configuration;

public class MakumbaJspConfiguration {

    public static final String PLACEHOLDER_CONTEXT_PATH = "_CONTEXT_PATH_";

    public static final String KEY_DISABLE_RESOURCES = "disableResources";

    public static final String KEY_CLIENT_SIDE_VALIDATION = "clientSideValidation";

    public static final String KEY_RELOAD_FORM_ON_ERROR = "reloadFormOnError";

    public static final String KEY_FORM_ANNOTATION = "formAnnotation";

    // calendar editor
    public static final String KEY_CALENDAR_EDITOR = "calendarEditor";

    public static final String KEY_CALENDAR_EDITOR_LINK = "calendarEditorLink";

    public static final String KEY_TOOLS_LOCATION = Configuration.PATH;

    // source code repository links
    public static final String KEY_REPOSITORY_URL = "repositoryURL";

    public static final String KEY_REPOSITORY_LINK_TEXT = "repositoryLinkText";

    /** Get the default calendar editor. FIXME: read this from some config, or so. */
    public static CalendarEditorProvider getCalendarProvider() {
        return KruseCalendarEditor.getInstance();
    }

    /** Get the default client-side validation provider. */
    public static ClientsideValidationProvider getClientsideValidationProvider() {
        return new LiveValidationProvider();
    }

    public static String getClientSideValidationDefault() {
        return // FIXME: check if the value in the file is ok, throw an exception otherwise
        Configuration.getApolicationConfiguration().getProperty("controllerConfig", KEY_CLIENT_SIDE_VALIDATION);
    }

    public static boolean getReloadFormOnErrorDefault() {
        return Configuration.getApolicationConfiguration().getBooleanProperty("controllerConfig",
            KEY_RELOAD_FORM_ON_ERROR);
    }

    public static String getDefaultFormAnnotation() {
        return // FIXME: check if the value in the file is ok, throw an exception otherwise
        Configuration.getApolicationConfiguration().getProperty("controllerConfig", KEY_FORM_ANNOTATION);
    }

    public static boolean getCalendarEditorDefault() {
        return Configuration.getApolicationConfiguration().getBooleanProperty("inputStyleConfig", KEY_CALENDAR_EDITOR);
    }

    public static String getDefaultCalendarEditorLink(String contextPath) {
        return Configuration.getApolicationConfiguration().getProperty("inputStyleConfig", KEY_CALENDAR_EDITOR_LINK).replaceAll(
            PLACEHOLDER_CONTEXT_PATH, contextPath);
    }

    public static String getMakumbaToolsLocation() {
        final String property = Configuration.getApolicationConfiguration().getProperty("makumbaToolPaths",
            KEY_TOOLS_LOCATION);
        return property.endsWith("/") ? property.substring(0, property.length() - 1) : property;
    }

    static Map<DeveloperTool, String> developerToolsLocations = new HashMap<DeveloperTool, String>();

    static Map<MakumbaServlet, String> servletLocations = new HashMap<MakumbaServlet, String>();
    static {

        for (DeveloperTool t : DeveloperTool.values()) {
            developerToolsLocations.put(t,
                Configuration.getApolicationConfiguration().getProperty("makumbaToolPaths", t.getKey()));
        }

        for (MakumbaServlet s : MakumbaServlet.values()) {
            servletLocations.put(s,
                Configuration.getApolicationConfiguration().getProperty("makumbaToolPaths", s.getKey()));
        }
    }

    public static String getToolLocation(DeveloperTool t) {
        return getCompletePath(developerToolsLocations.get(t));
    }

    public static String getServletLocation(MakumbaServlet s) {
        return getCompletePath(servletLocations.get(s));
    }

    public static String getRepositoryURL() {
        return Configuration.getApolicationConfiguration().getProperty("makumbaToolConfig", KEY_REPOSITORY_URL);
    }

    public static String getRepositoryLinkText() {
        return Configuration.getApolicationConfiguration().getProperty("makumbaToolConfig", KEY_REPOSITORY_LINK_TEXT);
    }

    public static Map<String, String> getJavaViewerSyntaxStyles() {
        return Configuration.getApolicationConfiguration().getPropertiesAsMap("javaViewerSyntaxStyles",
            Configuration.getDefaultConfig());
    }

    public static Map<String, String> getJspViewerSyntaxStyles() {
        return Configuration.getApolicationConfiguration().getPropertiesAsMap("jspViewerSyntaxStyles",
            Configuration.getDefaultConfig());
    }

    public static Map<String, String> getJspViewerSyntaxStylesTags() {
        return Configuration.getApolicationConfiguration().getPropertiesAsMap("jspViewerSyntaxStylesTags",
            Configuration.getDefaultConfig());
    }

    public static Map<String, Map<String, String>> getInternalCodeGeneratorTemplates() {
        return Configuration.getDefaultConfig().getPropertiesStartingWith("codeGeneratorTemplate:");
    }

    public static Map<String, Map<String, String>> getApplicationSpecificCodeGeneratorTemplates() {
        return Configuration.getApolicationConfiguration().getPropertiesStartingWith("codeGeneratorTemplate:");
    }

    private static String getCompletePath(String path) {
        return StringUtils.isBlank(path) || path.equals(Configuration.PROPERTY_NOT_SET) ? Configuration.PROPERTY_NOT_SET
                : getMakumbaToolsLocation() + path;
    }

}
