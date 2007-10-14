package org.makumba.devel;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.makumba.Pointer;

/**
 * This class combines some methods to print pages used by various developer support tools in package org.makumba.devel.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DevelUtils {

    /** Write the page footer to the given writer. */
    public static void printDeveloperSupportFooter(PrintWriter printWriter) throws IOException {
        printWriter.println("<hr><font size=\"-1\"><a href=\"http://www.makumba.org\">Makumba</a> developer support, version: "
                + org.makumba.MakumbaSystem.getVersion() + "</font>");
    }

    public static void writeStyles(PrintWriter w) {
        w.println("<style type=\"text/css\">");
        w.println("th {color:navy; background-color:lightblue; font-weight: normal;}");
        w.println("td.columnHead {color:navy; background-color:lightblue;}");
        w.println("tr.odd {background-color: #CCFFFF; }");
        w.println("span.active {color:lightblue; background-color: darkblue; padding: 5px; }");
        w.println("</style>");
        w.println();

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
        String result = "<span style=\"font-size: smaller;\">" + pointer.getType();
        result += " <span style=\"color: green; afont-size: x-small;\">[";
        result += "<a href=\"" + contextPath + "/dataView/" + pointer.getType() + "?ptr=" + pointer.toExternalForm()
                + "\" style=\"color: green\" title=\"Database Value: " + pointer.longValue() + "; DBSV|Unique Index: "
                + pointer.getDbsv() + "|" + pointer.getUid() + "\">" + pointer.toExternalForm() + "</a>";
        result += "]</span>";
        result += "</span>";
        return result;
    }

    public static void printPageHeader(PrintWriter writer, String title, String virtualPath, String realPath)
            throws IOException {
        writer.println("<body bgcolor=white>");
        writer.println("<table width=\"100%\" bgcolor=\"lightblue\">");
        writer.println("<tr>");
        writer.println("<td rowspan=\"2\">");

        if (title != null && !title.equals("") && !title.equals(virtualPath)) {
            writer.print("<font size=\"+2\"><font color=\"darkblue\">" + title + "</font></font>");
        } else if (virtualPath != null) {
            writer.print("<font size=\"+2\"><a href=\"" + virtualPath + "\"><font color=\"darkblue\">" + virtualPath
                    + "</font></a></font>");
        }
        if (realPath != null) {
            writer.println("<font size=\"-1\"><br>" + new File(realPath).getCanonicalPath() + "</font>");
        }
    }

}
