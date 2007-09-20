// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
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

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.providers.datadefinition.makumba.RecordParser;

/**
 * This class implements a viewer for MDD syntax highlighting.
 */
public class mddViewer extends DefinitionViewer {

    public mddViewer(HttpServletRequest req, HttpServlet sv) throws Exception {
        super(true, req, sv);
        java.net.URL u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "mdd");
        if (u == null)
            u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "idd");
        readFromURL(u);
        virtualPath = virtualPath.substring(1);
    }

    public void intro(PrintWriter w) {
        DataDefinition dd = null;
        try {
            dd = MakumbaSystem.getDataDefinition(virtualPath);
        } catch (DataDefinitionNotFoundError nf) {
            // FIXME: this is probably an include, we ignore it alltogether
        } catch (MakumbaError pe) {
            err = pe;
            w.print("<td align=\"center\" style=\"color: red;\">errors!<br><a href=\"#errors\">details</a></td>");
        }
        String browsePath = virtualPath.replace('.', '/').substring(0, virtualPath.lastIndexOf('.') + 1);

        w.println("<td align=\"right\" valign=\"top\" style=\"padding: 5px; padding-top: 10px\">");
        w.print("<span style=\"color:lightblue; background-color: darkblue; padding: 5px;\">mdd</span>&nbsp;&nbsp;&nbsp;");

        // link to validation definition, if existing
        if (RecordParser.findValidationDefinition(virtualPath) != null) {
            w.print("<a style=\"color: darkblue;\" href=\"" + (contextPath + "/validationDefinitions/" + virtualPath)
                    + "\">validation definition</a>&nbsp;&nbsp;&nbsp;");
        }

        // link to code generator
        if (dd != null) {
            w.print("<a style=\"color: darkblue;\" href=\"" + (contextPath + "/codeGenerator/" + virtualPath)
                    + "\">code generator</a>&nbsp;&nbsp;&nbsp;");
        } else {
            w.print("<span style=\"color:gray;\" title=\"Fix the errors in the MDD first!\">code generator</span>&nbsp;&nbsp;&nbsp;");
        }
        w.print("<a style=\"color: darkblue;\" href=\"" + browsePath + "\">browse</a>&nbsp;&nbsp;&nbsp;");
        w.println("</td>");
    }

    public String parseLine(String s) {
        StringBuffer result = new StringBuffer();
        String closeLine = "";
        int current = 0;
        String subFieldSeperator = "-&gt;";
        while (current < s.length()) {
            switch (s.charAt(current)) {
                case '=':
                    result.append("<span style=\"color:black\">=</span><span style=\"color:#0000AA\">");
                    closeLine = "</span>" + closeLine;
                    break;
                case '#':
                    result.append("<span style=\"background:#eeeeee; color:#777777\">#");
                    closeLine = "</span>" + closeLine;
                    break;
                case ';':
                    // check whether we have a simple ';' or have '->' (which gets translated to -gt;)
                    String substring = s.substring(current - subFieldSeperator.length() + 1, current + 1);
                    if (current > subFieldSeperator.length() && substring.equals(subFieldSeperator)) {
                        result.append(";<span style=\"color:red\">");
                    } else {
                        result.append(";<span style=\"color:green\">");
                    }
                    closeLine = "</span>" + closeLine;
                    break;
                default:
                    result.append(s.charAt(current));
            }
            current++;
        }
        return super.parseLine(result.toString() + closeLine);
    }

}
