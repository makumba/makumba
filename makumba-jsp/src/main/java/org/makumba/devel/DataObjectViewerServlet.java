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

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.tags.MakumbaJspConfiguration;
import org.makumba.db.makumba.DBConnectionWrapper;
import org.makumba.db.makumba.Database;
import org.makumba.db.makumba.sql.SQLDBConnection;
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
        super(DeveloperTool.OBJECT_VIEWER);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        browsePath = contextPath + MakumbaJspConfiguration.getToolLocation(DeveloperTool.DATA_LISTER) + "/"
                + browsePath;

        PrintWriter writer = response.getWriter();

        DataDefinition dd = null;

        if (request.getParameter("ptr") != null) {
            dataPointer = new Pointer(type, request.getParameter("ptr"));
        } else {
            writePageContentHeader(type, writer, null, DeveloperTool.DATA_LISTER);
            writer.println("<div class=\"alert alert-error\">No object to browse provided, use the dataLister in order to browse records</div>");
            DevelUtils.writePageEnd(writer);
            return;
        }

        try {
            dd = DataDefinitionProvider.getInstance().getDataDefinition(type);
        } catch (Throwable e) {
        }
        if (dd == null) {
            writePageContentHeader(type, writer, null, DeveloperTool.DATA_LISTER);
            writer.println("<div class=\"alert alert-error\">No valid type selected</div>");
            DevelUtils.writePageEnd(writer);
            return;
        } else {
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction t = tp.getConnectionTo(tp.getDefaultDataSourceName());
            Transaction tHolder = null;

            try {
                if (t instanceof DBConnectionWrapper) {
                    tHolder = t;
                    t = ((DBConnectionWrapper) t).getWrapped();
                }
                SQLDBConnection sqlConnection = (SQLDBConnection) t;
                Database hostDatabase = sqlConnection.getHostDatabase();

                writePageContentHeader(type, writer, t.getName(), DeveloperTool.DATA_LISTER);

                Vector<FieldDefinition> fields = DataServlet.getAllFieldDefinitions(dd);

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
                    DevelUtils.printErrorMessage(writer, "Problem executing query:", " found " + v.size() + " results!");
                    DevelUtils.printSQLQuery(writer, OQL);
                } else {
                    Dictionary<String, Object> values = v.firstElement();

                    writer.println("<table class=\"table table-striped table-condensed table-nonfluid\">");
                    writer.println("  <thead>");
                    writer.println("    <tr>");
                    writer.println("      <th>Field</th>");
                    writer.println("      <th>Value</th>");
                    writer.println("    </tr>");
                    writer.println("  </thead>");
                    writer.println("  <tbody>");
                    for (int i = 0; i < fields.size(); i++) {
                        FieldDefinition fd = fields.get(i);
                        if (fd.isIndexPointerField()) {
                            continue;
                        }
                        writer.println("    <tr>");
                        writer.print("    <th>" + fd.getName());
                        if (fd.isDefaultField()) {
                            writer.print("<br/><span style=\"color:grey;font-style:italic;font-size:smaller\">(default field)</span>");
                        } else if (fd.getIntegerType() == FieldDefinition._ptrOne) {
                            writer.print("<br/><span style=\"color:grey;font-style:italic;font-size:smaller\">(ptrOne)</span></td>");
                        } else if (fd.isComplexSet()) {
                            writer.print("<br/><span style=\"color:grey;font-style:italic;font-size:smaller\">(setComplex)</span></td>");
                        } else if (fd.isSetType() || fd.isPointer()) {
                            writer.print("<br/><span style=\"color:grey;font-style:italic;font-size:smaller\">("
                                    + (fd.isSetType() ? "set " : "ptr ") + fd.getPointedType().getName() + ")</span>");
                        }
                        writer.println("</th>");

                        writer.println("    <td>");

                        // special handling for external set types - query their values
                        if (fd.isSetType() || fd.getIntegerType() == FieldDefinition._ptrOne) {
                            boolean isEmpty = true;
                            String titleFieldName = fd.getPointedType().getTitleFieldName();
                            String fragmentTitleField = titleFieldName != null ? ", setEntry." + titleFieldName
                                    + " as setTitle " : "";
                            String oql = "SELECT setEntry as setEntry " + fragmentTitleField + "FROM " + dd.getName()
                                    + " o, o." + fd.getName() + " setEntry WHERE o=$1";
                            Vector<Dictionary<String, Object>> vSet = t.executeQuery(oql, dataPointer);
                            for (int j = 0; j < vSet.size(); j++) {
                                Dictionary<String, Object> dictionary = vSet.elementAt(j);
                                String setTitle = String.valueOf(dictionary.get("setTitle"));
                                if (fd.getIntegerType() == FieldDefinition._setIntEnum) {
                                    writer.print(" " + setTitle + " <i>(=" + fd.getNameFor(Integer.parseInt(setTitle))
                                            + ")</i>");
                                    isEmpty = false;
                                } else {
                                    Pointer ptrSetEntry = (Pointer) dictionary.get("setEntry");
                                    if (fd.isComplexSet() || fd.getIntegerType() == FieldDefinition._ptrOne) {

                                        if (dictionary.size() > 0) {
                                            writer.print(" "
                                                    + DevelUtils.writePointerValueLink(
                                                        contextPath,
                                                        ptrSetEntry,
                                                        StringUtils.isNotBlank(setTitle) ? setTitle
                                                                : ptrSetEntry.toString(), false) + " ");
                                            isEmpty = false;
                                        }

                                    } else {
                                        writer.print(" "
                                                + DevelUtils.writePointerValueLink(contextPath, ptrSetEntry, setTitle,
                                                    false) + " ");
                                        isEmpty = false;
                                    }
                                }
                            }
                            if (isEmpty) {
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
                                    writer.print(" <i>(=" + fd.getNameFor(Integer.parseInt(String.valueOf(value)))
                                            + ")</i>");
                                }
                            }
                        }
                        writer.println("</td>");

                        writer.println("    </tr>");
                    }
                    writer.println("  <tbody>");
                    writer.println("</table>");
                }
            } finally {
                if (t != null) {
                    t.close();
                }
                if (tHolder != null) {
                    t.close();
                }
            }
        }
        DevelUtils.writePageEnd(writer);
    }

}
