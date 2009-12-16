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

package org.makumba.view.html;

import java.util.Dictionary;

import org.makumba.HtmlUtils;
import org.makumba.view.FieldFormatter;
import org.makumba.view.RecordFormatter;

public class charViewer extends FieldViewer {
	static String[] params = { "default", "empty", "urlEncode", "html",
			"maxLength", "ellipsis", "ellipsisLength", "addTitle" };

	static String[][] paramValues = { null, null, { "true", "false" },
			{ "true", "false", "auto" }, null, null, null,
			{ "true", "false", "auto" } };

	public String[] getAcceptedParams() {
		return params;
	}

	public String[][] getAcceptedValue() {
		return paramValues;
	}

	private static final class SingletonHolder {
		static final FieldFormatter singleton = new charViewer();
	}

	private charViewer() {
	}

	public static FieldFormatter getInstance() {
		return SingletonHolder.singleton;
	}
	
	public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		String txt = o.toString();
		String ht = (String) formatParams.get("html");

		if (ht != null
				&& (ht.equals("true") || ht.equals("auto")
						&& HtmlUtils.detectHtml(txt)))
			return txt;

		return formatMaxLengthEllipsis(rf, fieldIndex, txt, formatParams);
	}
}