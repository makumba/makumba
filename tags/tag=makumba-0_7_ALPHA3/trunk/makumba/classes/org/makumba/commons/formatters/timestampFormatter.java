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

package org.makumba.commons.formatters;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Dictionary;

public class timestampFormatter extends dateFormatter {

	private static final class SingletonHolder {
		static final FieldFormatter singleton = new timestampFormatter();
	}

	private timestampFormatter() {
	}

	public static FieldFormatter getInstance() {
		return SingletonHolder.singleton;
	}

	public String formatNotNull(RecordFormatter rf, int fieldIndex, Object o, Dictionary formatParams) {
		DateFormat formatter = timestamp;
		String s = (String) formatParams.get("format");
		if (s != null) {
			formatter = new SimpleDateFormat(s, org.makumba.MakumbaSystem
					.getLocale());
			formatter.setCalendar(calendar);
		}

		return formatter.format((java.util.Date) o);
	}

	public static final DateFormat timestamp;

	static {
		timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S",
				org.makumba.MakumbaSystem.getLocale());
		timestamp.setCalendar(calendar);
	}

}
