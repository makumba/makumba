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
public class OracleTableManager extends org.makumba.db.makumba.sql.TableManager
{
	//moved from oracle.charManager
	  /** returns char */
	  protected String get_char_FieldDBType(String fieldName)
	  {
	  	switch (getFieldDefinition(fieldName).getIntegerType()){
	  	case FieldDefinition._char:
	  		case FieldDefinition._charEnum:
	  			return "VARCHAR2";		
	  	default:
	  		return super.get_char_FieldDBType(fieldName);
	  	}
	  }
}
