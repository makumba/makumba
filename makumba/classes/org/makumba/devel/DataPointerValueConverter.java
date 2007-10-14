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
import java.util.Collections;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.commons.Configuration;
import org.makumba.db.sql.SQLPointer;
import org.makumba.providers.TransactionProvider;

/**
 * This class provides an interface to convert Pointer values from DB values to the external form and vice-versa.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class DataPointerValueConverter extends DataServlet {

    private static final long serialVersionUID = 1L;

    public final static int FROM_DB = 10;

    public final static int FROM_EXTERNAL = 20;
    
    private Configuration config = new Configuration();
    
    private TransactionProvider tp = new TransactionProvider(config);


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
        browsePath = contextPath + "/dataList";

        String paramDataType = request.getParameter("dataType");
        String paramValue = request.getParameter("value");
        if (paramValue == null) {
            paramValue = "";
        }
        String paramFromType = request.getParameter("fromType");

        int mode = FROM_EXTERNAL;
        if (paramFromType != null && paramFromType.equals("db")) {
            mode = FROM_DB;
        }

        PrintWriter writer = response.getWriter();
        DevelUtils.writePageBegin(writer);
        DevelUtils.writeStyles(writer);
        writeScripts(writer);
        DevelUtils.writeTitleAndHeaderEnd(writer, "Value Converter");

        writePageContentHeader(type, writer, tp.getDefaultDataSourceName(), MODE_CONVERTOR);

        writer.println("<form>");
        writer.println("<table>");
        writer.println("  <tr>");
        writer.println("    <th>From</th>");
        writer.println("    <td>");
        writer.print("      Database value <input type=\"radio\" name=\"fromType\" value=\"db\""
                + (mode == FROM_DB ? " checked" : "") + ">");
        writer.println("      External form <input type=\"radio\" name=\"fromType\" value=\"external\""
                + (mode == FROM_EXTERNAL ? " checked" : "") + ">");
        writer.println("    </td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <th>Data type</th>");
        writer.println("    <td>");
        writer.println("      <select name=\"dataType\">");
        Vector v = org.makumba.MakumbaSystem.mddsInDirectory("dataDefinitions");
        Collections.sort(v);
        for (int i = 0; i < v.size(); i++) {
            String selected = (v.get(i).equals(paramDataType) ? " selected" : "");
            writer.println("        <option name=\"" + v.get(i) + "\"" + selected + ">" + v.get(i) + "</option>");
        }
        writer.println("      </select>");
        writer.println("    </td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <th>Value</th>");
        writer.println("    <td><input type=\"text\" name=\"value\" style=\"width: 100%\" value=\"" + paramValue
                + "\"></td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <td colspan=\"2\" align=\"center\"><input type=\"submit\" value=\"convert\" ></td>");
        writer.println("  </tr>");
        writer.println("</table>");
        writer.println("</form>");

        if (paramValue != null && !paramValue.equals("")) {
            Pointer pointer = null;
            if (paramFromType == null || paramFromType.equals("external")) {
                try {
                    pointer = new Pointer(paramDataType, paramValue);
                } catch (InvalidValueException e) {
                    writer.println("<span style=\"color: red;\">" + e.getMessage() + "</span>");
                }
            } else {
                try {
                    pointer = new SQLPointer(paramDataType, Long.parseLong(paramValue));
                } catch (NumberFormatException e) {
                    writer.println("<span style=\"color: red;\">The Database Pointer value given is not a number!</span>");
                    e.printStackTrace();
                }
            }
            if (pointer != null) {
                writer.println("<hr/>");
                writer.println("<table>");
                writer.println("  <tr>");
                writer.println("    <th>External Value</th>");
                writer.println("    <th>Database Value</th>");
                writer.println("    <th>DBSV:UID</th>");
                writer.println("  </tr>");
                writer.println("  <tr>");
                writer.println("    <td>" + pointer.toExternalForm() + "</td>");
                writer.println("    <td>" + pointer.longValue() + "</td>");
                writer.println("    <td>" + pointer.getDbsv() + ":" + pointer.getUid() + "</td>");
                writer.println("  </tr>");
                writer.println("</table>");
            }
        }

        DevelUtils.writePageEnd(writer);

    }

}
