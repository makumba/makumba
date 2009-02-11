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

package org.makumba.commons;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

/**
 * This servlet figures out a list of possible values given a beginning string on a field of a given type. Results are
 * returned as described at http://github.com/madrobby/scriptaculous/wikis/ajax-autocompleter TODO: this should work
 * with the currently used query language TODO: adapt for ptr: add an if which checks the fieldType, then, do the
 * queries necessary to get the title (as in the ptrEditor) and generate the right result (read scriptaculous doc)
 * 
 * @author Manuel Gay
 * @version $Id$
 */
public class AutoCompleteServlet extends HttpServlet {
    public static final String resourceDirectory = "makumbaResources";

    private static final long serialVersionUID = 1L;

    public static final String RESOURCE_PATH_JAVASCRIPT = "javaScript/";

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");

        // get the writer
        PrintWriter writer = resp.getWriter();

        String value = req.getParameter("value");
        String typeName = req.getParameter("type");
        String fieldName = req.getParameter("field");
        String fieldType = req.getParameter("fieldType");
        String queryLang = req.getParameter("queryLang");

        if (StringUtils.isBlank(typeName) || StringUtils.isBlank(fieldName)) {
            writer.println("{error: \"All 'type' and 'field' parameters need to be not-empty!\"}");
            return;
        }

        DataDefinition dd;
        QueryProvider qp = null;
        try {
            qp = QueryProvider.makeQueryRunner(TransactionProvider.getInstance().getDefaultDataSourceName(), queryLang);

            // check if the table exists
            try {
                dd = DataDefinitionProvider.getInstance().getDataDefinition(typeName);
                if (dd == null) {
                    writer.println("{error: \"No such table!\"}");
                    return;
                }
            } catch (Throwable e) {
                writer.println("{error: \"No such table!\"}");
                return;
            }

            // check if the field exists
            FieldDefinition fd = dd.getFieldDefinition(fieldName);
            if (fd == null) {
                writer.println("{error: \"No such field!\"}");
                return;
            }

            if (fieldType.equals("char")) {

                String query = "select p." + fieldName + " as possibility from " + typeName + " p where p." + fieldName
                        + " like '" + value + "%' group by p." + fieldName;

                Vector<Dictionary<String, Object>> v = new Vector<Dictionary<String, Object>>();
                v = qp.execute(query, null, 0, -1);

                if (v.size() > 0) {
                    String result = "<ul>";
                    for (Iterator<Dictionary<String, Object>> iterator = v.iterator(); iterator.hasNext();) {
                        Dictionary<String, Object> dictionary = iterator.next();
                        String possibility = (String) dictionary.get("possibility");
                        result += "<li>" + possibility + "</li>";
                    }
                    result += "</ul>";

                    writer.print(result);

                } else {
                    // we return an empty list
                    writer.print("<ul></ul>");
                }

            } else if (fieldType.equals("ptr")) {

                // compose queries

                Map<String, String> m = new HashMap<String, String>();

                String titleField = dd.getFieldDefinition(fieldName).getTitleField();
                String titleExpr = "choice." + titleField;

                String choiceType = dd.getFieldDefinition(fieldName).getPointedType().getName();

                m.put("oql", "SELECT choice as choice, " + titleExpr + " as title FROM " + choiceType + " choice "
                        + "WHERE " + titleExpr + " like '%" + value + "%' " + "ORDER BY title");
                FieldDefinition titleFieldDef = dd.getFieldDefinition(fieldName).getPointedType().getFieldOrPointedFieldDefinition(
                    titleField);
                if (titleFieldDef != null && titleFieldDef.getType().equals("ptr")) { // null if we have functions for
                                                                                      // title fields
                    titleExpr += ".id";
                }
                m.put("hql", "SELECT choice.id as choice, " + titleExpr + " as title FROM " + choiceType + " choice "
                        + "WHERE " + titleExpr + " like '%" + value + "%' " + "ORDER BY " + titleExpr);

                Vector<Dictionary<String, Object>> v = new Vector<Dictionary<String, Object>>();
                v = qp.execute(m.get(queryLang), null, 0, -1);

                if (v.size() > 0) {
                    String result = "<ul>";
                    for (Iterator<Dictionary<String, Object>> iterator = v.iterator(); iterator.hasNext();) {
                        Dictionary<String, Object> dictionary = iterator.next();
                        result += "<li id=\"" + ((Pointer) dictionary.get("choice")).toExternalForm() + "\">"
                                + (String) dictionary.get("title") + "</li>";
                    }
                    result += "</ul>";

                    writer.print(result);

                } else {
                    // we return an empty list
                    writer.print("<ul></ul>");
                }
            }
        } finally {
            if (qp != null)
                qp.close();
        }

        writer.flush();
        writer.close();
    }

}
