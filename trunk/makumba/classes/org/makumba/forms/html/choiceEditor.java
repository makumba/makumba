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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.makumba.HtmlChoiceWriter;
import org.makumba.Pointer;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.forms.tags.SearchFieldTag;

public abstract class choiceEditor extends FieldEditor {

    static String[] _params = { "default", "empty", "type", "size", "labelSeparator", "elementSeparator", "nullOption",
            "forceInputStyle" };

    static String[][] _paramValues = { null, null, { "hidden", "radio", "checkbox", "tickbox" }, null, null, null,
            null, SearchFieldTag.allowedSelectTypes };

    protected String nullOption = null;

    public String[] getAcceptedParams() {
        return _params;
    }

    public String[][] getAcceptedValue() {
        return _paramValues;
    }

    /** Get the available options. */
    public abstract Object getOptions(RecordFormatter rf, int fieldIndex, Dictionary formatParams);

    /** Gets the number of available options. */
    public abstract int getOptionsLength(RecordFormatter rf, int fieldIndex, Object opts);

    /** Gets the value of option 'i'. */
    public abstract Object getOptionValue(RecordFormatter rf, int fieldIndex, Object options, int i);

    /** Gets the title/label of option 'i'. */
    public abstract String formatOptionTitle(RecordFormatter rf, int fieldIndex, Object options, int i);

    /** Formats an option value, in the sequence of options. */
    public abstract String formatOptionValue(RecordFormatter rf, int fieldIndex, Object opts, int i, Object val);

    /** Formats an option value. */
    public abstract String formatOptionValue(RecordFormatter rf, int fieldIndex, Object val);

    /** Returns blank string, or " multiple " if multiple selections possible. */
    // FIXME, would be better with "boolean isMultiple()"
    public abstract String getMultiple(RecordFormatter rf, int fieldIndex);

    public abstract boolean isMultiple(RecordFormatter rf, int fieldIndex);

    /** Gets the default size of the select HTML box. */
    public abstract int getDefaultSize(RecordFormatter rf, int fieldIndex);

    /** Null values are ignored */
    public boolean shouldRemoveNullValue(RecordFormatter rf, int fieldIndex) {
        return true;
    }

    // height? orderBy? where?
    public String format(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
        String type = (String) formatParams.get("type");
        boolean hidden = "hidden".equals(type);
        boolean yn_radio = "radio".equals(type);
        boolean yn_checkbox = "checkbox".equals(type);
        boolean yn_tickbox = "tickbox".equals(type);
        String forceInputStyle = (String) formatParams.get("forceInputStyle");
        formatParams.remove("forceInputStyle");

        // check whether the enum Editor should have a null value option.
        // doing this from here seems a bit dirty, but the formatParams are not available in the subclass.
        if (this instanceof choiceEditor) {
            ((choiceEditor) this).setNullOption(formatParams.get("nullOption"));
        }

        if (yn_tickbox) {
            if (isMultiple(rf, fieldIndex))
                yn_checkbox = true;
            else
                yn_radio = true;
        }

        Vector value;
        o = getValueOrDefault(rf, fieldIndex, o, formatParams);
        if (o instanceof Vector) {
            value = (Vector) o;
        } else {
            value = new Vector(1);
            if (o != null)
                value.addElement(o);
        }

        // we clean up null values
        for (Iterator i = value.iterator(); i.hasNext();)
            if (i.next() == Pointer.Null) {
                if (shouldRemoveNullValue(rf, fieldIndex))
                    i.remove();
            }
        if (!hidden) {
            HtmlChoiceWriter hcw = new HtmlChoiceWriter(getInputName(rf, fieldIndex, formatParams));

            boolean forceSingleSelect = StringUtils.equals(forceInputStyle, "single");
            boolean forceMultipleSelect = StringUtils.equals(forceInputStyle, "multiple");

            int size = getIntParam(rf, fieldIndex, formatParams, "size");

            if (size == -1) {
                if (forceSingleSelect) { // if we force a set to be single select, use 1 as size
                    size = 1;
                } else {
                    size = getDefaultSize(rf, fieldIndex);
                }
            }
            hcw.setSize(size);
            hcw.setMultiple(!forceSingleSelect && (isMultiple(rf, fieldIndex) || forceMultipleSelect));
            hcw.setLiteralHtml(getExtraFormatting(rf, fieldIndex, formatParams));

            Object opt = getOptions(rf, fieldIndex, formatParams);
            List values = new ArrayList(getOptionsLength(rf, fieldIndex, opt));
            List labels = new ArrayList(getOptionsLength(rf, fieldIndex, opt));
            String[] valueFormattedList = new String[value.size()];

            for (int i = 0; i < getOptionsLength(rf, fieldIndex, opt); i++) {
                Object val = getOptionValue(rf, fieldIndex, opt, i);

                values.add(val == null ? null : formatOptionValue(rf, fieldIndex, opt, i, val));
                labels.add(formatOptionTitle(rf, fieldIndex, opt, i));
                // System.out.println(formatOptionTitle(opt,
                // i)+"="+formatOptionValue(opt, i, val));
            }
            hcw.setValues(values);
            hcw.setLabels(labels);

            try { // set deprecated values if data type supports it
                Vector dv = rf.dd.getFieldDefinition(fieldIndex).getDeprecatedValues();
                // System.out.println("setting deprecated:"+dv);
                if (dv != null && !dv.isEmpty()) {
                    String[] dvs = new String[dv.size()];
                    for (int i = 0; i < dv.size(); i++) {
                        dvs[i] = (String) dv.elementAt(i).toString();
                    }
                    hcw.setDeprecatedValues(dvs);
                }
            } catch (ClassCastException cce) {
            }

            for (int i = 0; i < value.size(); i++) {
                valueFormattedList[i] = formatOptionValue(rf, fieldIndex, value.get(i));
            }
            hcw.setSelectedValues(valueFormattedList);

            if (yn_radio || yn_checkbox) {
                String sep = (String) formatParams.get("elementSeparator");
                if (sep != null)
                    hcw.setOptionSeparator(sep);
                sep = (String) formatParams.get("labelSeparator");
                if (sep != null)
                    hcw.setTickLabelSeparator(sep);

                if (yn_radio)
                    return hcw.getRadioSelect();
                else
                    return hcw.getCheckboxSelect();
            }

            return hcw.getSelect();

        } else { // hidden

            StringBuffer sb = new StringBuffer();
            for (Enumeration f = value.elements(); f.hasMoreElements();) {
                Object val = f.nextElement();
                sb.append("<input type=\"hidden\" name=\"").append(getInputName(rf, fieldIndex, formatParams)).append(
                    "\" value=\"").append(formatOptionValue(rf, fieldIndex, val)).append("\">");
            }
            return sb.toString();
        }
    }

    /**
     * Return value if not null, or finds the default option and returns it as a Vector.
     */
    public Object getValueOrDefault(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
        if (o == null || (o instanceof Vector && ((Vector) o).size() == 0)) {
            String nullReplacer = (String) formatParams.get("default");
            if (nullReplacer != null) {
                Vector v = new Vector();
                v.add(nullReplacer);
                return v;
            }
        }
        return o;
    }

    /** Sets the value of the null option from the mak:input tag. */
    public void setNullOption(Object nullOption) {
        if (nullOption instanceof String && ((String) nullOption).trim().length() > 0) {
            this.nullOption = (String) nullOption;
        } else { // if we got no / an empty value, we need to empty the fields due to the re-use of the editors
            this.nullOption = null;
        }
    }

}
