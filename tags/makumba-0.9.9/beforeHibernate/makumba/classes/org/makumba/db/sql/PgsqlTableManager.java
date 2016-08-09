/*
 * Created on 18-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import org.makumba.FieldDefinition;
import org.makumba.Text;

/**
 * @author Bart
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class PgsqlTableManager extends org.makumba.db.sql.TableManager {
	//	moved from pgsql.charManager
	/**
	 * postgres can't check char width, we return true
	 * 
	 * @throws SQLException
	 */
	protected boolean check_char_Width(String fieldName, ResultSetMetaData rsm,
			int index) throws SQLException {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return true;
		default:
			return super.check_char_Width(fieldName, rsm, index);
		}
	}

	//Moved from pgsql.textManager
	/** returns Postgres TEXT */
	protected String getFieldDBType(String fieldName) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._text:
			return "TEXT";
		default:
			return super.getFieldDBType(fieldName);
		}

	}

	//	Moved from pgsql.textManager
	protected int getfieldSQLType(String fieldName) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._text:
			return Types.VARCHAR;
		default:
			return super.getSQLType(fieldName);
		}
	}

	//	Moved from pgsql.textManager
	public void setArgument(String fieldName, PreparedStatement ps, int n,
			Object o) throws SQLException {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._text:
			Text t = Text.getText(o);
			ps.setString(n, t.toString());
			break;
		default:
			super.setArgument(fieldName, ps, n, o);
		}
	}
}
