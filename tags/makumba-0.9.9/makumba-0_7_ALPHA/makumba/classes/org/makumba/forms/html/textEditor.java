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
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;

public class textEditor extends FieldEditor {
	
	private static final class SingletonHolder {
		static final FieldEditor singleton = new textEditor();
	}

	private textEditor() {}

	public static FieldFormatter getInstance() {
		return SingletonHolder.singleton;
	}

	static String[] _params = { "default", "empty", "type", "rows", "cols" };

	static String[][] _paramValues = { null, null, { "textarea", "file" },
			null, null };

	public String[] getAcceptedParams() {
		return _params;
	}

	public String[][] getAcceptedValue() {
		return _paramValues;
	}

	public String getParams(RecordFormatter rf, int fieldIndex,
			Dictionary formatParams) {
		return getIntParamString(rf, fieldIndex, formatParams, "rows")
				+ getIntParamString(rf, fieldIndex, formatParams, "cols");
	}

	public String formatNull(RecordFormatter rf, int fieldIndex,
			Dictionary formatParams) {
		return formatNotNull(rf, fieldIndex, null, formatParams);
	}

	public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o,
			Dictionary formatParams) {
		if (isTextArea(rf, fieldIndex, formatParams)) {
			return "<TEXTAREA name=\""
					+ getInputName(rf, fieldIndex, formatParams) + "\" "
					+ getParams(rf, fieldIndex, formatParams)
					+ getExtraFormatting(rf, fieldIndex, formatParams) + " >"
					+ formatValue(rf, fieldIndex, o, formatParams)
					+ "</TEXTAREA>";
		} else {
			return fileInput(rf, fieldIndex, formatParams);
		}
	}

	/**
	 * Formats the value to appear in an input statement. For textarea type data
	 * only!
	 */
	public String formatValue(RecordFormatter rf, int fieldIndex, Object o,
			Dictionary formatParams) {
		String s = (o == null) ? null : HtmlUtils.string2html(o.toString());
		return resetValueFormat(rf, fieldIndex, s, formatParams);
	}

	/*
	 * Formats the value to appear in hidden input statement: don't overload
	 * default behaviour set in FieldEditor.
	 */
	// public String formatHiddenValue(Object o, Dictionary formatParams) {}
	String fileInput(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		return "<INPUT name=\"" + getInputName(rf, fieldIndex, formatParams)
				+ "\" type=\"file\" "
				+ getExtraFormatting(rf, fieldIndex, formatParams) + " >";
	}

	boolean isTextArea(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		String s = (String) formatParams.get("type");
		if (s == null)
			return true;
		return s.equals("textarea");
	}
}
