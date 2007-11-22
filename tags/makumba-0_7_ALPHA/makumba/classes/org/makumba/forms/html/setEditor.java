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

import java.util.Vector;

import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;

public class setEditor extends ptrEditor {

	private static final class SingletonHolder {
		static final FieldEditor singleton = new setEditor();
	}

	private setEditor() {}

	public static FieldFormatter getInstance() {
		return SingletonHolder.singleton;
	}

	public String getMultiple(RecordFormatter rf, int fieldIndex) {
		return " multiple";
	}

	public boolean isMultiple(RecordFormatter rf, int fieldIndex) {
		return true;
	}

	public int getDefaultSize(RecordFormatter rf, int fieldIndex) {
		return 10;
	}

	public Object readFrom(RecordFormatter rf, int fieldIndex,
			org.makumba.commons.attributes.HttpParameters p, String suffix) {
		Object o = super.readFrom(rf, fieldIndex, p, suffix);
		if (o == null)
			return new Vector();

		/* we remove all nulls from the input */
		if (o instanceof Vector) {
			for (java.util.Iterator i = ((Vector) o).iterator(); i.hasNext();)
				if ("".equals(i.next()))
					i.remove();
		}
		return o;
	}
}
