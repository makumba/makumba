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

import java.io.UnsupportedEncodingException;
import java.util.Dictionary;

import org.apache.commons.lang.StringUtils;
import org.makumba.HtmlUtils;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;

public class charViewer extends FieldViewer {
    static String[] params = { "default", "empty", "urlEncode", "html", "format", "maxLength", "ellipsis",
            "ellipsisLength", "addTitle" };

    static String[][] paramValues = { null, null, { "true", "false" }, { "true", "false", "auto" },
            { "raw", "htmlescape", "stripHTML", "urlencode", "wiki", "auto" }, null, null, null,
            { "true", "false", "auto" } };

    @Override
    public String[] getAcceptedParams() {
        return params;
    }

    @Override
    public String[][] getAcceptedValue() {
        return paramValues;
    }

    private static final class SingletonHolder implements org.makumba.commons.SingletonHolder {
        static FieldFormatter singleton = new charViewer();

        public void release() {
            singleton = null;
        }

        public SingletonHolder() {
            org.makumba.commons.SingletonReleaser.register(this);
        }
    }

    private charViewer() {
    }

    public static FieldFormatter getInstance() {
        return SingletonHolder.singleton;
    }

    @Override
    public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary<String, Object> formatParams) {
        String txt = o.toString();
        String html = (String) formatParams.get("html");
        String format = (String) formatParams.get("format");

        if (html != null && format != null) {
            throw new InvalidValueException(rf.expr[fieldIndex],
                    "invalid combination of parameters 'html' and 'format'. 'html' is deprecated, please use only 'format'.");
        }

        if (StringUtils.equals(html, "true") || StringUtils.equals(format, "raw") || StringUtils.equals(html, "auto")
                && HtmlUtils.detectHtml(txt) || StringUtils.equals(format, "auto") && HtmlUtils.detectHtml(txt)) {
            return txt;
        } else if (StringUtils.equals(format, "urlencode")) {
            try {
                return java.net.URLEncoder.encode(txt, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeWrappedException(e);
            }
        } else {
            if (StringUtils.equals(format, "stripHTML")) {
                txt = HtmlUtils.stripHTMLTags(txt);
            }
            return formatMaxLengthEllipsis(rf, fieldIndex, txt, formatParams);
        }
    }

}
