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
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.MakumbaSystem;
import org.makumba.util.ClassResource;
import org.makumba.util.JavaParseData;
import org.makumba.util.SourceSyntaxPoints;
import org.makumba.util.SyntaxPoint;
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
    public static final String PROPERTIES_FILE_NAME = "javaSyntax.properties";

    public static Properties javaSyntaxProperties = new Properties();

    private static final String DEFAULT_JAVACOMMENT_STYLE = "color: green; font-style: italic; ";

    private static final String DEFAULT_JAVAMODIFIER_STYLE = "color: blue; font-weight: bold; ";

    private static final String DEFAULT_JAVARESERVEDWORD_STYLE = "color: purple; font-weight: bold; ";

    private static final String DEFAULT_JAVASTRINGLITERAL_STYLE = "color: red; font-style: italic; ";

    private boolean compiledJSP = false;

    private SourceSyntaxPoints syntaxPoints;

    private SyntaxPoint[] sourceSyntaxPoints;

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
            Properties readProperties = new Properties();
            readProperties.load(connection.getInputStream());

            // we load from the properties file the non-taglib properties, using defaults when necessary
            javaSyntaxProperties.put("JavaBlockComment", readProperties.getProperty("JavaBlockComment",
                    DEFAULT_JAVACOMMENT_STYLE));
            javaSyntaxProperties.put("JavaDocComment", readProperties.getProperty("javaDocComment",
                    DEFAULT_JAVACOMMENT_STYLE));
            javaSyntaxProperties.put("JavaLineComment", readProperties.getProperty("javaLineComment",
                    DEFAULT_JAVACOMMENT_STYLE));
            javaSyntaxProperties.put("JavaModifier", readProperties.getProperty("JavaReservedWord",
                    DEFAULT_JAVAMODIFIER_STYLE));
            javaSyntaxProperties.put("JavaReservedWord", readProperties.getProperty("JavaReservedWord",
                    DEFAULT_JAVARESERVEDWORD_STYLE));
            javaSyntaxProperties.put("JavaImport", readProperties.getProperty("JavaImport",
                    DEFAULT_JAVARESERVEDWORD_STYLE));
            javaSyntaxProperties.put("JavaStringLiteral", readProperties.getProperty("JavaStringLiteral",
                    DEFAULT_JAVASTRINGLITERAL_STYLE));
        } catch (Throwable t) { // the properties file was not found / readable / etc.

            MakumbaSystem.getLogger("org.makumba.devel.sourceViewer").fine(
                    "Java syntax highlighting properties file '" + PROPERTIES_FILE_NAME
                            + "' not found! Using default values.");
            // we use only default values
            javaSyntaxProperties.put("JavaDocComment", DEFAULT_JAVACOMMENT_STYLE);
            javaSyntaxProperties.put("JavaBlockComment", DEFAULT_JAVACOMMENT_STYLE);
            javaSyntaxProperties.put("JavaLineComment", DEFAULT_JAVACOMMENT_STYLE);
            javaSyntaxProperties.put("JavaModifier", DEFAULT_JAVAMODIFIER_STYLE);
            javaSyntaxProperties.put("JavaReservedWord", DEFAULT_JAVARESERVEDWORD_STYLE);
            javaSyntaxProperties.put("JavaImport", DEFAULT_JAVARESERVEDWORD_STYLE);
            javaSyntaxProperties.put("JavaStringLiteral", DEFAULT_JAVASTRINGLITERAL_STYLE);
        }
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
        } else {
            String filePath = jspClasspath + "/" + virtualPath.replace('.', '/') + ".java";
            File jspClassFile = new File(filePath);
            if (jspClassFile.exists()) {
                url = new URL("file://" + filePath);
                setSearchLevels(false, false, false, false); // for compiled JSP files, we search only for MDDs.
                compiledJSP = true;
            } else {
                MakumbaSystem.getLogger("org.makumba.devel.sourceViewer").info(
                        "Could not find the compiled JSP '" + virtualPath + "'");
            }
        }

        JavaParseData jspParseData = JavaParseData.getParseData("/", url.getFile(), JavaSourceAnalyzer.getInstance());
        jspParseData.getAnalysisResult(null);

        syntaxPoints = jspParseData.getSyntaxPoints();

        sourceSyntaxPoints = jspParseData.getSyntaxPoints().getSyntaxPoints();

        readFromURL(url);
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

    /** parse the text and write the output */
    public void parseText(PrintWriter writer) throws IOException {
        Date begin = new Date();
        printPageBegin(writer);

        SyntaxPoint lastSyntaxPoint = null;

        for (int j = 0; j < sourceSyntaxPoints.length; j++) {
            SyntaxPoint currentSyntaxPoint = sourceSyntaxPoints[j];
            String type = currentSyntaxPoint.getType();
            int currentLine = currentSyntaxPoint.getLine();

            if (type.equals("TextLine") && currentSyntaxPoint.isBegin()) { // begin of line found - we print the line numbers
                if (printLineNumbers) {
                    writer.print("\n<a style=\"font-style: normal; \" name=\"" + currentLine + "\" href=\"#"
                            + currentLine + "\" class=\"lineNo\">" + currentLine + ":\t</a>");
                }
            } else if (type.equals("TextLine") && !currentSyntaxPoint.isBegin()) { //end of line found
                writer.print(parseLine(htmlEscape(syntaxPoints.getLineText(currentLine).substring(
                        lastSyntaxPoint.getColumn() - 1, currentSyntaxPoint.getColumn() - 1))));
            } else { // we are in a syntax point
                if (currentSyntaxPoint.isBegin()) { // we are at the beginning of such a point
                    writer.print(parseLine(htmlEscape(syntaxPoints.getLineText(currentLine).substring(
                            lastSyntaxPoint.getColumn() - 1, currentSyntaxPoint.getColumn() - 1))));
                    writer.print("<span style=\"" + javaSyntaxProperties.get(type) + "; \">");
                } else { // we have an end point
                    writer.print(parseLine(htmlEscape(syntaxPoints.getLineText(currentLine).substring(
                            lastSyntaxPoint.getColumn() - 1, currentSyntaxPoint.getColumn() - 1))));
                    writer.print("</span>");
                }
            }
            lastSyntaxPoint = currentSyntaxPoint; // move pointer to last syntax Point
        }

        printPageEnd(writer);
        double time = new Date().getTime() - begin.getTime();
        MakumbaSystem.getLogger("org.makumba.devel.sourceViewer").finer(
                "Java sourcecode viewer took :" + (time / 1000) + " seconds");
    }

}

