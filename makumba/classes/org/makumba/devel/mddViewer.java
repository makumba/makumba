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
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaError;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.commons.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.datadefinition.makumba.RecordParser;
import org.makumba.providers.datadefinition.makumba.validation.ComparisonValidationRule;

/**
 * This class implements a viewer for MDD syntax highlighting.
 */
public class mddViewer extends LineViewer {

    private int validationRuleCounter = 0;

    protected MakumbaError err = null;

    private static final String subFieldSeperator = "-&gt;";

    private DataDefinition dd = null;

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
        try {
            dd = (new DataDefinitionProvider(new Configuration())).getDataDefinition(virtualPath);
        } catch (DataDefinitionNotFoundError nf) {
            // FIXME: this is probably an include, we ignore it alltogether
        } catch (MakumbaError pe) {
            err = pe;
        }
    }

    public void footer(PrintWriter pw) throws IOException {
        if (err != null)
            pw.println("<hr><a name=\"errors\"></a><pre>" + err.getMessage() + "</pre>");
        super.footer(pw);
    }

    public String getLineTag(String s) {
        String ln = s.trim();
        int eq;
        if (!ln.startsWith("#") && !ln.startsWith("!") && !ln.startsWith("=") && (eq = ln.indexOf('=')) != -1) {
            return ln.substring(0, eq).trim();
        }
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

    public void intro(PrintWriter w) {
        if (err != null) {
            w.print("<td align=\"center\" style=\"color: red;\">errors!<br><a href=\"#errors\">details</a></td>");
        }
        String browsePath = virtualPath.replace('.', '/').substring(0, virtualPath.lastIndexOf('.') + 1);

        w.println("<td align=\"right\" valign=\"top\" style=\"padding: 5px; padding-top: 10px\">");
        w.print("<span style=\"color:lightblue; background-color: darkblue; padding: 5px;\">mdd</span>&nbsp;&nbsp;&nbsp;");

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
        if (RecordParser.isValidationRule(s)) {
            return parseValidationLine(s);
        }
        s = htmlEscape(s);
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
                        result.append(";<span class=\"mddSubFieldSeparator\">");
                    } else {
                        result.append(";<span class=\"mddComment\">");
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

    public String parseValidationLine(String s) {
        validationRuleCounter += 1;
        StringBuffer result = new StringBuffer();
        if (s.trim().startsWith("#")) {
            return "<span class=\"mddLineComment\">" + s.trim() + "</span>";
        }
        result.append("<span name=\"validationRule\" class=\"mddValidationLine\">");
        boolean endsWithComment = false;
        String ruleName = s.trim();
        StringTokenizer tokenizer = new StringTokenizer(s, " ", true);
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            ValidationDefinition vd = dd.getValidationDefinition();
            if (vd != null && vd.getRulesSyntax().contains(token.trim())) {
                result.append("<span style=\"color:blue; font-weight: bold;\">" + htmlEscape(token) + "</span>");
            } else if (token.equals(";")) {
                endsWithComment = true;
                result.append("</span> <span style=\"color:green\">" + htmlEscape(token)
                        + htmlEscape(tokenizer.nextToken("")) + "</span>");
            } else {
                if (token.trim().startsWith(ComparisonValidationRule.now)
                        || token.trim().startsWith(ComparisonValidationRule.dateFunction)) {
                    Object value = "Error retrieving value!";
                    if (ruleName != null && dd != null && vd != null) {
                        ValidationRule rule = vd.getValidationRule(ruleName);
                        if (rule != null && rule instanceof ComparisonValidationRule
                                && ((ComparisonValidationRule) rule).isCompareToExpression()) {
                            value = (((ComparisonValidationRule) rule).evaluateExpression());
                        }
                    }
                    String id = "validationRule" + validationRuleCounter;
                    result.append("<a class=\"mddDateFunction\" title=\"" + value
                            + "\" onclick=\"javascript:toggleDateFunctionDisplay(" + id + ");\">");
                    result.append(htmlEscape(token));
                    if (token.trim().startsWith(ComparisonValidationRule.dateFunction)) {
                        while ((token = tokenizer.nextToken()).indexOf(")") == -1) {
                            result.append(htmlEscape(token));
                        }
                        result.append(htmlEscape(token));
                    }
                    result.append("</a>");
                    result.append("<span id=\"" + id
                            + "\" class=\"mddDateFunctionEvaluated\" style=\"display:none;\"> [" + value + "]</span>");
                    continue;
                }

                result.append(htmlEscape(token));
            }
        }
        if (!endsWithComment) {
            result.append("</span>");
        }
        result.append("</span>");
        return super.parseLine(result.toString());
    }

    @Override
    protected void writeAdditionalLinks(PrintWriter w) {
        if (dd.getValidationDefinition().hasValidationRules()) {
            w.println("<a href=\"javascript:toggleValidtionRuleDisplay();\">Hide validation rules</a>");
        }
    }

}
