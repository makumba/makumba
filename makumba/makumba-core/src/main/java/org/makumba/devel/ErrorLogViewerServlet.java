///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2010  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: ControllerFilter.java 4398 2010-01-03 19:53:36Z manuel_gay $
//  $Name$
/////////////////////////////////////

package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.LinkedHashSet;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.db.makumba.DBConnection;
import org.makumba.providers.Configuration;
import org.makumba.providers.DeveloperTool;
import org.makumba.providers.TransactionProvider;

/**
 * This class implements a query to the Error Log table. orderBy and limit are available, filters and pagination remain
 * to be added.
 * 
 * @author Gwenael Alizon
 * @version $Id: DataQueryServlet.java 4665 2010-03-31 19:29:14Z manuel_gay $
 */
public class ErrorLogViewerServlet extends DataServlet {

    protected static final long serialVersionUID = 1L;

    public final int QUERY_LANGUAGE_OQL = 10;

    private final String ERROR_LOG_MDD = "org.makumba.controller.ErrorLog";

    private final String ERROR_LOG_MDD_LABEL = "errorLog";

    private final String FIELD1 = "ErrorLog";

    private final String FIELD1_LABEL = "ErrorLog";

    private final String FIELD2 = "executionDate";

    private final String FIELD2_LABEL = "ExecutionDate";

    private final String FIELD3 = "url";

    private final String FIELD3_LABEL = "URL";

    private final String FIELD4 = "page";

    private final String FIELD4_LABEL = "Page";

    private final String FIELD5 = "exception";

    private final String FIELD5_LABEL = "Exception";

    private final String FIELD6 = "makumbaParameters";

    private final String FIELD6_LABEL = "MakumbaParameters";

    private final String FIELD7 = "makumbaController";

    private final String FIELD7_LABEL = "MakumbaController";

    public ErrorLogViewerServlet() {
        toolLocation = Configuration.getToolLocation(DeveloperTool.ERRORLOG_VIEWER);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        browsePath = contextPath + Configuration.getToolLocation(DeveloperTool.DATA_LISTER);

        String orderByField = request.getParameter("orderByField");
        if (orderByField == null || orderByField.equals("")) {
            orderByField = FIELD2_LABEL;
        }
        String orderByOrder = request.getParameter("orderByOrder");
        if (orderByOrder == null || orderByOrder.equals("")) {
            orderByOrder = "DESC";
        }
        String orderBy = orderByField + " " + orderByOrder;

        String query = "SELECT " + ERROR_LOG_MDD_LABEL + "." + FIELD1 + " AS " + FIELD1_LABEL + ", "
                + ERROR_LOG_MDD_LABEL + "." + FIELD2 + " AS " + FIELD2_LABEL + ", " + ERROR_LOG_MDD_LABEL + "."
                + FIELD3 + " AS " + FIELD3_LABEL + ", " + ERROR_LOG_MDD_LABEL + "." + FIELD4 + " AS " + FIELD4_LABEL
                + ", " + ERROR_LOG_MDD_LABEL + "." + FIELD5 + " AS " + FIELD5_LABEL + ", " + ERROR_LOG_MDD_LABEL + "."
                + FIELD6 + " AS " + FIELD6_LABEL + ", " + ERROR_LOG_MDD_LABEL + "." + FIELD7 + " AS " + FIELD7_LABEL
                + " " + "FROM " + ERROR_LOG_MDD + " " + ERROR_LOG_MDD_LABEL + " ORDER BY " + orderBy + " WHERE 1=1";

        int limit;
        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (NumberFormatException e) {
            limit = 100;
        }

        PrintWriter writer = response.getWriter();
        DevelUtils.writePageBegin(writer);
        DevelUtils.writeStylesAndScripts(writer, contextPath);
        DevelUtils.writeTitleAndHeaderEnd(writer, "Error Log viewer");

        writePageContentHeader(null, writer, null, MODE_ERROR_LOG);

        writer.println("<form method=\"get\">");
        writer.println("<table width=\"100%\" cellpadding=\"5\">");
        writer.println("  <tr>");
        writer.println("    <th>Query</th>");
        writer.println("    <td colspan=\"4\" width=\"100%\">" + query + "</td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <th>Order by</th>");
        writer.println("    <td colspan=\"3\"><input name=\"orderByField\" type=\"text\" size=\"25\" value=\""
                + orderByField + "\"><input name=\"orderByOrder\" type=\"text\" size=\"5\" value=\"" + orderByOrder
                + "\"></td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <th>Limit</th>");
        writer.println("    <td><input name=\"limit\" type=\"text\" value=\"" + limit + "\"></td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <td colspan=\"2\"><input type=\"submit\" accesskey=\"e\" value=\"Apply orderBy, limit & filters\"></td>");
        writer.println("  <tr>");
        writer.println("</table>");
        writer.println("</form>");

        if (query != null && !query.equals("")) {
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction t = tp.getConnectionTo(tp.getDefaultDataSourceName());

            try {
                Vector<Dictionary<String, Object>> results = t.executeQuery(query, null, 0, limit);

                org.makumba.db.makumba.Query oqlQuery = ((DBConnection) t).getQuery(query);

                // we need to figure out all the projection names used in the query
                // the projection names are only present if that row also has a non-null value
                // thus, let's search over all rows, and merge the keys together

                LinkedHashSet<String> projections = new LinkedHashSet<String>();
                for (int i = 0; i < results.size(); i++) {
                    Dictionary<String, Object> d = results.get(i);
                    projections.addAll(java.util.Collections.list(d.keys()));
                }

                // now iterate over all results
                for (int i = 0; i < results.size(); i++) {
                    Dictionary<String, Object> d = results.get(i);
                    if (i == 0) {
                        writer.println("<table cellpadding=\"5\">");
                        writer.println("<tr>");
                        writer.println("<th>#</th>");
                        for (String projection : projections) {
                            writer.println("<th>" + projection + "</th>");
                        }
                        writer.println("</tr>");
                    }
                    writer.println("<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\">");
                    writer.println("<td>" + (i + 1) + "</td>");
                    for (String projection : projections) {
                        Object value = d.get(projection);
                        if (value instanceof Pointer) {
                            writer.println("<td>" + DevelUtils.writePointerValueLink(contextPath, (Pointer) value)
                                    + "</td>");
                        } else {
                            writer.println("<td>" + value + "</td>");
                        }
                    }
                    writer.println("</tr>");
                    if (i + 1 == results.size()) {
                        writer.println("</table>");
                    }
                }
                if (results.size() > 0) {
                    writer.println("<span style=\"color: red; font-size: smaller; \"><i>Note that only projections that have at least one value not null will be shown</i></span>");
                } else {
                    writer.println("<span style=\"color: red \"><i>No results found!</i></span>");
                }

            } catch (RuntimeWrappedException e) {
                writer.println("<span style=\"color: red\"><i>" + e.getMessage() + "</i></span>");
                writer.println("");
                writer.println("<div id=\"showStackTrace\" style=\"display: inline;\"><a href=\"javascript:toggleStackTrace();\" title=\"Show full stack trace\">--></a></div>");
                writer.println("<div id=\"hideStackTrace\" style=\"display: none\"><a href=\"javascript:toggleStackTrace();\" title=\"Hide stack trace\"><--</a></div>");
                writer.println("<div id=\"stackTrace\" style=\"white-space: pre; display: none; color: red; font-style: italic; font-size: smaller; margin-left: 40px; \">");
                e.printStackTrace(writer);
                writer.println("</div>");
            } catch (org.makumba.OQLParseError e) {
                writer.println("<span style=\"color: red\">Incorrect OQL query: <i>" + e.getMessage() + "</i></span>");
            } finally {
                t.close();
            }
        }
        DevelUtils.writePageEnd(writer);
    }

}