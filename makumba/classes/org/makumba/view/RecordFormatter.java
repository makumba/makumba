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

package org.makumba.view;

import java.util.Dictionary;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;

public class RecordFormatter {
	public DataDefinition dd;

	public String[] expr;

	protected FieldFormatter[] formatterArray;

	public RecordFormatter(ComposedQuery q) {
		dd = (DataDefinition) q.getResultType();
		initFormatters();

		expr = new String[dd.getFieldNames().size()];

		for (int i = 0; i < dd.getFieldNames().size(); i++)
			expr[i] = q.getProjectionAt(i);
	}

	public RecordFormatter(DataDefinition dd, java.util.Hashtable names) {
		this.dd = dd;
		initFormatters();

		expr = new String[dd.getFieldNames().size()];

		for (int i = 0; i < dd.getFieldNames().size(); i++) {
			expr[i] = (String) names.get(dd.getFieldDefinition(i).getName());
		}
	}

	protected String applyParameters(FieldFormatter ff,
			Dictionary formatParams, String s) {
		return s;
	}

	public String format(int i, Object value, Dictionary formatParams) {
		formatterArray[i].checkParams(this, i, formatParams);
		return applyParameters(formatterArray[i], formatParams,
				formatterArray[i].format(this, i, value, formatParams));
	}

	protected void initFormatters() {
		formatterArray = new FieldFormatter[dd.getFieldNames().size()];
		for (int i = 0; i < dd.getFieldNames().size(); i++) {
			FieldDefinition fd = dd.getFieldDefinition(i);
			switch (fd.getIntegerType()) {
			case FieldDefinition._ptr:
			case FieldDefinition._ptrRel:
			case FieldDefinition._ptrOne:
			case FieldDefinition._ptrIndex:
				formatterArray[i] = ptrFormatter.getInstance();
				break;
			case FieldDefinition._intEnum:
				formatterArray[i] = intEnumFormatter.getInstance();
				break;
			case FieldDefinition._date:
				formatterArray[i] = dateFormatter.getInstance();
				break;
			case FieldDefinition._dateCreate:
			case FieldDefinition._dateModify:
				formatterArray[i] = timestampFormatter.getInstance();
				break;
			default:
				formatterArray[i] = FieldFormatter.getInstance();
			}
		}
	}
}