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

package org.makumba.controller.html;

import java.util.Dictionary;

import org.makumba.HtmlUtils;
import org.makumba.view.FieldFormatter;
import org.makumba.view.InvalidValueException;
import org.makumba.view.RecordFormatter;

public class charEditor extends FieldEditor {
	
	private static final class SingletonHolder {
		static final FieldEditor singleton = new charEditor();
	}

	/** Don't use this, use getInstance() */
	protected charEditor() {}

	public static FieldFormatter getInstance() {
		return SingletonHolder.singleton;
	}
	

//	public static final charEditor singleton = new charEditor();

	static String[] _params = { "default", "empty", "type", "size", "maxlength" };

	static String[][] _paramValues = { null, null, { "text", "password" },
			null, null };

	public String[] getAcceptedParams() {
		return _params;
	}

	public String[][] getAcceptedValue() {
		return _paramValues;
	}

    public int getWidth(RecordFormatter rf, int fieldIndex)
    { return  rf.dd.getFieldDefinition(fieldIndex).getWidth(); }
    
	public String getParams(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		String ret = getIntParamString(rf, fieldIndex, formatParams, "size");
		int n = getIntParam(rf, fieldIndex, formatParams, "maxlength");
		if (n > getWidth(rf, fieldIndex))
			throw new InvalidValueException(rf.expr[fieldIndex],
					"invalid too big for maxlength " + n);
		if (n == -1)
			n = getWidth(rf, fieldIndex);
		ret += "maxlength=\"" + n + "\" ";
		return ret;
	}

	/** Formats the input-field in case of null object */
	public String formatNull(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		return formatNotNull(rf, fieldIndex, null, formatParams);
	}

	/** Formats the input-field in case of not-null object */
	public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		String test = getParams(rf, fieldIndex, formatParams);
		return "<input name=\"" + getInputName(rf, fieldIndex, formatParams) + "\" type=\""
				+ getInputType(rf, fieldIndex, formatParams) + "\" value=\""
				+ formatValue(rf, fieldIndex, o, formatParams) + "\" "
				+ test + getExtraFormatting(rf, fieldIndex, formatParams)
				+ ">";
	}

	/** Formats the value to appear in an input statement. */
	public String formatValue(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		String s = (o == null) ? null : HtmlUtils.string2html(o.toString());
		return resetValueFormat(rf, fieldIndex, s, formatParams);
	}

	/*
	 * Formats the value to appear in hidden input statement: don't overload
	 * default behaviour set in FieldEditor.
	 */
	// public String formatHiddenValue(Object o, Dictionary formatParams) {}

	// public String getLiteral(Object o, Dictionary formatParams)
	// { }
	public String getInputType(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		String s = (String) formatParams.get("type");
		if (s == null)
			s = "text";
		return s;
	}
}
