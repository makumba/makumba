///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
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
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.devel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.Configuration;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.db.DBConnection;
import org.makumba.providers.TransactionProvider;

/**
 * This class implements a query interface in OQL to the database. Results are displayed and can then be shown with
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DataQueryServlet extends DataServlet {

    protected static final long serialVersionUID = 1L;

    public final int QUERY_LANGUAGE_OQL = 10;

    public final int QUERY_LANGUAGE_HQL = 20;

    private Configuration config = new Configuration();

    private TransactionProvider tp = new TransactionProvider(config);

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        browsePath = contextPath + "/dataList";
        String query = request.getParameter("query");
        if (query == null) {
            query = "";
        } else {
            query = query.trim();
        }
        int limit = 100;
        try {
            limit = Integer.parseInt(request.getParameter("limit"));
        } catch (NumberFormatException e) {
            limit = 100;
        }

        int queryLanguage = QUERY_LANGUAGE_OQL;
        if (request.getParameter("queryLanguage") != null && request.getParameter("queryLanguage").equals("hql")) {
            queryLanguage = QUERY_LANGUAGE_HQL;
        }

        PrintWriter writer = response.getWriter();
        DevelUtils.writePageBegin(writer);
        DevelUtils.writeStylesAndScripts(writer, contextPath);
        DevelUtils.writeTitleAndHeaderEnd(writer, "OQL Query Translater & executer");

        writePageContentHeader(null, writer, null, MODE_QUERY);

        writer.println("<form method=\"get\">");
        writer.println("<table width=\"100%\" cellpadding=\"5\">");
        writer.println("  <tr>");
        writer.println("    <th>Query</th>");
        writer.println("    <td colspan=\"3\" width=\"100%\"><textarea name=\"query\" style=\"width: 100%\" rows=\"2\">"
                + query + "</textarea></td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <th>Query&nbsp;Language</th>");
        writer.println("    <td>OQL <input name=\"queryLanguage\" type=\"radio\" value=\"oql\" checked> HQL <input name=\"queryLanguage\" type=\"radio\" value=\"hql\"></td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <th>Limit</th>");
        writer.println("    <td><input name=\"limit\" type=\"text\" value=\"" + limit + "\"></td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <td colspan=\"2\"><input type=\"submit\" value=\"Translate & Execute\"></td>");
        writer.println("  <tr>");
        writer.println("</table>");
        writer.println("</form>");

        if (query != null && !query.equals("")) {
            Transaction t = tp.getConnectionTo(tp.getDefaultDataSourceName());

            try {
                Vector results = t.executeQuery(query, null, 0, limit);

                if (queryLanguage == QUERY_LANGUAGE_OQL) {
                    org.makumba.db.Query oqlQuery = ((DBConnection)t).getQuery(query);
                    if (oqlQuery instanceof org.makumba.db.sql.Query) {
                        writer.println("<hr>");
                        org.makumba.db.sql.Query sqlQuery = (org.makumba.db.sql.Query) ((DBConnection)t).getQuery(query);
                        writer.println("SQL query: " + sqlQuery.getCommand() + ";<br>");
                        writer.println("<hr>");
                    }
                } else {
                    // TODO: hibernate querys still need to be implemented, a way to get the actual SQL is not yet clear
                    // SessionFactory hibernateSessionFactory = (SessionFactory) org.makumba.db.Database.findDatabase(
                    // MakumbaSystem.getDefaultDatabaseName()).getHibernateSessionFactory();
                    // Session session = hibernateSessionFactory.openSession();
                    // session.setCacheMode(CacheMode.IGNORE);
                    // org.hibernate.Transaction transaction = session.beginTransaction();
                    // Query q = session.createQuery(query);
                }

                ArrayList keys = new ArrayList();
                for (int i = 0; i < results.size(); i++) {
                    Dictionary d = (Dictionary) results.get(i);
                    if (i == 0) {
                        writer.println("<table cellpadding=\"5\">");
                        writer.println("<tr>");
                        Enumeration e = d.keys();
                        while (e.hasMoreElements()) {
                            keys.add(e.nextElement());
                        }
                        writer.println("<th>#</th>");
                        for (int j = 0; j < keys.size(); j++) {
                            writer.println("<th>" + keys.get(j) + "</th>");
                        }
                        writer.println("</tr>");
                    }
                    writer.println("<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\">");
                    writer.println("<td>" + (i + 1) + "</td>");
                    for (int j = 0; j < keys.size(); j++) {
                        Object value = d.get(keys.get(j));
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
            } catch (RuntimeWrappedException e) {
                writer.println("<span style=\"color: red\"><i>" + e.getMessage() + "</i></span>");
                writer.println("");
                writer.println("<div id=\"showStackTrace\" style=\"display: inline;\"><a href=\"javascript:toggleStackTrace();\" title=\"Show full stack trace\">--></a></div>");
                writer.println("<div id=\"hideStackTrace\" style=\"display: none\"><a href=\"javascript:toggleStackTrace();\" title=\"Hide stack trace\"><--</a></div>");
                writer.println("<div id=\"stackTrace\" style=\"display: none; color: red; font-style: italic; font-size: smaller; margin-left: 40px; \">");
                StackTraceElement[] traces = e.getStackTrace();
                for (int i = 0; i < traces.length; i++) {
                    writer.println("at " + traces[i] + "<br/>");
                }
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
