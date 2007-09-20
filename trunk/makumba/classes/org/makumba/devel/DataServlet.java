package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;

/**
 * This class provides basic functionality for data viewing and querying servlets.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public abstract class DataServlet extends HttpServlet {

    public static final int MODE_LIST = 10;

    public static final int MODE_QUERY = 30;

    public static final int MODE_VIEW = 20;

    public static final int MODE_CONVERTOR = 40;

    protected String browsePath;

    protected String contextPath;

    protected Pointer dataPointer;

    protected String pathInfo;

    protected String type;

    protected String virtualPath;

    static final Logger logger = MakumbaSystem.getMakumbaLogger("devel.codeGenerator");

    public DataServlet() {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        contextPath = request.getContextPath();
        pathInfo = request.getPathInfo();
        virtualPath = pathInfo;
        if (virtualPath == null) {
            virtualPath = "/";
        }

        type = virtualPath;
        if (type.startsWith("/")) {
            type = type.substring(1);
        }
        browsePath = type.replace('.', '/').substring(0, type.lastIndexOf('.') + 1);
    }

    protected void writeHeaderEnd(PrintWriter w, String title) {
        w.println("<title>" + title + "</title>");
        w.println("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" >");
        w.println("</head>");
        w.println();
    }

    protected void writePageBegin(PrintWriter w) {
        w.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
        w.println("<html>");
        w.println("<head>");
    }

    protected void writePageContentHeader(String type, PrintWriter w, String dataBaseName, int mode) {
        w.println("<body bgcolor=\"white\">");
        w.println("<table width=\"100%\" bgcolor=\"lightblue\">");
        w.println("  <tr>");
        w.println("    <td>");
        if (mode == MODE_VIEW || mode == MODE_LIST) {
            if (type != null && !type.equals("")) {
                w.println("      <a href=\"" + contextPath + "/dataDefinitions/" + type
                        + "\"><span style=\"font-size: x-large\"><span style=\"color: darkblue;\">" + type
                        + "</span></a> data</span>");
            } else {
                w.println("      <span style=\"font-size: large; color: darkblue;\">Browse to select type for data listing</span>");
            }
            if (dataPointer != null) {
                w.println(" <i>for Pointer " + dataPointer.toExternalForm()  + " (<span title=\"DBSV:UID\" style=\"border-bottom:thin dotted;\">" + dataPointer
                        + "</span> | <span title=\"Database value\" style=\"border-bottom:thin dotted;\">" + dataPointer.longValue() + "</span>)</i>");
            }
            w.println("<br>in Makumba database: " + dataBaseName);
        } else if (mode == MODE_QUERY) {
            w.println("      <span style=\"font-size: x-large\">Query translater & executer</span><br>");
            w.println("      <span style=\"font-size: small\">Insert your query in OQl here, and get the created SQL and the results of the query.</span>");
        } else if (mode == MODE_CONVERTOR) {
            w.println("      <span style=\"font-size: x-large\">Makumba Pointer value convertor</span>");
            w.println("<br>in Makumba database: " + dataBaseName);
        }
        w.println("    </td>");
        w.println("    <td align=\"right\" valign=\"top\" style=\"padding: 5px; padding-top: 10px\">");
        if (mode == MODE_CONVERTOR) {
            w.println("      <span class=\"active\">Pointer value converter</span>");
        } else {
            w.println("      <a href=\"" + contextPath + "/valueConverter\">Pointer value converter</a>");
        }
        w.println("      &nbsp;&nbsp;&nbsp;");
        if (mode == MODE_QUERY) {
            w.println("      <span class=\"active\">custom query</span>");
        } else {
            w.println("      <a href=\"" + contextPath + "/dataQuery\">custom query</a>");
        }
        w.println("      &nbsp;&nbsp;&nbsp;");
        if ((mode == MODE_LIST && !type.equals("")) || mode == MODE_VIEW) {
            w.println("      <a href=\"" + browsePath + "\">browse</a>");
            w.println("      &nbsp;&nbsp;&nbsp;");
            w.println("      <span class=\"active\">data</span>");
        } else if (mode == MODE_CONVERTOR || mode == MODE_QUERY) {
            w.println("      <a href=\"" + browsePath + "\">browse</a>");
        } else {
            w.println("      <span class=\"active\">browse</span>");
        }
        w.println("    </td>");
        w.println("  </tr>");
        w.println("</table>");
    }

    protected void writePageEnd(PrintWriter w) throws IOException {
        DevelUtils.printDeveloperSupportFooter(w);
        w.println("</body>");
        w.println("</html>");
    }

    protected String writePointerValueLink(Pointer pointer) {
        String result = "<span style=\"font-size: smaller;\">" + pointer.getType();
        result += " <span style=\"color: green; afont-size: x-small;\">[";
        result += "<a href=\"" + contextPath + "/dataView/" + pointer.getType() + "?ptr=" + pointer.toExternalForm()
                + "\" style=\"color: green\" title=\"Database Value: " + pointer.longValue() + "; DBSV|Unique Index: "
                + pointer.getDbsv() + "|" + pointer.getUid() + "\">" + pointer.toExternalForm() + "</a>";
        result += "]</span>";
        result += "</span>";
        return result;
    }

    protected void writeScripts(PrintWriter w) {
        w.println("<script language=\"javascript\">");
        w.println("<!--");
        w.println("  // toggles applicants list visibility on and off");
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
        w.println("  // -->");
        w.println("</script>");
        w.println();
    }

    protected void writeStyles(PrintWriter w) {
        w.println("<style type=\"text/css\">");
        w.println("th {color:navy; background-color:lightblue; font-weight: normal;}");
        w.println("td.columnHead {color:navy; background-color:lightblue;}");
        w.println("tr.odd {background-color: #CCFFFF; }");
        w.println("span.active {color:lightblue; background-color: darkblue; padding: 5px; }");
        w.println("</style>");
        w.println();

    }

    /** Extracts the fields and sets from a given DataDefinition. */
    public static Vector[] extractFields(DataDefinition dd, boolean skipDefaultFields) {
        Vector<FieldDefinition> fields = new Vector<FieldDefinition>();
        Vector<FieldDefinition> sets = new Vector<FieldDefinition>();
        // iterating over the DataDefinition, extracting normal fields and sets
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldDefinition fd = dd.getFieldDefinition(i);
            DataServlet.logger.finer("DEBUG INFO: Extracting fields: field name " + fd.getName() + " of type " + fd.getType());
    
            if (!skipDefaultFields || !fd.isDefaultField()) { // we skip default fields and index
                if (fd.shouldEditBySingleInput()) {
                    fields.add(fd);
                } else {
                    sets.add(fd);
                }
            }
        }
        return new Vector[] { fields, sets };
    }

}