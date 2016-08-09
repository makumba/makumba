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
import java.util.Hashtable;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.commons.formatters.FieldFormatter;
import org.makumba.commons.formatters.RecordFormatter;
import org.makumba.commons.formatters.dateFormatter;
import org.makumba.commons.formatters.intEnumFormatter;
import org.makumba.commons.formatters.ptrFormatter;
import org.makumba.commons.formatters.timestampFormatter;
import org.makumba.list.engine.ComposedQuery;

public class RecordViewer extends RecordFormatter {
    public RecordViewer(ComposedQuery q) {
        dd = (DataDefinition) q.getResultType();
        initFormatters();

        expr = new String[dd.getFieldNames().size()];

        for (int i = 0; i < dd.getFieldNames().size(); i++)
            expr[i] = q.getProjectionAt(i);
    }
	public RecordViewer(DataDefinition ri, Hashtable h) {
		super(ri, h);
	}

	protected String applyParameters(FieldFormatter ff,
			Dictionary formatParams, String s) {
		if (formatParams.get("urlEncode") != null)
			try {
				return java.net.URLEncoder.encode(s, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return s;
	}

	protected void initFormatters() {
		formatterArray = new FieldFormatter[dd.getFieldNames().size()];
		for (int i = 0; i < dd.getFieldNames().size(); i++) {
			FieldDefinition fd = dd.getFieldDefinition(i);
			switch (fd.getIntegerType()) {
			case FieldDefinition._char:
				formatterArray[i] = charViewer.getInstance();
				break;
			case FieldDefinition._text:
				formatterArray[i] = textViewer.getInstance();
				break;
			case FieldDefinition._date:
				formatterArray[i] = dateFormatter.getInstance();
				break;
			case FieldDefinition._dateCreate:
			case FieldDefinition._dateModify:
				formatterArray[i] = timestampFormatter.getInstance();
				break;
			case FieldDefinition._intEnum:
				formatterArray[i] = intEnumFormatter.getInstance();
				break;
			case FieldDefinition._ptr:
			case FieldDefinition._ptrIndex:
			case FieldDefinition._ptrOne:
			case FieldDefinition._ptrRel:
				formatterArray[i] = ptrFormatter.getInstance();
				break;
			default:
				formatterArray[i] = FieldViewer.getInstance();
			}
		}
	}
}
