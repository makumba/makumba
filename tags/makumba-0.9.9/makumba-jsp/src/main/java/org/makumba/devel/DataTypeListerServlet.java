package org.makumba.devel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.tags.MakumbaJspConfiguration;
import org.makumba.db.makumba.DBConnection;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * This class lists data for a specific MDD, resp. displays an MDD broswer if no MDD is passed as parameter.
 * 
 * @author Stefan Baebler
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DataTypeListerServlet extends DataServlet {

    protected static final long serialVersionUID = 1L;

    public DataTypeListerServlet() {
        super(DeveloperTool.DATA_LISTER);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        PrintWriter writer = response.getWriter();

        DataDefinition dd = null;

        int limit = 100;
        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (NumberFormatException e) {
            limit = 100;
        }

        try {
            dd = DataDefinitionProvider.getInstance().getDataDefinition(virtualPath);
        } catch (Throwable e) {
        }
        if (dd == null) { // make a directory listing
            doDirectoryListing(request, response, writer);
        } else { // display data from that MDD
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction t = tp.getConnectionTo(tp.getDefaultDataSourceName());

            try {
                String dataBaseName = t.getName();
                writePageContentHeader(type, writer, dataBaseName, DeveloperTool.DATA_LISTER);
                String titleField = request.getParameter("titleField");
                if (titleField == null || titleField.trim().equals("")) {
                    titleField = dd.getTitleFieldName();
                }
                String otherField = request.getParameter("otherField");
                if (otherField == null || otherField.trim().equals("")) {
                    otherField = dd.getTitleFieldName();
                }

                writer.println("<form method=\"get\" class=\"form-horizontal\">");
                writer.println("  <input type=\"hidden\" name=\"type\" value=\"" + type + "\">");
                writer.println("  <div class=\"control-group\">");
                writer.println("    <label class=\"control-label\" for=\"titleField\">Title field</label>");
                writer.println("    <div class=\"controls\">");
                writer.println("      <select id=\"titleField\" name=\"titleField\">");
                for (int i = 3; i < dd.getFieldDefinitions().size(); i++) {
                    FieldDefinition fdAll = dd.getFieldDefinition(i);
                    String e = dd.getFieldDefinition(i).getName();
                    writer.print("          <option value=\"" + e + "\" ");
                    if (e.equals(titleField)) {
                        writer.print("selected");
                    }
                    writer.println(">" + fdAll.getName() + " (" + fdAll.getType() + ")</option>");
                }
                writer.println("      </select>");

                writer.println("    </div>");
                writer.println("  </div>");
                writer.println("  <div class=\"control-group\">");
                writer.println("    <label class=\"control-label\" for=\"otherField\">Other field</label>");
                writer.println("    <div class=\"controls\">");
                writer.println("      <select id=\"otherField\" name=\"otherField\">");
                for (int i = 3; i < dd.getFieldDefinitions().size(); i++) {
                    FieldDefinition fdAll = dd.getFieldDefinition(i);
                    String e = dd.getFieldDefinitions().get(i).getName();
                    writer.print("          <option value=\"" + e + "\" ");
                    if (e.equals(otherField)) {
                        writer.print("selected");
                    }
                    writer.println(">" + fdAll.getName() + " (" + fdAll.getType() + ")</option>");
                }
                writer.println("      </select>");

                writer.println("    </div>");
                writer.println("  </div>");
                writer.println("  <div class=\"control-group\">");
                writer.println("    <label class=\"control-label\" for=\"limit\">Limit</label>");
                writer.println("    <div class=\"controls\">");
                writer.println("      <input id=\"limit\" name=\"limit\" type=\"text\" size=\"5\" value=\"" + limit
                        + "\">");
                writer.println("    </div>");
                writer.println("  </div>");
                writer.println("  <div class=\"control-group\">");
                writer.println("    <div class=\"controls\">");
                writer.println("      <input TYPE=\"submit\" class=\"btn\" value=\"View\">");
                writer.println("    </div>");
                writer.println("  </div>");
                writer.println("</form>");

                String what = "";
                for (int i = 3; i < dd.getFieldDefinitions().size(); i++) {
                    if (i > 3) {
                        what = what + ", ";
                    }
                    what = what + "obj." + dd.getFieldDefinition(i).getName() + " AS "
                            + dd.getFieldDefinition(i).getName(); // col\"+(i+1);
                }

                String query = "SELECT obj as ptr, obj." + titleField + " as title, obj." + otherField
                        + " as other FROM " + type + " obj";
                Vector<Dictionary<String, Object>> results = t.executeQuery(query, null, 0, limit);

                writer.println("<table class=\"table table-striped table-condensed table-nonfluid\">");
                writer.println("  <thead>");
                writer.println("    <tr>");
                writer.println("      <th>#</th>");
                writer.println("      <th>" + titleField + "</th>");
                writer.println("      <th>" + otherField + "</th>");
                writer.println("      </th>");
                writer.println("    </tr>");
                writer.println("  </thead>");
                writer.println("  <tbody>");
                for (int i = 0; i < results.size(); i++) {
                    writer.println("<tr>");
                    writer.println("<td>" + (i + 1) + "</td>");
                    writer.println("<td>");
                    writer.println("<a href=\"" + contextPath
                            + MakumbaJspConfiguration.getToolLocation(DeveloperTool.OBJECT_VIEWER) + "/" + type
                            + "?ptr=" + ((Pointer) results.elementAt(i).get("ptr")).toExternalForm() + "\">");
                    Dictionary<String, Object> dictionary = results.elementAt(i);
                    Object value = dictionary.get("title");
                    if (value == null || value.equals("")) {
                        value = "<i>[none]</i>";
                    } else if (value instanceof Pointer) {
                        Pointer pointer = (Pointer) value;
                        value = DevelUtils.writePointerValueLink(contextPath, pointer);
                    }
                    writer.println(value);

                    writer.println("</a>");
                    writer.println("</td>");
                    writer.println("<td>");
                    Object otherValue = dictionary.get("other");
                    if (otherValue == null || otherValue.equals("")) {
                        otherValue = "<i>[none]</i>";
                    } else if (otherValue instanceof Pointer) {
                        Pointer pointer = (Pointer) otherValue;
                        otherValue = DevelUtils.writePointerValueLink(contextPath, pointer);
                    }
                    writer.println(otherValue);

                    writer.println("</td>");

                    writer.println("</tr>");
                }
                writer.println("  </tbody>");
                writer.println("</table>");

                org.makumba.db.makumba.Query oqlQuery = ((DBConnection) t).getQuery(query);
                if (oqlQuery instanceof org.makumba.db.makumba.sql.Query) {
                    writer.println("<hr>");
                    org.makumba.db.makumba.sql.Query sqlQuery = (org.makumba.db.makumba.sql.Query) ((DBConnection) t).getQuery(query);
                    writer.println("<p>SQL query:</p>");
                    DevelUtils.printSQLQuery(writer, sqlQuery.getCommand(new HashMap<String, Object>()) + ";");
                }

            } finally {
                t.close();
            }
        }
        DevelUtils.writePageEnd(writer);
    }

    private void doDirectoryListing(HttpServletRequest request, HttpServletResponse response, PrintWriter writer)
            throws IOException, FileNotFoundException {
        String servletPath = request.getContextPath()
                + MakumbaJspConfiguration.getToolLocation(DeveloperTool.DATA_LISTER);
        String requestURI = request.getRequestURI();
        String pathInfo = requestURI.substring(requestURI.indexOf(servletPath) + servletPath.length());
        if (DevelUtils.redirected(request, response, pathInfo)) {
            return;
        }
        // FIXME should not depend directly on MDDProvider
        java.net.URL u = DataDefinitionProvider.findDataDefinitionOrDirectory(virtualPath, "mdd");
        if (u == null) {
            u = DataDefinitionProvider.findDataDefinitionOrDirectory(virtualPath, "idd");
        }
        if (u == null) {
            throw new FileNotFoundException(virtualPath);
        }
        String realPath = u.getFile();
        File dir = new File(realPath);
        String relativeDirectory = dir.getName();
        if (dir.getAbsolutePath().indexOf("dataDefinitions/") != -1) { // MDD viewer
            relativeDirectory = dir.getAbsolutePath().substring(dir.getAbsolutePath().indexOf("dataDefinitions"));
        }

        writePageContentHeader(type, writer, TransactionProvider.getInstance().getDefaultDataSourceName(),
            DeveloperTool.DATA_LISTER);

        writer.print("<pre>");
        if (!relativeDirectory.equals("dataDefinitions")) {
            writer.println("<b><a href=\"../\"><i class=\"icon-arrow-left\"></i>../</a></b> (up one level)");
        }
        // process and display directories
        SourceViewControllerHandler.processDirectory(writer, dir, "dd");

        // process and display files
        String[] list = dir.list();
        Arrays.sort(list);
        for (String s : list) {
            if (s.indexOf(".") != -1 && s.endsWith("dd")) {
                String ddname = pathInfo + s;
                ddname = ddname.substring(1, ddname.lastIndexOf(".")).replace('/', '.');
                String addr = contextPath + MakumbaJspConfiguration.getToolLocation(DeveloperTool.DATA_LISTER) + "/"
                        + ddname;
                writer.println("<b><a href=\"" + addr + "\"><i class=\"icon-file\"></i>" + s + "</a></b>");
            }
        }
        writer.print("</pre>");
    }
}
