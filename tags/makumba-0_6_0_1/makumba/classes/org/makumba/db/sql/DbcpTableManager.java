/*
 * Created on 18-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.sql;

import org.makumba.FieldDefinition;

/**
 * @author Bart
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DbcpTableManager extends org.makumba.db.sql.TableManager {
	
	//Moved from dbcp.textManager
	/** ask this field to write its contribution in a SQL CREATE statement */
	public String inCreate(String fieldName, Database d) {
		if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._text)
			return getFieldDBName(fieldName) + " "
					+ getFieldDBType(fieldName, d) + "(1024000)";
		else
			return super.inCreate(fieldName, d);
	}

	//moved from dbcp.timeStampManager
	/** a timestamp is always sent as null to the database */
	public String writeConstant(String fieldName, Object o) {
		if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._text)
			return "TIMESTAMP " + super.writeConstant(fieldName, o);
		else
			return super.writeConstant(fieldName, o);
	}
}
