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
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.Database;
import org.makumba.db.makumba.sql.SQLDBConnection;
import org.makumba.providers.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * /** This class shows a single object from the DB.<br>
 * TODO: Values of sets are not yet displayed.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DataObjectViewerServlet extends DataServlet {

    private static final long serialVersionUID = 1L;

    public DataObjectViewerServlet() {
        toolLocation = Configuration.getDataViewerLocation();
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        browsePath = contextPath + Configuration.getDataListerLocation() + "/" + browsePath;

        PrintWriter writer = response.getWriter();
        DevelUtils.writePageBegin(writer);
        DevelUtils.writeStylesAndScripts(writer, contextPath);
        DevelUtils.writeTitleAndHeaderEnd(writer, "Data Viewer");

        DataDefinition dd = null;

        dataPointer = new Pointer(type, request.getParameter("ptr"));

        try {
            dd = (DataDefinitionProvider.getInstance()).getDataDefinition(virtualPath);
        } catch (Throwable e) {
        }
        if (dd == null) {

        } else {
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction t = tp.getConnectionTo(tp.getDefaultDataSourceName());

            try {
                if (t instanceof DBConnectionWrapper) {
                    t = ((DBConnectionWrapper) t).getWrapped();
                }
                SQLDBConnection sqlConnection = (SQLDBConnection) t;
                Database hostDatabase = sqlConnection.getHostDatabase();

                String dataBaseName = t.getName();
                writePageContentHeader(type, writer, dataBaseName, MODE_LIST);
                writer.println("<br/>");

                Vector<FieldDefinition>[] allFields = DataServlet.extractFields(dd, false);
                Vector<FieldDefinition> fields = allFields[0];

                String OQL = "";
                for (int i = 0; i < fields.size(); i++) {
                    FieldDefinition fd = fields.get(i);
                    if (!fd.isSetType() && !fd.isIndexPointerField()) {
                        if (OQL.trim().length() > 0) {
                            OQL += ", ";
                        }
                        // use hostDatabase.getFieldNameInSource() to avoid problems when the field name is a reserved
                        OQL += "o." + fd.getName() + " AS " + hostDatabase.getFieldNameInSource(dd, fd.getName());
                    }
                }
                OQL = "SELECT " + OQL + " FROM " + type + " o WHERE o=$1";
                Vector<Dictionary<String, Object>> v = t.executeQuery(OQL, dataPointer);
                if (v.size() != 1) {
                    writer.println("<span style=\"color: red;\">Problem executing query:</span><br>");
                    writer.println(OQL + "<br><br>");
                    writer.println("<span style=\"color: red;\">==&gt; found " + v.size() + " results!</span>");
                } else {
                    Dictionary<String, Object> values = v.firstElement();

                    writer.println("<table>");
                    writer.println("  <tr>");
                    writer.println("    <th>Field</th>");
                    writer.println("    <th>Value</th>");
                    writer.println("  </tr>");
                    for (int i = 0; i < fields.size(); i++) {
                        FieldDefinition fd = fields.get(i);
                        if (fd.isIndexPointerField()) {
                            continue;
                        }
                        writer.println("  <tr>");
                        writer.print("    <td class=\"columnHead\">" + fd.getName());
                        if (fd.isDefaultField()) {
                            writer.print("<br/><span style=\"color:grey;font-style:italic;font-size:smaller\">(default field)</span>");
                        } else if (fd.isSetType() || fd.isPointer()) {
                            writer.print("<br/><span style=\"color:grey;font-style:italic;font-size:smaller\">("
                                    + (fd.isSetType() ? "set " : "ptr ") + fd.getPointedType().getName() + ")</span>");
                        }
                        writer.println("</td>");

                        writer.print("    <td>");
                        if (fd.isSetType()) { // special handling for external set types - query their values
                            String oql = "SELECT setEntry as setEntry, setEntry."
                                    + fd.getPointedType().getTitleFieldName() + " as setTitle FROM " + dd.getName()
                                    + " o, o." + fd.getName() + " setEntry WHERE o=$1";
                            Vector<Dictionary<String, Object>> vSet = t.executeQuery(oql, dataPointer);
                            for (int j = 0; j < vSet.size(); j++) {
                                Dictionary<String, Object> dictionary = vSet.elementAt(j);
                                final String setTitle = dictionary.get("setTitle").toString();
                                if (fd.getIntegerType() == FieldDefinition._setIntEnum) {
                                    writer.print(" " + setTitle + " <i>(="
                                            + fd.getNameFor((Integer.parseInt(setTitle))) + ")</i>");
                                } else {
                                    writer.print(" "
                                            + DevelUtils.writePointerValueLink(contextPath,
                                                (Pointer) dictionary.get("setEntry"), setTitle, false) + " ");
                                }
                            }
                            if (vSet.size() == 0) {
                                writer.print("<span style=\"color:grey;font-style:italic;font-size:smaller\">(empty)</span>");
                            }
                        } else {
                            // use hostDatabase.getFieldNameInSource() to avoid problems when the field name is a
                            // reserved
                            Object value = values.get(hostDatabase.getFieldNameInSource(dd, fd.getName()));
                            if (value instanceof Pointer) {
                                writer.print(" "
                                        + DevelUtils.writePointerValueLink(contextPath, (Pointer) value, null, false)
                                        + " ");
                            } else {
                                writer.print(value);
                                // for intEnums, also write their textual values
                                if (fd.getIntegerType() == FieldDefinition._intEnum && value != null) {
                                    writer.print(" <i>(=" + fd.getNameFor((Integer.parseInt(String.valueOf(value))))
                                            + ")</i>");
                                }
                            }
                        }
                        writer.println("</td>");

                        writer.println("  </tr>");
                    }
                    writer.println("</table>");
                }
            } finally {
                t.close();
            }
        }
        DevelUtils.writePageEnd(writer);
    }

}
