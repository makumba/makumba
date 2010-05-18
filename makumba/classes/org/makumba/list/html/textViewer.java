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

package org.makumba.list.html;

import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.makumba.HtmlUtils;
import org.makumba.Text;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;

public class textViewer extends FieldViewer {
    static String[] params = { "default", "empty", "lineSeparator", "longLineLength", "html", "format" };

    static String[][] paramValues = { null, null, null, null, { "true", "false", "auto" },
            { "raw", "htmlescape", "stripHTML", "urlencode", "wiki", "auto" } };

    public String[] getAcceptedParams() {
        return params;
    }

    public String[][] getAcceptedValue() {
        return paramValues;
    }

    static int screenLength = 30;

    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
        static FieldFormatter singleton = new textViewer();
        
        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    private textViewer() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        if(o instanceof Text)
            o=((Text)o).getString();
        String txt = o.toString();
        String html = (String) formatParams.get("html");
        String format = (String) formatParams.get("format");

        if (html != null && format != null) {
            throw new InvalidValueException(rf.expr[fieldIndex],
                    "invalid combination of parameters 'html' and 'format'. 'html' is deprecated, please use only 'format'.");
        }

        if (StringUtils.equals(html, "true") || StringUtils.equals(format, "raw")
                || (StringUtils.equals(html, "auto") && HtmlUtils.detectHtml(txt))
                || (StringUtils.equals(format, "auto") && HtmlUtils.detectHtml(txt))) {
            return txt;
        } else if (StringUtils.equals(format, "urlencode")) {
            return java.net.URLEncoder.encode(txt);
        }

        if (StringUtils.equals(format, "stripHTML")) {
            txt = HtmlUtils.stripHTMLTags(txt);
        }

        String startSeparator = "<p>";
        String endSeparator = "</p>";
        String s = (String) formatParams.get("lineSeparator");
        if (s != null) {
            startSeparator = s;
            endSeparator = "";
        }

        int n = getIntParam(rf, fieldIndex, formatParams, "longLineLength");
        if (n == -1)
            n = screenLength;

        if (HtmlUtils.maxLineLength(txt) > n)
            // special text formatting
            return HtmlUtils.text2html(txt, startSeparator, endSeparator);
        else if (txt.indexOf('\n') < 0)
            // single, short line of text
            return HtmlUtils.string2html(txt);
        else
            // else: text preformatted
            return "<pre style=\"margin:0px\">" + HtmlUtils.string2html(txt) + "</pre>";
    }
}
