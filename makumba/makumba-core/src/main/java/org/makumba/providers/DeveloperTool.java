///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
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
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.providers;

/**
 * @author Rudolf Mayer
 * @author Manuel Bernhardt
 */
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
    REGEXP_TESTER("regexpTester", "Regular Expression Tester", "Test regular expressions used in MDD validation rules",
            true),
    CACHE_CLEANER("makumbaCacheCleaner", "Makumba Cache Cleaner",
            "Cleans all internal Makumba caches, like queries, data-definitions.<br/>"
                    + "Useful during development, to avoid having to restart the servlet container", false),
    MDD_GRAPH_VIEWER("mddGraphViewer","MDD Graph Viewer","View the ER graph of the MDDs",true);

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
