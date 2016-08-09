/*
 * Created on 18-apr-2005
 */
package org.makumba.db.makumba.sql;

import org.makumba.FieldDefinition;

/**
 * @author Bart
 * 
 * TODO In sql.qed.redirectManager.properties, date = dateSQL --> needs to be
 * implemented??
 */

public class QedTableManager extends org.makumba.db.makumba.sql.TableManager {

	//	  moved from qed.textManager
	protected String getfieldDBType(String fieldName) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._text:
			return "TEXT";
		case FieldDefinition._binary:
			return "BLOB";
		default:
			return super.getFieldDBType(fieldName);
		}
	}

	//		moved from qed.textManager
	protected boolean unmodified(String fieldName, int type, int size,
			java.util.Vector columns, int index) throws java.sql.SQLException {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._binary:
		case FieldDefinition._text:
			System.out.println(type);
			return super.unmodified(fieldName, type, size, columns, index)
					|| type == java.sql.Types.BLOB;
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return super.unmodified(fieldName, type, size, columns, index)
					|| type == java.sql.Types.VARCHAR;
		default:
			return super.unmodified(fieldName, type, size, columns, index);
		}
	}

	//	moved from qed.charManager
	/** returns char */
	protected String get_char_DBType() {
		return "VARCHAR";
	}
}
