/*
 * Created on 20-sep-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.makumba.db.makumba.sql;

import org.makumba.FieldDefinition;


public class HsqldbTableManager extends org.makumba.db.makumba.sql.TableManager {
	
	@Override
    protected int getSQLType(String fieldName) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._text:
			return -4;
		default:
			return super.getSQLType(fieldName);
		}
	}
	@Override
    protected String getColumnAlterKeyword() {
		return "ALTER COLUMN";
	}
	
	@Override
    public String getFieldDBIndexName(String fieldName) {
		return getFieldDBName(fieldName)+"_"+this.getDBName();
	}


}
