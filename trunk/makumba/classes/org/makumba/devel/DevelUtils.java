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
        w.println("  th {color:navy; background-color:lightblue; font-weight: normal;}");
        w.println("  td.columnHead {color:navy; background-color:lightblue;}");
        w.println("  tr.odd {background-color: #CCFFFF; }");
        w.println("  span.active {color:lightblue; background-color: darkblue; padding: 5px; }");
        w.println("</style>");
        w.println();

    }
    public static void writeViewerStyles(PrintWriter w) {
        w.println("<style type=\"text/css\">");
        w.println("  .mddSubFieldSeparator {color:red; }");
        w.println("  .mddComment {color:green; }");
        w.println("  .mddValidationLine {background: Aquamarine; }");
        w.println("  .mddLineComment {color:gray; }");
        w.println("  .mddDateFunctionEvaluated {color: grey; font-style:italic; display:none; }");
        w.println("  .mddDateFunction {color: navy; text-decoration:underline; }");
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

    public static void writeScripts(PrintWriter w) {
        w.println("<script language=\"javascript\">");
        w.println("<!--");
        w.println("  // toggles stack trace visibility on and off");
        w.println("  function toggleStackTrace() {");
        w.println("    if (document.getElementById('stackTrace').style.display == 'none') {");
        w.println("      document.getElementById('stackTrace').style.display = \"block\";");
        w.println("      document.getElementById('hideStackTrace').style.display = \"inline\";");
        w.println("      document.getElementById('showStackTrace').style.display = \"none\";");
        w.println("    } else {");
        w.println("      document.getElementById('stackTrace').style.display = \"none\";");
        w.println("      document.getElementById('hideStackTrace').style.display = \"none\";");
        w.println("      document.getElementById('showStackTrace').style.display = \"inline\";");
        w.println("    }");
        w.println("  }");
        w.println("  // toggles reference SQL details visibility on and off");
        w.println("  function toggleSQLDisplay(element, link) {");
        w.println("    if (element.style.display == \"none\") {");
        w.println("      element.style.display = \"block\";");
        w.println("      link.innerHTML=\"[-]\";");
        w.println("    } else {");
        w.println("      element.style.display = \"none\";");
        w.println("      link.innerHTML=\"[+]\";");
        w.println("    }");
        w.println("  }");
        w.println("  // toggles date function evaluation visibility on and off");
        w.println("  function toggleDateFunctionDisplay(element) {");
        w.println("    if (element.style.display == \"none\") {");
        w.println("      element.style.display = \"inline\";");
        w.println("    } else {");
        w.println("      element.style.display = \"none\";");
        w.println("    }");
        w.println("  }");
        w.println("  // toggles date function evaluation visibility on and off");
        w.println("  function toggleValidtionRuleDisplay() {");
        w.println("    var elements = document.getElementsByName('validationRule');");
        w.println("    for (i=0; i<elements.length; i++) {");
        w.println("      if (elements[i].style.display == \"none\") {");
        w.println("        elements[i].style.display = \"inline\";");
        w.println("      } else {");
        w.println("        elements[i].style.display = \"none\";");
        w.println("      }");
        w.println("    }");
        w.println("  }");
        w.println("  // -->");
        w.println("</script>");
        w.println();
    }

}
