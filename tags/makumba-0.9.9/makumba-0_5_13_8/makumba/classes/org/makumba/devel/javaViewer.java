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

package org.makumba.devel;

import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.MakumbaSystem;
import org.makumba.util.ClassResource;
import org.makumba.view.jsptaglib.TomcatJsp;

/**
 * the java viewer. It should be a filter from another (mb third-party) viewer that links known .java and .mdd sources. See SourceViewServlet for the
 * filter architecture
 * 
 * @version $ID $
 * @author Stefan Baebler
 * @author Rudolf Mayer
 *  
 */
public class javaViewer extends LineViewer {
    /** the name of the properties file configuring what to highlight how */
    public static final String PROPERTIES_FILE_NAME = "config/javaSyntax.properties";

    public static Properties javaSyntaxProperties = new Properties();

    private boolean compiledJSP = false;

    static {
        initProperties();
    }

    /**
     * Loads the properties file, if that fails uses {@link org.makumba.devel.javaViewer#initDefaultProperties() initDefaultProperties}to get default
     * values.
     */
    private static void initProperties() {
        try {
            URLConnection connection = (ClassResource.get(PROPERTIES_FILE_NAME)).openConnection();
            javaSyntaxProperties.load(connection.getInputStream());
        } catch (Throwable t) {
            javaSyntaxProperties = initDefaultProperties();
            MakumbaSystem.getLogger().info(
                    "Java syntax highlighting properties file '" + PROPERTIES_FILE_NAME
                            + "' not found! Using default values.");
        }
    }

    /**
     * defines some default syntax elements which should be highlighted. will be used when loading the properties file fails.
     */
    public static Properties initDefaultProperties() {
        Properties defaultProperties = new Properties();
        defaultProperties.put("public", "color:blue; font-weight: bold; ");
        defaultProperties.put("private", "color:blue; font-weight: bold; ");
        defaultProperties.put("static", "color:blue; font-weight: bold; ");
        defaultProperties.put("void", "color:blue; font-weight: bold; ");
        defaultProperties.put("class", "color:blue; font-weight: bold; ");
        defaultProperties.put("package", "color:blue; font-weight: bold; ");
        defaultProperties.put("import", "color:blue; font-weight: bold; ");
        defaultProperties.put("super", "color:blue; font-weight: bold; ");
        defaultProperties.put("return", "color:blue; font-weight: bold; ");
        defaultProperties.put("throws", "color:blue; font-weight: bold; ");
        defaultProperties.put("inner", "color:blue; font-weight: bold; ");
        defaultProperties.put("outer", "color:blue; font-weight: bold; ");

        defaultProperties.put("if", "color:green; font-weight: bold; ");
        defaultProperties.put("else", "color:green; font-weight: bold; ");
        defaultProperties.put("for", "color:green; font-weight: bold; ");
        defaultProperties.put("while", "color:green; font-weight: bold; ");
        defaultProperties.put("do", "color:green; font-weight: bold; ");
        defaultProperties.put("switch", "color:green; font-weight: bold; ");
        defaultProperties.put("case", "color:green; font-weight: bold; ");

        defaultProperties.put("int", "color:red; font-weight: bold; ");
        defaultProperties.put("long", "color:red; font-weight: bold; ");
        defaultProperties.put("short", "color:red; font-weight: bold; ");
        defaultProperties.put("byte", "color:red; font-weight: bold; ");
        defaultProperties.put("double", "color:red; font-weight: bold; ");
        defaultProperties.put("float", "color:red; font-weight: bold; ");
        defaultProperties.put("boolean", "color:red; font-weight: bold; ");
        return defaultProperties;
    }

    public javaViewer(HttpServletRequest req, HttpServlet sv) throws Exception {
        super(true, req, sv);
        initProperties();
        servletContext = sv.getServletContext();
        jspClasspath = TomcatJsp.getContextCompiledJSPDir(sv.getServletContext());

        contextPath = req.getContextPath();
        virtualPath = req.getPathInfo().substring(1);
        URL url = org.makumba.util.ClassResource.get(virtualPath.replace('.', '/') + ".java");
        if (url != null) {
            setSearchLevels(false, false, true, true);
            readFromURL(url);
        } else {
            String filePath = jspClasspath + "/" + virtualPath.replace('.', '/') + ".java";
            File jspClassFile = new File(filePath);
            if (jspClassFile.exists()) {
                url = new URL("file://" + filePath);
                setSearchLevels(false, false, false, false); // for compiled JSP files, we search only for MDDs.
                compiledJSP = true;
                readFromURL(url);
            } else {
                MakumbaSystem.getLogger().info("Could not find the compiled JSP '" + virtualPath + "'");
            }
        }
    }

    /**
     * 
     * Utilises the super-class' method, and performs additionally syntax highlighting for java keywords.
     * 
     * @see org.makumba.devel.LineViewer#parseLine(java.lang.String)
     */
    public String parseLine(String s) {
        String result = super.parseLine(s);
        if (compiledJSP) {
            return result;
        }
        Iterator syntax = javaSyntaxProperties.keySet().iterator();
        while (syntax.hasNext()) {
            String keyWord = String.valueOf(syntax.next());
            result = result.replaceAll(keyWord + " ", "<span style=\"" + javaSyntaxProperties.getProperty(keyWord)
                    + "\">" + keyWord + "</span> ");
        }
        return result;
    }

}

