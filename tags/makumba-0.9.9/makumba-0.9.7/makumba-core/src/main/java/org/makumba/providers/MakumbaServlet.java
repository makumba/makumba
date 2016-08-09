package org.makumba.providers;

public enum MakumbaServlet {

    DOWNLOAD("makumbaDownload", "Download", "Download of file-type data"),
    RESOURCES("makumbaResources", "Resources",
            "Resources (javaScript, images,...) needed for calendar editor, live-validation, ..."),
    UNIQUENESS("makumbaUniquenessValidator", "Uniqueness", "AJAX uniqueness check"),
    AUTOCOMPLETE("makumbaAutoComplete", "Autocomplete", "AJAX autcomplete"),
    VALUE_EDITOR("makumbaValueEditor", "Value Editor", "Tool for edit-in-place"),
    RELATION_CRAWLER("relationCrawler", "Relation Crawler",
            "Runs a detection of file relations between JSP, MDD and Java Business Logics"),

    ;

    private String key;

    private String name;

    private String description;

    /**
     * Constructor for a DeveloperTool
     * 
     * @param key
     *            the name of the key in the configuration file
     * @param name
     *            the name of the tool, also used in the developer tools menu
     * @param description
     *            the description used in the main tools page
     */
    MakumbaServlet(String key, String name, String description) {
        this.key = key;
        this.name = name;
        this.description = description;
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

}
