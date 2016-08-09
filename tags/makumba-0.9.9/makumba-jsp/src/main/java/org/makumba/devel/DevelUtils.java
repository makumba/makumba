package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.makumba.Pointer;
import org.makumba.commons.http.MakumbaResourceServlet;
import org.makumba.commons.http.MakumbaServlet;
import org.makumba.commons.tags.MakumbaJspConfiguration;
import org.makumba.providers.Configuration;

/**
 * This class combines some methods to print pages used by various developer support tools in package org.makumba.devel.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DevelUtils {

    /** Write the page footer to the given writer. */
    public static void printDeveloperSupportFooter(PrintWriter w) throws IOException {
        w.println("<hr style=\"clear: both;\"/>\n"
                + "<a style=\"font-size: smaller\" href=\"http://www.makumba.org\">Makumba</a> developer support; version:"
                + org.makumba.MakumbaSystem.getVersion() + " " + Configuration.getRemoteDataSourceConfigurationPath());
    }

    public static void writeScripts(PrintWriter w, String contextPath, String... additionalScripts) {
        String path = contextPath + MakumbaJspConfiguration.getServletLocation(MakumbaServlet.RESOURCES) + "/"
                + MakumbaResourceServlet.RESOURCE_PATH_JAVASCRIPT;
        w.println("<script type=\"text/javascript\" src=\"" + path + "jquery.min.js\"></script>");
        w.println("<script type=\"text/javascript\" src=\"" + path + "makumbaDevelScripts.js\"></script>");
        w.println("<script type=\"text/javascript\" src=\"" + path + "bootstrap.min.js\"></script>");
        w.println("<script type=\"text/javascript\" src=\"" + path + "highlight.pack.js\"></script>");
        if (additionalScripts != null) {
            for (String s : additionalScripts) {
                w.println("<script type=\"text/javascript\" src=\"" + path + s + "\"></script>\n");
            }
        }
    }

    public static void writeStyles(PrintWriter w, String contextPath, String... additionalStyles) {
        String path = contextPath + MakumbaJspConfiguration.getServletLocation(MakumbaServlet.RESOURCES) + "/"
                + MakumbaResourceServlet.RESOURCE_PATH_CSS;
        w.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"" + path + "bootstrap.min.css\"/>");
        w.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"" + path
                + "makumbaDevelStyles.css\"/>");
        if (additionalStyles != null) {
            for (String s : additionalStyles) {
                w.println("<link rel=\"stylesheet\" type=\"text/css\" media=\"all\" href=\"" + path + s + "\"/>");
            }
        }
    }

    public static void writeStylesAndScripts(PrintWriter w, String contextPath, String... additionalScripts) {
        writeScripts(w, contextPath, additionalScripts);
        writeStyles(w, contextPath);
    }

    public static void writeTitleAndHeaderEnd(PrintWriter w, String title) {
        w.println("<title>" + title + "</title>");
        w.println("</head>");
        w.println();
    }

    public static void writePageBegin(PrintWriter w) {
        w.println("<!DOCTYPE html>");
        w.println("<html>");
        w.println("<head>");
        w.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");
    }

    public static void writePageEnd(PrintWriter w) throws IOException {
        printDeveloperSupportFooter(w);
        w.println("</div>");
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
        result += "<a href=\"" + contextPath + MakumbaJspConfiguration.getToolLocation(DeveloperTool.OBJECT_VIEWER)
                + "/" + pointer.getType() + "?ptr=" + pointer.toExternalForm() + "\" style=\"color: green\" title=\""
                + (haveTitle ? "Pointer: " + pointer.toExternalForm() + "; " : "") + "Database Value: "
                + pointer.longValue() + "; DBSV|Unique Index: " + pointer.getDbsv() + "|" + pointer.getUid() + "\">"
                + (haveTitle ? pointerTitle : pointer.toExternalForm()) + "</a>";
        result += "]</span> ";
        result += "</span>";
        return result;
    }

    public static void printNavigationBegin(PrintWriter w, String title) throws IOException {
        w.println("<body>");
        w.println("<div class=\"navbar\">");
        w.println("  <div class=\"navbar-inner\">");
        w.println("    <a class=\"brand\" href=\"#\">" + title + "</a>");
        w.println("    <ul class=\"nav\">");
        w.println("      <li class=\"divider-vertical\"></li>");
    }

    public static void printNavigationEnd(PrintWriter w) {
        w.println("    </ul>");
        w.println("  </div>");
        w.println("</div>");
        w.println("<div class=\"wrapper\">");
    }

    public static void printPopoverLink(PrintWriter w, String name, String title, String popoverId) {
        w.println("<li><a href=\"#\" rel=\"popover\" data-title=\"" + title + "\" data-popover-id=\"" + popoverId
                + "\" title=\"" + title + "\">" + name + "</a></li>");
    }

    public static void printNavigationButton(PrintWriter w, String name, String link, String title, int type) {
        switch (type) {
            case 2:
                w.println("<li class=\"disabled\"><a href=\"#\" title=\"" + title + "\">" + name + "</a></li>");
                break;
            case 1:
                w.println("<li class=\"active\"><a href=\"#\" title=\"" + title + "\">" + name + "</a></li>");
                break;
            case 0:
                w.println("<li><a href=\"" + link + "\" title=\"" + title + "\">" + name + "</a></li>");
                break;
        }
    }

    public static void printErrorMessage(PrintWriter w, String note, String message) {
        if (StringUtils.isBlank(note)) {
            w.println("<div class=\"alert alert-error\"><strong>" + note + "</strong>" + message + "</div>");
        } else {
            w.println("<div class=\"alert alert-error\">" + message + "</div>");
        }
    }

    public static void printSQLQuery(PrintWriter w, String sql) {
        w.println("<pre>");
        w.print("<code>");
        w.println(sql);
        w.println("</code></pre>");
    }

    public static void writeDevelUtilLinks(PrintWriter w, String toolKey, String contextPath) {
        w.println("</ul>");
        w.println("<ul class=\"nav pull-right\">");
        w.println("<li class=\"divider-vertical\"></li>");
        w.println("<li class=\"dropdown\"><a href=\"#\" class=\"dropdown-toggle\" data-toggle=\"dropdown\">Other tools<b class=\"caret\"></b></a>");
        w.println("<ul class=\"dropdown-menu\">");
        for (DeveloperTool t : DeveloperTool.values()) {
            if (!t.getKey().equals(toolKey) && t.isGeneric()) {
                if (MakumbaJspConfiguration.getToolLocation(t) == null
                        || MakumbaJspConfiguration.getToolLocation(t) == Configuration.PROPERTY_NOT_SET) {
                    w.print("<li class=\"disabled\"><a tabindex=\"-1\" href=\"#\" title=\"Tool disabled via Makumba.conf\">"
                            + t.getName() + "</a></li>");
                } else if (t.isGeneric()) {
                    w.print("<li><a tabindex=\"-1\" href=\"" + contextPath + MakumbaJspConfiguration.getToolLocation(t)
                            + "\">" + t.getName() + "</a></li>");
                }
            }
        }
        w.println("  </ul>");
        w.println("</li>");
    }

    public static String getVirtualPath(HttpServletRequest req, String toolLocation) {
        String path = req.getRequestURI();
        if (path == null) {
            path = "/";
        }
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
        printNavigationBegin(w, title);
        w.println("</div>");
        w.println(message);
        writePageEnd(w);
        w.close();
    }

}
