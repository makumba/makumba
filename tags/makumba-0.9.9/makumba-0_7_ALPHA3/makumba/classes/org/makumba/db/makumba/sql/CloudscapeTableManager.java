/*
 * Created on 18-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.makumba.sql;

import org.makumba.FieldDefinition;

/**
 * @author Bart
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class CloudscapeTableManager extends org.makumba.db.makumba.sql.TableManager {
	
	//moved from cloudscape.textManager
	/** ask this field to write its contribution in a SQL CREATE statement */
	public String inCreate(String fieldName, Database d) {
		if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._text)
			return getFieldDBName(fieldName) + " "
					+ getFieldDBType(fieldName, d) + "(1024000)";
		else
			return super.inCreate(fieldName, d);
	}
}
