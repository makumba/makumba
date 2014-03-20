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
import org.makumba.commons.tags.MakumbaJspConfiguration;

/**
 * This class provides basic functionality for data viewing and querying servlets.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public abstract class DataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected String browsePath;

    protected String contextPath;

    protected Pointer dataPointer;

    protected String type;

    protected String virtualPath;

    static final Logger logger = java.util.logging.Logger.getLogger("org.makumba.devel.codeGenerator");

    protected DeveloperTool toolType;

    protected String toolLocation = null;

    protected String toolName;

    protected String[] additionalScripts;

    public DataServlet(DeveloperTool toolType) {
        this.toolType = toolType;
        toolLocation = MakumbaJspConfiguration.getToolLocation(toolType);
        browsePath = contextPath + MakumbaJspConfiguration.getToolLocation(toolType);
        toolName = toolType.getName();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        contextPath = request.getContextPath();
        virtualPath = DevelUtils.getVirtualPath(request, toolLocation);
        if (virtualPath == null) {
            virtualPath = "/";
        }

        // URL-decode the type, to preserve a potential "->" in the type name (indicating a setComplex/ptrOne)
        type = URLDecoder.decode(virtualPath, System.getProperty("file.encoding"));
        if (type.startsWith("/")) {
            type = type.substring(1);
        }
        browsePath = type.replace('.', '/').substring(0, type.lastIndexOf('.') + 1);

        PrintWriter writer = response.getWriter();

        DevelUtils.writePageBegin(writer);
        DevelUtils.writeStylesAndScripts(writer, contextPath, additionalScripts);
        DevelUtils.writeTitleAndHeaderEnd(writer, toolName);
    }

    protected void writePageContentHeader(String type, PrintWriter w, String dataBaseName, DeveloperTool tool)
            throws IOException {
        DevelUtils.printNavigationBegin(w, toolName);

        if (tool == DeveloperTool.OBJECT_ID_CONVERTER) {
            DevelUtils.printNavigationButton(w, "Pointer value converter", "#", "", 1);
        }

        if (tool == DeveloperTool.DATA_LISTER && !type.equals("") || tool == DeveloperTool.OBJECT_VIEWER) {
            DevelUtils.printNavigationButton(w, "browse", browsePath, "", 0);
            DevelUtils.printNavigationButton(w, "data", "#", "", 1);
        } else if (tool == DeveloperTool.OBJECT_ID_CONVERTER || tool == DeveloperTool.DATA_QUERY) {

            DevelUtils.printNavigationButton(w, "browse", browsePath, "", 0);
        } else if (tool == DeveloperTool.REGEXP_TESTER) {
        } else {
            DevelUtils.printNavigationButton(w, "browse", "#", "", 1);
        }

        DevelUtils.writeDevelUtilLinks(w, tool.getKey(), contextPath);
        DevelUtils.printNavigationEnd(w);

        if (tool == DeveloperTool.OBJECT_VIEWER || tool == DeveloperTool.DATA_LISTER) {
            if (type != null && !type.equals("")) {
                w.println("<h2><a href=\"" + contextPath
                        + MakumbaJspConfiguration.getToolLocation(DeveloperTool.MDD_VIEWER) + "/" + type + "\">" + type
                        + "</a></h2>");
            } else {
                w.println("  <p class=\"lead\">Browse to select type for data listing</p>");
            }
            if (dataPointer != null) {
                w.println(" <p class=\"lead\">Showing data for Pointer <em>" + dataPointer.toExternalForm()
                        + " <small>(<abbr title=\"DBSV:UID\">" + dataPointer
                        + "</abbr> | <abbr title=\"Database value\">" + dataPointer.longValue()
                        + "</abbr>)</small></em></p>");
            }
        } else if (tool == DeveloperTool.DATA_QUERY) {
            w.println("      <p class=\"lead\">Insert your query in OQL here, and get the created SQL and the results of the query.</p>");
        } else if (tool == DeveloperTool.ERRORLOG_VIEWER) {
            w.println("      <p class=\"lead\">List of Makumba errors</p>");
        } else if (tool == DeveloperTool.OBJECT_ID_CONVERTER) {
            w.println("      <p class=\"lead\">Convert Makumba pointer value from various formats</p>");
        } else if (tool == DeveloperTool.REGEXP_TESTER) {
            w.println("      <p class=\"lead\">Test regular expression for data validation</p>");
        }

        if (tool == DeveloperTool.OBJECT_VIEWER || tool == DeveloperTool.DATA_LISTER
                || tool == DeveloperTool.OBJECT_ID_CONVERTER) {
            if (dataBaseName != null) {
                w.println("<p>Data from Makumba database: " + dataBaseName + "</p>");
            }
        }

    }

    /**
     * Extracts and separates the fields from a given DataDefinition. The second element of the returned array contains
     * all setComplex fields, the first element all other fields.
     */
    public static Vector<FieldDefinition>[] separateFieldTypes(DataDefinition dd, boolean skipDefaultFields) {
        Vector<FieldDefinition> fields = new Vector<FieldDefinition>();
        Vector<FieldDefinition> sets = new Vector<FieldDefinition>();
        // iterating over the DataDefinition, extracting normal fields and sets
        for (int i = 0; i < dd.getFieldDefinitions().size(); i++) {
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
        @SuppressWarnings("unchecked")
        Vector<FieldDefinition>[] vectors = new Vector[] { fields, sets };
        return vectors;
    }

    public static Vector<FieldDefinition> getAllFieldDefinitions(DataDefinition dd) {
        Vector<FieldDefinition> fields = new Vector<FieldDefinition>();
        for (int i = 0; i < dd.getFieldDefinitions().size(); i++) {
            fields.add(dd.getFieldDefinition(i));
        }
        return fields;
    }

}