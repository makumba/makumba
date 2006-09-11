package org.makumba.db.hibernate.customtypes;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;
import org.makumba.Text;

public class TextUserType implements UserType {
	private static final int[] SQL_TYPES = {Types.BLOB};
	
	public int[] sqlTypes() { return SQL_TYPES; }
	
	public Class returnedClass() { return Text.class; }
	
	public boolean equals(Object x, Object y) {
		if (x == y) return true;
		if (x == null || y == null) return false;
		return x.equals(y);
	}
	public Object deepCopy(Object value) { return value; }
	
	public boolean isMutable() { return false; }
	
	public Object nullSafeGet(ResultSet resultSet, String[] names, Object owner) throws HibernateException, SQLException {
		Text text = null;
		InputStream is = resultSet.getBinaryStream(names[0]);
		if (resultSet.wasNull()) return null;
		text = Text.getText(is);		
		return text;
	}
	
	public void nullSafeSet(PreparedStatement statement, Object value, int index)
			throws HibernateException, SQLException {
		if (value == null) {
			statement.setNull(index, Types.BLOB);
		} else {
			Text text = Text.getText(value);
			statement.setBinaryStream(index, text.toBinaryStream(), text.length());
		}
	}
	
	public int hashCode(Object arg0) throws HibernateException {
		return 0;
	}
	public Serializable disassemble(Object arg0) throws HibernateException {
		return null;
	}
	public Object assemble(Serializable arg0, Object arg1) throws HibernateException {
		return null;
	}
	public Object replace(Object arg0, Object arg1, Object arg2) throws HibernateException {
		return null;
	}
}
