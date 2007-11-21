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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class InformixTableManager extends org.makumba.db.makumba.sql.TableManager
{

	//moved from informix.dateTimeManager; .textManager and .timeStampManager
	protected String getFieldDBType(String fieldName)
	  {
		switch (getFieldDefinition(fieldName).getIntegerType()){
			case FieldDefinition._date:
				 return "DATETIME YEAR TO FRACTION";
			case FieldDefinition._text:
				return "BYTE";
			case FieldDefinition._dateCreate:
			case FieldDefinition._dateModify:
				  return "DATETIME YEAR TO FRACTION";
			default:
				return super.getFieldDBType(fieldName);
		}
	   
	  }
		
}
