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
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.ArrayUtils;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.commons.RegExpUtils;
import org.makumba.commons.tags.MakumbaJspConfiguration;
import org.makumba.controller.Logic;
import org.makumba.providers.DataDefinitionProvider;

/**
 * This class implements a viewer for MDD syntax highlighting.<br>
 * FIXME the syntax of validation rules and function definitions needs to be adapted
 */
public class mddViewer extends LineViewer {

    private int validationRuleCounter = 0;

    private static final String subFieldSeperator = "-&gt;";

    private DataDefinition dd = null;

    private DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    public mddViewer(HttpServletRequest req) throws Exception {
        super(true, req);
        setSearchLevels(false, false, false, true);
        viewerName = "MDD Viewer";
        contextPath = req.getContextPath();
        virtualPath = DevelUtils.getVirtualPath(req, MakumbaJspConfiguration.getToolLocation(DeveloperTool.MDD_VIEWER));
        java.net.URL u = DataDefinitionProvider.findDataDefinitionOrDirectory(virtualPath, "mdd");
        if (u == null) {
            u = DataDefinitionProvider.findDataDefinitionOrDirectory(virtualPath, "idd");
        }
        readFromURL(u);
        virtualPath = virtualPath.substring(1);

        try {
            dd = ddp.getDataDefinition(virtualPath);
        } catch (DataDefinitionNotFoundError nf) {
            // FIXME: this is probably an include, we ignore it alltogether
        } catch (MakumbaError pe) {
            parseError = pe;
        }
    }

    @Override
    public String getLineTag(String s) {
        String ln = s.trim();
        int eq;
        if (!ln.startsWith("#") && !ln.startsWith("!") && !ln.startsWith("=") && (eq = ln.indexOf('=')) != -1) {
            return ln.substring(0, eq).trim();
        }
        return null;
    }

    @Override
    public void printLine(PrintWriter w, String s, String toPrint) throws IOException {
        if (parseError != null) {
            // we go thru the error text, if we find this particular line, we display its error message
            // this is a hack, it should rather go thru the multiple exceptions
            LineNumberReader lr = new LineNumberReader(new StringReader(parseError.getMessage()));
            String e = null;
            String before = null;
            while (true) {
                before = e;
                e = lr.readLine();
                if (e == null) {
                    break;
                }
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

    @Override
    public void navigation(PrintWriter w) {
        String browsePath = virtualPath.replace('.', '/').substring(0, virtualPath.lastIndexOf('.') + 1);

        printFileRelations(w);

        if (dd != null) {
            w.println("<li>");
            DevelUtils.printPopoverLink(w, "BL methods", "BL Method signatures for " + dd.getName(), "blMethods");
            w.println("</li>");
            w.println("<div id=\"blMethods\" style=\"display: none;\">");
            writeBLHandlers(w, dd);
            w.println("</div>");
        } else if (dir.getName().endsWith(".idd")) { // we don't have a BL for for idd's
            DevelUtils.printNavigationButton(w, "BL methods", "#", "There's no BL for .idd files!", 2);
        } else {
            DevelUtils.printNavigationButton(w, "BL methods", "#", "Fix the errors in the MDD first!", 2);
        }
        DevelUtils.printNavigationButton(w, "mdd", "#", "", 1);

        // link to code generator
        if (dd != null) {
            DevelUtils.printNavigationButton(
                w,
                "code generator",
                contextPath + MakumbaJspConfiguration.getToolLocation(DeveloperTool.CODE_GENERATOR) + "/" + virtualPath,
                "", 0);
        } else if (dir.getName().endsWith(".idd")) { // we don't have a BL for for idd's
            DevelUtils.printNavigationButton(w, "code generator", "#",
                "There's no code to be generated for .idd files!", 2);
        } else {
            DevelUtils.printNavigationButton(w, "code generator", "#", "Fix the errors in the MDD first!", 2);
        }
        DevelUtils.printNavigationButton(w, "browse", browsePath, "", 0);

        DevelUtils.writeDevelUtilLinks(w, DeveloperTool.MDD_VIEWER.getKey(), contextPath);
    }

    private void writeBLHandlers(PrintWriter w, DataDefinition dataDef) {
        w.print("<pre><code>");
        StringBuffer sb = new StringBuffer();
        String ddMethodName = Logic.upperCase(dataDef.getName());
        if (dataDef.getParentField() != null) {
            CodeGenerator.addOnAddHandler(sb, 0, ddMethodName);
        } else {
            CodeGenerator.addOnNewHandler(sb, 0, ddMethodName);
        }
        CodeGenerator.addOnEditHandler(sb, 0, ddMethodName);
        CodeGenerator.addOnDeleteHandler(sb, 0, ddMethodName);
        w.print(sb);
        w.println("</code></pre>");

        for (FieldDefinition fd : CodeGenerator.extractSetComplex(dataDef)) {
            w.println("<br/>");
            writeBLHandlers(w, fd.getPointedType());
        }
    }

    @Override
    public String parseLine(String s) {
        StringBuffer result = new StringBuffer();
        String closeLine = "";
        int current = 0;
        if (isValidationRule(s)) {
            return parseValidationLine(s);
        } else if (isFunction(s)) {
            return parseFunctionLine(s);
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

    private String parseFunctionLine(String s) {
        StringBuilder result = new StringBuilder();
        try {
            result.append("<span name=\"mddFunction\" class=\"mddFunction\">");
            int endOfFunctionDefinition = s.indexOf("}");

            // prevent special cases when the ";" is part of the function
            int commentBegin = s.indexOf(";", endOfFunctionDefinition);
            if (commentBegin == -1) {
                commentBegin = s.length();
            }
            String name = htmlEscape(s.substring(0, s.indexOf("(")));
            String params = htmlEscape(s.substring(s.indexOf("("), s.indexOf(")") + 1));
            String definition = htmlEscape(s.substring(s.indexOf("{"), endOfFunctionDefinition + 1));
            String message = htmlEscape(s.substring(endOfFunctionDefinition + 1, commentBegin));
            result.append("<span class=\"mddFunctionName\">" + name + "</span>");
            result.append("<span class=\"mddFunctionParams\">" + params + "</span>");
            result.append("<span class=\"mddFunctionDefinition\">" + definition + "</span>");
            result.append("<span class=\"mddFunctionMessage\">" + message + "</span>");
            result.append("</span>");
            if (s.indexOf(";") != -1) {
                result.append("<span class=\"mddComment\">" + htmlEscape(s.substring(commentBegin)) + "</span>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = new StringBuilder(
                    "<span name=\"mddFunction\" class=\"mddFunction\" style=\"color:red\" title=\"Errors during highlighting: ");
            result.append(e.getMessage()).append("\">");
            result.append(s);
            result.append("</span>");
        }
        return super.parseLine(result.toString());
    }

    public String parseValidationLine(String s) {
        validationRuleCounter += 1;
        if (dd == null) {
            // if the mdd wasn't parsed (due to an error), we cannot get details on the validation definition
            return "<span name=\"validationRule\" class=\"mddValidationLine\">" + s + "</span>";
        }

        return parseNewValidationLine(s);
    }

    private String parseNewValidationLine(String s) {

        StringBuffer result = new StringBuffer();
        result.append("<span name=\"validationRule\" class=\"mddValidationLine\">");
        boolean endsWithComment = false;
        // String ruleName = s.trim();
        StringTokenizer tokenizer = new StringTokenizer(s, " ", true);
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            // ValidationDefinition vd = ddp.getValidationDefinition(dd.getName());
            if (ArrayUtils.contains(basicValidationRuleOperators, token.trim())) {
                result.append("<span style=\"color:blue; font-weight: bold;\">" + htmlEscape(token) + "</span>");
            } else if (token.equals(";")) {
                endsWithComment = true;
                result.append("</span> <span style=\"color:green\">" + htmlEscape(token)
                        + htmlEscape(tokenizer.nextToken("")) + "</span>");
            } else {
                if (token.trim().startsWith(now) || token.trim().startsWith(dateFunction)) {
                    Object value = "Error retrieving value!";
                    /*
                    if (ruleName != null && dd != null && vd != null) {
                        ValidationRule rule = vd.getValidationRule(ruleName);
                        if (rule != null && rule instanceof ComparisonValidationRule
                                && ((ComparisonValidationRule) rule).isCompareToExpression()) {
                            value = ((ComparisonValidationRule) rule).evaluateExpression();
                        }
                    }
                    */
                    String id = "validationRule" + validationRuleCounter;
                    result.append("<a class=\"mddDateFunction\" title=\"" + value
                            + "\" onclick=\"javascript:toggleDateFunctionDisplay(" + id + ");\">");
                    result.append(htmlEscape(token));
                    if (token.trim().startsWith(dateFunction)) {
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
        if (dd != null && ddp.getValidationDefinition(dd.getName()) != null
                && ddp.getValidationDefinition(dd.getName()).hasValidationRules()) {
            w.println("<a href=\"javascript:toggleValidtionRuleDisplay();\">Hide validation rules</a>");
        }
        if (dd != null && ddp.getQueryFragmentFunctions(dd.getName()).getFunctions().size() > 0) {
            w.println("<a href=\"javascript:toggleFunctionDisplay();\">Hide functions</a>");
        }
    }

    @Override
    protected void printPageBeginAdditional(PrintWriter writer) {
        if (parseError != null) {
            writer.print("<div class=\"alert alert-error\"><strong>Errors!</strong><a href=\"#errors\">See details</a></div>");
        }
    }

    /**
     * Regluar expressions for syntax highlighting of validation rules and functions.<br>
     * FIXME these do not yet recognize the new syntax of VRs
     */

    public static final String now = "$now";

    public static final String today = "$today";

    public static final String dateFunction = "date(";

    private static final String[] basicValidationRuleOperators = new String[] { "matches", "range", "length", "compare" };

    public static final String VALIDATION_INDICATOR = "%";

    // regular expressions for multi-field uni
    public static final String multiUniqueRegExpElement = RegExpUtils.LineWhitespaces + "(" + RegExpUtils.fieldName
            + ")" + RegExpUtils.LineWhitespaces;

    public static final String multiUniqueRegExpElementRepeatment = "(?:" + RegExpUtils.LineWhitespaces + "," + "(?:"
            + multiUniqueRegExpElement + "))*";

    public static final String multiUniqueRegExp = RegExpUtils.LineWhitespaces + "(?:" + multiUniqueRegExpElement + ")"
            + multiUniqueRegExpElementRepeatment + RegExpUtils.LineWhitespaces;

    public static final Pattern multiUniquePattern = Pattern.compile(multiUniqueRegExp);

    public static final String validationRuleErrorMessageSeparatorChar = " : ";

    // regular expressions for validation definitions //
    public static final String validationDefinitionRegExp = RegExpUtils.LineWhitespaces + "(" + RegExpUtils.fieldName
            + ")" + RegExpUtils.LineWhitespaces + VALIDATION_INDICATOR + "(matches|length|range|compare|unique)"
            + RegExpUtils.LineWhitespaces + "=" + RegExpUtils.LineWhitespaces + "(.+)" + RegExpUtils.LineWhitespaces
            + validationRuleErrorMessageSeparatorChar + RegExpUtils.LineWhitespaces + ".+";

    public static final Pattern validationDefinitionPattern = Pattern.compile(validationDefinitionRegExp);

    // regular expressions for function definitions //
    public static final String funcDefParamTypeRegExp = "(?:char|char\\[\\]|int|real|date|intEnum|charEnum|text|binary|ptr|set|setIntEnum|setCharEnum|ptr)";

    public static final String funcDefParamValueRegExp = "(?:\\d+|" + RegExpUtils.fieldName + ")";

    /** defines "int a" or "int 5". */
    public static final String funcDefParamRegExp = funcDefParamTypeRegExp + RegExpUtils.minOneLineWhitespace
            + funcDefParamValueRegExp + "(?:" + RegExpUtils.minOneLineWhitespace + funcDefParamValueRegExp + ")?";

    /** treats (int a, char b, ...) */
    public static final String funcDefParamRepeatRegExp = "\\((" + "(?:" + funcDefParamRegExp + ")" + "(?:"
            + RegExpUtils.LineWhitespaces + "," + RegExpUtils.LineWhitespaces + funcDefParamRegExp + ")*"
            + RegExpUtils.LineWhitespaces + ")?\\)";

    /** treats function(params) { queryFragment } errorMessage. */
    public static final String funcDefRegExp = "(" + RegExpUtils.fieldName + "%)?" + "(" + RegExpUtils.fieldName + ")"
            + funcDefParamRepeatRegExp + RegExpUtils.LineWhitespaces + "\\{" + RegExpUtils.LineWhitespaces
            + "(.[^\\}]+)" + RegExpUtils.LineWhitespaces + "(?:\\}" + RegExpUtils.LineWhitespaces + "(.*))?";

    public static final Pattern funcDefPattern = Pattern.compile(funcDefRegExp);

    public static final String ruleDefRegExp = "(" + RegExpUtils.fieldName + ")" + "\\(" + RegExpUtils.LineWhitespaces
            + "(?:" + RegExpUtils.fieldName + ")" + "(?:" + RegExpUtils.LineWhitespaces + ","
            + RegExpUtils.LineWhitespaces + RegExpUtils.fieldName + ")*" + RegExpUtils.LineWhitespaces + "\\)"
            + RegExpUtils.LineWhitespaces + "\\{" + RegExpUtils.LineWhitespaces + "(.[^\\}]+)"
            + RegExpUtils.LineWhitespaces + "(?:\\}" + RegExpUtils.LineWhitespaces + "(.*))?";

    public static final Pattern ruleDefPattern = Pattern.compile(ruleDefRegExp);

    public static final String constraintDefRegExp = "(" + RegExpUtils.fieldName + ")" + "\\." + "("
            + RegExpUtils.fieldName + ")" + RegExpUtils.LineWhitespaces + "((.*))?";

    public static final Pattern constraintDefPattern = Pattern.compile(constraintDefRegExp);

    public static final Pattern ident = Pattern.compile("[a-zA-Z]\\w*");

    public static boolean isValidationRule(String s) {
        return validationDefinitionPattern.matcher(s).matches();
    }

    public static boolean isFunction(String s) {
        return funcDefPattern.matcher(s).matches();
    }

}
