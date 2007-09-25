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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.makumba.MakumbaSystem;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.commons.formatters.dateFormatter;
import org.makumba.controller.http.HttpParameters;

public class dateEditor extends FieldEditor {

    private static final class SingletonHolder {
        static final FieldEditor singleton = new dateEditor();
    }

    private dateEditor() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    static String[] _params = { "format", "calendarEditor", "calendarEditorLink" };

    static String[][] _paramValues = { null, new String[] { "true", "false" }, null };

    public String[] getAcceptedParams() {
        return _params;
    }

    public String[][] getAcceptedValue() {
        return _paramValues;
    }

    static final String recognized = "dMyHms";

    static int[] lowLimits = { 1, 0, -1, 0, 0, 0 };

    static int[] hiLimits = { 31, 11, -1, 23, 59, 59 };

    public static int[] components = { Calendar.DAY_OF_MONTH, Calendar.MONTH, Calendar.YEAR, Calendar.HOUR_OF_DAY,
            Calendar.MINUTE, Calendar.SECOND };

    static String[] componentNames = { "day", "month", "year", "hour", "minute", "second" };

    String getNullName(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
        return getNullName(rf, fieldIndex, getSuffix(rf, fieldIndex, formatParams));
    }

    String getNullName(RecordFormatter rf, int fieldIndex, String suffix) {
        return getInputName(rf, fieldIndex, suffix) + "_null";
    }

    String getComponentName(RecordFormatter rf, int fieldIndex, int i, String suffix) {
        return getInputName(rf, fieldIndex, suffix) + "_" + i;
    }

    String getComponentName(RecordFormatter rf, int fieldIndex, int i, Dictionary formatParams) {
        return getComponentName(rf, fieldIndex, i, getSuffix(rf, fieldIndex, formatParams));
    }

    public String format(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
        String format = (String) formatParams.get("format");
        if (format == null)
            format = "dd MMMMM yyyy";
        if (o == org.makumba.Pointer.NullDate)
            o = null;
        Date d = (Date) o;
        StringBuffer sb = new StringBuffer();
        boolean hidden = "hidden".equals(formatParams.get("type"));
        if (d == null) {
            d = (Date) rf.dd.getFieldDefinition(fieldIndex).getDefaultValue();
            sb.append("<input type=\"hidden\" name=\"").append(getNullName(rf, fieldIndex, formatParams)).append("\">");
        }
        int n = 0;
        while (true) {
            n = findNextFormatter(rf, fieldIndex, sb, format, n, hidden);
            if (n == -1)
                break;
            n = formatFrom(rf, fieldIndex, sb, d, format, n, hidden, formatParams);
        }

        String inputName = getInputName(rf, fieldIndex, getSuffix(rf, fieldIndex, formatParams));
        String calendarEditor = (String) formatParams.get("calendarEditor");
        if (!calendarEditor.equals("false")) {
            sb.append(MakumbaSystem.getCalendarProvider().formatEditorCode(inputName,
                (String) formatParams.get("calendarEditorLink")));
        }

        return sb.toString();
    }

    void formatComponent(RecordFormatter rf, int fieldIndex, StringBuffer sb, Date d, String fmt, int component,
            boolean hidden, Dictionary formatParams) {
        SimpleDateFormat df = new SimpleDateFormat(fmt, org.makumba.MakumbaSystem.getLocale());
        df.setCalendar(dateFormatter.calendar);

        String name = getComponentName(rf, fieldIndex, component, formatParams);

        if (hidden) {
            Calendar c = new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
            c.setTime(d);
            sb.append("<input type=\"hidden\" name=\"").append(name).append("\" id=\"").append(name).append(
                "\" value=\"").append(c.get(components[component])).append("\">");
        } else {
            String val = df.format(d);

            if (lowLimits[component] == -1) {// year
                sb.append("<input type=\"text\" name=\"").append(name).append("\" id=\"").append(name).append(
                    "\" value=\"").append(val).append("\" maxlength=\"").append(fmt.length()).append("\" size=\"").append(
                    fmt.length()).append("\"").append(getExtraFormatting(rf, fieldIndex, formatParams)).append(">");
            } else {
                sb.append("<select name=\"").append(name).append("\" id=\"").append(name).append("\"").append(
                    getExtraFormatting(rf, fieldIndex, formatParams)).append(">");
                Calendar c = new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
                c.clear();
                c.set(1900, 0, 1); // set 1900,Jan,1st as the date to start
                // building interface from
                for (int i = lowLimits[component]; i <= hiLimits[component]; i++) {
                    c.set(components[component], i);
                    String opt = df.format(c.getTime());
                    sb.append("<option value=\"").append(i).append("\"");
                    if (opt.equals(val))
                        sb.append(" selected");
                    sb.append(">").append(opt).append("</option>");
                }
                sb.append("</select>");
            }
        }
    }

    public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.controller.http.HttpParameters pr,
            String suffix) {
        Calendar c = new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
        c.clear();
        for (int i = 0; i < components.length; i++) {
            String name = getComponentName(rf, fieldIndex, i, suffix);
            Object o = pr.getParameter(name);
            if (o == null)
                continue;
            if (o instanceof Vector)
                throw new InvalidValueException(rf.expr[fieldIndex], "Multiple value not allowed for '"
                        + componentNames[i] + "' component");
            int n = -1;
            try {
                n = Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
                throw new InvalidValueException(rf.expr[fieldIndex], "Non-integer value not allowed for '"
                        + componentNames[i] + "' component: " + o);
            }
            c.set(components[i], n);
        }
        Date d = c.getTime();
        if (d.equals(rf.dd.getFieldDefinition(fieldIndex).getDefaultValue())
                && pr.getParameter(getNullName(rf, fieldIndex, suffix)) != null)
            return null;
        return d;
    }

    /**
     * This method is used to get the date field in case of a form reload due to validation errors, and is used from
     * {@link BasicValueTag#doMakumbaEndTag(org.makumba.commons.formatters.jsptaglib.PageCache)}. It is basically
     * i simplified version of {@link #readFrom(RecordFormatter, int, HttpParameters, String)}.
     */
    public static Object readFrom(String name, HttpParameters pr) {
        Calendar c = new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
        c.clear();
        for (int i = 0; i < components.length; i++) {
            Object o = pr.getParameter(name + "_" + i);
            if (o == null)
                continue;
            int n = -1;
            try {
                n = Integer.parseInt((String) o);
            } catch (NumberFormatException e) {
            }
            c.set(components[i], n);
        }
        return c.getTime();
    }

    int formatFrom(RecordFormatter rf, int fieldIndex, StringBuffer sb, Date d, String format, int n, boolean hidden,
            Dictionary formatParams) {
        int m = n;
        char c = format.charAt(n);
        while (++n < format.length() && format.charAt(n) == c)
            ;
        formatComponent(rf, fieldIndex, sb, d, format.substring(m, n), recognized.indexOf(c), hidden, formatParams);
        return n;
    }

    int findNextFormatter(RecordFormatter rf, int fieldIndex, StringBuffer sb, String format, int n, boolean hidden) {
        StringBuffer quoted = null;
        for (; n < format.length(); n++) {
            char c = format.charAt(n);
            if (c == '\'')
                if (quoted != null) // existing quote
                    if (quoted.length() == 0) // double quote
                    {
                        if (!hidden)
                            sb.append('\'');
                        quoted = null;
                    } else // closed quote
                    {
                        if (!hidden)
                            sb.append(quoted.toString());
                        quoted = null;
                    }
                else
                    // new quote
                    quoted = new StringBuffer();
            else if (quoted != null) {
                quoted.append(c);
            } else // we're outside quotes
            if (!Character.isLetter(c)) // non-letters don't need quotes
            {
                if (!hidden)
                    sb.append(c);
            } else if (recognized.indexOf(c) == -1)
                throw new InvalidValueException(rf.expr[fieldIndex], "unrecognized formatting letter \'" + c
                        + "\' in date format string <" + format + ">");
            else
                return n;
        }
        if (quoted != null)
            throw new InvalidValueException(rf.expr[fieldIndex], "unterminated single quote in date format string <"
                    + format + ">");
        return -1;
    }
}
