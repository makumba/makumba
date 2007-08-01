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
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.Transaction;

/**
 * /** This class shows a single object from the DB.<br>
 * TODO: Values of sets are not yet displayed.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DataObjectViewerServlet extends DataServlet {

    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        browsePath = contextPath + "/dataList/" + browsePath;

        PrintWriter writer = response.getWriter();
        writePageBegin(writer);
        writeStyles(writer);
        writeScripts(writer);
        writeHeaderEnd(writer, "Data Viewer");

        DataDefinition dd = null;

        dataPointer = new Pointer(type, request.getParameter("ptr"));

        try {
            dd = MakumbaSystem.getDataDefinition(virtualPath);
        } catch (Throwable e) {
        }
        if (dd == null) {

        } else {
            Transaction t = MakumbaSystem.getConnectionTo(MakumbaSystem.getDefaultDatabaseName());

            try {
                String dataBaseName = t.getName();
                writePageContentHeader(type, writer, dataBaseName, MODE_LIST);
                writer.println("<br/>");

                Vector[] allFields = CodeGenerator.extractFields(dd);
                Vector fields = allFields[0];

                String OQL = "SELECT ";
                for (int i = 0; i < fields.size(); i++) {
                    FieldDefinition fd = (FieldDefinition) fields.get(i);
                    if (fd.getIntegerType() != FieldDefinition._set) {
                        OQL += "o." + fd.getName() + " AS " + fd.getName();
                    } else {
                        OQL += "\"<i>&lt;SET&gt;</i>\" AS " + fd.getName();
                    }
                    if (i + 1 < fields.size()) {
                        OQL += ", ";
                    }
                }
                OQL += " FROM " + type + " o WHERE o=$1";
                Vector v = t.executeQuery(OQL, dataPointer);
                if (v.size() != 1) {
                    writer.println("<span style=\"color: red;\">Problem executing query:</span><br>");
                    writer.println(OQL + "<br><br>");
                    writer.println("<span style=\"color: red;\">==&gt; found " + v.size() + " results!</span>");
                } else {
                    Dictionary values = (Dictionary) v.firstElement();

                    writer.println("<table>");
                    writer.println("  <tr>");
                    writer.println("    <th>Field</th>");
                    writer.println("    <th>Value</th>");
                    writer.println("  </tr>");
                    for (int i = 0; i < fields.size(); i++) {
                        FieldDefinition fd = (FieldDefinition) fields.get(i);
                        writer.println("  <tr>");
                        writer.println("    <td class=\"columnHead\">" + fd.getName() + "</td>");
                        Object value = values.get(fd.getName());
                        if (value instanceof Pointer) {
                            writer.println("    <td>" + writePointerValueLink((Pointer) value) + "</td>");
                        } else {
                            writer.println("    <td>" + value + "</td>");
                        }
                        writer.println("  </tr>");
                    }
                    writer.println("</table>");
                }
            } finally {
                t.close();
            }
        }
        writePageEnd(writer);
    }

}
