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

import java.util.Dictionary;

import org.makumba.HtmlUtils;
import org.makumba.ProgrammerError;
import org.makumba.commons.StringUtils;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.commons.http.MakumbaServlet;
import org.makumba.commons.tags.MakumbaJspConfiguration;

public class charEditor extends FieldEditor {

    private static final class SingletonHolder {
        static FieldEditor singleton = new charEditor();
    }

    /** Don't use this, use getInstance() */
    protected charEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    // public static final charEditor singleton = new charEditor();

    static String[] _params = { "default", "empty", "type", "size", "maxlength", "autoComplete", "clearDefault" };

    static String[][] _paramValues = { null, null, { "text", "password" }, null, null,
            new String[] { "true", "false" }, new String[] { "true", "false" } };

    @Override
    public String[] getAcceptedParams() {
        return _params;
    }

    @Override
    public String[][] getAcceptedValue() {
        return _paramValues;
    }

    public int getWidth(RecordFormatter rf, int fieldIndex) {
        return rf.dd.getFieldDefinition(fieldIndex).getWidth();
    }

    public String getParams(RecordFormatter rf, int fieldIndex, Dictionary<String, Object> formatParams) {
        String ret = getIntParamString(rf, fieldIndex, formatParams, "size");
        int n = getIntParam(rf, fieldIndex, formatParams, "maxlength");
        if (n > getWidth(rf, fieldIndex)) {
            throw new InvalidValueException(rf.expr[fieldIndex], "invalid too big for maxlength " + n);
        }
        if (n == -1) {
            n = getWidth(rf, fieldIndex);
        }
        ret += "maxlength=\"" + n + "\" ";
        return ret;
    }

    /** Formats the input-field in case of null object */
    @Override
    public String formatNull(RecordFormatter rf, int fieldIndex, Dictionary<String, Object> formatParams) {
        return formatNotNull(rf, fieldIndex, null, formatParams);
    }

    /** Formats the input-field in case of not-null object */
    @Override
    public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        boolean autoComplete = formatParams.get("autoComplete") != null
                && formatParams.get("autoComplete").equals("true");
        boolean clearDefault = formatParams.get("clearDefault") != null
                && formatParams.get("clearDefault").equals("true");
        String test = getParams(rf, fieldIndex, formatParams);
        String res = "", id = "";

        res += "<input name=\""
                + getInputName(rf, fieldIndex, formatParams)
                + "\" type=\""
                + getInputType(rf, fieldIndex, formatParams)
                + "\" value=\""
                + formatValue(rf, fieldIndex, o, formatParams)
                + "\" "
                + test
                + getExtraFormatting(rf, fieldIndex, formatParams)
                + (autoComplete ? "autocomplete=\"off\"" : "")
                + (clearDefault ? "onBlur=\"if(this.value=='') this.value='"
                        + HtmlUtils.escapeQuotes(HtmlUtils.string2html(getDefaultValueFormat(rf, fieldIndex,
                            formatParams)))
                        + "';\" onFocus=\"if(this.value=='"
                        + HtmlUtils.escapeQuotes(HtmlUtils.string2html(getDefaultValueFormat(rf, fieldIndex,
                            formatParams))) + "') this.value='';\"" : "") + ">";

        // the second part of the auto-complete, i.e. the dropdown that appears
        if (autoComplete && !getInputType(rf, fieldIndex, formatParams).equals("password")) {
            // getting the id won't work for dates and the other type commented in the hack in FieldFormatter
            id = StringUtils.getParam("id", getExtraFormatting(rf, fieldIndex, formatParams));

            res += "<div id=\"autocomplete_choices_" + id + "\" class=\"autocomplete\"></div>";

            res += "<script type=\"text/javascript\">MakumbaAutoComplete.AutoComplete(\"" + id + "\", \""
                    + MakumbaJspConfiguration.getServletLocation(MakumbaServlet.AUTOCOMPLETE) + "\", \""
                    + rf.dd.getFieldDefinition(fieldIndex).getOriginalFieldDefinition().getDataDefinition().getName()
                    + "\", \"" + rf.dd.getFieldDefinition(fieldIndex).getOriginalFieldDefinition().getName()
                    + "\", \"char\", \"" + (String) formatParams.get("org.makumba.forms.queryLanguage")
                    + "\");</script>";

        } else if (autoComplete && getInputType(rf, fieldIndex, formatParams).equals("password")) {
            throw new ProgrammerError("Can't use auto-complete on an input field of type 'password'!");
        }
        return res;

    }

    /** Formats the value to appear in an input statement. */
    @Override
    public String formatValue(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        return HtmlUtils.string2html(resetValueFormat(rf, fieldIndex, o == null ? null : o.toString(), formatParams));
    }

    /*
     * Formats the value to appear in hidden input statement: don't overload
     * default behaviour set in FieldEditor.
     */
    // public String formatHiddenValue(Object o, Dictionary formatParams) {}

    // public String getLiteral(Object o, Dictionary formatParams)
    // { }
    public String getInputType(RecordFormatter rf, int fieldIndex, Dictionary<String, Object> formatParams) {
        String s = (String) formatParams.get("type");
        if (s == null) {
            s = "text";
        }
        return s;
    }
}
