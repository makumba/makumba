package org.makumba.devel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.datadefinition.makumba.RecordParser;

/**
 * This class lists data for a specific MDD, resp. displays an MDD broswer if no MDD is passed as parameter.
 * 
 * @author Stefan Baebler
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DataTypeListerServlet extends DataServlet {

    protected static final long serialVersionUID = 1L;

    private Configuration config = new Configuration();

    private TransactionProvider tp = new TransactionProvider(config);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        PrintWriter writer = response.getWriter();
        DevelUtils.writePageBegin(writer);
        DevelUtils.writeStyles(writer);
        writeScripts(writer);
        DevelUtils.writeTitleAndHeaderEnd(writer, "Data Lister");

        DataDefinition dd = null;

        int limit = 100;
        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (NumberFormatException e) {
            limit = 100;
        }

        try {
            dd = (new DataDefinitionProvider(new Configuration())).getDataDefinition(virtualPath);
        } catch (Throwable e) {
        }
        if (dd == null) { // make a directory listing
            doDirectoryListing(request, response, writer);
        } else { // display data from that MDD
            Transaction t = tp.getConnectionTo(tp.getDefaultDataSourceName());

            try {
                String dataBaseName = t.getName();
                writePageContentHeader(type, writer, dataBaseName, MODE_LIST);
                Vector fields = dd.getFieldNames();
                String titleField = request.getParameter("titleField");
                if (titleField == null || titleField.trim().equals("")) {
                    titleField = dd.getTitleFieldName();
                }
                String otherField = request.getParameter("otherField");
                if (otherField == null || otherField.trim().equals("")) {
                    otherField = dd.getTitleFieldName();
                }

                writer.println("<table border=\"0\" cellpadding=\"5\">");
                writer.println("  <tr class=\"even\" valign=\"bottom\">");
                writer.println("    <th align=\"center\"><b>#</b></th>");
                writer.println("    <form method=\"get\">");
                writer.println("    <input type=\"hidden\" name=\"type\" value=\"" + type + "\">");

                writer.println("    <th>");
                writer.println("      <select size=\"1\" name=\"titleField\">");
                for (int i = 3; i < fields.size(); i++) {
                    FieldDefinition fdAll = dd.getFieldDefinition(i);
                    String e = (String) fields.elementAt(i);
                    writer.print("          <option value=\"" + e + "\" ");
                    if (e.equals(titleField)) {
                        writer.print("selected");
                    }
                    writer.println(">" + fdAll.getName() + " (" + fdAll.getType() + ")</option>");
                }
                writer.println("        </select>");

                writer.println("    </th>");
                writer.println("    <th>");
                writer.println("      <select size=\"1\" name=\"otherField\">");
                for (int i = 3; i < fields.size(); i++) {
                    FieldDefinition fdAll = dd.getFieldDefinition(i);
                    String e = (String) fields.elementAt(i);
                    writer.print("          <option value=\"" + e + "\" ");
                    if (e.equals(otherField)) {
                        writer.print("selected");
                    }
                    writer.println(">" + fdAll.getName() + " (" + fdAll.getType() + ")</option>");
                }
                writer.println("      </select>");

                writer.println("    </th>");
                writer.println("    <th>Limit</th>");
                writer.println("    <td>");
                writer.println("      <input name=\"limit\" type=\"text\" size=\"5\" value=\"" + limit + "\">");
                writer.println("      <input TYPE=\"submit\" value=\"View\">");
                writer.println("    </td>");
                writer.println("    </form>");
                writer.println("  </tr>");

                String what = "";
                for (int i = 3; i < fields.size(); i++) {
                    if (i > 3)
                        what = what + ", ";
                    what = what + "obj." + fields.elementAt(i) + " AS " + fields.elementAt(i); // col\"+(i+1);
                }

                String query = "SELECT obj as ptr, obj." + titleField + " as title" + ", obj." + otherField
                        + " as other" + " FROM " + type + " obj";
                Vector results = t.executeQuery(query, null, 0, limit);

                for (int i = 0; i < results.size(); i++) {
                    writer.println("<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\">");
                    writer.println("<td>" + (i + 1) + "</td>");
                    writer.println("<td>");
                    writer.println("<a href=\"" + contextPath + "/dataView/" + type + "?ptr="
                            + ((Pointer) ((Dictionary) results.elementAt(i)).get("ptr")).toExternalForm() + "\">");
                    Dictionary dictionary = ((Dictionary) results.elementAt(i));
                    Object value = dictionary.get("title");
                    if (value == null || value.equals("")) {
                        value = "<i>[none]</i>";
                    } else if (value instanceof Pointer) {
                        Pointer pointer = ((Pointer) value);
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
                        Pointer pointer = ((Pointer) otherValue);
                        otherValue = DevelUtils.writePointerValueLink(contextPath, pointer);
                    }
                    writer.println(otherValue);

                    writer.println("</td>");

                    writer.println("</tr>");
                }

                writer.println("</table>");

                org.makumba.db.Query oqlQuery = t.getQuery(query);
                if (oqlQuery instanceof org.makumba.db.sql.Query) {
                    writer.println("<hr>");
                    org.makumba.db.sql.Query sqlQuery = (org.makumba.db.sql.Query) t.getQuery(query);
                    writer.println("SQL query: " + sqlQuery.getCommand() + ";<br>");
                }

            } finally {
                t.close();
            }
        }
        DevelUtils.writePageEnd(writer);
    }

    private void doDirectoryListing(HttpServletRequest request, HttpServletResponse response, PrintWriter writer)
            throws IOException, FileNotFoundException {
        if (pathInfo == null) {
            pathInfo = "";
        }
        if (!pathInfo.endsWith("/")) {
            response.sendRedirect(contextPath + request.getServletPath() + pathInfo + "/");
            return;
        }
        // FIXME should not depend directly on RecordParser
        java.net.URL u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "mdd");
        if (u == null) {
            u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "idd");
        }
        if (u == null)
            throw new FileNotFoundException(virtualPath);
        String realPath = u.getFile();
        File dir = new File(realPath);
        String relativeDirectory = dir.getName();
        if (dir.getAbsolutePath().indexOf("dataDefinitions/") != -1) { // MDD viewer
            relativeDirectory = dir.getAbsolutePath().substring(dir.getAbsolutePath().indexOf("dataDefinitions"));
        }

        writePageContentHeader(type, writer, tp.getDefaultDataSourceName(), MODE_LIST);

        writer.print("<pre style=\"margin-top:0\">");
        if (!relativeDirectory.equals("dataDefinitions")) {
            writer.println("<b><a href=\"../\">../</a></b> (up one level)");
        }
        // process and display directories
        SourceViewServlet.processDirectory(writer, dir, "dd");

        // process and display files
        String[] list = dir.list();
        Arrays.sort(list);
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            if (s.indexOf(".") != -1 && s.endsWith("dd")) {
                String ddname = pathInfo + s;
                ddname = ddname.substring(1, ddname.lastIndexOf(".")).replace('/', '.');
                String addr = contextPath + "/dataList/" + ddname;
                writer.println("<a href=\"" + addr + "\">" + s + "</a>");
            }
        }
    }
}
