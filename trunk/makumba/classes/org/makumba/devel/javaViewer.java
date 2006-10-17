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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaSystem;
import org.makumba.util.ClassResource;
import org.makumba.util.JavaParseData;
import org.makumba.util.SourceSyntaxPoints;
import org.makumba.util.StringUtils;
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

    // styles are roughly the same as the ECLIPSE standards
    private static final String DEFAULT_JAVACOMMENT_STYLE = "color: #1BA55F; font-family: monospace; ";

    private static final String DEFAULT_JAVADOC_STYLE = "color: #3F5FBF; font-family: monospace; ";

    private static final String DEFAULT_JAVAMODIFIER_STYLE = "color: blue; font-weight: bold; font-family: monospace; ";

    private static final String DEFAULT_JAVARESERVEDWORD_STYLE = "color: #7F0055; font-weight: bold; font-family: monospace; ";

    private static final String DEFAULT_JAVASTRINGLITERAL_STYLE = "color: #FF0000; font-style: italic; font-family: monospace; ";

    private boolean compiledJSP = false;
    
    private boolean haveFile = false;

    private SourceSyntaxPoints syntaxPoints;

    private SyntaxPoint[] sourceSyntaxPoints;

    private JavaParseData javaParseData;

    private URL url;
    
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
                    DEFAULT_JAVADOC_STYLE));
            javaSyntaxProperties.put("JavaLineComment", readProperties.getProperty("javaLineComment",
                    DEFAULT_JAVACOMMENT_STYLE));
            javaSyntaxProperties.put("JavaModifier", readProperties.getProperty("JavaReservedWord",
                    DEFAULT_JAVAMODIFIER_STYLE));
            javaSyntaxProperties.put("JavaReservedWord", readProperties.getProperty("JavaReservedWord",
                    DEFAULT_JAVARESERVEDWORD_STYLE));
            javaSyntaxProperties.put("JavaStringLiteral", readProperties.getProperty("JavaStringLiteral",
                    DEFAULT_JAVASTRINGLITERAL_STYLE));
        } catch (Throwable t) { // the properties file was not found / readable / etc 
            // --> use default values
            MakumbaSystem.getMakumbaLogger("org.makumba.devel.sourceViewer").fine(
                    "Java syntax highlighting properties file '" + PROPERTIES_FILE_NAME
                            + "' not found! Using default values.");
            javaSyntaxProperties.put("JavaDocComment", DEFAULT_JAVADOC_STYLE);
            javaSyntaxProperties.put("JavaBlockComment", DEFAULT_JAVACOMMENT_STYLE);
            javaSyntaxProperties.put("JavaLineComment", DEFAULT_JAVACOMMENT_STYLE);
            javaSyntaxProperties.put("JavaModifier", DEFAULT_JAVAMODIFIER_STYLE);
            javaSyntaxProperties.put("JavaReservedWord", DEFAULT_JAVARESERVEDWORD_STYLE);
            javaSyntaxProperties.put("JavaStringLiteral", DEFAULT_JAVASTRINGLITERAL_STYLE);
        }
    }

    public javaViewer(HttpServletRequest req, HttpServlet sv) throws Exception {
        super(true, req, sv);
        servletContext = sv.getServletContext();
        jspClasspath = TomcatJsp.getContextCompiledJSPDir(sv.getServletContext());

        contextPath = req.getContextPath();
        virtualPath = req.getPathInfo();
        if (virtualPath == null) {
            virtualPath = "/";
        } else {
            virtualPath = virtualPath.substring(1);
        }
        if (virtualPath.endsWith(".java")) {
            url = org.makumba.util.ClassResource.get(virtualPath);
        } else {
            url = org.makumba.util.ClassResource.get(virtualPath.replace('.', '/') + ".java");
        }
        if (url != null) {
            setSearchLevels(false, false, false, true);
            haveFile = true;
        } else {
            String filePath = jspClasspath + "/" + virtualPath.replace('.', '/') + ".java";
            File jspClassFile = new File(filePath);
            if (jspClassFile.exists()) {
                url = new URL("file://" + filePath);
                setSearchLevels(false, false, false, true); // for compiled JSP files, we search only for MDDs.
                compiledJSP = true;
                haveFile = true;
            } else {
                String s = virtualPath;
                if (s.startsWith("/")) {
                    s = s.substring(1);
                }
                url= org.makumba.util.ClassResource.get(s.replace('.', '/'));
            }
        }
        
        if (haveFile) { // we actually read a file
            // uncomment this for testing purposes to clean the analyzer cache.
            // NamedResources.cleanStaticCache(JavaParseData.analyzedPages);
            javaParseData = JavaParseData.getParseData("/", url.getFile(), JavaSourceAnalyzer.getInstance());
            javaParseData.getAnalysisResult(null);
            syntaxPoints = javaParseData.getSyntaxPoints();
            sourceSyntaxPoints = javaParseData.getSyntaxPoints().getSyntaxPoints();
            addImportedPackages(javaParseData.getImportedPackages());
            importedClasses = javaParseData.getImportedClasses();
        }
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
            // we highlight the word if we have a style defined for this syntax point typ
            if (javaSyntaxProperties.getProperty(keyWord) != null) { 
                result = result.replaceAll(keyWord + " ", "<span style=\"" + javaSyntaxProperties.getProperty(keyWord)
                        + "\">" + keyWord + "</span> ");
            }
        }
        return result;
    }

    /** parse the text and write the output */
    public void parseText(PrintWriter writer) throws IOException {
        long begin = System.currentTimeMillis();
        printPageBegin(writer);

        SyntaxPoint lastSyntaxPoint = null;
        int insideComment = 0;

        for (int j = 0; sourceSyntaxPoints != null && j < sourceSyntaxPoints.length; j++) {
            SyntaxPoint currentSyntaxPoint = sourceSyntaxPoints[j];
            String type = currentSyntaxPoint.getType();
            int currentLine = currentSyntaxPoint.getLine();

            if (type.equals("TextLine") && currentSyntaxPoint.isBegin()) { // begin of line found - we print the line numbers
                if (printLineNumbers) {
                    writer.print("\n");
                    if (!hideLineNumbers) {
                        writer.print("<a style=\"font-style: normal; \" name=\"" + currentLine + "\" href=\"#"
                            + currentLine + "\" class=\"lineNo\">" + currentLine + ":\t</a>");
                    }
                }
            } else {
                String beforeSyntaxPoint = syntaxPoints.getLineText(currentLine).substring(
                    lastSyntaxPoint.getColumn() - 1, currentSyntaxPoint.getColumn() - 1);
                if (type.equals("TextLine") && !currentSyntaxPoint.isBegin()) { // end of line found
                    writer.print(parseLine(htmlEscape(beforeSyntaxPoint)));
                } else { // we are in a syntax point
                    if (currentSyntaxPoint.isBegin()) { // we are at the beginning of the point
                        if (JavaParseData.isCommentSyntaxPoint(currentSyntaxPoint.getType())) {
                            insideComment++;
                        }
                        writer.print(parseLine(htmlEscape(beforeSyntaxPoint)));
                        // we treat class imports at the end of the syntax point
                        if (!JavaParseData.isClassUsageSyntaxPoint(currentSyntaxPoint.getType())) {
                            // we don't highlight literals inside comments
                            if (!(insideComment > 0 && currentSyntaxPoint.getType().equals("JavaStringLiteral"))
                                    && javaSyntaxProperties.get(type) != null) {
                                writer.print("<span style=\"" + javaSyntaxProperties.get(type) + "; \">");
                            }
                        }

                    } else { // we have the end of the point
                        if (JavaParseData.isCommentSyntaxPoint(currentSyntaxPoint.getType())) {
                            insideComment--;
                        }
                        if (JavaParseData.isClassUsageSyntaxPoint(currentSyntaxPoint.getType())) {
                            // generate links to used classes
                            Class webappClass = findClass(beforeSyntaxPoint);
                            String classLink = null;
                            if (webappClass == null) {
                                webappClass = findClassSimple(beforeSyntaxPoint);
                            }
                            if (webappClass != null) {      
                                classLink = formatClassLink(webappClass, null, beforeSyntaxPoint);
                            }
                            if (classLink != null) {
                                writer.print(classLink);
                            } else {
                                writer.print(beforeSyntaxPoint);
                            }
                            writer.print("</a>");
                        } else if (currentSyntaxPoint.getType().equals("JavaMethodInvocation")) {
                            String object = null;
                            String method = null; 
                            String[] parts = null;
                            if (beforeSyntaxPoint.indexOf(".") != -1) { // we actually do have a "." inside the syntax point
                                parts = beforeSyntaxPoint.split("\\.");
                            } else { // we need to go back one more syntax point
                                parts = syntaxPoints.getLineText(currentLine).substring(
                                    sourceSyntaxPoints[j-2].getColumn() - 1, currentSyntaxPoint.getColumn() - 1).split("\\.");
                            }                                

                            if (parts.length > 1) {
                                object = parts[0]; 
                                method = parts[1]; 
                            }
                            Class variableClass = null;
                            String classLink = null;
                            if (object.equals("super")) { // provide link to super class
                                variableClass = findClass(javaParseData.getSuperClass());
                                classLink = formatClassLink(variableClass, method,  beforeSyntaxPoint);
                            } else if (!object.equals("this")) { // don't care about usage of this
                                String className = javaParseData.getDefinedObjectClassName(object, currentSyntaxPoint.getPosition());
                                if (className == null) { // try to check if we use a static field
                                    className = object;
                                }
                                if (className != null) {
                                    variableClass = findClass(className);
                                    classLink = object + "." + formatClassLink(variableClass, method,  method);
                                }
                            }
                            if (classLink != null) {
                                writer.print(classLink);
                            } else {
                                writer.print(beforeSyntaxPoint);
                            }
                            javaParseData.getViewedClass();
                        } else if (currentSyntaxPoint.getType().equals("MakumbaFormHandler")) {
                            // add links to makumba form handlers, e.g. on_editMdd(..)
                            String[] parts = splitHandlerMethodName(beforeSyntaxPoint);
                            DataDefinition dd = null;
                            if (parts != null) {
                                String mddName = findMddNameFromHandler(parts[1]);
                                try {
                                    dd = MakumbaSystem.getDataDefinition(mddName);
                                    writer.print(parts[0] + "<a href=\"" + contextPath + "/dataDefinitions/" + dd.getName()
                                        + "\" title=\"'" + parts[2] + "'-handler for " + dd.getName()
                                        + "\" class=\"classLink\">" + parts[1] + "</a>");
                                } catch (DataDefinitionNotFoundError e) {
                                    mddName = findMddNameFromHandler(parts[1], true);
                                    try {
                                        dd = MakumbaSystem.getDataDefinition(mddName);
                                        DataDefinition parentDd = dd.getParentField().getDataDefinition();
                                        writer.print(parts[0] + "<a href=\"" + contextPath + "/dataDefinitions/" + parentDd.getName()
                                            + "\" title=\"'" + parts[2] + "'-handler for " + dd.getName()
                                            + "\" class=\"classLink\">" + parts[1] + "</a>");
                                    } catch (DataDefinitionNotFoundError e1) {
                                        // do nothing, just don't use this possible MDD
                                    } catch (NullPointerException e1) {
                                        // do nothing, just don't use this possible MDD
                                    }
                                }
                            }
                            if (dd == null) { // did not find the mdd --> just continue without displaying a link
                                writer.print(parseLine(htmlEscape(beforeSyntaxPoint)));
                            }
                        } else {
                            writer.print(parseLine(htmlEscape(beforeSyntaxPoint)));
                            // we don't highlight literals inside comments
                            if (!(insideComment > 0 && currentSyntaxPoint.getType().equals("JavaStringLiteral"))
                                    && javaSyntaxProperties.get(type) != null) {
                                writer.print("</span>");
                            }
                        }
                    }
                }
            }
            lastSyntaxPoint = currentSyntaxPoint; // move pointer to last syntax Point
        }

        printPageEnd(writer);
        double timeTaken = System.currentTimeMillis() - begin;
        MakumbaSystem.getMakumbaLogger("org.makumba.devel.sourceViewer").info(
                "Java sourcecode viewer took :" + (timeTaken / 1000.0) + " seconds");
    }

    private String findMddNameFromHandler(String encodedMddName) {
        return findMddNameFromHandler(encodedMddName, false);
    }
    /**
     * Discovers the name of an MDD from an encoded MDD name. Used for discovering the name of the MDD from a handler
     * method. e.g. general.address.City becomes on_editGeneralAddressCity in an edit handler.
     */
    private String findMddNameFromHandler(String encodedMddName, boolean upperCaseSecondButLast) {
        String mddName = "";
        ArrayList partList = new ArrayList();

        // Pattern camelCase = Pattern.compile("[^A-Z*|A-Z^A-Z*]*");
        // Pattern camelCase = Pattern.compile("[A-Z(^A-Z)*]*");
        Pattern camelCase = Pattern.compile("[A-Z]");
        Matcher m = camelCase.matcher(encodedMddName);
        int currentPos = 0;
        while (m.find()) {
            if (m.start() > currentPos) {
                String s = encodedMddName.substring(currentPos, m.start());
                partList.add(s);
                currentPos = m.start();
            }
        }

        partList.add(encodedMddName.substring(currentPos));
        String[] parts = (String[]) partList.toArray(new String[partList.size()]);
        for (int i = 0; i < parts.length; i++) {
            if (upperCaseSecondButLast) {
                if (i + 2 < parts.length) {
                    mddName += StringUtils.lowerCaseBeginning(parts[i]) + ".";
                } else if (i + 2 == parts.length) {
                    mddName += parts[i] + ".";
                } else {
                    mddName += StringUtils.lowerCaseBeginning(parts[i]);
                }

            } else {
                if (i + 1 < parts.length) {
                    mddName += StringUtils.lowerCaseBeginning(parts[i]) + ".";
                } else {
                    mddName += parts[i];
                }
            }
        }
        return mddName;
    }
    
    /**
     * splits the name of a handler method into different parts. For e.g. on_editGeneralAddressCity, the return values
     * are as follows:<br>
     * index [0]: handler type (e.g. <i>on_edit</i>).<br>
     * index [1]: encoded MDD name (e.g. <i>GeneralAddressCity</i>).<br>
     * index [2]: printable name of the handler action (e.g. <i>Edit</i>):
     */
    private String[] splitHandlerMethodName(String handlerName) {
        String encodedMddName = null;
        String[] formTypes = { "on_new", "on_edit", "on_add", "on_delete" };
        for (int i = 0; encodedMddName == null && i < formTypes.length; i++) {
            if (handlerName.indexOf(formTypes[i]) != -1) {
                encodedMddName = handlerName.substring(formTypes[i].length());
            }
        }
        String type = handlerName.substring(handlerName.indexOf("on_") + "on_".length(),
            handlerName.indexOf(encodedMddName));
        return new String[] { handlerName.substring(0, handlerName.indexOf(encodedMddName)), encodedMddName,
                type.substring(0, 1).toUpperCase() + type.substring(1) };
    }

    public void intro(PrintWriter w) {
        w.print("<td align=\"center\" bgcolor=\"darkblue\"><font color=\"lightblue\">Java</font></td>");
        String path = contextPath + "/classes/" + virtualPath.substring(0, virtualPath.lastIndexOf('/') + 1);
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        w.print("<td align=\"center\"><a href=\"/" + path + "\"><font color=\"darkblue\">browse</font></a></td>");
    }

}
