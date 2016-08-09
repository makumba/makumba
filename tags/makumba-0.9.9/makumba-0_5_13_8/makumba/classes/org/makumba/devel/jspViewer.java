// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.MakumbaSystem;
import org.makumba.util.ClassResource;
import org.makumba.util.JspParseData;
import org.makumba.util.SourceSyntaxPoints;
import org.makumba.util.SyntaxPoint;
import org.makumba.view.jsptaglib.TomcatJsp;

/**
 * 
 * This classe implements a viewer for .jsp files, and provides highlighting of <mak:>, <jsp:>and JSTL tags.
 * 
 * @version $ID $
 * @author Stefan Baebler
 * @author Rudolf Mayer
 *  
 */
public class jspViewer extends LineViewer {
    /**
     * Usage: put the beginning of the tag-name as key, and the style-info you want for the created span around that tag. both 'key' and /'key' will
     * be matched - if you want a different colouring for /'key', put that as an own entry, and make sure it is put <b>before </b> the 'key' entry -
     * otherwise it will get matched twice.
     */
    public static Properties jspSyntaxProperties = new Properties();

    private static final String PROPERTIES_FILE_NAME = "config/jspSyntax.properties";

    static {
        initProperties();
    }

    /**
     * Loads the properties file, if that fails uses {@link org.makumba.devel.javaViewer#initDefaultProperties() initDefaultProperties}to get default
     * values.
     */
    private static void initProperties() {
        // TODO: should we parse the tag-lib import and check for the prefix?
        try {
            URLConnection connection = (ClassResource.get(PROPERTIES_FILE_NAME)).openConnection();
            jspSyntaxProperties.load(connection.getInputStream());
        } catch (Throwable t) {
            jspSyntaxProperties = initDefaultProperties();
            MakumbaSystem.getLogger().info(
                    "JSP syntax highlighting properties file '" + PROPERTIES_FILE_NAME
                            + "' not found! Using default values.");
        }
    }

    /**
     * defines some default taglibs which should be highlighted. will be used when loading the properties file fails.
     */
    public static Properties initDefaultProperties() {
        Properties defaultProperties = new Properties();
        defaultProperties.put("mak", "background:#eecccc; color:green; border:red thin; font-weight: bold; ");
        defaultProperties.put("jsp", "background:lightcyan; color:green; border:red thin; font-weight: bold; ");
        defaultProperties.put("fmt", "background::chartreuse; color:green; border:red thin; font-weight: bold; ");
        defaultProperties.put("c", "background:lightblue; color:green; border:red thin; font-weight: bold; ");
        return defaultProperties;
    }

    private static Set syntaxKeys = jspSyntaxProperties.keySet();

    boolean hasLogic;

    private TreeSet sourceSyntaxPoints;

    private SourceSyntaxPoints syntaxPoints;

    private boolean hideComments = false;

    private boolean hideHTML = false;

    private boolean hideJSTLCore = false;

    private boolean hideJSTLFormat = false;

    private boolean hideMakumba = false;

    private boolean hideJava = false;

    private int extraLength() {
        return 1;
    }

    public jspViewer(HttpServletRequest req, HttpServlet sv) throws Exception {
        super(true, req, sv);
        setSearchLevels(true, false, false, true);
        this.servlet = sv;
        hideComments = Boolean.valueOf(String.valueOf(req.getParameter("hideComments"))).booleanValue();
        hideHTML = Boolean.valueOf(String.valueOf(req.getParameter("hideHTML"))).booleanValue();
        hideJSTLCore = Boolean.valueOf(String.valueOf(req.getParameter("hideJSTLCore"))).booleanValue();
        hideJSTLFormat = Boolean.valueOf(String.valueOf(req.getParameter("hideJSTLFormat"))).booleanValue();
        hideMakumba = Boolean.valueOf(String.valueOf(req.getParameter("hideMakumba"))).booleanValue();
        hideJava = Boolean.valueOf(String.valueOf(req.getParameter("hideJava"))).booleanValue();

        String thisFile = TomcatJsp.getJspURI(req);
        thisFile = thisFile.substring(0, thisFile.length() - 1);

        JspParseData jspParseData = JspParseData.getParseData(sv.getServletContext().getRealPath("/"), thisFile,
                JspxJspAnalyzer.singleton);
        jspParseData.getAnalysisResult(null);

        syntaxPoints = jspParseData.getSyntaxPoints();

        sourceSyntaxPoints = jspParseData.getSyntaxPoints().getSyntaxPoints();

        contextPath = req.getContextPath();
        String servletPath = req.getServletPath();
        virtualPath = servletPath.substring(0, servletPath.length() - extraLength());
        jspSourceViewExtension = servletPath.substring(servletPath.length() - extraLength());
        realPath = sv.getServletConfig().getServletContext().getRealPath(virtualPath);
        reader = new FileReader(realPath);
        servletPath = servletPath.substring(0, servletPath.indexOf(".")) + ".jsp";
        logicPath = contextPath + "/logic" + servletPath;
        hasLogic = !(org.makumba.controller.Logic.getLogic(servletPath) instanceof org.makumba.LogicNotFoundException);
    }

    public void intro(PrintWriter w) throws IOException {
        w.print("<td>\n"
                + "<table align=\"right\" cellpading=\"5\" cellspacing=\"5\">\n"
                + "<tr>\n"
                + "<td align=\"right\"><a href=\""
                + contextPath
                + virtualPath
                + "\"><font color=\"darkblue\">execute</font></a>&nbsp;&nbsp;&nbsp;</td>\n"
                + "<td align=\"right\" bgcolor=\"darkblue\"><font color=\"lightblue\">source</font>&nbsp;&nbsp;&nbsp;</td>\n"
                + "<td align=\"right\"><a href=\"" + logicPath + "\"><font color=\"darkblue\">business logic"
                + (hasLogic ? "" : " (none)") + "</font></a></td>\n");

        String lg = org.makumba.controller.http.ControllerFilter.getLoginPage(virtualPath);
        if (lg != null)
            w.print("<td align=\"right\">&nbsp;&nbsp;&nbsp;<a href=\"" + contextPath + lg
                    + "x\"><font color=\"darkblue\">login page</font></a></td>\n");

        w.print("</tr>\n" + "</table>\n" + "</td>\n" + "</tr>\n" + "<tr>\n" + "<td align=\"right\">"
                + "<form method=\"get\" action>\n" + "Hide:"
                + " <input type=\"checkbox\" name=\"hideComments\" value=\"true\""
                + (hideComments == true ? " checked=\"checked\"" : "") + ">Comments  \n"
                + "<input type=\"checkbox\" name=\"hideHTML\" value=\"true\""
                + (hideHTML == true ? " checked=\"checked\"" : "") + ">HML  \n"
                + "<input type=\"checkbox\" name=\"hideJava\" value=\"true\""
                + (hideJava == true ? " checked=\"checked\"" : "") + ">Java  \n"
                + "<input type=\"checkbox\" name=\"hideJSTLCore\" value=\"true\""
                + (hideJSTLCore == true ? " checked=\"checked\"" : "") + ">JSTL Core  \n"
                + "<input type=\"checkbox\" name=\"hideJSTLFormat\" value=\"true\""
                + (hideJSTLFormat == true ? " checked=\"checked\"" : "") + ">JSTL Format  \n"
                + "<input type=\"checkbox\" name=\"hideMakumba\" value=\"true\""
                + (hideMakumba == true ? " checked=\"checked\"" : "") + ">Makumba  \n"
                + "<input type=\"submit\" value=\"apply\"> \n" + "</form>\n" + "</td>\n" + "</tr>\n");
    }

    /** parse the text and write the output */
    public void parseText(PrintWriter writer) throws IOException {
        Date begin = new Date();
        Object[] syntaxElements = syntaxKeys.toArray();
        Iterator iterator = sourceSyntaxPoints.iterator();

        printPageBegin(writer);

        SyntaxPoint lastSyntaxPoint = null;
        boolean shallWrite = true;
        boolean inTag = false;

        StringBuffer currentText = new StringBuffer();

        while (iterator.hasNext()) {
            SyntaxPoint currentSyntaxPoint = (SyntaxPoint) iterator.next();
            String type = currentSyntaxPoint.getType();
            int currentLine = currentSyntaxPoint.getLine();

            if (type.equals("TextLine") && currentSyntaxPoint.isBegin()) { // begin of line found - we just move the last point marker
                lastSyntaxPoint = currentSyntaxPoint;
            } else if (type.equals("TextLine") && !currentSyntaxPoint.isBegin()) { //end of line found

                // we write if we are not on column 1 (empty text line) and either are showing HTML or are in a tag
                if (currentSyntaxPoint.getColumn() > 1 && (!hideHTML || inTag) && shallWrite) {
                    currentText.append(parseLine(htmlEscape(syntaxPoints.getLineText(currentLine).substring(
                            lastSyntaxPoint.getColumn() - 1, currentSyntaxPoint.getColumn() - 1))));
                }

                // if the current line contained any text to write we print the line number
                if ((!currentText.toString().trim().equals("") || currentSyntaxPoint.getColumn() == 1) && shallWrite
                        && printLineNumbers) {
                    writer.print("\n<a name=\"" + currentLine + "\" href=\"#" + currentLine + "\" class=\"lineNo\">"
                            + currentLine + ":\t</a>");
                }
                writer.print(currentText.toString());
                currentText = new StringBuffer();
                lastSyntaxPoint = currentSyntaxPoint; // move pointer to last syntax Point
            } else if (isTagToHighlight(type)) { // we are in a tag that might be highlighted
                //debugSystemPoint(point);
                if (currentSyntaxPoint.isBegin()) { // we are at the beginning of such a tag
                    // we write what was possibly before
                    if (currentSyntaxPoint.getColumn() > 1 && !hideHTML && shallWrite) {
                        currentText.append(parseLine(htmlEscape(syntaxPoints.getLineText(currentLine).substring(
                                lastSyntaxPoint.getColumn() - 1, currentSyntaxPoint.getColumn() - 1))));
                    }

                    inTag = true;
                    String tagType = syntaxPoints.getLineText(currentLine).substring(currentSyntaxPoint.getColumn());

                    // we have a scriplet (<% ... %>)
                    if (type.equals("JspScriptlet")) {
                        if (hideJava) { // check whether show or hide
                            shallWrite = false;
                        } else {
                            currentText.append("<span style=\"background:gold;color:green; border:red thin; font-weight: bold; \">");
                        }
                    } else if (type.equals("JspComment")) { // we have a JSP comment (<%-- ... --%>)
                        if (hideComments) { // check whether show or hide
                            shallWrite = false;
                        } else {
                            currentText.append("<span style=\"color:gray; \">");
                        }
                    } else if (type.equals("JSPSystemTag")) {
                        currentText.append("<span style=\"" + jspSyntaxProperties.getProperty("jsp") + "; \">");
                    } else {// we have any other taglib tag
                        if (((tagType.startsWith("mak") || tagType.startsWith("/mak")) && hideMakumba)
                                || ((tagType.startsWith("c") || tagType.startsWith("/c")) && hideJSTLCore)
                                || ((tagType.startsWith("fmt") || tagType.startsWith("/fmt")) && hideJSTLFormat)) {
                            shallWrite = false;
                        }

                        if (shallWrite) { // do the defined highlighting
                            for (int i = 0; i < syntaxElements.length; i++) {
                                if (tagType.startsWith(String.valueOf(syntaxElements[i]))
                                        || tagType.startsWith(String.valueOf("/" + syntaxElements[i]))) {
                                    currentText.append("<span style=\"" + jspSyntaxProperties.get(syntaxElements[i])
                                            + "; \">");
                                    break;
                                }
                            }
                        }
                    }
                    lastSyntaxPoint = currentSyntaxPoint; // move pointers and set flage
                    inTag = true;
                } else { // we have an end-tag
                    if (shallWrite) {// write content & end of highlighting?
                        currentText.append((parseLine(htmlEscape(syntaxPoints.getLineText(currentLine).substring(
                                lastSyntaxPoint.getColumn() - 1, currentSyntaxPoint.getColumn() - 1)))));
                        currentText.append("</span>");
                    }
                    shallWrite = true;
                    lastSyntaxPoint = currentSyntaxPoint;
                    inTag = false;
                }
            }
        }

        printPageEnd(writer);
        double time = new Date().getTime() - begin.getTime();
        MakumbaSystem.getLogger("devel.sourceViewer").fine("Sourcecode viewer took :" + (time / 1000) + " seconds");
    }

    /**
     * @param type
     * @return
     */
    private boolean isTagToHighlight(String type) {
        return type.equals("JspTagBegin") || type.equals("JspTagEnd") || type.equals("JspTagSimple")
                || isSystemtag(type);
    }

    private boolean isSystemtag(String type) {
        return type.equals("JspComment") || type.equals("JspScriptlet") || type.equals("JSPSystemTag");
    }

    public static void main(String args[]) {
        try {
            jspViewer.initDefaultProperties().store(new FileOutputStream("default.properties"),
                    "Makumba Developer Support - JSP syntax highlighting");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

