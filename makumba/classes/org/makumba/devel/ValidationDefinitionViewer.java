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
//  $Id: mddViewer.java,v 2.5 2006/09/26 15:19:09 rosso_nero Exp $
//  $Name:$
/////////////////////////////////////

package org.makumba.devel;

import java.io.PrintWriter;
import java.net.URL;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.controller.validation.ComparisonValidationRule;
import org.makumba.providers.datadefinition.makumba.RecordParser;

/**
 * This class implements a viewer for validation definition syntax highlighting.
 */
public class ValidationDefinitionViewer extends DefinitionViewer {

    private ValidationDefinition validationDefinition;

    public ValidationDefinitionViewer(HttpServletRequest req, HttpServlet sv) throws Exception {
        super(true, req, sv);
        virtualPath = virtualPath.substring(1);
        URL validationDefinition = RecordParser.findValidationDefinition(virtualPath);
        readFromURL(validationDefinition);
    }

    public void intro(PrintWriter w) {
        DataDefinition dd = null;
        try {
            dd = MakumbaSystem.getDataDefinition(virtualPath);
        } catch (DataDefinitionNotFoundError nf) {
            // FIXME: this is probably an include, we ignore it alltogether
        } catch (MakumbaError pe) {
        }
        try {
            validationDefinition = dd.getValidationDefinition();
        } catch (MakumbaError e) {
            err = e;
            w.print("<td align=\"center\" style=\"color: red;\">errors!<br><a href=\"#errors\">details</a></td>");
        }

        String browsePath = virtualPath.replace('.', '/').substring(0, virtualPath.lastIndexOf('.') + 1);
        String mddViewerPath = contextPath + "/dataDefinitions/" + virtualPath;

        w.println("<td align=\"right\" valign=\"top\" style=\"padding: 5px; padding-top: 10px\">");
        w.println("<a style=\"color: darkblue;\" href=\"" + mddViewerPath + "\">mdd</a>&nbsp;&nbsp;&nbsp;");
        w.print("<span style=\"color:lightblue; background-color: darkblue; padding: 5px;\">validation definition</span>&nbsp;&nbsp;&nbsp;");
        if (dd != null) {
            String generatorPath = contextPath + "/codeGenerator/" + virtualPath;
            w.print("<a style=\"color: darkblue;\" href=\"" + generatorPath + "\">code generator</a>&nbsp;&nbsp;&nbsp;");
        }
        w.print("<a style=\"color: darkblue;\" href=\"" + browsePath + "\">browse</a>&nbsp;&nbsp;&nbsp;");
        w.println("</td>");
    }

    public String parseLine(String s) {
        StringBuffer result = new StringBuffer();
        if (s.trim().startsWith("#")) {
            return "<span style=\"color:grey\">" + s.trim() + "</span>";
        }
        int colonCount = 0;
        boolean validLine = false;
        boolean endsWithComment = false;
        String ruleName = null;
        if (s.split(":").length >= 3) {
            validLine = true;
            result.append("<span style=\"color:teal;\">");
            ruleName = s.split(":")[0].trim();
        }
        StringTokenizer tokenizer = new StringTokenizer(s, " ", true);
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (validationDefinition.getRulesSyntax().contains(token.trim())) {
                result.append("<span style=\"color:blue; font-weight: bold;\">" + htmlEscape(token) + "</span>");
            } else if (token.equals(";")) {
                endsWithComment = true;
                result.append("</span> <span style=\"color:green\">" + htmlEscape(token)
                        + htmlEscape(tokenizer.nextToken("")) + "</span>");
            } else {
                if (token.trim().startsWith(ComparisonValidationRule.now)
                        || token.trim().startsWith(ComparisonValidationRule.dateFunction)) {
                    Object value = "Error retrieving value!";
                    if (ruleName != null && validationDefinition != null) {
                        ValidationRule rule = validationDefinition.getValidationRule(ruleName);
                        if (rule != null && rule instanceof ComparisonValidationRule
                                && ((ComparisonValidationRule) rule).isCompareToExpression()) {
                            value = (((ComparisonValidationRule) rule).evaluateExpression());
                        }
                    }
                    result.append("<a style=\"color: pink; text-decoration:underlinef; border-bottom:thin dotted;\" title=\""
                            + value + "\">");
                    result.append(htmlEscape(token));
                    if (token.trim().startsWith(ComparisonValidationRule.dateFunction)) {
                        while ((token = tokenizer.nextToken()).indexOf(")") == -1) {
                            result.append(htmlEscape(token));
                        }
                        result.append(htmlEscape(token));
                    }
                    result.append("</a>");
                    result.append(" <span style=\"color: grey; font-style:italic;\">[" + value + "]</span>");
                    continue;
                }

                if (validLine && token.trim().equals(":")) {
                    colonCount++;
                    if (colonCount > 0 && colonCount < 3) {
                        result.append("</span>");
                    }
                    if (colonCount == 1) {
                        result.append("<span style=\"color:navy;\">");
                    }
                    if (colonCount == 2) {
                        result.append("<span style=\"font-weight: bold;\">");
                    }
                }
                result.append(htmlEscape(token));
            }
        }
        if (!endsWithComment) {
            result.append("</span>");
        }
        return super.parseLine(result.toString());
    }
}
