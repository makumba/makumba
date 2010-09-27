package org.makumba.providers;

public enum DeveloperTool {

    MDD_VIEWER("mddViewer", "DataDefinition viewer", "View data definitions", true),
    JAVA_VIEWER("javaViewer", "Business logics viewer", "View Java Business Logics", true),
    LOGIC_DISCOVERY("logicDiscovery", "Logic Discovery", "View Business Logics associated with a certain page", true),
    CODE_GENERATOR("codeGenerator", "Code generator", "Generate forms & lists from data definitions", false),
    DATA_QUERY("dataQueryTool", "Data query", "Free-form MQL queries", true),
    DATA_LISTER("dataLister", "Data lister", "List data from a certain type", true),
    OBJECT_VIEWER("dataObjectViewer", "Object viewer", "View a specific object", false),
    OBJECT_ID_CONVERTER("objectIdConverter", "Pointer value converter",
            "Convert pointer values between internal/external/DB form", true),
    REFERENCE_CHECKER("referenceChecker", "Reference Checker",
            "Checks creation the status of foreign and unique keys and displays broken references", true),
    ERRORLOG_VIEWER("errorLogViewer", "Error log viewer", "List logged makumba errors", true),
    CACHE_CLEANER("makumbaCacheCleaner", "Makumba Cache Cleaner",
            "Cleans all internal Makumba caches, like queries, data-definitions.<br/>"
                    + "Useful during development, to avoid having to restart the servlet container", false);

    private String key;

    private String name;

    private String description;

    private boolean generic;

    /**
     * Constructor for a DeveloperTool
     * 
     * @param key
     *            the name of the key in the configuration file
     * @param name
     *            the name of the tool, also used in the developer tools menu
     * @param description
     *            the description used in the main tools page
     * @param generic
     *            whether this is a tool available by default
     */
    DeveloperTool(String key, String name, String description, boolean generic) {
        this.key = key;
        this.name = name;
        this.description = description;
        this.generic = generic;
    }

    public String getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isGeneric() {
        return generic;
    }

}
