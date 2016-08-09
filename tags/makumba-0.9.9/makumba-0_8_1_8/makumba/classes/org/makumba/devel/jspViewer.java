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

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.Date;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.makumba.ProgrammerError;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.engine.SourceSyntaxPoints;
import org.makumba.analyser.engine.SyntaxPoint;
import org.makumba.analyser.engine.TomcatJsp;
import org.makumba.commons.ClassResource;
import org.makumba.providers.Configuration;

/**
 * This classe implements a viewer for .jsp files, and provides highlighting of <mak:>, <jsp:>and JSTL tags.
 * 
 * @version $Id$
 * @author Stefan Baebler
 * @author Rudolf Mayer
 */
public class jspViewer extends LineViewer {
    /**
     * Usage: put the beginning of the tag-name as key, and the style-info you want for the created span around that
     * tag. both 'key' and /'key' will be matched - if you want a different colouring for /'key', put that as an own
     * entry, and make sure it is put <b>before </b> the 'key' entry - otherwise it will get matched twice.
     */

    private static final String PROPERTIES_FILE_NAME = "jspSyntax.properties";

    /**
     * defines some default taglibs which should be highlighted. will be used when loading the properties file fails.
     */
    private static Properties defaultTaglibProperties = new Properties();
    static {
        defaultTaglibProperties.put("mak", "background:#eecccc; color:green; border:red thin; font-weight: bold; ");
        defaultTaglibProperties.put("jsp", "background:lightcyan; color:green; border:red thin; font-weight: bold; ");
        defaultTaglibProperties.put("fmt", "background::chartreuse; color:green; border:red thin; font-weight: bold; ");
        defaultTaglibProperties.put("c", "background:lightblue; color:green; border:red thin; font-weight: bold; ");
    }

    private static Properties taglibgSytleProperties = new Properties();

    private static Properties SystemStyleProperties = new Properties();

    private static final String DEFAULT_JSPSYSTEMTAG_STYLE = defaultTaglibProperties.getProperty("jsp");

    private static final String DEFAULT_JSPSCRIPLET_STYLE = "background:gold;color:green; border:red thin; font-weight: bold;";

    private static final String DEFAULT_JSPCOMMENT_STYLE = "color:gray;";

    private static final String DEFAULT_JSPEXPRESSIONLANGUAGE_STYLE = "font-style:italic;";

    private static final String DEFAULT_HIBERNATEPAGE_STYLE = "background-color: #CCFFCC;";

    private static String hibernateCodeBackgroundStyle;

    static {
        initProperties();
    }

    /**
     * Loads the properties file, if that fails uses {@link org.makumba.devel.javaViewer#initDefaultProperties()
     * initDefaultProperties}to get default values.
     */
    private static void initProperties() {
        // TODO: should we parse the tag-lib import and check for the prefix?
        try {
            URLConnection connection = (ClassResource.get(PROPERTIES_FILE_NAME)).openConnection();
            Properties jspSyntaxProperties = new Properties();
            jspSyntaxProperties.load(connection.getInputStream());

            // we load from the properties file the non-taglib properties, using defaults when necessary
            SystemStyleProperties.put("ExpressionLanguage", jspSyntaxProperties.getProperty("ExpressionLanguage",
                DEFAULT_JSPEXPRESSIONLANGUAGE_STYLE));
            SystemStyleProperties.put("JspScriptlet", jspSyntaxProperties.getProperty("JspScriptlet",
                DEFAULT_JSPSCRIPLET_STYLE));
            SystemStyleProperties.put("JspComment", jspSyntaxProperties.getProperty("JspComment",
                DEFAULT_JSPCOMMENT_STYLE));
            SystemStyleProperties.put("JSPSystemTag", jspSyntaxProperties.getProperty("JSPSystemTag",
                jspSyntaxProperties.getProperty("jsp", DEFAULT_JSPSYSTEMTAG_STYLE)));

            // now we remove the non-taglib properties, and use the remainders as taglib properties fiel
            jspSyntaxProperties.remove("JspScriptlet");
            jspSyntaxProperties.remove("JspComment");
            jspSyntaxProperties.remove("JSPSystemTag");
            jspSyntaxProperties.remove("ExpressionLanguage");
            taglibgSytleProperties = jspSyntaxProperties;

            // set background colour for hibernate code
            hibernateCodeBackgroundStyle = jspSyntaxProperties.getProperty("HibernatePage", DEFAULT_HIBERNATEPAGE_STYLE);

        } catch (Throwable t) { // the properties file was not found / readable / etc.

            java.util.logging.Logger.getLogger("org.makumba." + "org.makumba.devel.sourceViewer").fine(
                "JSP syntax highlighting properties file '" + PROPERTIES_FILE_NAME
                        + "' not found! Using default values.");

            // we use only default values
            SystemStyleProperties.put("ExpressionLanguage", DEFAULT_JSPEXPRESSIONLANGUAGE_STYLE);
            SystemStyleProperties.put("JspScriptlet", DEFAULT_JSPSCRIPLET_STYLE);
            SystemStyleProperties.put("JspComment", DEFAULT_JSPCOMMENT_STYLE);
            SystemStyleProperties.put("JSPSystemTag", DEFAULT_JSPSYSTEMTAG_STYLE);
            taglibgSytleProperties = defaultTaglibProperties;
            hibernateCodeBackgroundStyle = DEFAULT_HIBERNATEPAGE_STYLE;
        }
    }

    private static Set syntaxKeys = taglibgSytleProperties.keySet();

    boolean hasLogic;

    protected SyntaxPoint[] sourceSyntaxPoints;

    protected SourceSyntaxPoints syntaxPoints;

    private boolean hideComments = false;

    private boolean hideHTML = false;

    private boolean hideJSTLCore = false;

    private boolean hideJSTLFormat = false;

    private boolean hideMakumba = false;

    private boolean hideJava = false;

    private int extraLength() {
        return 1;
    }

    public jspViewer(HttpServletRequest req, boolean printLineNumbers) throws Exception {
        super(printLineNumbers, req);
        setSearchLevels(true, false, false, true);
        hideLineNumbers = request.getParameter(PARAM_HIDE_LINES) == null
                || request.getParameter(PARAM_HIDE_LINES).equals("true");
    }

    public jspViewer(HttpServletRequest req) throws Exception {
        super(true, req);
        setSearchLevels(true, false, false, true);
        hideComments = Boolean.valueOf(String.valueOf(req.getParameter("hideComments"))).booleanValue();
        hideHTML = Boolean.valueOf(String.valueOf(req.getParameter("hideHTML"))).booleanValue();
        hideJSTLCore = Boolean.valueOf(String.valueOf(req.getParameter("hideJSTLCore"))).booleanValue();
        hideJSTLFormat = Boolean.valueOf(String.valueOf(req.getParameter("hideJSTLFormat"))).booleanValue();
        hideMakumba = Boolean.valueOf(String.valueOf(req.getParameter("hideMakumba"))).booleanValue();
        hideJava = Boolean.valueOf(String.valueOf(req.getParameter("hideJava"))).booleanValue();

        String thisFile = TomcatJsp.getJspURI(req);
        thisFile = thisFile.substring(0, thisFile.length() - 1);

        contextPath = req.getContextPath();
        String _servletPath = req.getServletPath();
        virtualPath = _servletPath.substring(0, _servletPath.length() - extraLength());
        jspSourceViewExtension = _servletPath.substring(_servletPath.length() - extraLength());
        realPath = request.getSession().getServletContext().getRealPath(virtualPath);
        _servletPath = _servletPath.substring(0, _servletPath.indexOf(".")) + ".jsp";
        logicPath = contextPath + Configuration.getLogicDiscoveryViewerLocation() + _servletPath;
        hasLogic = !(org.makumba.controller.Logic.getLogic(_servletPath) instanceof org.makumba.LogicNotFoundException);

        JspParseData jspParseData = new JspParseData(request.getSession().getServletContext().getRealPath("/"),
                thisFile, JspxJspAnalyzer.getInstance());
        try {
            sourceSyntaxPoints = jspParseData.getSyntaxPointArray(null);

            // set background colour for hibernate code
            if (jspParseData.isUsingHibernate()) {
                codeBackgroundStyle = hibernateCodeBackgroundStyle;
            }

            syntaxPoints = jspParseData.getSyntaxPoints();
        } catch (ProgrammerError e) {
            caughtError = e;
        }
        reader = new FileReader(realPath);
    }

    public void intro(PrintWriter w) throws IOException {
        w.println("<td align=\"right\" style=\"color: darkblue; padding: 5px; padding-top: 10px\">");
        printFileRelations(w);
        w.println("&nbsp;&nbsp;&nbsp;");
        String executePath = contextPath + virtualPath;
        if (StringUtils.isNotEmpty(request.getQueryString())) {
            executePath += "?" + request.getQueryString();
        }
        w.println("<a href=\"" + executePath + "\">execute</a>&nbsp;&nbsp;&nbsp;");
        w.println("<span style=\"color:lightblue; background-color: darkblue; padding: 5px;\">source</span>&nbsp;&nbsp;&nbsp;");
        w.println("<a href=\"" + logicPath + "\">business logic" + (hasLogic ? "" : " (none)") + "</a>");

        String lg = org.makumba.devel.ErrorControllerHandler.getLoginPage(this.request, virtualPath);
        if (lg != null) {
            w.println("&nbsp;&nbsp;&nbsp;<a href=\"" + contextPath + lg + "x\">login page</a>&nbsp;&nbsp;&nbsp;");
        }
        
        w.println("&nbsp;&nbsp;&nbsp;");
        DevelUtils.writeDevelUtilLinks(w, "", contextPath);

        w.println("</td>");
        w.println("</tr>");
        w.println("<tr>");
        w.println("<td align=\"right\" style=\" font-size: smaller;\">");
        w.println("<form method=\"get\" action>");
        w.println("Hide:" + " <input type=\"checkbox\" name=\"hideComments\" value=\"true\""
                + (hideComments ? " checked=\"checked\"" : "") + ">Comments  ");
        w.println("<input type=\"checkbox\" name=\"hideHTML\" value=\"true\""
                + (hideHTML ? " checked=\"checked\"" : "") + ">HML  ");
        w.println("<input type=\"checkbox\" name=\"hideJava\" value=\"true\""
                + (hideJava ? " checked=\"checked\"" : "") + ">Java  ");
        w.println("<input type=\"checkbox\" name=\"hideJSTLCore\" value=\"true\""
                + (hideJSTLCore ? " checked=\"checked\"" : "") + ">JSTL Core  ");
        w.println("<input type=\"checkbox\" name=\"hideJSTLFormat\" value=\"true\""
                + (hideJSTLFormat ? " checked=\"checked\"" : "") + ">JSTL Format  ");
        w.println("<input type=\"checkbox\" name=\"hideMakumba\" value=\"true\""
                + (hideMakumba ? " checked=\"checked\"" : "") + ">Makumba  ");
        w.println("<input type=\"submit\" value=\"apply\"> ");
        w.println("</form>");
        w.println("</td>");
    }

    /**
     * Parse the text and write the output <br>
     * Known problems: when hiding parts of the code (e.g. HTML, JSTL,..) nested tags work only up to one level (i.e. a
     * tag nested in a tag nested in a tag might not be hidden/shown as expected)
     */
    public void parseText(PrintWriter writer) throws IOException {
        // if we have no syntaxpoints, maybe due to an exception, we just display the text w/o highlighting
        if (sourceSyntaxPoints == null) {
            super.parseText(writer);
            return;
        }
        Date begin = new Date();
        Object[] syntaxElements = syntaxKeys.toArray();
        printPageBegin(writer);

        SyntaxPoint lastSyntaxPoint = null;
        boolean shallWrite = true;
        boolean lastShallWrite = false;
        int inTag = 0;

        StringBuffer currentText = new StringBuffer();

        for (int j = 0; sourceSyntaxPoints != null && j < sourceSyntaxPoints.length; j++) {
            SyntaxPoint currentSyntaxPoint = sourceSyntaxPoints[j];
            String type = currentSyntaxPoint.getType();
            int currentLine = currentSyntaxPoint.getLine();
            String lineText = syntaxPoints.getLineText(currentLine);
            int currentLineLength = lineText.length();

            if (currentSyntaxPoint.getOriginalColumn(currentLineLength) > syntaxPoints.getLineText(currentLine).length() + 1) {
                java.util.logging.Logger.getLogger("org.makumba." + "org.makumba.devel.sourceViewer").finest(
                    "skipped syntax Point due to wrong offset: " + (currentSyntaxPoint.isBegin() ? "begin " : "end ")
                            + currentSyntaxPoint.getType() + " " + currentSyntaxPoint.getLine() + ":"
                            + currentSyntaxPoint.getColumn() + ":; linelength is: "
                            + syntaxPoints.getLineText(currentLine).length());
                continue;
            }
            if (type.equals("TextLine") && currentSyntaxPoint.isBegin()) { // begin of line found - we just move the
                                                                           // last point marker
                lastSyntaxPoint = currentSyntaxPoint;
            } else if (type.equals("TextLine") && !currentSyntaxPoint.isBegin()) { // end of line found

                // we write if we are not on column 1 (empty text line) and either are showing HTML or are in a tag
                if (currentSyntaxPoint.getOriginalColumn(currentLineLength) > 1 && (!hideHTML || inTag > 0)
                        && shallWrite) {
                    currentText.append(parseLine(htmlEscape(lineText.substring(
                        lastSyntaxPoint.getOriginalColumn(currentLineLength) - 1,
                        currentSyntaxPoint.getOriginalColumn(currentLineLength) - 1))));
                }

                // if the current line contained any text to write or we are outside a tag & shall write html
                if ((!currentText.toString().trim().equals("") || (inTag < 1 && !hideHTML) || (inTag > 0 && shallWrite))
                        && printLineNumbers) {
                    writer.print("\n");
                    if (!hideLineNumbers) {
                        writer.print("<a style=\"font-weight: normal; \" name=\"" + currentLine + "\" href=\"#"
                                + currentLine + "\" class=\"lineNo\">" + currentLine + ":\t</a>");
                    }
                }
                writer.print(currentText.toString());
                currentText = new StringBuffer();
                lastSyntaxPoint = currentSyntaxPoint; // move pointer to last syntax Point
            } else if (isTagToHighlight(type)) { // we are in a tag that might be highlighted
                if (currentSyntaxPoint.isBegin()) { // we are at the beginning of such a tag
                    inTag++;
                    if (inTag > 1) { // we are in a nested tag
                        lastShallWrite = shallWrite;
                        if (lastShallWrite) {
                            currentText.append(parseLine(htmlEscape(lineText.substring(
                                lastSyntaxPoint.getOriginalColumn(currentLineLength) - 1,
                                currentSyntaxPoint.getOriginalColumn(currentLineLength) - 1))));
                        }
                    } else if (currentSyntaxPoint.getOriginalColumn(currentLineLength) > 1 && !hideHTML && shallWrite) { // not
                                                                                                                         // in
                                                                                                                         // a
                                                                                                                         // tag
                                                                                                                         // ,
                                                                                                                         // but
                                                                                                                         // maybe
                        // there was HTMl before?
                        currentText.append(parseLine(htmlEscape(lineText.substring(
                            lastSyntaxPoint.getOriginalColumn(currentLineLength) - 1,
                            currentSyntaxPoint.getOriginalColumn(currentLineLength) - 1))));
                    }

                    String tagType = lineText.substring(currentSyntaxPoint.getOriginalColumn(currentLineLength));

                    // we have a scriplet (<% ... %>)
                    if (type.equals("JspScriptlet")) {
                        if (hideJava) { // check whether show or hide
                            shallWrite = false;
                        } else {
                            currentText.append("<span style=\"" + SystemStyleProperties.get("JspScriptlet") + "; \">");
                        }
                    } else if (type.equals("JspComment")) { // we have a JSP comment (<%-- ... --%>)
                        if (hideComments) { // check whether show or hide
                            shallWrite = false;
                        } else {
                            currentText.append("<span style=\"" + SystemStyleProperties.get("JspComment") + "; \">");
                        }
                    } else if (type.equals("JSPSystemTag")) { // we have a JSP system tag (<%@ .. %>)
                        currentText.append("<span style=\"" + SystemStyleProperties.get("JSPSystemTag") + "; \">");
                    } else if (type.equals("ExpressionLanguage")) { // we have JSP EL ($...})
                        currentText.append("<span style=\"" + SystemStyleProperties.get("ExpressionLanguage") + "; \">");
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
                                    currentText.append("<span style=\"" + taglibgSytleProperties.get(syntaxElements[i])
                                            + "; \">");
                                    break;
                                }
                            }
                        }
                    }
                    lastSyntaxPoint = currentSyntaxPoint; // move pointers and set flage
                } else { // we have an end-tag
                    if (shallWrite) {// write content & end of highlighting?
                        currentText.append((parseLine(htmlEscape(lineText.substring(
                            lastSyntaxPoint.getOriginalColumn(currentLineLength) - 1,
                            currentSyntaxPoint.getOriginalColumn(currentLineLength) - 1)))));
                        currentText.append("</span>");
                    }
                    if (inTag > 1) { // in a nested tag?
                        shallWrite = lastShallWrite; // remember if we were supposed to write or not.
                    } else {
                        shallWrite = true;
                    }
                    lastSyntaxPoint = currentSyntaxPoint;
                    inTag--;
                }
            }
        }

        printPageEnd(writer);
        double time = new Date().getTime() - begin.getTime();
        java.util.logging.Logger.getLogger("org.makumba." + "org.makumba.devel.sourceViewer").finer(
            "Sourcecode viewer took :" + (time / 1000) + " seconds");
    }

    /**
     * @param type
     * @return
     */
    private boolean isTagToHighlight(String type) {
        return type.equals("JspTagBegin") || type.equals("JspTagEnd") || type.equals("JspTagSimple")
                || type.equals("ExpressionLanguage") || isSystemtag(type);
    }

    private boolean isSystemtag(String type) {
        return type.equals("JspComment") || type.equals("JspScriptlet") || type.equals("JSPSystemTag");
    }
    
    @Override
    protected void printPageBeginAdditional(PrintWriter writer) throws IOException {
        super.printPageBeginAdditional(writer);
    }

}
