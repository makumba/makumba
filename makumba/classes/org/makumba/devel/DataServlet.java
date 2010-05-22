package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Vector;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.providers.Configuration;

/**
 * This class provides basic functionality for data viewing and querying servlets.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public abstract class DataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public static final int MODE_LIST = 10;

    public static final int MODE_QUERY = 30;

    public static final int MODE_ERROR_LOG = 50;

    public static final int MODE_VIEW = 20;

    public static final int MODE_CONVERTOR = 40;

    protected String browsePath;

    protected String contextPath;

    protected Pointer dataPointer;

    protected String type;

    protected String virtualPath;

    static final Logger logger = java.util.logging.Logger.getLogger("org.makumba.devel.codeGenerator");

    protected String toolLocation = null;

    public DataServlet() {
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        contextPath = request.getContextPath();
        virtualPath = DevelUtils.getVirtualPath(request, toolLocation);
        if (virtualPath == null) {
            virtualPath = "/";
        }

        // URL-decode the type, to preserver a potential "->" in the type name (indicating a setComplex/ptrOne)
        type = URLDecoder.decode(virtualPath, System.getProperty("file.encoding"));
        if (type.startsWith("/")) {
            type = type.substring(1);
        }
        browsePath = type.replace('.', '/').substring(0, type.lastIndexOf('.') + 1);
    }

    protected void writePageContentHeader(String type, PrintWriter w, String dataBaseName, int mode) {
        w.println("<body bgcolor=\"white\">");
        w.println("<table width=\"100%\" bgcolor=\"lightblue\">");
        w.println("  <tr>");
        w.println("    <td>");
        String toolKey = null;
        if (mode == MODE_VIEW || mode == MODE_LIST) {
            if (type != null && !type.equals("")) {
                w.println("      <a href=\"" + contextPath + Configuration.getMddViewerLocation() + "/" + type
                        + "\"><span style=\"font-size: x-large\"><span style=\"color: darkblue;\">" + type
                        + "</span></a> data</span>");
            } else {
                w.println("      <span style=\"font-size: large; color: darkblue;\">Browse to select type for data listing</span>");
            }
            if (dataPointer != null) {
                w.println(" <i>for Pointer " + dataPointer.toExternalForm()
                        + " (<span title=\"DBSV:UID\" style=\"border-bottom:thin dotted;\">" + dataPointer
                        + "</span> | <span title=\"Database value\" style=\"border-bottom:thin dotted;\">"
                        + dataPointer.longValue() + "</span>)</i>");
            }
            w.println("<br>in Makumba database: " + dataBaseName);
            toolKey = Configuration.KEY_DATA_LISTER;
        } else if (mode == MODE_QUERY) {
            w.println("      <span style=\"font-size: x-large\">Query translater & executer</span><br>");
            w.println("      <span style=\"font-size: small\">Insert your query in OQl here, and get the created SQL and the results of the query.</span>");
            toolKey = Configuration.KEY_DATA_QUERY_TOOL;
        } else if (mode == MODE_ERROR_LOG) {
            w.println("      <span style=\"font-size: x-large\">Error log viewer</span><br>");
            w.println("      <span style=\"font-size: small\">List of Makumba errors</span>");
            toolKey = Configuration.KEY_ERRORLOG_VIEWER;
        } else if (mode == MODE_CONVERTOR) {
            w.println("      <span style=\"font-size: x-large\">Makumba Pointer value convertor</span>");
            w.println("<br>in Makumba database: " + dataBaseName);
            toolKey = Configuration.KEY_OBJECT_ID_CONVERTER;
        }
        w.println("    </td>");
        w.println("    <td align=\"right\" valign=\"top\" style=\"padding: 5px; padding-top: 10px\">");
        if (mode == MODE_CONVERTOR) {
            w.println("      <span class=\"active\">Pointer value converter</span>");
        }
        w.println("      &nbsp;&nbsp;&nbsp;");
        if (mode == MODE_LIST && !type.equals("") || mode == MODE_VIEW) {
            w.println("      <a href=\"" + browsePath + "\">browse</a>");
            w.println("      &nbsp;&nbsp;&nbsp;");
            w.println("      <span class=\"active\">data</span>");
        } else if (mode == MODE_CONVERTOR || mode == MODE_QUERY) {
            w.println("      <a href=\"" + browsePath + "\">browse</a>");
        } else {
            w.println("      <span class=\"active\">browse</span>");
        }

        w.println("&nbsp;&nbsp;&nbsp;");
        DevelUtils.writeDevelUtilLinks(w, toolKey, contextPath);

        w.println("    </td>");
        w.println("  </tr>");
        w.println("</table>");
    }

    /**
     * Extracts and separates the fields from a given DataDefinition. The second element of the returned array contains
     * all setComplex fields, the first element all other fields.
     */
    public static Vector<FieldDefinition>[] separateFieldTypes(DataDefinition dd, boolean skipDefaultFields) {
        Vector<FieldDefinition> fields = new Vector<FieldDefinition>();
        Vector<FieldDefinition> sets = new Vector<FieldDefinition>();
        // iterating over the DataDefinition, extracting normal fields and sets
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            FieldDefinition fd = dd.getFieldDefinition(i);
            DataServlet.logger.finer("DEBUG INFO: Extracting fields: field name " + fd.getName() + " of type "
                    + fd.getType());

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

    public static Vector<FieldDefinition> getAllFieldDefinitions(DataDefinition dd) {
        Vector<FieldDefinition> fields = new Vector<FieldDefinition>();
        for (int i = 0; i < dd.getFieldNames().size(); i++) {
            fields.add(dd.getFieldDefinition(i));
        }
        return fields;
    }

}