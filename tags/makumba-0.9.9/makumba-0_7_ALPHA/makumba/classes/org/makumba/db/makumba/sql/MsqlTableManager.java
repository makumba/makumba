/*
 * Created on 18-apr-2005
 */
package org.makumba.db.makumba.sql;

import org.makumba.FieldDefinition;

/**
 * @author Bart
 * 
 * TODO In sql.msql.redirectManager.properties, timeStamp=date --> needs to be
 * implemented??
 */
public class MsqlTableManager extends org.makumba.db.makumba.sql.TableManager {

	//	moved from msql.textManager
	/** msql needs an 'approximative size' for text fields. */
	public String inCreate(String fieldName, Database d) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._text:
			return super.inCreate(fieldName, d) + "(255)";
		default:
			return super.inCreate(fieldName, d);
		}
	}

	//	Moved from msql.textManager
	/** what is the database level type of this field? */
	protected String getFieldDBType(String fieldName) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._text:
			return "TEXT";
		default:
			return super.getFieldDBType(fieldName);
		}
	}
}
