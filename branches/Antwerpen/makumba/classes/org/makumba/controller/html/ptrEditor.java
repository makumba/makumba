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
import java.util.Iterator;
import java.util.Vector;

import org.makumba.Database;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.util.ChoiceSet;
import org.makumba.view.RecordFormatter;

public class ptrEditor extends choiceEditor {
	protected ptrEditor() {
	}

	public static final ptrEditor singleton = new ptrEditor();

	public void onStartup(RecordFormatter rf, int fieldIndex) {
		((RecordEditor)rf).db[fieldIndex] = ((RecordEditor)rf).database;
		((RecordEditor)rf).query[fieldIndex] = "SELECT choice as choice, choice." + rf.dd.getFieldDefinition(fieldIndex).getTitleField()
				+ " as title FROM " + rf.dd.getFieldDefinition(fieldIndex).getReferredType().getName()
				+ " choice ORDER BY title";
	}

	public Object getOptions(RecordFormatter rf, int fieldIndex,
			Dictionary formatParams) {
		ChoiceSet c = (ChoiceSet) formatParams.get(ChoiceSet.PARAMNAME);
		if (c != null)
			return c;

		Vector v = null;

		Database dbc = MakumbaSystem.getConnectionTo(((RecordEditor)rf).db[fieldIndex]);
		try {
			v = dbc.executeQuery(((RecordEditor)rf).query[fieldIndex], null);
		} finally {
			dbc.close();
		}
		c = new ChoiceSet();
		for (Iterator i = v.iterator(); i.hasNext();) {
			Dictionary d = (Dictionary) i.next();
			c.add(d.get("choice"), d.get("title").toString(), false, false);
		}
		return c;
	}

	public int getOptionsLength(RecordFormatter rf, int fieldIndex, Object opts) {
		return ((ChoiceSet) opts).size();
	}

	public Object getOptionValue(RecordFormatter rf, int fieldIndex,
			Object options, int i) {
		return ((ChoiceSet.Choice) ((ChoiceSet) options).get(i)).getValue();
	}

	public String formatOptionValue(RecordFormatter rf, int fieldIndex, Object val) {
		if (val == Pointer.Null)
			return "";
		return ((Pointer) val).toExternalForm();
	}

	public String formatOptionValue(RecordFormatter rf, int fieldIndex,
			Object opts, int i, Object val) {
		return formatOptionValue(rf, fieldIndex, val);
	}

	public String formatOptionTitle(RecordFormatter rf, int fieldIndex,
			Object options, int i) {
		return ""
				+ ((ChoiceSet.Choice) ((ChoiceSet) options).get(i)).getTitle();
	}

	public Object readFrom(RecordFormatter rf, int fieldIndex,
			org.makumba.controller.http.HttpParameters p, String suffix) {
		Object o = super.readFrom(rf, fieldIndex, p, suffix);
		if ("".equals(o))
			return null;
		return o;
	}

	public String getMultiple(RecordFormatter rf, int fieldIndex) {
		return "";
	}

	public boolean isMultiple(RecordFormatter rf, int fieldIndex) {
		return false;
	}

	public int getDefaultSize(RecordFormatter rf, int fieldIndex) {
		return 1;
	}
}
