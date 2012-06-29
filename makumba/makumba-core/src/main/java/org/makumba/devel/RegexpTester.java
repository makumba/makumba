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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionParseError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaSystem;
import org.makumba.ValidationDefinition;
import org.makumba.ValidationRule;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.DeveloperTool;
import org.makumba.providers.TransactionProvider;
import org.makumba.providers.datadefinition.mdd.validation.RegExpValidationRule;

/**
 * This class provides an interface to convert Pointer values from DB values to the external form and vice-versa.
 * 
 * @author Rudolf Mayer
 * @version $Id$
 */
public class RegexpTester extends DataServlet {

    private static final long serialVersionUID = 1L;

    public RegexpTester() {
        super(DeveloperTool.REGEXP_TESTER);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);

        boolean fromMdd = StringUtils.equals(request.getParameter("fromType"), "mdd");

        String paramDataType = request.getParameter("dataType");
        String paramRegex = request.getParameter("regex");
        String paramTestValues = request.getParameter("testValues");

        PrintWriter writer = response.getWriter();
        DevelUtils.writePageBegin(writer);
        DevelUtils.writeStylesAndScripts(writer, contextPath,
            MakumbaSystem.getClientsideValidationProvider().getNeededJavaScriptFileNames());
        DevelUtils.writeTitleAndHeaderEnd(writer, DeveloperTool.REGEXP_TESTER.getName());

        LinkedHashMap<FieldDefinition, String> fieldsWithRegexp = new LinkedHashMap<FieldDefinition, String>();
        ArrayList<DataDefinitionParseError> errors = new ArrayList<DataDefinitionParseError>();
        Vector<String> v = DataDefinitionProvider.getInstance().getDataDefinitionsInDefaultLocations("test.brokenMdds");
        Collections.sort(v);

        for (int i = 0; i < v.size(); i++) {
            try {
                DataDefinition mdd = DataDefinitionProvider.getMDD(v.get(i));
                ValidationDefinition validationDefinition = DataDefinitionProvider.getInstance().getValidationDefinition(
                    v.get(i));
                for (FieldDefinition fd : mdd.getFieldDefinitions()) {
                    Collection<ValidationRule> validationRules = validationDefinition.getValidationRules(fd.getName());
                    for (ValidationRule validationRule : validationRules) {
                        if (validationRule instanceof RegExpValidationRule) {
                            String expression = ((RegExpValidationRule) validationRule).getExpression();
                            fieldsWithRegexp.put(mdd.getFieldDefinition(fd.getName()), expression);
                        }
                    }
                }
            } catch (DataDefinitionParseError e) {
                errors.add(e);
            }
        }

        writePageContentHeader(type, writer, TransactionProvider.getInstance().getDefaultDataSourceName(),
            DeveloperTool.REGEXP_TESTER);

        writer.println("<form>");
        writer.println("<table>");
        writer.println("  <tr>");
        writer.println("    <th> <input type=\"radio\" name=\"fromType\" value=\"manual\""
                + (fromMdd ? "" : " checked") + "> Regular expression</th>");
        writer.println("    <td>");
        writer.println("      <input name=\"regex\" value=\"" + (fromMdd || paramRegex == null ? "" : paramRegex)
                + "\">&nbsp;");
        writer.println("    </td>");
        writer.println("  </tr>");

        writer.println("  <tr>");
        writer.println("    <th> <input type=\"radio\" name=\"fromType\" value=\"mdd\"" + (fromMdd ? " checked" : "")
                + "> MDD</th>");
        writer.println("    <td>");

        // print MDD fields
        writer.println("      <select name=\"dataType\">");
        Set<FieldDefinition> fds = fieldsWithRegexp.keySet();
        for (FieldDefinition fieldDefinition : fds) {
            String s = fieldDefinition.getDataDefinition().getName() + "#" + fieldDefinition.getName();
            String selected = s.equals(paramDataType) ? " selected" : "";
            writer.println("        <option value=\"" + s + "\"" + selected + ">" + s + " &nbsp; &nbsp; &nbsp; "
                    + fieldsWithRegexp.get(fieldDefinition) + " </option>");
        }
        writer.println("      </select>");

        if (errors.size() > 0) {
            writer.println("      <div style=\"color: red; font-size: smaller;\"> Encountered the following parsing errors:");
            writer.println("        <div style=\"color: gray; font-size: x-small;\">");
            for (DataDefinitionParseError e : errors) {
                writer.println("        " + e.getMessage() + "<br/>");
            }
            writer.println("        </div>");
            writer.println("      </div>");
        }
        writer.println("    </td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <th>Tets values</th>");
        writer.println("    <td>");
        writer.println("      <textarea name=\"testValues\" style=\"width: 100%\" rows=\"4\">"
                + (StringUtils.isNotBlank(paramTestValues) ? paramTestValues : "") + "</textarea>");
        writer.println("    <td>");
        writer.println("  </tr>");
        writer.println("  <tr>");
        writer.println("    <td colspan=\"2\" align=\"center\">");
        writer.println("      <input type=\"submit\" value=\"Test\" >");
        writer.println("    </td>");
        writer.println("  </tr>");
        writer.println("</table>");
        writer.println("</form>");

        String regex = null;
        if (fromMdd) {
            String[] split = paramDataType.split("#");
            DataDefinition mdd = DataDefinitionProvider.getMDD(split[0]);
            FieldDefinition fd = mdd.getFieldDefinition(split[1]);
            regex = fieldsWithRegexp.get(fd);
        } else {
            regex = paramRegex;
        }

        if (StringUtils.isNotBlank(regex)) {
            writer.println("<hr/>");

            Pattern p = null;
            writer.println("<div>");
            writer.println("  Regular expression " + regex);
            try {
                p = Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                writer.println("  <div style=\"color: red;\"> &nbsP; Error: " + e.getMessage() + "</div>");
            }
            writer.println("</div>");

            if (p != null && StringUtils.isNotBlank(paramTestValues)) {
                String[] testLines = paramTestValues.split(System.getProperty("line.separator"));

                // server-side testing
                writer.println("<div style=\"width: 50%; float:left;\">");
                writer.println("  <h2> Server-side Evaluation </h2>");
                writer.println("  <table>");
                writer.println("    <tr>");
                writer.println("      <th>Test</th>");
                writer.println("      <th>Match?</th>");
                writer.println("    </tr>");
                for (String testValue : testLines) {
                    testValue = testValue.replace("\r", "");
                    Matcher matcher = p.matcher(testValue);
                    writer.println("    <tr>");
                    writer.println("      <td> " + testValue + " </td> <td style=\"color: "
                            + (matcher.matches() ? "green" : "red") + ";\"> " + matcher.matches() + " </td>");
                    writer.println("    </tr>");
                }
                writer.println("  </table>");
                writer.println("</div>");

                // server-side testing
                writer.println("<div style=\"width: 50%; float:right;\">");
                writer.println("  <h2> Client-side Evaluation with "
                        + MakumbaSystem.getClientsideValidationProvider().getClass().getSimpleName() + " </h2>");
                writer.println("  <table>");
                writer.println("    <tr>");
                writer.println("      <th>Test</th>");
                writer.println("      <th>Match?</th>");
                writer.println("    </tr>");
                for (int i = 0; i < testLines.length; i++) {
                    String testValue = testLines[i].replace("\r", "");
                    String elementId = "testValue_" + i;

                    writer.println("    <tr>");
                    writer.println("      <td> " + testValue + " </td>");
                    writer.println("      <td>");
                    writer.println("        <div id=\"" + elementId
                            + "\" style=\"color: red\"> Not evaluated (check Javascript console for errors) </div>");
                    writer.println("        <script type=\"text/javascript\">");
                    // FIXME: the way below to evaluate the regexp is currently specific to the LiveValidationProvider
                    // A fix would be an additional method in ClientsideValidationProvider that provides that code
                    writer.println("          var pattern = "
                            + MakumbaSystem.getClientsideValidationProvider().formatRegularExpression(regex) + ";");
                    writer.println("          document.getElementById('" + elementId + "').innerHTML = pattern.test('"
                            + testValue + "');");
                    writer.println("          if (pattern.test('" + testValue + "')) {");
                    writer.println("            document.getElementById('" + elementId + "').style.color='green';");
                    writer.println("          }");
                    writer.println("        </script>");
                    writer.println("      </td>");
                    writer.println("    </tr>");
                }
                writer.println("  </table>");
                writer.println("</div>");

                // writer.println("<div style=\"clear: both;\"> &nbsp; </div>");

            }
        }

        DevelUtils.writePageEnd(writer);

    }
}
