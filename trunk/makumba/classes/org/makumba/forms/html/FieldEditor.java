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

import org.makumba.ProgrammerError;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.InvalidValueException;
import org.makumba.commons.formatters.RecordFormatter;

public class FieldEditor extends org.makumba.commons.formatters.FieldFormatter {

    public static final String ERROR_NO_INT = "invalid integer";
    
    public static final String ERROR_NO_BOOLEAN = "invalid boolean";

    public static final String ERROR_NO_REAL = "invalid real";
	
	/* see http://c2.com/cgi/wiki?JavaSingleton */
	private static final class SingletonHolder {
		static final FieldEditor singleton = new FieldEditor();
	}	

	/** Don't use this, use getInstance() */
	protected FieldEditor() {}	

	public static FieldFormatter getInstance() {
		return SingletonHolder.singleton;
	}

	static String[] params = { "default", "empty", "type" };

	static String[][] paramValues = { null, null, { "hidden" } };

	public String[] getAcceptedParams() {
		return params;
	}

	public String[][] getAcceptedValue() {
		return paramValues;
	}

	static final String suffixName = "org.makumba.editorSuffix";

	public static String getSuffix(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		return (String) formatParams.get(suffixName);
	}

	public static void setSuffix(Dictionary formatParams, String suffix) {
		formatParams.put(suffixName, suffix);
	}

	public void checkParam(RecordFormatter rf, int fieldIndex, String name, String val) {
		if (name.equals(extraFormattingParam))
			return;
		if (name.equals("type") && val.equals("hidden"))
			return;
		super.checkParam(rf, fieldIndex, name, val);
	}

	public String format(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		String s = (String) formatParams.get("type");
		if (s != null && s.equals("hidden"))
			return formatHidden(rf, fieldIndex, o, formatParams);
		return formatShow(rf, fieldIndex, o, formatParams);
	}

	public String formatShow(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		// this will call formatNull and formatNotNull which should be redefined
		// or, the entire formatShow should be redefined
		return super.format(rf, fieldIndex, o, formatParams);
	}

	public String formatHidden(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		return "<input type=\"hidden\" name=\"" + getInputName(rf, fieldIndex, formatParams)
				+ "\" value=\"" + formatHiddenValue(rf, fieldIndex, o, formatParams) + "\" "
				+ getExtraFormatting(rf, fieldIndex, formatParams) + ">";
	}

	/** Formats the value to appear in hidden input statement. */
	public String formatHiddenValue(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		// default : same treatment as formatting for normal input.
		return formatValue(rf, fieldIndex, o, formatParams);
	}

	/** Formats the value to appear in an input statement. */
	public String formatValue(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		// return super.format(o, formatParams);
		throw new ProgrammerError(
				"If this method is needed, overload it in the inheriting class");
	}

	public void onStartup(RecordFormatter rf, int fieldIndex) {
	}

	public String getInputName(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		return getInputName(rf, fieldIndex, getSuffix(rf, fieldIndex, formatParams));
	}

	public String getInputName(RecordFormatter rf, int fieldIndex, String suffix) {
		return rf.expr[fieldIndex] + suffix;
	}

	public static final String extraFormattingParam = "makumba.extraFormatting";

	public static String getExtraFormatting(RecordFormatter rf, int fieldIndex, Dictionary formatParams) {
		return (String) formatParams.get(extraFormattingParam);
	}

	public static void setExtraFormatting(Dictionary formatParams,
			String extraFormatting) {
		formatParams.put(extraFormattingParam, extraFormatting);
	}

	public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.commons.attributes.HttpParameters p,
			String suffix) {
		return p.getParameter(getInputName(rf, fieldIndex, suffix));
	}

	protected Integer toInt(RecordFormatter rf, int fieldIndex, Object o) {
		if (o == null)
			return null;
		String s = ("" + o).trim();
		if (s.length() == 0)
			return null;
		try {
			return new Integer(Integer.parseInt(s));
		} catch (NumberFormatException e) {
            throw new InvalidValueException(rf.expr[fieldIndex], ERROR_NO_INT+": " + o);
		}
	}
	
	protected Boolean toBoolean(RecordFormatter rf, int fieldIndex, Object o) {
        if (o == null)
            return null;
        if(o.toString().equalsIgnoreCase("true") || o.toString().equalsIgnoreCase("yes") || o.toString().equalsIgnoreCase("1")) {
            return true;
        } else if(o.toString().equalsIgnoreCase("false") || o.toString().equalsIgnoreCase("no") || o.toString().equalsIgnoreCase("0")) {
            return false;
        } else {
            throw new InvalidValueException(rf.expr[fieldIndex], ERROR_NO_BOOLEAN+": " + o);
        }
    }

	protected Double toReal(RecordFormatter rf, int fieldIndex, Object o) {
		if (o == null)
			return null;
		String s = ("" + o).trim();
		if (s.length() == 0)
			return null;
		try {
			return new Double(Double.parseDouble(s));
		} catch (NumberFormatException e) {
			try {
				return new Double(Double.parseDouble(s.replace(',', '.')));
			} catch (NumberFormatException e2) {
				try {
					return new Double(Double.parseDouble(s.replace('.', ',')));
				} catch (NumberFormatException e3) {
                    throw new InvalidValueException(rf.expr[fieldIndex], ERROR_NO_REAL+": " + o);
				}
			}
		}
	}

}
