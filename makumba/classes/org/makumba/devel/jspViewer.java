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
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.makumba.MakumbaError;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.analyser.PageCache;
import org.makumba.analyser.TagData;
import org.makumba.analyser.engine.JspParseData;
import org.makumba.analyser.engine.SourceSyntaxPoints;
import org.makumba.analyser.engine.SyntaxPoint;
import org.makumba.analyser.engine.TomcatJsp;
import org.makumba.commons.MakumbaJspAnalyzer;
import org.makumba.commons.MultipleKey;
import org.makumba.db.makumba.DBConnection;
import org.makumba.db.makumba.Query;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.tags.QueryTag;
import org.makumba.providers.Configuration;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.query.FunctionInliner;

/**
 * This class implements a viewer for .jsp files, and provides highlighting of <mak:>, <jsp:>and JSTL tags.
 * 
 * @version $Id$
 * @author Stefan Baebler
 * @author Rudolf Mayer
 */
public class jspViewer extends LineViewer {

    private static final Logger logger = java.util.logging.Logger.getLogger("org.makumba.org.makumba.devel.sourceViewer");

    boolean hasLogic;

    protected SyntaxPoint[] sourceSyntaxPoints;

    protected SourceSyntaxPoints syntaxPoints;

    private boolean hideComments = false;

    private boolean hideHTML = false;

    private boolean hideJSTLCore = false;

    private boolean hideJSTLFormat = false;

    private boolean hideMakumba = false;

    private boolean hideJava = false;

    private Map<Object, Object> queryCache;

    private Map<MultipleKey, TagData> tagDataCache = new LinkedHashMap<MultipleKey, TagData>();

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

        ServletContext servletContext = request.getSession().getServletContext();

        // compute the physical path of the currently viewed page on the webserver
        String thisFile = TomcatJsp.getJspURI(req);
        thisFile = thisFile.substring(0, thisFile.length() - 1);

        contextPath = req.getContextPath();
        String _servletPath = req.getServletPath();
        virtualPath = _servletPath.substring(0, _servletPath.length() - extraLength());
        jspSourceViewExtension = _servletPath.substring(_servletPath.length() - extraLength());
        realPath = servletContext.getRealPath(virtualPath);
        _servletPath = _servletPath.substring(0, _servletPath.indexOf(".")) + ".jsp";
        logicPath = contextPath + Configuration.getLogicDiscoveryViewerLocation() + _servletPath;
        hasLogic = !(org.makumba.controller.Logic.getLogic(_servletPath) instanceof org.makumba.LogicNotFoundException);
        jspClasspath = TomcatJsp.getContextCompiledJSPDir(servletContext);

        JspParseData jspParseData = new JspParseData(servletContext.getRealPath("/"), thisFile,
                JspxJspAnalyzer.getInstance());
        try {
            sourceSyntaxPoints = jspParseData.getSyntaxPointArray(null);

            // set background colour for hibernate code
            if (jspParseData.isUsingHibernate()) {
                additionalCodeStyleClasses = "hibernatePage";
            }

            syntaxPoints = jspParseData.getSyntaxPoints();
        } catch (ProgrammerError e) {
            caughtError = e;
        }

        String compiledJSPFile = findCompiledJSPClassName(TomcatJsp.getFullCompiledJSPDir(servletContext), virtualPath);
        if (compiledJSPFile != null) {
            additionalHeaderInfo = "<a style=\"font-size:smaller;\" href=\"" + request.getContextPath()
                    + Configuration.getJavaViewerLocation() + "/" + compiledJSPFile + "\">[Compiled Version]</a>";
        }

        reader = new FileReader(realPath);
        String path = TomcatJsp.getJspURI(request);
        if (path.endsWith(jspSourceViewExtension)) {
            path = path.substring(0, path.length() - jspSourceViewExtension.length());
        }
        JspParseData jpd = JspParseData.getParseData(request.getSession().getServletContext().getRealPath("/"), path,
            MakumbaJspAnalyzer.getInstance());
        PageCache pageCache = null;
        try {
            // FIXME: this is a bit of a hack...
            // If there is an error while calling jpd.getAnalysisResult(), the first time we call this, we actually get
            // the exception thrown. Subsequent calls will however have cached the error as the result (via
            // NamedResources), and return us the error ..
            // thus, before we cast to pageCache, we check the class of the result
            // and if the class is a throwable, we throw it on ...
            Object analysisResult = jpd.getAnalysisResult(null);
            if (analysisResult instanceof MakumbaError) {
                throw (MakumbaError) analysisResult;
            } else if (analysisResult instanceof Throwable) {
                throw (Throwable) analysisResult;
            }
            pageCache = (PageCache) analysisResult;
        } catch (MakumbaError pe) {
            // page analysis failed
            parseError = pe;
            return;
        } catch (Throwable t) {
            // page analysis failed
            parseError = t;
            logger.warning("Page analysis for page " + path + " failed with an unexpected error '" + t.getMessage()
                    + "' (" + t.getClass() + ")");
            return;
        }

        // get only query tags from all the tags in the page
        final Map<Object, Object> tempTagDataCache = pageCache.retrieveCache(MakumbaJspAnalyzer.TAG_DATA_CACHE);
        if (tempTagDataCache != null) {
            final Set<Object> keySet = tempTagDataCache.keySet();
            for (Object key : keySet) {
                final Object value = tempTagDataCache.get(key);
                if (key instanceof MultipleKey && value instanceof TagData) {
                    TagData td = (TagData) value;
                    final Object tagObject = td.getTagObject();
                    if (tagObject instanceof QueryTag) {
                        tagDataCache.put((MultipleKey) key, (TagData) value);
                    }
                } else { // shouldn't happen
                    logger.warning("Unexpected contents in " + MakumbaJspAnalyzer.TAG_DATA_CACHE + " cache: " + key
                            + " => " + value);
                }
            }
        }
        queryCache = pageCache.retrieveCache(MakumbaJspAnalyzer.QUERY);
    }

    @Override
    public void intro(PrintWriter w) throws IOException {
        if (parseError != null) {
            w.print("<td rowspan=\"2\" align=\"center\" style=\"color: red;\">errors!<br><a href=\"#errors\">details</a></td>");
        }
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
        w.println("Hide: <input type=\"checkbox\" name=\"hideComments\" value=\"true\""
                + (hideComments ? " checked=\"checked\"" : "") + ">Comments  ");
        w.println("<input type=\"checkbox\" name=\"hideHTML\" value=\"true\""
                + (hideHTML ? " checked=\"checked\"" : "") + ">HTML  ");
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
    @Override
    public void parseText(PrintWriter writer) throws IOException {
        // if we have no syntaxpoints, maybe due to an exception, we just display the text w/o highlighting
        if (sourceSyntaxPoints == null) {
            super.parseText(writer);
            return;
        }
        Date begin = new Date();
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
                logger.finest("skipped syntax Point due to wrong offset: "
                        + (currentSyntaxPoint.isBegin() ? "begin " : "end ") + currentSyntaxPoint.getType() + " "
                        + currentSyntaxPoint.getLine() + ":" + currentSyntaxPoint.getColumn() + ":; linelength is: "
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
                    StringBuilder content = new StringBuilder(parseTagContent(lastSyntaxPoint, currentSyntaxPoint,
                        lineText, currentLineLength));
                    // if we have a mak:list or mak:object tag with a query, close the query annotation link
                    MultipleKey tagKey = getTagDataKey(lastSyntaxPoint);
                    if (tagKey != null && queryCache != null && queryCache.get(tagKey) != null) {
                        content.insert(endOfQueryTagName(content), "</a>");
                    }
                    currentText.append(content);
                }

                // if the current line contained any text to write or we are outside a tag & shall write html
                if ((!currentText.toString().trim().equals("") || inTag < 1 && !hideHTML || inTag > 0 && shallWrite)
                        && printLineNumbers) {
                    writer.print("\n");
                    writeLineNumber(writer, currentLine, !hideLineNumbers);
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
                            currentText.append(parseTagContent(lastSyntaxPoint, currentSyntaxPoint, lineText,
                                currentLineLength));
                        }
                    } else if (currentSyntaxPoint.getOriginalColumn(currentLineLength) > 1 && !hideHTML && shallWrite) {
                        // not in a tag , but maybe there was HTMl before?
                        currentText.append(parseTagContent(lastSyntaxPoint, currentSyntaxPoint, lineText,
                            currentLineLength));
                    }

                    String tagType = lineText.substring(currentSyntaxPoint.getOriginalColumn(currentLineLength));

                    // we have a scriplet (<% ... %>)
                    if (type.equals("JspScriptlet")) {
                        if (hideJava) { // check whether show or hide
                            shallWrite = false;
                        } else {
                            currentText.append("<span class=\"jspScriptlet\">");
                        }
                    } else if (type.equals("JspComment")) { // we have a JSP comment (<%-- ... --%>)
                        if (hideComments) { // check whether show or hide
                            shallWrite = false;
                        } else {
                            currentText.append("<span class=\"jspComment\">");
                        }
                    } else if (type.equals("JSPSystemTag")) { // we have a JSP system tag (<%@ .. %>)
                        currentText.append("<span class=\"jspSystemTag\">");
                    } else if (type.equals("ExpressionLanguage")) { // we have JSP EL ($...})
                        currentText.append("<span class=\"expressionLanguage\">");
                    } else {// we have any other taglib tag
                        if ((tagType.startsWith("mak") || tagType.startsWith("/mak")) && hideMakumba
                                || (tagType.startsWith("c") || tagType.startsWith("/c")) && hideJSTLCore
                                || (tagType.startsWith("fmt") || tagType.startsWith("/fmt")) && hideJSTLFormat) {
                            shallWrite = false;
                        }

                        if (shallWrite) { // do the defined highlighting
                            String tagClass = tagType;
                            if (tagClass.contains(":")) {
                                tagClass = tagClass.substring(0, tagType.indexOf(":")) + "Tag";
                            }
                            if (tagClass.startsWith("/")) {
                                tagClass = tagClass.substring(1);
                            }

                            // if we have a mak:list or mak:object tag, annotate the query with a pop-up
                            MultipleKey tagKey = getTagDataKey(currentSyntaxPoint);
                            if (tagKey != null && queryCache != null && queryCache.get(tagKey) != null) {

                                currentText.append("<span class=\"" + tagClass + "\">");
                                String divId = "query" + currentSyntaxPoint.getLine() + "x"
                                        + currentSyntaxPoint.getColumn();
                                currentText.append("<div id=\"" + divId
                                        + "\" class=\"popup queryPopup\" style=\"display: none;\">");

                                String queryOQL = ((ComposedQuery) queryCache.get(tagKey)).getComputedQuery();
                                currentText.append("OQL: " + queryOQL + "<br/>");
                                // FIXME: use default inliner, with QueryAnalysisProvider.inlineFunctions
                                String queryInlined = FunctionInliner.inline(queryOQL,
                                    QueryProvider.getQueryAnalzyer("oql"));
                                if (!queryInlined.equals(queryOQL)) {
                                    currentText.append("OQL inlined: " + queryInlined + "<br/>");
                                }

                                Transaction t = null;
                                try {
                                    t = TransactionProvider.getInstance().getConnectionTo(
                                        TransactionProvider.getInstance().getDefaultDataSourceName());
                                    Query oqlQuery = ((DBConnection) t).getQuery(queryOQL);
                                    if (oqlQuery instanceof org.makumba.db.makumba.sql.Query) {
                                        org.makumba.db.makumba.sql.Query sqlQuery = (org.makumba.db.makumba.sql.Query) oqlQuery;
                                        currentText.append("SQL: " + sqlQuery.getCommand(new HashMap<String, Object>())
                                                + "<br/>");
                                    }
                                } catch (Exception e) {
                                    currentText.append("<i>Problem generating SQL: "
                                            + org.makumba.commons.StringUtils.getExceptionStackTrace(e) + "</i>");
                                } finally {
                                    if (t != null) {
                                        t.close();
                                    }
                                }

                                currentText.append("</div>");
                                currentText.append("<a href=\"javascript:toggleElementDisplay(" + divId
                                        + ");\" title=\"Click to show the query details\">");
                            } else {
                                currentText.append("<span class=\"" + tagClass + "\">");
                            }

                        }
                    }
                    lastSyntaxPoint = currentSyntaxPoint; // move pointers and set flage
                } else { // we have an end-tag
                    if (shallWrite) {// write content & end of highlighting?
                        StringBuilder content = new StringBuilder(parseTagContent(lastSyntaxPoint, currentSyntaxPoint,
                            lineText, currentLineLength));
                        // if we have a mak:list or mak:object tag with a query, close the query annotation link
                        MultipleKey tagKey = getTagDataKey(lastSyntaxPoint);
                        if (tagKey != null && queryCache != null && queryCache.get(tagKey) != null) {
                            content.insert(endOfQueryTagName(content), "</a>");
                        }
                        currentText.append(content);
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
        logger.finer("Sourcecode viewer took :" + time / 1000 + " seconds");
    }

    private int endOfQueryTagName(StringBuilder content) {
        return content.indexOf(":list") > 0 ? content.indexOf(":list") + ":list".length() : content.indexOf(":object")
                + ":object".length();
    }

    private String parseTagContent(SyntaxPoint lastSyntaxPoint, SyntaxPoint currentSyntaxPoint, String lineText,
            int currentLineLength) {
        return parseLine(htmlEscape(lineText.substring(lastSyntaxPoint.getOriginalColumn(currentLineLength) - 1,
            currentSyntaxPoint.getOriginalColumn(currentLineLength) - 1)));
    }

    private MultipleKey getTagDataKey(SyntaxPoint currentSyntaxPoint) {
        if (tagDataCache != null) {
            for (MultipleKey key : tagDataCache.keySet()) {
                TagData td = tagDataCache.get(key);
                if (td.getStartLine() == currentSyntaxPoint.getLine()
                        && td.getStartColumn() == currentSyntaxPoint.getColumn()) {
                    return key;
                }
            }
        }
        return null;
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
