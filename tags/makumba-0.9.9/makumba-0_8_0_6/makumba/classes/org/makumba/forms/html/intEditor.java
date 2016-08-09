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

package org.makumba.forms.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;

import org.makumba.HtmlChoiceWriter;
import org.makumba.ProgrammerError;
import org.makumba.ValidationRule;
import org.makumba.commons.StringUtils;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.providers.datadefinition.makumba.validation.NumberRangeValidationRule;

public class intEditor extends charEditor {

    private static final class SingletonHolder {
        static final FieldEditor singleton = new intEditor();
    }

    /** Don't use this, use getInstance() */
    protected intEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    static String[] __params = { "default", "empty", "size", "maxlength", "type", "stepSize" };

    static String[][] __paramValues = { null, null, null, null, new String[] { "spinner", "select", "radio" }, null };

    @Override
    public String[] getAcceptedParams() {
        return __params;
    }

    @Override
    public String[][] getAcceptedValue() {
        return __paramValues;
    }

    @Override
    public int getWidth(RecordFormatter rf, int fieldIndex) {
        return 10;
    }

    /** Formats the value to appear in an input statement. */
    @Override
    public String formatValue(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
        // note: only diff with charEditor.formatValue is not calling
        // string2html
        // maybe can just as well not redefine this method?
        String s = (o == null) ? null : o.toString();
        return resetValueFormat(rf, fieldIndex, s, formatParams);
    }

    @Override
    public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.commons.attributes.HttpParameters par,
            String suffix) {
        Object o = par.getParameter(getInputName(rf, fieldIndex, suffix));

        if (o instanceof java.util.Vector) {
            throw new InvalidValueException(rf.expr[fieldIndex], "multiple value not accepted for integer: " + o);
        }
        return toInt(rf, fieldIndex, o);
    }

    @Override
    public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
        int stepSize = 1;
        try {
            stepSize = Integer.parseInt((String) formatParams.get("stepSize"));
        } catch (NumberFormatException e) {
        }
        if (StringUtils.equals(formatParams.get("type"), "spinner")) {
            StringBuffer spinner = new StringBuffer();
            // spinnerCode.append("<div style=\"display:inline;\">\n");
            spinner.append(super.formatNotNull(rf, fieldIndex, o, formatParams)).append("\n");
            // spinnerCode.append("<div style=\"float: left;\">\n");
            // spinner.append("<div style=\"top: 0\">");
            spinner.append("<table valign=\"top\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"display: inline;\">\n<tr><td>");
            String inputValue = "document.getElementById('" + getInputName(rf, fieldIndex, formatParams) + "').value";
            spinner.append("<input type=\"button\" value=\"+\" onclick=\"" + inputValue + " = " + inputValue + "==''? "
                    + stepSize + ":parseInt(" + inputValue + ") + " + stepSize
                    + ";\" style=\"font-size:5px;margin:0;padding:0;width:15px;height:12px;\">");
            // spinner.append(" </div>\n");
            // spinner.append("<div style=\"top: 0\">");
            spinner.append("</td></tr>\n<tr><td>");
            spinner.append("<input type=\"button\" value=\"-\" onclick=\"" + inputValue + "-=" + stepSize
                    + ";\" style=\"font-size:5px;margin:0;padding:0;width:15px;height:12px;\">");
            spinner.append("</td></tr>\n    </table>");
            // spinner.append(" </div>\n");
            // spinner.append("</div>\n");
            // spinner.append("</div>\n");
            return spinner.toString();
        } else if (StringUtils.equalsAny(formatParams.get("type"), "select", "radio")) {
            Collection<ValidationRule> validationRules = rf.dd.getFieldDefinition(fieldIndex).getValidationRules();
            for (ValidationRule validationRule : validationRules) {
                if (validationRule instanceof NumberRangeValidationRule) {
                    int lower = ((NumberRangeValidationRule) validationRule).getLowerLimit().intValue();
                    int upper = ((NumberRangeValidationRule) validationRule).getUpperLimit().intValue();
                    HtmlChoiceWriter writer = new HtmlChoiceWriter(getInputName(rf, fieldIndex, formatParams));
                    ArrayList<String> values = new ArrayList<String>();
                    for (int i = lower; i <= upper; i += stepSize) {
                        values.add(String.valueOf(i));
                    }
                    if (!values.contains(String.valueOf(upper))) {
                        values.add(String.valueOf(upper));
                    }
                    writer.setValues(values);
                    writer.setLabels(values);
                    if (o != null) {
                        writer.setSelectedValues(o.toString());
                    }
                    return StringUtils.equals(formatParams.get("type"), "select") ? writer.getSelectOne()
                            : writer.getRadioSelect();
                }
            }
            throw new ProgrammerError("");
        } else {
            return super.formatNotNull(rf, fieldIndex, o, formatParams);
        }
    }

}
