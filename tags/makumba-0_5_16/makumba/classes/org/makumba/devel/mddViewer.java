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

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.abstr.RecordParser;

/**
 * 
 * This class implements a viewer for MDD syntax highlighting.
 * 
 *  
 */
public class mddViewer extends LineViewer {
    private MakumbaError err;

    public mddViewer(HttpServletRequest req, HttpServlet sv) throws Exception {
        super(true, req, sv);
        setSearchLevels(false, false, false, true);
        virtualPath = req.getPathInfo();
        contextPath = req.getContextPath();
        if (virtualPath == null)
            virtualPath = "/";
        java.net.URL u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "mdd");
        if (u == null)
            u = RecordParser.findDataDefinitionOrDirectory(virtualPath, "idd");
        readFromURL(u);
        virtualPath = virtualPath.substring(1);
    }

    public void intro(PrintWriter w) {
        try {
            MakumbaSystem.getDataDefinition(virtualPath);
        } catch (DataDefinitionNotFoundError nf) {
            // FIXME: this is probably an include, we ignore it alltogether
        } catch (MakumbaError pe) {
            err = pe;
            w.print("<td align=\"center\"><font color=\"red\">errors!<br><a href=\"#errors\">details</a></font></td>");

        }
        w.print("<td align=\"center\" bgcolor=\"darkblue\"><font color=\"lightblue\">mdd</font></td>");
        w.print("<td align=\"center\"><a href=\""
                + virtualPath.replace('.', '/').substring(0, virtualPath.lastIndexOf('.') + 1)
                + "\"><font color=\"darkblue\">browse</font></a></td>");
    }

    public void footer(PrintWriter pw) throws IOException {
        if (err != null)
            pw.println("<hr><a name=\"errors\"></a><pre>" + err.getMessage() + "</pre>");
        super.footer(pw);
    }

    public String getLineTag(String s) {
        String ln = s.trim();
        int eq;
        if (!ln.startsWith("#") && !ln.startsWith("!") && !ln.startsWith("=") && (eq = ln.indexOf('=')) != -1)
            return ln.substring(0, eq).trim();
        return null;
    }

    public void printLine(PrintWriter w, String s, String toPrint) throws IOException {
        if (err != null) {
            // we go thru the error text, if we find this particular line, we display its error message
            // this is a hack, it should rather go thru the multiple exceptions
            LineNumberReader lr = new LineNumberReader(new StringReader(err.getMessage()));
            String e = null;
            String before = null;
            while (true) {
                before = e;
                e = lr.readLine();
                if (e == null)
                    break;
                if (e.length() > 0 && e.equals(s)) {
                    w.print("<span style=\"background-color: pink;\">");
                    super.printLine(w, s, e);
                    w.print("</span>\t<span style=\"color:red;\">" + lr.readLine() + " "
                            + before.substring(before.indexOf(':') + 1) + "</span>\r\n");
                    return;
                }
            }
        }
        super.printLine(w, s, toPrint);
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
                String substring = s.substring(current-subFieldSeperator.length()+1, current+1);
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

