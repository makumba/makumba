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

import java.util.Vector;

import org.makumba.view.RecordFormatter;

public class setintEnumEditor extends setcharEnumEditor {
	
	private static final class SingletonHolder {
		static final FieldEditor singleton = new setintEnumEditor();
	}
		
	protected setintEnumEditor() {}
		
	public static FieldEditor getInstance() {
		return SingletonHolder.singleton;
	}

	public Object getOptionValue(RecordFormatter rf, int fieldIndex, Object options, int i) {
		return new Integer(rf.dd.getFieldDefinition(fieldIndex).getIntAt(i));
	}

	public Object readFrom(RecordFormatter rf, int fieldIndex, org.makumba.controller.http.HttpParameters par,
			String suffix) {
		Object o = par.getParameter(getInputName(rf, fieldIndex, suffix));

		if (o == null || o == org.makumba.Pointer.NullSet)
			return o;
		if (o instanceof Vector) {
			Vector v = (Vector) o;
			for (int i = 0; i < v.size(); i++)
				v.setElementAt(toInt(rf, fieldIndex, v.elementAt(i)), i);
			return v;
		}
		return toInt(rf, fieldIndex, o);
	}
}
