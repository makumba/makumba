/*
 * Created on 18-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.makumba.sql;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.makumba.FieldDefinition;

/**
 * @author Bart
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class OdbcjetV4TableManager extends OdbcjetTableManager {

	//moved from odbcjet.v4.dateTimeManager, .timestampManager
	static DateFormat odbcDate = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss",
			Locale.UK);

	//Moved from odbcjet.v4.dateTimeManager
	public String writeConstant(String fieldName, Object o) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._date:
			return "\'"
					+ odbcDate.format(new Timestamp(((java.util.Date) o)
							.getTime())) + "\'";
		default:
			return super.writeConstant(fieldName, o);
		}
	}
}
