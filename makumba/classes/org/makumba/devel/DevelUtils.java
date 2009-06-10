package org.makumba.devel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.makumba.Pointer;
import org.makumba.commons.MakumbaResourceServlet;
import org.makumba.providers.Configuration;
import org.makumba.providers.TransactionProvider;

/**
 * This class combines some methods to print pages used by various developer support tools in package org.makumba.devel.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DevelUtils {

    /** Write the page footer to the given writer. */
    public static void printDeveloperSupportFooter(PrintWriter w) throws IOException {
        w.println("<hr><font size=\"-1\"><a href=\"http://www.makumba.org\">Makumba</a> developer support, version: "
                + org.makumba.MakumbaSystem.getVersion() + "; using database "
                + TransactionProvider.getInstance().getDefaultDataSourceName() + "</font>");
    }

    public static void writeScripts(PrintWriter w, String contextPath) {
        w.println("<script type=\"text/javascript\" src=\"" + contextPath + Configuration.getMakumbaResourcesLocation()
                + "/" + MakumbaResourceServlet.RESOURCE_PATH_JAVASCRIPT + "makumbaDevelScripts.js\">" + "</script>\n");
    }

    public static void writeStyles(PrintWriter w, String contextPath) {
        w.println("<link rel=\"StyleSheet\" type=\"text/css\" media=\"all\" href=\"" + contextPath
                + Configuration.getMakumbaResourcesLocation() + "/" + MakumbaResourceServlet.RESOURCE_PATH_CSS
                + "makumbaDevelStyles.css\"/>");
    }

    public static void writeStylesAndScripts(PrintWriter w, String contextPath) {
        writeScripts(w, contextPath);
        writeStyles(w, contextPath);
    }

    public static void writeTitleAndHeaderEnd(PrintWriter w, String title) {
        w.println("<title>" + title + "</title>");
        w.println("</head>");
        w.println();
    }

    public static void writePageBegin(PrintWriter w) {
        w.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        w.println("<html>");
        w.println("<head>");
        w.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");
    }

    public static void writePageEnd(PrintWriter w) throws IOException {
        printDeveloperSupportFooter(w);
        w.println("</body>");
        w.println("</html>");
    }

    public static String writePointerValueLink(String contextPath, Pointer pointer) {
        return writePointerValueLink(contextPath, pointer, null, true);
    }

    public static String writePointerValueLink(String contextPath, Pointer pointer, String pointerTitle,
            boolean printType) {
        String result = "<span style=\"font-size: smaller;\"> ";
        if (printType) {
            result += pointer.getType();
        }
        result += " <span style=\"color: green; afont-size: x-small;\">[";
        boolean haveTitle = StringUtils.isNotBlank(pointerTitle);
        result += "<a href=\"" + contextPath + Configuration.getDataViewerLocation() + "/" + pointer.getType()
                + "?ptr=" + pointer.toExternalForm() + "\" style=\"color: green\" title=\""
                + (haveTitle ? "Pointer: " + pointer.toExternalForm() + "; " : "") + "Database Value: "
                + pointer.longValue() + "; DBSV|Unique Index: " + pointer.getDbsv() + "|" + pointer.getUid() + "\">"
                + (haveTitle ? pointerTitle : pointer.toExternalForm()) + "</a>";
        result += "]</span> ";
        result += "</span>";
        return result;
    }

    public static void printPageHeader(PrintWriter w, String title) throws IOException {
        printPageHeader(w, title, null, null, null);
    }

    public static void printPageHeader(PrintWriter w, String title, String virtualPath, String realPath,
            String repositoryLink) throws IOException {
        w.println("<body bgcolor=white>");
        w.println("<table width=\"100%\" bgcolor=\"lightblue\">");
        w.println("<tr>");
        w.println("<td rowspan=\"2\">");

        if (!StringUtils.isBlank(title) && !title.equals(virtualPath)) {
            w.print("<font size=\"+2\"><font color=\"darkblue\">" + title + "</font></font>");
        } else if (virtualPath != null) {
            w.print("<font size=\"+2\"><a href=\"" + virtualPath + "\"><font color=\"darkblue\">" + virtualPath
                    + "</font></a></font>");
        }
        if (StringUtils.isNotBlank(repositoryLink)) {
            w.println(repositoryLink);
        }
        if (realPath != null) {
            w.println("<font size=\"-1\"><br>" + new File(realPath).getCanonicalPath() + "</font>");
        }
    }

    public static void writeDevelUtilLinks(PrintWriter w, String toolKey, String contextPath) {
        Map<String, String> allGenericDeveloperToolsMap = Configuration.getAllGenericDeveloperToolsMap();
        w.println("<a href=\"javascript:toggleElementDisplay(developerTools);\">Other tools</a>");
        w.println("<div id=\"developerTools\" class=\"popup\" style=\"display: none; right: 8px;\">");
        for (String key : allGenericDeveloperToolsMap.keySet()) {
            if (!key.equals(toolKey)) {
                if (Configuration.getConfigProperty(key).equals(Configuration.PROPERTY_NOT_SET)) {
                    w.print("<span style=\"color: grey\">" + allGenericDeveloperToolsMap.get(key)
                            + ": disabled </span><br/>");
                } else {
                    w.print("<a href=\"" + contextPath + Configuration.getMakumbaToolsLocation()
                            + Configuration.getConfigProperty(key) + "\">" + allGenericDeveloperToolsMap.get(key)
                            + "</a><br/>");
                }
            }
        }
        w.println("</div>");
    }

    public static String getVirtualPath(HttpServletRequest req, String toolLocation) {
        String path = req.getRequestURI();
        if (path == null)
            path = "/";
        if (path.startsWith(req.getContextPath())) {
            path = path.substring(req.getContextPath().length());
        }
        if (path.startsWith(toolLocation)) {
            path = path.substring(toolLocation.length());
        }
        if (path.equals("")) {
            path = "/";
        }
        return path;
    }

    public static boolean redirected(HttpServletRequest req, HttpServletResponse res, String servletPath)
            throws IOException {
        if (!servletPath.endsWith("/")) {
            res.sendRedirect(req.getRequestURI() + "/");
            return true;
        }
        return false;
    }

    public static void printResponseMessage(ServletResponse res, String title, String message) throws IOException {
        PrintWriter w = res.getWriter();
        res.setContentType("text/html");
        writePageBegin(w);
        writeTitleAndHeaderEnd(w, title);
        printPageHeader(w, title);
        w.println("</table>");
        w.println(message);
        writePageEnd(w);
        w.close();
    }

}
