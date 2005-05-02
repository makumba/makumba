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
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.view.FieldFormatter;
import org.makumba.view.RecordFormatter;

public class RecordEditor extends RecordFormatter {
	String database;
	
	String[] db;

	String[] query;

	public RecordEditor(DataDefinition ri, Hashtable h, String database) {
		super(ri, h);
		this.database = database;
	}

	public Dictionary readFrom(HttpServletRequest req, String suffix) {
		Dictionary data = new Hashtable();
		for (int i = 0; i < dd.getFieldNames().size(); i++) {
			FieldEditor fe = (FieldEditor) formatterArray[i];
			if (fe.getInputName(this, i, suffix) == null)
				continue;
			Object o = fe.readFrom(this, i,
					org.makumba.controller.http.RequestAttributes
							.getParameters(req), suffix);
			if (o != null)
				o = dd.getFieldDefinition(i).checkValue(o);
			else
				o = dd.getFieldDefinition(i).getEmptyValue();

			org.makumba.controller.http.RequestAttributes.setAttribute(req, fe
					.getInputName(this, i, suffix)
					+ "_type", dd.getFieldDefinition(i));

			if (o != null)
				// the data is written in the dictionary without the suffix
				data.put(fe.getInputName(this, i, ""), o);
			org.makumba.controller.http.RequestAttributes.setAttribute(req, fe
					.getInputName(this, i, suffix), o);
		}
		return data;
	}

	public void config() {
		Object a[] = { this };
		for (int i = 0; i < dd.getFieldNames().size(); i++) {
			((FieldEditor) formatterArray[i]).onStartup(this, i);
		}
	}

	protected void initFormatters() {
		formatterArray = new FieldFormatter[dd.getFieldNames().size()];
		for (int i = 0; i < dd.getFieldNames().size(); i++) {
			FieldDefinition fd = dd.getFieldDefinition(i);
			switch (fd.getIntegerType()) {
			case FieldDefinition._ptr:
				formatterArray[i] = ptrEditor.singleton;
				break;
			case FieldDefinition._ptrOne:
			case FieldDefinition._setComplex:
				formatterArray[i] = FieldEditor.singleton;
				break;
			case FieldDefinition._int:
				formatterArray[i] = intEditor.singleton;
				break;
			case FieldDefinition._intEnum:
				formatterArray[i] = intEnumEditor.singleton;
				break;
			case FieldDefinition._char:
				formatterArray[i] = charEditor.singleton;
				break;
			case FieldDefinition._charEnum:
				formatterArray[i] = charEnumEditor.singleton;
				break;
			case FieldDefinition._text:
				formatterArray[i] = textEditor.singleton;
				break;
			case FieldDefinition._date:
				formatterArray[i] = dateEditor.singleton;
				break;
			case FieldDefinition._set:
				formatterArray[i] = setEditor.singleton;
				break;
			//			case FieldDefinition._nil:
			//				formatterArray[i] = nilEditor.singleton;
			//				break;
			case FieldDefinition._real:
				formatterArray[i] = realEditor.singleton;
				break;
			case FieldDefinition._setCharEnum:
				formatterArray[i] = setcharEnumEditor.singleton;
				break;
			case FieldDefinition._setIntEnum:
				formatterArray[i] = setintEnumEditor.singleton;
				break;
			case FieldDefinition._dateCreate:
			case FieldDefinition._dateModify:
			case FieldDefinition._ptrIndex:
			case FieldDefinition._ptrRel:
				formatterArray[i] = errorEditor.singleton;
				break;
			default:
				throw new RuntimeException("Shouldn't be here");
			}
		}
	}

}
