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
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.db.makumba.DBConnection;
import org.makumba.providers.DeveloperTool;
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

    public DataQueryServlet() {
        super(DeveloperTool.DATA_QUERY);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
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

        writePageContentHeader(null, writer, null, DeveloperTool.DATA_QUERY);

        writer.println("<form method=\"get\" class=\"form-horizontal\">");
        writer.println("  <div class=\"control-group\">");
        writer.println("    <label class=\"control-label\" for=\"query\">Query</label>");
        writer.println("    <div class=\"controls\">");
        writer.println("      <textarea id=\"query\" name=\"query\" style=\"width: 100%\" rows=\"2\">" + query
                + "</textarea>");
        writer.println("    </div>");
        writer.println("  </div>");
        writer.println("  <div class=\"control-group\">");
        writer.println("    <label class=\"control-label\">Query Language</label>");
        writer.println("    <div class=\"controls\">");
        writer.println("      <label class=\"radio inline\"><input name=\"queryLanguage\" type=\"radio\" value=\"oql\" checked>OQL</label>");
        writer.println("      <label class=\"radio inline\"><input name=\"queryLanguage\" type=\"radio\" value=\"hql\">HQL</label>");
        writer.println("    </div>");
        writer.println("  </div>");
        writer.println("  <div class=\"control-group\">");
        writer.println("    <label class=\"control-label\" for=\"limit\">Limit</label>");
        writer.println("    <div class=\"controls\">");
        writer.println("      <input id=\"limit\" name=\"limit\" type=\"text\" value=\"" + limit + "\"></td>");
        writer.println("    </div>");
        writer.println("  </div>");
        writer.println("  <div class=\"control-group\">");
        writer.println("    <div class=\"controls\">");
        writer.println("      <input type=\"submit\" class=\"btn\" accesskey=\"e\" value=\"Translate & (E)xecute\">");
        writer.println("    </div>");
        writer.println("  </div>");
        writer.println("</form>");

        if (query != null && !query.equals("")) {
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction t = tp.getConnectionTo(tp.getDefaultDataSourceName());

            try {
                Vector<Dictionary<String, Object>> results = t.executeQuery(query, null, 0, limit);

                if (queryLanguage == QUERY_LANGUAGE_OQL) {
                    org.makumba.db.makumba.Query oqlQuery = ((DBConnection) t).getQuery(query);
                    if (oqlQuery instanceof org.makumba.db.makumba.sql.Query) {
                        writer.println("<hr>");
                        org.makumba.db.makumba.sql.Query sqlQuery = (org.makumba.db.makumba.sql.Query) ((DBConnection) t).getQuery(query);
                        writer.println("<p><strong>SQL Query:</strong></p>");
                        DevelUtils.printSQLQuery(writer, sqlQuery.getCommand(new HashMap<String, Object>()) + ";");
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

                // we need to figure out all the projection names used in the query
                // the projection names are only present if that row also has a non-null value
                // thus, let's search over all rows, and merge the keys together

                Set<String> projections = new LinkedHashSet<String>();
                for (int i = 0; i < results.size(); i++) {
                    Dictionary<String, Object> d = results.get(i);
                    projections.addAll(java.util.Collections.list(d.keys()));
                }

                // now iterate over all results
                for (int i = 0; i < results.size(); i++) {
                    Dictionary<String, Object> d = results.get(i);
                    if (i == 0) {
                        writer.println("<table class=\"table table-striped table-condensed table-nonfluid\">");
                        writer.println("  <thead>");
                        writer.println("    <tr>");
                        writer.println("      <th>#</th>");
                        for (String projection : projections) {
                            writer.println("      <th class=\"text-center\">" + projection + "</th>");
                        }
                        writer.println("    </tr>");
                        writer.println("  </thead>");
                        writer.println("  <tbody>");
                    }
                    writer.println("    <tr>");
                    writer.println("      <td>" + (i + 1) + "</td>");
                    for (String projection : projections) {
                        Object value = d.get(projection);
                        if (value instanceof Pointer) {
                            writer.println("      <td>"
                                    + DevelUtils.writePointerValueLink(contextPath, (Pointer) value) + "</td>");
                        } else {
                            writer.println("      <td>" + value + "</td>");
                        }
                    }
                    writer.println("</tr>");
                    if (i + 1 == results.size()) {
                        writer.println("  </tbody>");
                        writer.println("</table>");
                    }
                }
                if (results.size() > 0) {
                    writer.println("<div class=\"alert alert-error\">Note that only projections that have at least one value not null will be shown</div>");
                } else {
                    writer.println("<div class=\"alert alert-error\">No results found!</div>");
                }

            } catch (RuntimeWrappedException e) {
                writer.println("<div class=\"alert alert-error\">" + e.getMessage() + "</div>");
                writer.println("<div id=\"showStackTrace\" style=\"display: inline;\"><a href=\"javascript:toggleStackTrace();\" title=\"Show full stack trace\">--></a></div>");
                writer.println("<div id=\"hideStackTrace\" style=\"display: none\"><a href=\"javascript:toggleStackTrace();\" title=\"Hide stack trace\"><--</a></div>");
                writer.println("<pre id=\"stackTrace\" style=\"display:none\">");
                e.printStackTrace(writer);
                writer.println("</pre>");
            } catch (org.makumba.OQLParseError e) {
                writer.println("<div class=\"alert alert-error\"><strong>Incorrect OQL query:</strong> "
                        + e.getMessage() + "</div>");
            } finally {
                t.close();
            }
        }
        DevelUtils.writePageEnd(writer);
    }
}
