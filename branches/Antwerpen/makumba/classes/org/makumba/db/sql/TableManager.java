///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.db.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DBError;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.NotUniqueError;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.db.DBConnection;
import org.makumba.db.DBConnectionWrapper;
import org.makumba.db.Table;

public class TableManager extends Table {
	protected String tbname;

	protected String handlerList;

	protected String indexDBField;

	protected String indexField;

	protected String modTable;

	protected long primaryKeyCurrentIndex;

	protected int dbsv;

	boolean alter;

	boolean exists_;

	Hashtable handlerExist = new Hashtable();

	Dictionary keyIndex;

	String preparedInsertString, preparedDeleteString,
			preparedDeleteFromString;

	/** The query that searches for duplicates on this field */
	String checkDuplicate;

	public boolean exists() {
		return exists_;
	}

	public boolean exists(String s) {
		return handlerExist.get(s) != null;
	}

	public String getDBName() {
		return tbname;
	}

	protected org.makumba.db.sql.Database getSQLDatabase() {
		return (org.makumba.db.sql.Database) getDatabase();
	}

	protected boolean usesHidden() {
		return true;
	}

	void makeKeyIndex() {
		if (keyIndex == null) {
			keyIndex = new Hashtable();

			for (int i = 0; i < getDataDefinition().getFieldNames().size(); i++) {
				FieldDefinition fi = getDataDefinition().getFieldDefinition(i);
				if (!fi.getType().startsWith("set"))
					keyIndex.put(fi.getName(), new Integer(i));
			}
		}
	}

	/** the SQL table opening. might call create() or alter() */
	protected void open(Properties config) {
		setTableAndFieldNames(config);
		if (!getDataDefinition().isTemporary()) {
			DBConnectionWrapper dbcw = (DBConnectionWrapper) getSQLDatabase()
					.getDBConnection();
			SQLDBConnection dbc = (SQLDBConnection) dbcw.getWrapped();
			try {
				checkStructure(dbc, config);
				initFields(dbc, config);
				preparedInsertString = prepareInsert();
				preparedDeleteString = prepareDelete();
				preparedDeleteFromString = "DELETE FROM " + getDBName()
						+ " WHERE " + indexDBField + " >= ?" + " AND "
						+ indexDBField + " <= ?";
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				dbcw.close();
			}
		} else
			makeKeyIndex();

	}

	/** the SQL table opening. might call create() or alter() */
	protected void setTableAndFieldNames(Properties config) {

		tbname = config.getProperty(getDataDefinition().getName());
		/*
		 * find the shortest possible table name, according to what is defined
		 * in config a config with rule and table: best.minerva.student=bms
		 * best.minerva.student->fields
		 * 
		 * will create table _bms__fields_ instead of
		 * _best_minerva_student__fields_ as it did before
		 */
		if (tbname == null) {
			String key = Database.findConfig(config, getDataDefinition()
					.getName());
			String shortname = getDataDefinition().getName();
			if (key != null)
				shortname = config.getProperty(key)
						+ getDataDefinition().getName().substring(key.length());

			tbname = getSQLDatabase().getTableName(shortname);
		} else if (tbname.indexOf('.') != -1)
			tbname = getSQLDatabase().getTableName(tbname);

		/*
		 * setDbName(fieldName, config) which will probably call set_int_DbName,
		 * set_date_DbName, ...
		 */
		/* TODO: callAll() */
		for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
			String fieldName = (String) e.nextElement();
			if (getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
			setFieldDBName(fieldName, config);
		}
	}

	boolean admin;

	public boolean canAdmin() {
		return admin;
	}

	protected void checkStructure(SQLDBConnection dbc, Properties config) {
		String s = Database.findConfig(config, "admin#"
				+ getDataDefinition().getName());
		admin = (s != null && config.getProperty(s).trim().equals("true"));

		s = Database.findConfig(config, "alter#"
				+ getDataDefinition().getName());
		alter = (s != null && config.getProperty(s).trim().equals("true"));

		MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
				getDatabase().getConfiguration() + ": checking "
						+ getDataDefinition().getName() + " as " + tbname);

		try {
			CheckingStrategy cs = null;
			if (getSQLDatabase().catalog != null)
				cs = new CatalogChecker(getSQLDatabase().catalog);
			else
				throw new MakumbaError(getDatabase().getName()
						+ ": could not open catalog");

			if (cs.shouldCreate()) {
				create(dbc, tbname, alter);

				exists_ = alter;
				config.put("makumba.wasCreated", "");
				makeKeyIndex();
			} else {
				exists_ = true;
				alter(dbc, cs);
			}
		} catch (SQLException sq) {
			sq.printStackTrace();
			throw new org.makumba.DBError(sq);
		}
	}

	Hashtable indexes = new Hashtable();

	Hashtable extraIndexes;

	protected void initFields(SQLDBConnection dbc, Properties config)
			throws SQLException {
		try {
			ResultSet rs = dbc.getMetaData().getIndexInfo(null, null,
					getDBName(), false, false);
			while (rs.next()) {
				String iname = rs.getString("INDEX_NAME");
				boolean non_unique = rs.getBoolean("NON_UNIQUE");
				if (iname != null)
					indexes.put(iname.toLowerCase(), new Boolean(non_unique));

			}
			rs.close();

		} catch (SQLException e) {
			Database.logException(e, dbc);
			throw new DBError(e);
		}

		extraIndexes = (Hashtable) indexes.clone();

		for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
			String fieldName = (String) e.nextElement();
			if (getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
			onStartup(fieldName, config, dbc);
		}

		if (alter)
			for (Enumeration ei = extraIndexes.keys(); ei.hasMoreElements();) {
				String indexName = (String) ei.nextElement();
				try {
					Statement st = dbc.createStatement();
					st.executeUpdate("DROP INDEX " + indexName + " ON "
							+ getDBName());
					org.makumba.MakumbaSystem.getMakumbaLogger(
							"db.init.tablechecking").info(
							"INDEX DROPPED on " + getDataDefinition().getName()
									+ "#" + indexName);
					st.close();
				} catch (SQLException e) {
				}
			}
		else {
			StringBuffer extraList = new StringBuffer();
			String separator = "";
			for (Enumeration ei = extraIndexes.keys(); ei.hasMoreElements();) {
				extraList.append(separator).append(ei.nextElement());
				separator = ", ";
			}
			if (extraList.length() > 0)
				MakumbaSystem.getMakumbaLogger("db.init.tablechecking")
						.warning(
								"Extra indexes on "
										+ getDataDefinition().getName() + ": "
										+ extraList);
		}

		StringBuffer sb = new StringBuffer();
		fieldList(sb, dd.getFieldNames().elements());
		handlerList = sb.toString();
		indexField = dd.getIndexPointerFieldName();
		indexDBField = getFieldDBName(indexField);
	}

	protected interface CheckingStrategy {
		boolean hasMoreColumns() throws SQLException;

		String columnName() throws SQLException;

		int columnType() throws SQLException;

		String columnTypeName() throws SQLException;

		boolean checkColumn(String fieldName) throws SQLException;

		boolean shouldCreate() throws SQLException;
	}

	class CatalogChecker implements CheckingStrategy {
		Vector columns;

		Hashtable column;

		int i = 0;

		CatalogChecker(Hashtable catalog) throws SQLException {
			columns = (Vector) catalog.get(tbname);
			if (columns == null) {
				columns = (Vector) catalog.get(tbname.toLowerCase());
				if (columns == null) {
					columns = (Vector) catalog.get(tbname.toUpperCase());
					if (columns != null)
						tbname = tbname.toUpperCase();
				} else
					tbname = tbname.toLowerCase();

			}
		}

		public boolean shouldCreate() {
			return columns == null;
		}

		public boolean hasMoreColumns() throws SQLException {
			if (i < columns.size()) {
				column = (Hashtable) columns.elementAt(i);
				i++;
				return true;
			}
			return false;
		}

		public String columnName() throws SQLException {
			return (String) column.get("COLUMN_NAME");
		}

		public int columnType() throws SQLException {
			return ((Integer) column.get("DATA_TYPE")).intValue();
		}

		public int columnSize() throws SQLException {
			return ((Integer) column.get("COLUMN_SIZE")).intValue();
		}

		public String columnTypeName() throws SQLException {
			return (String) column.get("TYPE_NAME");
		}

		public boolean checkColumn(String fieldName) throws SQLException {
			return unmodified(fieldName, columnType(), columnSize(), columns, i);
		}
	}

	public int deleteFrom(DBConnection here, DBConnection source) {
		if (!exists())
			return 0;
		if (!canAdmin())
			throw new MakumbaError("no administration approval for "
					+ getDataDefinition().getName());

		if (here instanceof DBConnectionWrapper)
			here = ((DBConnectionWrapper) here).getWrapped();
		PreparedStatement ps = (PreparedStatement) ((SQLDBConnection) here)
				.getPreparedStatement(preparedDeleteFromString);
		try {
			ps.setInt(1, source.getHostDatabase().getMinPointerValue());
			ps.setInt(2, source.getHostDatabase().getMaxPointerValue());
		} catch (SQLException e) {
			org.makumba.db.sql.Database.logException(e);
			throw new DBError(e);
		}
		int n = getSQLDatabase().exec(ps);

		resetPrimaryKey();
		return n;
	}

	/** checks if an alteration is needed, and calls doAlter if so */
	protected void alter(SQLDBConnection dbc, CheckingStrategy cs)
			throws SQLException {
		Vector present = new Vector();
		Vector add = new Vector();
		Vector modify = new Vector();
		Vector drop = new Vector();
		Object withness = new Object();

		while (cs.hasMoreColumns()) {
			String dbfn = cs.columnName();
			boolean found = false;
			for (Enumeration e = dd.getFieldNames().elements(); e
					.hasMoreElements();) {
				String fieldName = (String) e.nextElement();
				if (getFieldDefinition(fieldName).getType().startsWith("set"))
					continue;
				if (getFieldDBName(fieldName).toLowerCase().equals(
						dbfn.toLowerCase())) {
					handlerExist.put(fieldName, withness);
					/*
					 * We only need to know the size of present later, doens't
					 * matter which values are inside
					 */
					present.addElement(fieldName);
					if (!cs.checkColumn(fieldName)
							&& !(alter && alter(dbc, fieldName, "MODIFY"))) {
						MakumbaSystem.getMakumbaLogger("db.init.tablechecking")
								.warning(
										"should modify: " + fieldName + " "
												+ getFieldDBName(fieldName)
												+ " "
												+ getFieldDBType(fieldName)
												+ " " + cs.columnType() + " "
												+ cs.columnName());
						modify.addElement(fieldName);
					}
					found = true;
				}
			}
			if (found)
				continue;
			drop.addElement(dbfn);
			MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning(
					"extra field: " + cs.columnName() + " " + cs.columnType()
							+ " " + cs.columnTypeName());
		}

		Vector v = new Vector();
		keyIndex = new Hashtable();

		for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
			String fieldName = (String) e.nextElement();
			if (getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
			if (handlerExist.get(fieldName) == null
					&& !(alter && alter(dbc, fieldName, "ADD"))) {
				add.addElement(fieldName);
				MakumbaSystem.getMakumbaLogger("db.init.tablechecking")
						.warning(
								"should add " + fieldName + " "
										+ getFieldDBName(fieldName) + " "
										+ getFieldDBType(fieldName));
			} else {
				keyIndex.put(fieldName, new Integer(v.size()));
				v.addElement(fieldName);
			}
		}

		doAlter(dbc, drop, present, add, modify);
	}

	boolean alter(SQLDBConnection dbc, String fieldName, String op)
			throws SQLException {
		Statement st = dbc.createStatement();
		String s = "ALTER TABLE " + getDBName() + " " + op + " "
				+ inCreate(fieldName, getSQLDatabase());
		MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
				getSQLDatabase().getConfiguration() + ": " + s);
		try {
			String command = "DROP INDEX " + getFieldDBIndexName(fieldName)
					+ " ON " + getDBName();
			st.executeUpdate(command);
			MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
					"SUCCESS: " + command);
		} catch (SQLException e) {
		}
		st.executeUpdate(s);
		handlerExist.put(fieldName, "");
		dbc.commit();
		st.close();
		return true;
	}

	/**
	 * do the needed alterations after examining the data definition of the
	 * existing table. a temporary copy table is created, and the fields are
	 * copied from it to the re-CREATEd table. ALTER TABLE might be used
	 * instead, and drivers that don't support it will have their own
	 * RecordManager, extending this one.
	 * 
	 * @param drop
	 *            the names of the db fields that should be dropped (they might
	 *            not be)
	 * @param present
	 *            the abstract fields that exist in the DB, in DB order
	 * @param add
	 *            the abstract fields that are not present in the db and need to
	 *            be added
	 * @param modify
	 *            the abstract fields that exist in the db but need to be
	 *            modified to the new abstract definition
	 */
	protected void doAlter(SQLDBConnection dbc, Vector drop, Vector present,
			Vector add, Vector modify) throws SQLException {
		//  MakumbaSystem.getLogger("debug.db").severe(drop);
		// MakumbaSystem.getLogger("debug.db").severe(present);
		// MakumbaSystem.getLogger("debug.db").severe(add);
		// MakumbaSystem.getLogger("debug.db").severe(modify);

		if (add.size() == 0 && modify.size() == 0)
			return;

		if (present.size() == 0)
			create(dbc, tbname, alter);
	}

	/** for odbc */
	protected void indexCreated(SQLDBConnection dbc) {
	}

	/** for mysql */
	protected String createDbSpecific(String command) {
		return command;
	}

	/** mysql needs to have it adjustable */
	protected String getTableMissingStateName(SQLDBConnection dbc) {
		return "tableMissing";
	}

	/** a table creation, from this table's RecordInfo */
	protected void create(SQLDBConnection dbc, String tblname, boolean really)
			throws SQLException {
		Statement st = dbc.createStatement();
		Object[] dbArg = { getSQLDatabase() };
		if (really)
			try {
				st.executeUpdate("DROP TABLE " + tblname);
			} catch (SQLException e) {
				getSQLDatabase().checkState(e, getTableMissingStateName(dbc));
			}

		/* TODO: concatAll() */

		StringBuffer ret = new StringBuffer();
		String fieldName;
		String sep = "";
		for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {

			fieldName = (String) e.nextElement(); 
			if(getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
			ret.append(sep).append(inCreate(fieldName, getSQLDatabase()));
			sep = ",";
		}
		String command = "CREATE TABLE " + tblname + "(" + ret + ")";

		command = createDbSpecific(command);
		if (!really) {
			MakumbaSystem.getMakumbaLogger("db.init.tablechecking").warning(
					"would be:\n" + command);
			return;
		}
		MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(command);
		st.executeUpdate(command);
		dbc.commit();
		st.close();
	}

	/* TODO: move to initFields */
	/** list the given fields in a command field1, field2 ... */
	protected void fieldList(StringBuffer command, Enumeration e) {
		String comma = "";

		while (e.hasMoreElements()) {
			String fieldName = (String) e.nextElement();
			if(getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
			command.append(comma);
			comma = ", ";
			command.append(getFieldDBName(fieldName));
		}
	}

	/** Check if the given database tablename s actually exists in the database */
	boolean checkTableDBName(String s) {
		for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
			String fieldName = (String) e.nextElement();
			if (getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
			if (getFieldDBName(fieldName) != null
					&& getFieldDBName(fieldName).toLowerCase().equals(
							s.toLowerCase()))
				return true;
		}
		return false;
	}

	//---------------------------------------

	protected String prepareInsert() {

		/* TODO: concatAll() */
		StringBuffer ret = new StringBuffer();
		String fieldName;
		String sep="";
		
		for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {

			fieldName = (String) e.nextElement(); 
			if(getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
				ret.append(sep).append(inPreparedInsert(fieldName));
				sep=",";
			}

		return "INSERT INTO " + tbname + " (" + handlerList + ") VALUES ("
				+ ret + ")";
	}

	public Pointer insertRecordImpl(DBConnection dbc, Dictionary d) {
		boolean wasIndex = d.get(indexField) != null;
		boolean wasCreate = d.get("TS_create") != null;
		boolean wasModify = d.get("TS_create") != null;

		//    while(true)
		try {
			if (dbc instanceof DBConnectionWrapper)
				dbc = ((DBConnectionWrapper) dbc).getWrapped();

			PreparedStatement ps = (PreparedStatement) ((SQLDBConnection) dbc)
					.getPreparedStatement(preparedInsertString);

			int n = 0;
			for (Enumeration e = dd.getFieldNames().elements(); e
					.hasMoreElements();) {
				n++;
				String fieldName = (String) e.nextElement();
				if (getFieldDefinition(fieldName).getType().startsWith("set"))
					continue;
				try {
					setInsertArgument(fieldName, ps, n, d);
				} catch (Throwable ex) {
					//throw new DBError(ex, (getRecordInfo().getName())+"
					// "+(fm.getName())+" "+(d.get(fm.getName())));
					throw new org.makumba.DBError(ex,
							"insert into \""
									+ getDataDefinition().getName()
									+ "\" at field \""
									+ fieldName
									+ "\" could not assign value \""
									+ d.get(fieldName)
									+ "\" "
									+ (d.get(fieldName) != null ? ("of type \""
											+ d.get(fieldName).getClass()
													.getName() + "\"") : ""));

				}
			}
			if (getSQLDatabase().exec(ps) == -1)
				throw findDuplicates((SQLDBConnection) dbc, d);
			Pointer ret = (Pointer) d.get(indexField);
			if (!wasIndex)
				d.remove(indexField);
			if (!wasCreate)
				d.remove("TS_create");
			if (!wasModify)
				d.remove("TS_modify");
			return ret;
		}/*
		  * catch(ReconnectedException re) { prepareStatements(); continue; }
		  */
		//      catch(SQLException e) { throw new org.makumba.DBError (e); }
		catch (Throwable t) {
			if (!(t instanceof DBError))
				t = new org.makumba.DBError(t);
			throw (DBError) t;
		}
	}

	protected NotUniqueError findDuplicates(SQLDBConnection dbc, Dictionary d) {
		Dictionary duplicates = new Hashtable();
		for (Enumeration e = dd.getFieldNames().elements(); e.hasMoreElements();) {
			String fieldName = (String) e.nextElement();
			if (getFieldDefinition(fieldName).getType().startsWith("set"))
				continue;
			if (checkDuplicate(fieldName, dbc, d))
				duplicates.put(fieldName, d.get(fieldName));
		}
		return new NotUniqueError(getDataDefinition().getName(), duplicates);
	}

	protected String prepareDelete() {
		return "DELETE FROM " + tbname + " WHERE "
				+ inPreparedUpdate(indexField);
	}

	public void deleteRecord(DBConnection dbc, Pointer uid) {
		if (dbc instanceof DBConnectionWrapper)
			dbc = ((DBConnectionWrapper) dbc).getWrapped();

		PreparedStatement ps = (PreparedStatement) ((SQLDBConnection) dbc)
				.getPreparedStatement(preparedDeleteString);

		//    while(true)
		try {
			setUpdateArgument(getDBName(), ps, 1, uid);
			getSQLDatabase().exec(ps);
			//break;
		}//catch(ReconnectedException e) { continue; }
		catch (SQLException f) {
			org.makumba.db.sql.Database.logException(f);
			throw new DBError(f);
		}
	}

	public void updateRecord(DBConnection dbc, Pointer uid, Dictionary d) {
		if (dbc instanceof DBConnectionWrapper)
			dbc = ((DBConnectionWrapper) dbc).getWrapped();
		d.remove(indexField);
		d.remove("TS_create");

		//d.put("TS_modify", "");
		d.put("TS_modify", new java.util.Date());

		StringBuffer command = new StringBuffer("UPDATE ").append(tbname)
				.append(" SET ");

		String s = "";
		for (Enumeration e = d.keys(); e.hasMoreElements();) {
			if (s.length() > 0)
				command.append(",");
			String fieldName = (String) e.nextElement();
			String fieldDBName = getFieldDBName(fieldName);
			if (fieldDBName == null)
				throw new org.makumba.DBError(new Exception("no such field "
						+ fieldDBName + " in " + this.getDBName()));
			command.append(s = inPreparedUpdate(fieldName));
		}

		command.append(" WHERE " + inPreparedUpdate(indexField));

		//    while(true)
		try {
			PreparedStatement st = ((SQLDBConnection) dbc)
					.getPreparedStatement(command.toString());

			int n = 1;
			for (Enumeration e = d.keys(); e.hasMoreElements(); n++)
				setUpdateArgument((String) e.nextElement(), st, n, d);

			setUpdateArgument(getDBName(), st, n, uid);

			if (getSQLDatabase().exec(st) == -1)
				throw findDuplicates((SQLDBConnection) dbc, d);
			return;
		}//catch(ReconnectedException re) { continue; }
		catch (SQLException se) {
			throw new org.makumba.DBError(se);
		}
	}

	protected void fillResult(ResultSet rs, Dictionary p)
			throws java.sql.SQLException {
		int n = dd.getFieldNames().size();
		for (int i = 0; i < n;) {
			if (dd.getFieldDefinition(i).getType().startsWith("set"))
				continue;
			setValue(dd.getFieldDefinition(i).getName(), p, rs, ++i);
		}
	}

	protected void fillResult(ResultSet rs, Object[] data)
			throws java.sql.SQLException {
		int n = dd.getFieldNames().size();
		for (int i = 0; i < n; i++) {
			if (dd.getFieldDefinition(i).getType().startsWith("set"))
				continue;
			try {
				data[i] = getValue(dd.getFieldDefinition(i).getName(), rs,
						i + 1);
			} catch (ArrayIndexOutOfBoundsException e) {
				org.makumba.MakumbaSystem
						.getMakumbaLogger("db.query.execution").log(
								java.util.logging.Level.SEVERE,
								"" + i + " " + dd.getName()
										+ " " + keyIndex + " "
										+ dd.getFieldNames(), e);
				throw e;
			}
		}
	}

	public Object getValue(ResultSet rs, String fieldName, int i) {
		try {
			return getValue(fieldName, rs, i);
		} catch (SQLException e) {
			throw new org.makumba.DBError(e);
		}
	}

	//moved from FieldManager
	/**
	 * get the java value of the recordSet column corresponding to this field.
	 * This method should return null if the SQL field is null
	 */
	public Object getValue(String fieldName, ResultSet rs, int i)
			throws SQLException {
		if (!getFieldDefinition(fieldName).getType().startsWith("set")) {
			switch (getFieldDefinition(fieldName).getIntegerType()) {
			case FieldDefinition._ptr:
			case FieldDefinition._ptrRel:
			case FieldDefinition._ptrOne:
			case FieldDefinition._ptrIndex:
				return get_ptrDB_Value(fieldName, rs, i);
			case FieldDefinition._int:
			case FieldDefinition._intEnum:
				return get_int_Value(fieldName, rs, i);
			case FieldDefinition._char:
			case FieldDefinition._charEnum:
				return get_char_Value(fieldName, rs, i);
			case FieldDefinition._text:
				return get_text_Value(fieldName, rs, i);
			case FieldDefinition._date:
				return get_dateTime_Value(fieldName, rs, i);
			case FieldDefinition._dateCreate:
			case FieldDefinition._dateModify:
				return get_timeStamp_Value(fieldName, rs, i);
			case FieldDefinition._nil:
				return get_nil_Value(fieldName, rs, i);
			default:
				return base_getValue(fieldName, rs, i);
			}
		} else {
			throw new RuntimeException("shouldn't be here");
		}
	}

	//original getValue() from FieldManager
	public Object base_getValue(String fieldName, ResultSet rs, int i)
			throws SQLException {
		Object o = rs.getObject(i);
		if (rs.wasNull())
			return null;
		//	return getDefaultValue();
		return o;
	}

	//moved from ptrDBManager
	/** return the value as a Pointer */
	public Object get_ptrDB_Value(String fieldName, ResultSet rs, int i)
			throws SQLException {
		Object o = base_getValue(fieldName, rs, i);
		if (o == null)
			return o;
		return new SQLPointer(dd.getFieldDefinition(fieldName).getReferredType().getName(), ((Number) o).longValue());
	}

	//moved from intManager
	public Object get_int_Value(String fieldName, ResultSet rs, int i)
			throws SQLException {
		int n = rs.getInt(i);
		if (rs.wasNull())
			return null;
		return new Integer(n);
	}

	//moved from charManager
	/**
	 * get the java value of the recordSet column corresponding to this field.
	 * This method should return null if the SQL field is null
	 */
	public Object get_char_Value(String fieldName, ResultSet rs, int i)
			throws SQLException {
		Object o = base_getValue(fieldName, rs, i);
		if (o == null)
			return o;
		if (o instanceof byte[])
			return new String((byte[]) o);
		return o;
	}

	//moved from textManager
	/**
	 * get the java value of the recordSet column corresponding to this field.
	 * This method should return null if the SQL field is null
	 */
	public Object get_text_Value(String fieldName, ResultSet rs, int i)
			throws SQLException {
		Object o = base_getValue(fieldName, rs, i);
		if (o == null)
			return o;
		return Text.getText(o);

		/*
		 * InputStream is= rs.getBinaryStream(i); if(is==null ) return null;
		 * return new Text(is);
		 */
	}

	//moved from dateTimeManager
	/**
	 * get the java value of the recordSet column corresponding to this field.
	 * This method should return null if the SQL field is null
	 */
	public Object get_dateTime_Value(String fieldName, ResultSet rs, int i)
			throws SQLException {
		Object o = rs.getObject(i);
		if (rs.wasNull())
			return null;
		return o;
	}

	//moved from nilManager
	public Object get_nil_Value(String fieldName, ResultSet rs, int i) {
		return null;
	}

	//moved from timeStampManager
	public Object get_timeStamp_Value(String fieldName, ResultSet rs, int i)
			throws SQLException {
		Object o = rs.getTimestamp(i);
		if (rs.wasNull())
			return null;
		//  return getDefaultValue();
		//        if(o instanceof java.lang.BigDecimal)

		// System.out.println(o.getClass());
		return o;
	}

	//moved from FieldManager
	/**
	 * ask this field to write write its argumment value in a prepared UPDATE
	 * SQL statement
	 */
	public void setUpdateArgument(String fieldName, PreparedStatement ps,
			int n, Object o) throws SQLException {
		if (o == getFieldDefinition(fieldName).getEmptyValue())
			setNullArgument(fieldName, ps, n);
		else
			try {
				setArgument(fieldName, ps, n, o);
			} catch (SQLException e) {
				org.makumba.MakumbaSystem.getMakumbaLogger(
						"db.update.execution").log(
						java.util.logging.Level.SEVERE,
						getDBName() + "  " + o.getClass(), e);
				throw e;
			}
	}

	//moved from FieldManager
	/**
	 * ask this field to write write its argumment value in a prepared UPDATE
	 * SQL statement
	 */
	public void setUpdateArgument(String fieldName, PreparedStatement ps,
			int n, Dictionary d) throws SQLException {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._dateCreate:
		case FieldDefinition._ptrIndex:
			throw new RuntimeException("shouldn't be called"); //doesn't go to
		// return in this
		// case
		case FieldDefinition._dateModify:
			nxt(fieldName, d);
			break;
		}
		setUpdateArgument(fieldName, ps, n, d.get(fieldName));
	}

	//moved from FieldManager
	/** set a null argument of this type in a prepared SQL statement */
	public void setNullArgument(String fieldName, PreparedStatement ps, int n)
			throws SQLException {
		ps.setNull(n, getSQLType(fieldName));
	}

	//moved from FieldManager
	/** set a non-null argument of this type in a prepared SQL statement */
	public void setArgument(String fieldName, PreparedStatement ps, int n,
			Object o) throws SQLException {
		if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._text)
			set_text_Argument(fieldName, ps, n, o);
		else
			ps.setObject(n, toSQLObject(fieldName, o));
	}

	//moved from textManager
	public void set_text_Argument(String fieldName, PreparedStatement ps,
			int n, Object o) throws SQLException {
		Text t = Text.getText(o);
		ps.setBinaryStream(n, t.toBinaryStream(), t.length());
		//ps.setBytes(n, t.getBytes());
	}

	//moved from FieldManager
	/** what is the SQL type of this field? */
	protected int getSQLType(String fieldName) {

		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._ptr:
		case FieldDefinition._ptrRel:
		case FieldDefinition._ptrOne:
		case FieldDefinition._ptrIndex:
			return get_ptrDB_SQLType(fieldName);
		case FieldDefinition._int:
		case FieldDefinition._intEnum:
			return get_int_SQLType(fieldName);
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return get_char_SQLType(fieldName);
		case FieldDefinition._text:
			return get_text_SQLType(fieldName);
		case FieldDefinition._date:
			return get_dateTime_SQLType(fieldName);
		case FieldDefinition._real:
			return get_real_SQLType(fieldName);
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return get_timeStamp_SQLType(fieldName);
		default:
			throw new RuntimeException("" + fieldName + " should be redefined");
		}
	}

	//moved from ptrDBManager
	public int get_ptrDB_SQLType(String fieldName) {
		return Types.INTEGER;
	}

	//moved from intManager
	protected int get_int_SQLType(String fieldName) {
		return java.sql.Types.INTEGER;
	}

	//moved from charManager
	protected int get_char_SQLType(String fieldName) {
		return java.sql.Types.VARCHAR;
	}

	//moved from textManager
	protected int get_text_SQLType(String fieldName) {
		return java.sql.Types.LONGVARBINARY;
	}

	//moved from dateTimeManager
	public int get_dateTime_SQLType(String fieldName) {
		return java.sql.Types.TIMESTAMP;
	}

	//moved from realManager
	protected int get_real_SQLType(String fieldName) {
		return java.sql.Types.DOUBLE;
	}

	//moved from timeStampManager
	public int get_timeStamp_SQLType(String fieldName) {
		return java.sql.Types.TIMESTAMP;
	}

	//moved from FieldManager
	/** transform the object for a SQL insert or update */
	public Object toSQLObject(String fieldName, Object o) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._ptr:
		case FieldDefinition._ptrRel:
		case FieldDefinition._ptrOne:
		case FieldDefinition._ptrIndex:
			return toSQL_ptrDB_Object(fieldName, o);
		case FieldDefinition._date:
			return toSQL_dateTime_Object(fieldName, o);
		default:
			return o;

		}
	}

	//original toSQLObject() from FieldManager
	public Object base_toSQLObject(String fieldName, Object o) {
		return o;
	}

	//moved from ptrDBManager
	/** ask this field to write a value of its type in a SQL statement */
	public Object toSQL_ptrDB_Object(String fieldName, Object o) {
		return new Integer((int) ((Pointer) o).longValue());
	}

	//moved from dateTimeManager
	public Object toSQL_dateTime_Object(String fieldName, Object o) {
		return new Timestamp(((java.util.Date) o).getTime());
	}

	//Moved from FieldManager
	/**
	 * sets the database-level name of this field, normally identical with its
	 * abstract-level name, unless the database has some restrictions, or the
	 * configuration indicates that the field exists in the table with another
	 * name
	 */
	public void setFieldDBName(String fieldName, Properties config) {
		String dbname1 = null;
		dbname1 = config.getProperty(this.getDBName() + "#"
				+ getFieldDBName(fieldName));
		if (dbname1 == null) {
			dbname1 = this.getSQLDatabase().getFieldName(fieldName);
			while (checkTableDBName(dbname1))
				dbname1 = dbname1 + "_";
		}
		fieldDBNames.put(fieldName, dbname1);
	}

	//moved from FieldManager method getDBName()
	/** the database-level name of the field */
	public String getFieldDBName(String fieldName) {
		return (String) fieldDBNames.get(fieldName);
	}

	//moved from FieldManager
	/** ask this field to write its contribution in a SQL CREATE statement */
	public String inCreate(String fieldName, Database d) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return in_char_Create(fieldName, d);
		default:
			return base_inCreate(fieldName, d);
		}
	}

	//original inCreate() from FieldManager
	public String base_inCreate(String fieldName, Database d) {
		return getFieldDBName(fieldName) + " "
				+ this.getFieldDBType(fieldName, d);
	}

	//moved from charManager
	/** write in CREATE, in the form name char[size] */
	public String in_char_Create(String fieldName, Database d) {
		String s = Database.getEngineProperty(d.getEngine() + "."
				+ "charBinary");
		if (s != null && s.equals("true"))
			s = " BINARY";
		else
			s = "";
		//should width be computed by getDBType() instead?
		return getFieldDBName(fieldName) + " " + getFieldDBType(fieldName, d)
				+ "(" + getFieldDefinition(fieldName).getWidth() + ")" + s;
		//return
		// super.inCreate(d)+"("+getFieldDefinition(fieldName).getWidth()()+")"+s;
	}

	//moved from FieldManager
	/**
	 * ask this field to write its argument placeholder in a prepared UPDATE SQL
	 * statement
	 */
	public String inPreparedUpdate(String fieldName) {
		return getFieldDBName(fieldName) + "=?";
	}

	//moved from FieldManager
	/** what is the database level type of this field? */
	protected String getFieldDBType(String fieldName) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._ptr:
		case FieldDefinition._ptrRel:
		case FieldDefinition._ptrOne:
		case FieldDefinition._ptrIndex:
			return get_ptrDB_FieldDBType(fieldName);
		case FieldDefinition._int:
		case FieldDefinition._intEnum:
			return get_int_FieldDBType(fieldName);
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return get_char_FieldDBType(fieldName);
		case FieldDefinition._text:
			return get_text_FieldDBType(fieldName);
		case FieldDefinition._date:
			return get_dateTime_FieldDBType(fieldName);
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return get_timeStamp_FieldDBType(fieldName);
		case FieldDefinition._real:
			return get_real_FieldDBType(fieldName);
		default:
			throw new RuntimeException("" + fieldName + " should be redefined");
		}
	}

	//moved from ptrDBManager
	/** returns INT */
	protected String get_ptrDB_FieldDBType(String fieldName) {
		return "INTEGER";
	}

	//moved from intManager
	/** Use standard SQL name, unless defined otherwise in sqlEngines.properties. */
	protected String get_int_FieldDBType(String fieldName) {
		return "INTEGER"; //standard name
	}

	//moved from charManager
	/** returns char */
	protected String get_char_FieldDBType(String fieldName) {
		return "VARCHAR";
	}

	//moved from textManager
	/** returns text */
	protected String get_text_FieldDBType(String fieldName) {
		return "LONG VARBINARY";
	}

	//moved from dateTimeManager
	/** returns datetime */
	protected String get_dateTime_FieldDBType(String fieldName) {
		return "DATETIME";
	}

	//moved from realManager
	/** Use standard SQL name, unless defined otherwise in sqlEngines.properties. */
	protected String get_real_FieldDBType(String fieldName) {
		return "DOUBLE PRECISION"; //standard name
	}

	//moved from timeStampManager
	/** returns timestamp */
	protected String get_timeStamp_FieldDBType(String fieldName) {
		return "TIMESTAMP";
	}

	//moved from FieldManager
	/** what is the database level type of this field? */
	protected String getFieldDBType(String fieldName, Database d) {
		String s = Database.getEngineProperty(d.getEngine() + "."
				+ getFieldDefinition(fieldName).getDataType());
		if (s == null)
			return getFieldDBType(fieldName);
		return s;
	}

	//moved from FieldManager
	/**
	 * Ask this field how to name the index on this field. Normally called from
	 * manageIndexes().
	 */
	public String getFieldDBIndexName(String fieldName) {
		//return rm.getDBName()+"_"+getDBName();
		return getFieldDBName(fieldName);
	}

	//moved from FieldManager
	/**
	 * ask this field to write write its argument placeholder ('?') in a
	 * prepared INSERT SQL statement
	 */
	public String inPreparedInsert(String fieldName) {
		return "?";
	}

	//moved from FieldManager, adapted to dateCreateJavaManager,
	// dateModifyJavaManager and ptrIndexJavaManager
	/**
	 * ask this field to write write its argumment value in a prepared INSERT
	 * SQL statement
	 */
	public void setInsertArgument(String fieldName, PreparedStatement ps,
			int n, Dictionary d) throws SQLException {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			if (d.get(fieldName) == null)
				nxt(fieldName, d);
			set_timeStamp_InsertArgument(fieldName, ps, n, d);
			break;
		case FieldDefinition._ptrIndex:
			org.makumba.Pointer p = (org.makumba.Pointer) d.get(fieldName);
			if (p != null) {
				base_setInsertArgument(fieldName, ps, n, d);
				if (p.getDbsv() == dbsv
						&& p.longValue() > this.primaryKeyCurrentIndex)
					this.primaryKeyCurrentIndex = p.longValue();
				return;
			}
			ps.setInt(n, (int) nxt_ptrIndex(fieldName, d).longValue());
		default:
			base_setInsertArgument(fieldName, ps, n, d);
		}
	}

	//original setInsertArgument from FieldManager
	public void base_setInsertArgument(String fieldName, PreparedStatement ps,
			int n, Dictionary d) throws SQLException {
		Object o = d.get(fieldName);
		if (o == null
				|| o.equals(getFieldDefinition(fieldName).getEmptyValue()))
			setNullArgument(fieldName, ps, n);
		else
			setArgument(fieldName, ps, n, o);
	}

	//moved from timeStampManager
	public void set_timeStamp_InsertArgument(String fieldName,
			PreparedStatement ps, int n, java.util.Dictionary d)
			throws SQLException {
		Object o = d.get(fieldName);
		if (o instanceof java.util.Date && !(o instanceof Timestamp))
			d.put(fieldName, new Timestamp(((java.util.Date) o).getTime()));
		base_setInsertArgument(fieldName, ps, n, d);
	}

	//moved from FieldManager
	/**
	 * ask this field to write write its argumment value in a prepared SQL
	 * statement for copying
	 */
	public void setCopyArgument(String fieldName, PreparedStatement ps, int n,
			Dictionary d) throws SQLException {
		try {
			Object o = d.get(fieldName);
			if (o == null
					|| o.equals(getFieldDefinition(fieldName).getEmptyValue()))
				setNullArgument(fieldName, ps, n);
			else
				setArgument(fieldName, ps, n, o);
		} catch (Exception e) {
			throw new RuntimeException(fieldName + " " + e.getMessage());
		}
	}

	//moved from FieldManager, adapted to dateCreateJavaManager,
	// dateModifyJavaManager and ptrIndexJavaManager
	/** OLDCODE ask this field to write its value in a SQL INSERT statement */
	public String inInsert(String fieldName, Dictionary d) {
		try {
			switch (getFieldDefinition(fieldName).getIntegerType()) {
			case FieldDefinition._dateCreate:
			case FieldDefinition._dateModify:
				if (d.get(fieldName) == null)
					nxt(fieldName, d);
				break;
			case FieldDefinition._ptrIndex:
				org.makumba.Pointer p = (org.makumba.Pointer) d.get(fieldName);
				if (p == null)
					return "" + nxt_ptrIndex(fieldName, d).longValue();
				else if (p.getDbsv() == dbsv
						&& p.longValue() > this.primaryKeyCurrentIndex) {
					this.primaryKeyCurrentIndex = p.longValue();
				}
			}
			return writeConstant(fieldName, d.get(fieldName));
		} catch (NullPointerException e) {
			return "null";
		}
	}

	//moved from FieldManager
	/** OLDCODE ask this field to write its value in a SQL INSERT statement */
	public String inCopy(String fieldName, Dictionary d) {
		return inInsert(fieldName, d);
	}

	//moved from FieldManager and adapted to dateCreateJavaManager and
	// dateModifyJavaManager
	/**
	 * OLDCODE ask this field to write its contribution in a SQL UPDATE
	 * statement should return "" if this field doesn't want to take part in the
	 * update
	 */
	public String inUpdate(String fieldName, Dictionary d) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._dateCreate:
		case FieldDefinition._ptrIndex:
			throw new RuntimeException("shouldn't be called");
		case FieldDefinition._dateModify:
			nxt(fieldName, d);
			break;
		}
		return getDBName() + "=" + writeConstant(fieldName, d.get(fieldName));
	}

	//moved from FieldManager
	/**
	 * ask this field to write its contribution in a SQL UPDATE statement should
	 * return "" if this field doesn't want to take part in the update
	 */
	public String inCondition(String fieldName, Dictionary d, String cond) {
		return getDBName() + cond + writeConstant(fieldName, d.get(fieldName));
	}

	//moved from FieldManager
	/** ask this field to write its contribution in a SQL CREATE statement */
	//  public String inCreate(){ return getDBName()+" "+getDBType(null);}
	//moved from FieldManager
	/** ask this field to write a value of its type in a SQL statement */
	public String writeConstant(String fieldName, Object o) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return write_char_Constant(fieldName, o);
		case FieldDefinition._text:
			return write_text_Constant(fieldName, o);
		case FieldDefinition._date:
			return write_dateTime_Constant(fieldName, o);
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return write_timeStamp_Constant(fieldName, o);
		default:
			if (o == getFieldDefinition(fieldName).getEmptyValue())
				return "null";
			return toSQLObject(fieldName, o).toString();
		}
	}

	//original writeConstant from FieldManager
	public String base_writeConstant(String fieldName, Object o) {
		if (o == getFieldDefinition(fieldName).getEmptyValue())
			return "null";
		return toSQLObject(fieldName, o).toString();
	}

	//moved from charHandler
	/** does apostrophe escape */
	public String write_char_Constant(String fieldName, Object o) {
		return org.makumba.db.sql.Database.SQLEscape(o.toString());
	}

	//moved from textManager
	/** does apostrophe escape */
	public String write_text_Constant(String fieldName, Object o) {
		return org.makumba.db.sql.Database.SQLEscape(o.toString());
	}

	//moved from dateTimeManager
	/** writes the date between apostrophes */
	public String write_dateTime_Constant(String fieldName, Object o) {
		return "\'" + new Timestamp(((java.util.Date) o).getTime()) + "\'";
		//"\'"+super.writeConstant(o)+"\'";
	}

	//moved from timeStampManager
	/** writes the date between apostrophes */
	public String write_timeStamp_Constant(String fieldName, Object o) {
		return "\'" + base_writeConstant(fieldName, o) + "\'";
	}

	//moved from FieldManager
	/** what is the property of the current engine? */
	protected String getEngineProperty(String fieldName, String s) {
		Database d = getSQLDatabase();
		return Database.getEngineProperty(d.getEngine() + "." + s);
	}

	//moved from FieldManager
	/** ask this field to perform actions when the table is open */
	public void onStartup(String fieldName, Properties config,
			SQLDBConnection dbc) throws SQLException {
		if (alter && shouldIndex(fieldName))
			manageIndexes(fieldName, dbc);

		if (shouldIndex(fieldName))
			extraIndexes.remove(getFieldDBIndexName(fieldName).toLowerCase());

		checkDuplicate = "SELECT 1 FROM " + getDBName() + " WHERE "
				+ getFieldDBName(fieldName) + "=?";
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._ptrIndex:
			dbsv = getSQLDatabase().getDbsv();
			Statement st = dbc.createStatement();
			resetPrimaryKey();
			ResultSet rs = st.executeQuery("SELECT MAX("
					+ getFieldDBName(fieldName) + "), COUNT("
					+ getFieldDBName(fieldName) + ") FROM " + tbname
					+ " WHERE " + getFieldDBName(fieldName) + ">="
					+ primaryKeyCurrentIndex + " AND "
					+ getFieldDBName(fieldName) + "<="
					+ getSQLDatabase().getMaxPointerValue());
			rs.next();
			if (rs.getLong(2) > 0)
				primaryKeyCurrentIndex = rs.getLong(1);
			rs.close();
			st.close();
		}
	}

	/**
	 * called at table open. determines the maximum index with this database's
	 * dbsv public void onStartup(String fieldName, TableManager rm,
	 * java.util.Properties p, SQLDBConnection dbc) throws SQLException {
	 * super.onStartup(fieldName, p, dbc);
	 *  }
	 */

	//moved from FieldManager
	/** Tell whether this type of field should be indexed. */
	public boolean shouldIndex(String fieldName) {
		if (getFieldDefinition(fieldName).getIntegerType() == FieldDefinition._text)
			return should_text_Index(fieldName);
		else
			return true;
	}

	//moved from textManager
	public boolean should_text_Index(String fieldName) {
		return false;
	}

	//moved from FieldManager
	/** Examine DB indexes. */
	public boolean isIndexOk(String fieldName, SQLDBConnection dbc) {
		Boolean b = (Boolean) indexes.get(getFieldDBIndexName(fieldName)
				.toLowerCase());
		if (b != null)
			return (getFieldDefinition(fieldName).isUnique() == !b
					.booleanValue());
		return false;
	} //end isIndexOk()

	//moved from FieldManager
	/**
	 * Ask this field to add/remove indexes as needed, normally called from
	 * onStartup().
	 */
	public void manageIndexes(String fieldName, SQLDBConnection dbc)
			throws SQLException {
		String keyName = getFieldDBIndexName(fieldName);
		String brief = getDataDefinition().getName() + "#" + fieldName + " ("
				+ getFieldDefinition(fieldName).getDescription() + ")";

		if (!isIndexOk(fieldName, dbc)) {
			//org.makumba.MakumbaSystem.getMakumbaLogger("db.init.tablechecking").info(
			//	"ALTERING INDEX on field "+getName()+" of
			// "+rm.getRecordInfo().getName() );

			try { //drop the old, wrong index if it exists
				Statement st = dbc.createStatement();
				st.executeUpdate(indexDropSyntax(fieldName));
				org.makumba.MakumbaSystem.getMakumbaLogger(
						"db.init.tablechecking").info(
						"INDEX DROPPED on " + brief);
				st.close();

			} catch (SQLException e) {
			}

			boolean createNormalEvenIfUnique = false;

			if (getFieldDefinition(fieldName).isUnique()) {
				try {
					//try creating unique index
					Statement st = dbc.createStatement();
					st.executeUpdate(indexCreateUniqueSyntax(fieldName));
					org.makumba.MakumbaSystem.getMakumbaLogger(
							"db.init.tablechecking").info(
							"UNIQUE INDEX ADDED on " + brief);
					st.close();
					indexCreated(dbc);
				} catch (SQLException e) {
					//log all errors
					org.makumba.MakumbaSystem.getMakumbaLogger(
							"db.init.tablechecking").warning(
							//rm.getDatabase().getConfiguration()+": "+ //DB
							// name
							"Problem adding UNIQUE INDEX on " + brief + ": "
									+ e.getMessage() + " [ErrorCode: "
									+ e.getErrorCode() + ", SQLstate:"
									+ e.getSQLState() + "]");
					createNormalEvenIfUnique = true;
				}
			}

			if (createNormalEvenIfUnique
					|| !getFieldDefinition(fieldName).isUnique()) {
				try {
					//create normal index
					Statement st = dbc.createStatement();
					st.executeUpdate(indexCreateSyntax(fieldName));
					org.makumba.MakumbaSystem.getMakumbaLogger(
							"db.init.tablechecking").info(
							"INDEX ADDED on " + brief);
					st.close();
					indexCreated(dbc);
				} catch (SQLException e) {
					org.makumba.MakumbaSystem.getMakumbaLogger(
							"db.init.tablechecking").warning(
							//rm.getDatabase().getConfiguration()+": "+ //DB
							// name
							"Problem adding INDEX on " + brief + ": "
									+ e.getMessage() + " [ErrorCode: "
									+ e.getErrorCode() + ", SQLstate:"
									+ e.getSQLState() + "]");
				}
			}

		}//isIndexOk

	}//method

	//moved from FieldManager
	/** Syntax for index creation. */
	public String indexCreateSyntax(String fieldName) {
		return "CREATE INDEX " + getFieldDBIndexName(fieldName) + " ON "
				+ getDBName() + " (" + getFieldDBName(fieldName) + ")";
	}

	//moved from FieldManager
	/** Syntax for unique index creation. */
	public String indexCreateUniqueSyntax(String fieldName) {
		return "CREATE UNIQUE INDEX " + getFieldDBIndexName(fieldName) + " ON "
				+ getDBName() + " (" + getFieldDBName(fieldName) + ")";
	}

	//moved from FieldManager
	/** Syntax for dropping index. */
	public String indexDropSyntax(String fieldName) {
		return "DROP INDEX " + getFieldDBIndexName(fieldName) + " ON "
				+ getDBName();
	}

	//moved from FieldManager
	/**
	 * set the java value in a data chunk. If the value in the recordset is SQL
	 * null, a NullPointerException is thrown
	 */
	public void setValue(String fieldName, Dictionary d, ResultSet rs, int i)
			throws SQLException {
		Object o = getValue("", rs, i);
		if (o != null)
			d.put(fieldName, o);
		else
			d.remove(fieldName);
	}

	//moved from FieldManager
	/**
	 * set the java value in a data chunk. If the value in the recordset is SQL
	 * null, a NullPointerException is thrown
	 */
	public void setValue(String fieldName, Object[] data, ResultSet rs, int i)
			throws SQLException {
		data[i] = getValue("", rs, i);
	}

	//moved from FieldManager
	protected void checkCopy(String fieldName, String s) {
		if (!admin)
			throw new org.makumba.InvalidValueException(
					getFieldDefinition(fieldName),
					"you cannot insert an "
							+ s
							+ " field unless the type "
							+ getDataDefinition().getName()
							+ " has administration approval in the database connection file");
	}

	//moved from FieldManager
	/**
	 * return whether there was a duplicate for this field when inserting the
	 * given data
	 */
	public boolean checkDuplicate(String fieldName, SQLDBConnection dbc,
			Dictionary data) {
		if (!getFieldDefinition(fieldName).isUnique())
			return false;
		Object val = data.get(fieldName);
		if (val == null) // FIXME: not sure about null duplicates
			return false;
		PreparedStatement ps = dbc.getPreparedStatement(checkDuplicate);
		try {
			setUpdateArgument(fieldName, ps, 1, val);
			return ps.executeQuery().next();
		} catch (SQLException se) {
			Database.logException(se, dbc);
			throw new org.makumba.DBError(se, checkDuplicate);
		}

	}


	//moved from FieldManager
	/**
	 * check if the column from the SQL database (read from the catalog) still
	 * coresponds with the abstract definition of this field
	 */
	protected boolean unmodified(String fieldName, int type, int size,
			Vector columns, int index) throws SQLException {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return unmodified_char(fieldName, type, size, columns, index);
		default:
			return base_unmodified(fieldName, type, size, columns, index);
		}
	}

	//original unmodified() from FieldManager
	protected boolean base_unmodified(String fieldName, int type, int size,
			Vector columns, int index) throws SQLException {
		return type == getSQLType(fieldName);
	}

	//moved from charManager
	/**
	 * Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL
	 * column is still large enough, this returns true. Some SQL drivers
	 * allocate more anyway.
	 */
	protected boolean unmodified_char(String fieldName, int type, int size,
			java.util.Vector columns, int index) throws SQLException {
		return (base_unmodified(fieldName, type, size, columns, index) || type == java.sql.Types.CHAR)
				&& check_char_Width(fieldName, size);
	}

	//moved from wrapperManager
	/**
	 * check if the column from the SQL database still coresponds with the
	 * abstract definition of this field
	 */
	protected boolean unmodified_wrapper(String fieldName, int type, int size,
			java.util.Vector v, int index) throws SQLException {
		return base_unmodified(fieldName, type, size, v, index);
	}

	//moved from charManager
	/** check the char width */
	protected boolean check_char_Width(String fieldName, ResultSetMetaData rsm,
			int index) throws SQLException {
		// some drivers might allocate more, it's their business
		return rsm.getColumnDisplaySize(index) >= getFieldDefinition(fieldName)
				.getWidth();
	}

	//moved from charManager
	/** check the char width */
	protected boolean check_char_Width(String fieldName, int width)
			throws SQLException {
		// some drivers might allocate more, it's their business
		return width >= getFieldDefinition(fieldName).getWidth();
	}

	//moved from ptrIndexJavaManager
	protected void resetPrimaryKey() {
		primaryKeyCurrentIndex = getSQLDatabase().getMinPointerValue();
	}

	//	moved from dateCreateJavaManager and dateModifyJavaManager
	void nxt(String fieldName, Dictionary d) {
		switch (getFieldDefinition(fieldName).getIntegerType()) {
		case FieldDefinition._dateCreate:
			d.put(fieldName, d.get(dd.getLastModificationDateFieldName()));
			break;
		case FieldDefinition._dateModify:
			d.put(fieldName, new Timestamp(new java.util.Date().getTime()));
			break;
		}
	}

	//moved from ptrIndexJavaManager
	public SQLPointer nxt_ptrIndex(String fieldName, Dictionary d) {
		SQLPointer i = new SQLPointer(dd.getName(), nextId_ptrIndex());
		d.put(fieldName, i);
		return i;
	}

	//moved from ptrIndexJavaManager
	/** determines the unique index by incrementing a counter */
	protected synchronized long nextId_ptrIndex() {
		return ++primaryKeyCurrentIndex;
	}

	//	Moved from dateCreateJavaManager, dateModifyJavaManager and
	// ptrIndexJavaManager
	public void checkInsert(String fieldName, Dictionary d) {
		Object o = d.get(fieldName);
		if (o != null) {
			switch (getFieldDefinition(fieldName).getIntegerType()) {
			case FieldDefinition._dateCreate:
				checkCopy(fieldName, "creation date");
				break;
			case FieldDefinition._dateModify:
				checkCopy(fieldName, "modification date");
				break;
			case FieldDefinition._ptrIndex:
				checkCopy(fieldName, "index");
			default:
				base_checkInsert(fieldName, d);
				return;
			}
			d.put(fieldName, getFieldDefinition(fieldName).checkValue(o));
		}
	}
	
	public void base_checkInsert(String fieldName, Dictionary d) {
		getFieldDefinition(fieldName).checkInsert(d);
	}
	
//	moved from RecordHandler
	  public void checkInsert(Dictionary d, Dictionary except)
	  {
	  	dd.checkFieldNames(d);
	  	for(Enumeration e= dd.getFieldNames().elements(); e.hasMoreElements(); )
	  	{
	  		String name = (String)e.nextElement();
	  		if(except.get(name)==null){
	  			checkInsert(name, d);
	  		}
	  	}
	  }
	
	//	moved from dateCreateJavaManager, dateModifyJavaManager and
	// ptrIndexJavaManager
	public void checkUpdate(String fieldName, Dictionary d) {
		Object o = d.get(fieldName);
		if (o != null)
			switch (getFieldDefinition(fieldName).getIntegerType()) {
			case FieldDefinition._dateCreate:
				throw new org.makumba.InvalidValueException(
						getFieldDefinition(fieldName),
						"you cannot update a creation date");
			case FieldDefinition._dateModify:
				throw new org.makumba.InvalidValueException(
						getFieldDefinition(fieldName),
						"you cannot update a modification date");
			case FieldDefinition._ptrIndex:
				throw new org.makumba.InvalidValueException(
						getFieldDefinition(fieldName),
						"you cannot update an index pointer");
			default:
				base_checkUpdate(fieldName, d);
			}
	}
	
	public void base_checkUpdate(String fieldName, Dictionary d) {
		getFieldDefinition(fieldName).checkUpdate(d);
	}

//	moved from RecordHandler
	  public void checkUpdate(Dictionary d, Dictionary except)
	  {
	  	dd.checkFieldNames(d);
	  	for(Enumeration e= dd.getFieldNames().elements(); e.hasMoreElements(); )
	  	{
	  		String name = (String)e.nextElement();
	  		if(except.get(name)==null){
	  			checkUpdate(name, d);
	  		}
	  	}
	  }

	//	moved from timeStampManager
	public Object check_timeStamp_ValueImpl(String fieldName, Object value) {
		Object o = getFieldDefinition(fieldName).checkValueImpl(value);
		if (o instanceof java.util.Date && !(o instanceof Timestamp))
			o = new Timestamp(((java.util.Date) o).getTime());
		return o;
	}

}
