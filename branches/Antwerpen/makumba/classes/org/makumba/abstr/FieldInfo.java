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

//TODO extra comments about changes from refactoring

package org.makumba.abstr;

import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.Text;

/**
 * This is a structure containing the elementary data about a field: name, type,
 * attributes, description, and other type-specific extra info. All this
 * information is available through the associated <a
 * href=org.makumba.abstr.FieldHandler.html#_top_>FieldHandler </a>
 */
public class FieldInfo implements java.io.Serializable, FieldDefinition {
	DataDefinition dd;

	static final HashMap integerTypeMap = new HashMap();

	public DataDefinition getDataDefinition() {
		return dd;
	}

	//TODO adapt setIntEnum and setCharEnum in FieldDefinition
	public static FieldInfo getFieldInfo(String name, Object type,
			boolean typeSearch) {
		if (type instanceof FieldInfo)
			return new FieldInfo(name, (FieldInfo) type);
		String t = ((String) type).trim();

		if (!typeSearch || t.indexOf(" ") == -1)
			return new FieldInfo(name, t);

		t = name + "=" + t;

		return (FieldInfo) new RecordParser().parse(t).getFieldDefinition(name);
	}

	public FieldInfo(DataDefinition ri, String name) {
		this.dd = ri;
		this.name = name;
	}

	public FieldInfo(FieldInfo fi) {
		this(fi.dd, fi.name);
		type = fi.type;
		fixed = fi.fixed;
		notNull = fi.notNull;
		unique = fi.unique;
		defaultValue = fi.defaultValue;
		description = fi.description;
	}

	/** for temporary field info */
	public FieldInfo(String name, FieldInfo fi) {
		this.name = name;
		type = fi.type;
		fixed = fi.fixed;
		notNull = fi.notNull;
		unique = fi.unique;
		defaultValue = fi.defaultValue;
		description = fi.description;
		extra1 = fi.extra1;
		extra2 = fi.extra2;
		extra3 = fi.extra3;
		if (type.equals("ptrIndex")) {
			type = "ptr";
			extra1 = fi.getDataDefinition();
		}
	}

	public FieldInfo(String name, String t) {
		try {
			this.name = name;
			this.type = t;
			fixed = false;
			notNull = false;
			unique = false;
			if (type.equals("char"))
				extra2 = new Integer(255);
			else if (type.startsWith("char")) {
				int n = type.indexOf("[");
				int m = type.indexOf("]");
				if (!type.endsWith("]")
						|| type.substring(3, n).trim().length() > 1)
					throw new InvalidValueException("invalid char type " + type);

				extra2 = new Integer(Integer.parseInt(type.substring(n + 1, m)));
				type = "char";
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new InvalidValueException("bad type " + type);
		} catch (NumberFormatException f) {
			throw new InvalidValueException("bad char[] size " + type);
		}
	}

	static {
		integerTypeMap.put("ptr", new Integer(FieldDefinition._ptr));
		integerTypeMap.put("ptrRel", new Integer(FieldDefinition._ptrRel));
		integerTypeMap.put("ptrOne", new Integer(FieldDefinition._ptrOne));
		integerTypeMap.put("ptrIndex", new Integer(FieldDefinition._ptrIndex));
		integerTypeMap.put("int", new Integer(FieldDefinition._int));
		integerTypeMap.put("intEnum", new Integer(FieldDefinition._intEnum));
		integerTypeMap.put("char", new Integer(FieldDefinition._char));
		integerTypeMap.put("charEnum", new Integer(FieldDefinition._charEnum));
		integerTypeMap.put("text", new Integer(FieldDefinition._text));
		integerTypeMap.put("date", new Integer(FieldDefinition._date));
		integerTypeMap.put("dateCreate", new Integer(
				FieldDefinition._dateCreate));
		integerTypeMap.put("dateModify", new Integer(
				FieldDefinition._dateModify));
		integerTypeMap.put("set", new Integer(FieldDefinition._set));
		integerTypeMap.put("setComplex", new Integer(
				FieldDefinition._setComplex));
		integerTypeMap.put("nil", new Integer(FieldDefinition._nil));
		integerTypeMap.put("real", new Integer(FieldDefinition._real));
		integerTypeMap.put("setcharEnum", new Integer(
				FieldDefinition._setCharEnum));
		integerTypeMap.put("setintEnum", new Integer(
				FieldDefinition._setIntEnum));
	}

	public boolean isAssignableFrom(FieldDefinition fi) {
		switch (fi.getIntegerType()) {
		case FieldDefinition._int:
			return is_int_AssignableFrom(fi);
		case FieldDefinition._intEnum:
			return is_intEnum_AssignableFrom(fi);
		case FieldDefinition._ptr:
		case FieldDefinition._ptrRel:
			return is_ptrRel_AssignableFrom(fi);
		case FieldDefinition._real:
			return is_real_AssignableFrom(fi);
		case FieldDefinition._set:
			return is_set_AssignableFrom(fi);
		default:
			return base_isAssignableFrom(fi);
		}
	}

	//Original from FieldHandler
	public boolean base_isAssignableFrom(FieldDefinition fi) {
		return fi.getType().equals("nil") || getType().equals(fi.getType());
	}

	//moved from intHandler
	public boolean is_int_AssignableFrom(FieldDefinition fi) {
		return base_isAssignableFrom(fi) || fi.getType().equals("intEnum");
	}

	//moved from IntEnumHandler
	public boolean is_intEnum_AssignableFrom(FieldDefinition fi) {
		return is_int_AssignableFrom(fi) || fi.getType().equals("int")
				|| fi.getType().equals("char");
	}

	//moved from ptrRelHandler
	public boolean is_ptrRel_AssignableFrom(FieldDefinition fi) {
		return "nil".equals(fi.getType())
				|| getType().equals(fi.getType())
				&& ((FieldInfo) fi).extra1 instanceof DataDefinition
				&& ((DataDefinition) ((FieldInfo) fi).extra1).getName().equals(
						getForeignTable().getName());
	}

	//moved from realHandler
	public boolean is_real_AssignableFrom(FieldDefinition fi) {
		return base_isAssignableFrom(fi) || fi.getType().equals("intEnum")
				|| fi.getType().equals("int");
	}

	//moved from setHandler
	public boolean is_set_AssignableFrom(FieldDefinition fi) {
		return "nil".equals(fi.getType())
				|| getType().equals(fi.getType())
				&& getForeignTable().getName().equals(
						fi.getForeignTable().getName());
	}

	public String toString() {
		return getType();
	}

	String name;

	String type;

	boolean fixed;

	boolean notNull;

	boolean unique;

	Object defaultValue;

	String description;

	// those fields are only used by some types
	Object extra1, extra2, extra3;

	/** check if the value can be assigned */
	public Object checkValue(Object value) {
		switch (getIntegerType()) {
		case FieldDefinition._setIntEnum:
			return check_setintEnum_Value(value);
		default:
			return base_checkValue(value);
		}
	}

	//Original from FieldHandler
	/** check if the value can be assigned */
	public Object base_checkValue(Object value) {
		if (!value.equals(getNull()))
			return checkValueImpl(value);
		return value;
	}

	//moved from setintEnumHandler
	public Object check_setintEnum_Value(Object value) {
		try {
			// may be just an Integer
			Object o = getEnum().checkValue(value);
			Vector v = new Vector();
			if (o != null && o instanceof Integer)
				v.addElement(o);
			return v;
		} catch (org.makumba.InvalidValueException ive) {
		}

		normalCheck(value);
		Vector v = (Vector) value;

		for (int i = 0; i < v.size(); i++) {
			if (v.elementAt(i) == null
					|| v.elementAt(i).equals(org.makumba.Pointer.NullInteger))
				throw new org.makumba.InvalidValueException(this,
						"set members cannot be null");
			v.setElementAt(getEnum().checkValue(v.elementAt(i)), i);
		}
		return v;
	}

	/** check if the value can be assigned */
	public void checkInsert(Dictionary d) {
		Object o = d.get(getName());
		if (isNotNull() && (o == null || o.equals(getNull())))
			throw new org.makumba.InvalidValueException(this,
					"A non-null value is needed for notnull fields");
		if (o != null)
			d.put(getName(), checkValue(o));
	}

	/** check if the value can be assigned */
	public void checkUpdate(Dictionary d) {
		Object o = d.get(getName());
		if (o == null)
			return;
		if (isFixed())
			throw new org.makumba.InvalidValueException(this,
					"You cannot update a fixed field");
		d.put(getName(), checkValue(o));
	}

	/**
	 * Get deprecated values of the enumerator, works only for intEnum type.
	 * 
	 * @return <code>Vector</code>, or <code>null</code> if called on other
	 *         types
	 */
	public Vector getDeprecatedValues() {
		switch (getIntegerType()) {
		case FieldDefinition._intEnum:
			return get_intEnum_DeprecatedValues();
		default:
			return null;
		}
	}

	//moved from intEnumHandler
	public Vector get_intEnum_DeprecatedValues() {
		return (Vector) this.extra3;
	}

	/**
	 * the value returned in case there is no value in the database and no
	 * default value is indicated
	 */
	public Object getEmptyValue() {
		switch (getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
		case FieldDefinition._text:
			return "";
		case FieldDefinition._date:
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return emptyDate;
		case FieldDefinition._int:
		case FieldDefinition._intEnum:
			return emptyInt;
		case FieldDefinition._real:
			return emptyReal;
		default:
			return null;
		}
	}

	public Object getNull() {

		switch (getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return Pointer.NullString;
		case FieldDefinition._date:
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return Pointer.NullDate;
		case FieldDefinition._int:
		case FieldDefinition._intEnum:
			return Pointer.NullInteger;
		case FieldDefinition._ptr:
		case FieldDefinition._ptrIndex:
		case FieldDefinition._ptrOne:
		case FieldDefinition._ptrRel:
		case FieldDefinition._setComplex:
				return Pointer.Null;
		case FieldDefinition._real:
			return Pointer.NullReal;
		case FieldDefinition._set:
		case FieldDefinition._setCharEnum:
		case FieldDefinition._setIntEnum:
			return Pointer.NullSet;
		case FieldDefinition._text:
			return Pointer.NullText;
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from dateHandler
	static final Date emptyDate;

	static {
		Calendar c = new GregorianCalendar(org.makumba.MakumbaSystem
				.getTimeZone());
		c.clear();
		c.set(1900, 0, 1);
		emptyDate = c.getTime();
	}

	//moved from intHandler
	static final Object emptyInt = new Integer(0);

	//moved from realHandler
	static final Object emptyReal = new Double(0d);

	/** the name of this handler, normally the same with the name of the field */
	public String getName() {
		return getDataName();
	}

	/** the data field this handler is associated to */
	public final String getDataName() {
		return name;
	}

	/** tells wether this field has a description originally */
	public boolean hasDescription() {
		return !description.equals(name);
	}

	/**
	 * returns field's description, if present. If not present (null or "") it
	 * returns field name.
	 */
	public String getDescription() {
		if (description == null)
			return name;
		if (description.trim().equals(""))
			return name;
		return description;
	}

	/** returns field's type */
	public String getType() {
		return type;
	}

	/** returns field type's integer value */
	public int getIntegerType() {
		return ((Integer) integerTypeMap.get(getType())).intValue();
	}

	// should be set while parsing
	// intEnum has int, set has null, etc
	public String getDataType() {
		switch (getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return "char";
		case FieldDefinition._date:
			return "datetime";
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return "timestamp";
		case FieldDefinition._int:
		case FieldDefinition._intEnum:
			return "int";
		case FieldDefinition._ptr:
		case FieldDefinition._ptrIndex:
		case FieldDefinition._ptrOne:
		case FieldDefinition._ptrRel:
			return "pointer";
		case FieldDefinition._real:
			return "real";
		case FieldDefinition._set:
			return "set";
		case FieldDefinition._setCharEnum:
			return "setchar";
		case FieldDefinition._setComplex:
			return "null";
		case FieldDefinition._setIntEnum:
			return "setint";
		case FieldDefinition._text:
			return "text";
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	// intEnum has int, set has null, etc
	public Class getJavaType() {
		switch (getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return java.lang.String.class;
		case FieldDefinition._date:
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return java.util.Date.class;
		case FieldDefinition._int:
		case FieldDefinition._intEnum:
			return java.lang.Integer.class;
		case FieldDefinition._ptr:
		case FieldDefinition._ptrIndex:
		case FieldDefinition._ptrOne:
		case FieldDefinition._ptrRel:
			return Pointer.class;
		case FieldDefinition._real:
			return java.lang.Double.class;
		case FieldDefinition._set:
		case FieldDefinition._setCharEnum:
		case FieldDefinition._setIntEnum:
			return java.util.Vector.class;
		case FieldDefinition._setComplex:
			return null;
		case FieldDefinition._text:
			return org.makumba.Text.class;
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/** tells wether this field is fixed */
	public boolean isFixed() {
		return fixed;
	}

	/** tells wether this field is not null */
	public boolean isNotNull() {
		return notNull;
	}

	/** tells wether this field is unique */
	public boolean isUnique() {
		return unique;
	}

	/** returns the defa()ult value of this field */
	public Object getDefaultValue() {
		if (defaultValue == null)
			return getEmptyValue();
		return defaultValue;
	}

	/**
	 * works only for intEnum, charEnum, setintEnum, setcharEnum types
	 */
	public Enumeration getValues() {
		switch (getIntegerType()) {
		case FieldDefinition._charEnum:
		case FieldDefinition._intEnum:
			return ((Vector) this.extra1).elements();
		case FieldDefinition._setCharEnum:
		case FieldDefinition._setIntEnum:
			return ((Vector) getEnum().extra1).elements();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for intEnum, charEnum, setintEnum, setcharEnum types
	 *  
	 */
	public Enumeration getNames() {
		switch (getIntegerType()) {

		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for intEnum, charEnum, setintEnum, setcharEnum types
	 *  
	 */
	public int getEnumeratorSize() {
		switch (getIntegerType()) {
		case FieldDefinition._charEnum:
		case FieldDefinition._intEnum:
			return ((Vector) this.extra1).size();
		case FieldDefinition._setCharEnum:
		case FieldDefinition._setIntEnum:
			return ((Vector) getEnum().extra1).size();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for intEnum, charEnum, setintEnum, setcharEnum types
	 *  
	 */
	public String getStringAt(int i) {
		switch (getIntegerType()) {
		case FieldDefinition._charEnum:
		case FieldDefinition._intEnum:
			return ((Vector) this.extra1).elementAt(i).toString();
		case FieldDefinition._setCharEnum:
		case FieldDefinition._setIntEnum:
			return ((Vector) getEnum().extra1).elementAt(i).toString();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for intEnum type
	 *  
	 */
	public String getNameFor(int i) {
		switch (getIntegerType()) {
		case FieldDefinition._intEnum:
			return get_intEnum_NameFor(i);
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from intEnumHandler
	public String get_intEnum_NameFor(int n) {
		Vector names = (Vector) this.extra2;
		Vector values = (Vector) this.extra1;
		for (int i = 0; i < values.size(); i++)
			if (values.elementAt(i).equals(new Integer(n)))
				return (String) names.elementAt(i);
		throw new org.makumba.InvalidValueException(this,
				"Can't find a name for " + n + " in " + values + " with names "
						+ names);
	}

	/**
	 * works only for intEnum, charEnum, setintEnum, setcharEnum types
	 *  
	 */
	public String getNameAt(int i) {
		switch (getIntegerType()) {
		case FieldDefinition._charEnum:
		case FieldDefinition._intEnum:
			return (String) ((Vector) this.extra1).elementAt(i);
		case FieldDefinition._setCharEnum:
			return (String) ((Vector) getEnum().extra1).elementAt(i);
		case FieldDefinition._setIntEnum:
			return (String) ((Vector) getEnum().extra2).elementAt(i);
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for int, intEnum, setintEnum types
	 *  
	 */
	public int getDefaultInt() {
		switch (getIntegerType()) {
		case FieldDefinition._int:
		case FieldDefinition._intEnum:
			return ((Integer) getDefaultValue()).intValue();
		case FieldDefinition._setIntEnum:
			return ((Integer) getEnum().defaultValue).intValue();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for intEnum, setintEnum types
	 *  
	 */
	public int getIntAt(int i) {
		switch (getIntegerType()) {
		case FieldDefinition._intEnum:
			return ((Integer) ((Vector) this.extra1).elementAt(i)).intValue();
		case FieldDefinition._setIntEnum:
			return ((Integer) ((Vector) getEnum().extra1).elementAt(i))
					.intValue();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for char, text, charEnum, setcharEnum types
	 *  
	 */
	public String getDefaultString() {
		switch (getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
		case FieldDefinition._text:
			return (String) getDefaultValue();
		case FieldDefinition._setCharEnum:
			return (String) getEnum().defaultValue;
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for char, charEnum, setcharEnum types
	 *  
	 */
	public int getWidth() {
		switch (getIntegerType()) {
		case FieldDefinition._char:
		case FieldDefinition._charEnum:
			return ((Integer) this.extra2).intValue();
		case FieldDefinition._setCharEnum:
			return ((Integer) getEnum().extra2).intValue();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//inserted 20050418
	public Object checkValueImpl(Object value) {
		switch (getIntegerType()) {
		case FieldDefinition._char:
			return check_char_ValueImpl(value);
		case FieldDefinition._charEnum:
			return check_charEnum_ValueImpl(value);
		case FieldDefinition._date:
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return check_date_ValueImpl(value);
		case FieldDefinition._int:
			return check_int_ValueImpl(value);
		case FieldDefinition._intEnum:
			return check_intEnum_ValueImpl(value);
		case FieldDefinition._ptr:
		case FieldDefinition._ptrIndex:
		case FieldDefinition._ptrOne:
		case FieldDefinition._ptrRel:
		case FieldDefinition._setIntEnum:
			return check_ptrIndex_ValueImpl(value);
		case FieldDefinition._real:
			return check_real_ValueImpl(value);
		case FieldDefinition._set:
			return check_set_ValueImpl(value);
		case FieldDefinition._setCharEnum:
			return check_setcharEnum_ValueImpl(value);
		case FieldDefinition._setComplex:
			return check_setComplex_ValueImpl(value);
		case FieldDefinition._text:
			return check_text_ValueImpl(value);
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from charHandler
	public Object check_char_ValueImpl(Object value) {
		normalCheck(value);
		String s = (String) value;
		if (s.length() > getWidth())
			throw new InvalidValueException(this,
					"String too long for char[] field. Maximum width: "
							+ getWidth() + " given width " + s.length()
							+ ".\n\tGiven value <" + s + ">");
		return value;
	}

	//moved from charEnumHandler
	public Object check_charEnum_ValueImpl(Object value) {
		check_char_ValueImpl(value);

		Vector names = (Vector) this.extra1;

		for (int i = 0; i < names.size(); i++)
			if (names.elementAt(i).equals(value))
				return value;
		throw new org.makumba.InvalidValueException(this,
				"value set to char enumerator (" + value
						+ ") is not a member of " + names);
	}

	//moved from dateHandler
	public Object check_date_ValueImpl(Object value) {
		return normalCheck(value);
	}

	//moved from intHandler
	public Object check_int_ValueImpl(Object value) {
		return normalCheck(value);
	}

	//moved from intEnumHandler
	public Object check_intEnum_ValueImpl(Object value) {
		Vector names = (Vector) this.extra2;
		Vector values = (Vector) this.extra1;
		if (value instanceof Integer) {
			for (int i = 0; i < values.size(); i++)
				if (values.elementAt(i).equals(value))
					return value;
			throw new org.makumba.InvalidValueException(this,
					"int value set to int enumerator (" + value
							+ ") is not a member of " + values);
		}
		if (!(value instanceof String))
			throw new org.makumba.InvalidValueException(
					this,
					"int enumerators only accept values of type Integer or String. Value supplied ("
							+ value
							+ ") is of type "
							+ value.getClass().getName());

		for (int i = 0; i < names.size(); i++)
			if (names.elementAt(i).equals(value))
				return values.elementAt(i);

		throw new org.makumba.InvalidValueException(this,
				"string value set to int enumerator (" + value
						+ ") is neither a member of " + names
						+ " nor amember of " + values);
	}

	//moved from ptrIndexHandler
	public Object check_ptrIndex_ValueImpl(Object value) {
		if (value instanceof Pointer) {
			if (!((Pointer) value).getType().equals(getPointedType().getName()))
				throw new InvalidValueException(this, getPointedType()
						.getName(), (Pointer) value);
			return value;
		}
		if (value instanceof String)
			return new Pointer(getPointedType().getName(), (String) value);
		throw new InvalidValueException(
				this,
				"Only java.lang.String and org.makumba.Pointer are assignable to makumba pointers, given value <"
						+ value + "> is of type " + value.getClass().getName());
	}

	//moved from realHandler
	public Object check_real_ValueImpl(Object value) {
		if (value instanceof Integer)
			return value;
		return normalCheck(value);
	}

	//moved from setHandler
	public Object check_set_ValueImpl(Object value) {
		try {
			// may be just a pointer
			Object o = check_ptrIndex_ValueImpl(value);
			Vector v = new Vector();
			if (o != null && o instanceof Pointer)
				v.addElement(o);
			return v;
		} catch (org.makumba.InvalidValueException ive) {
		}

		normalCheck(value);

		Vector v = (Vector) value;

		FieldDefinition ptr = getForeignTable().getFieldDefinition(
				getForeignTable().getIndexPointerFieldName());

		for (int i = 0; i < v.size(); i++) {
			if (v.elementAt(i) == null
					|| v.elementAt(i).equals(org.makumba.Pointer.Null))
				throw new org.makumba.InvalidValueException(this,
						"set members cannot be null");
			try {
				v.setElementAt(ptr.checkValue(v.elementAt(i)), i);
			} catch (org.makumba.InvalidValueException e) {
				throw new org.makumba.InvalidValueException(this,
						"the set member <" + v.elementAt(i)
								+ "> is not assignable to pointers of type "
								+ getForeignTable().getName());
			}
		}
		return v;
	}

	//moved from setcharEnumHandler
	public Object check_setcharEnum_ValueImpl(Object value) {
		try {
			Object o = getEnum().checkValue(value);
			Vector v = new Vector();
			if (o != null && o instanceof String)
				v.addElement(o);
			return v;
		} catch (org.makumba.InvalidValueException ive) {
		}

		normalCheck(value);

		Vector v = (Vector) value;

		for (int i = 0; i < v.size(); i++) {
			if (v.elementAt(i) == null
					|| v.elementAt(i).equals(org.makumba.Pointer.NullString))
				throw new org.makumba.InvalidValueException(this,
						"set members cannot be null");
			v.setElementAt(getEnum().checkValue(v.elementAt(i)), i);
		}
		return v;
	}

	//moved from setComplexHandler
	public Object check_setComplex_ValueImpl(Object value) {
		throw new org.makumba.InvalidValueException(this,
				"subsets cannot be assigned directly");
	}

	//moved from textHandler
	public Object check_text_ValueImpl(Object value) {
		try {
			return Text.getText(value);
		} catch (InvalidValueException e) {
			throw new InvalidValueException(this, e.getMessage());
		}
	}

	//moved from setcharEnumHandler and setintEnumHandler
	FieldInfo getEnum() {
		return (FieldInfo) ((DataDefinition) this.extra1)
				.getFieldDefinition("enum");
	}

	//moved from ptrOneHandler
	public DataDefinition get_ptrOne_Subtable() {
		return (DataDefinition) this.extra1;
	}

	/**
	 * works only for date type
	 *  
	 */
	public Date getDefaultDate() {
		switch (getIntegerType()) {
		case FieldDefinition._date:
		case FieldDefinition._dateCreate:
		case FieldDefinition._dateModify:
			return (Date) getDefaultValue();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	/**
	 * works only for ptr, ptrRel and set types
	 *  
	 */
	public DataDefinition getForeignTable() {
		switch (getIntegerType()) {
		case FieldDefinition._ptr:
		case FieldDefinition._ptrRel:
			return get_ptrRel_ForeignTable();
		case FieldDefinition._set:
			return get_set_ForeignTable();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from prtRelHandler
	public DataDefinition get_ptrRel_ForeignTable() {
		return (DataDefinition) this.extra1;
	}

	//moved from setHandler
	public DataDefinition get_set_ForeignTable() {
		if (this.extra3 == null) // automatic set
			return pointerToForeign().getForeignTable();
		else
			return (DataDefinition) this.extra3; // manually made
	}

	//moved from setHandler
	FieldDefinition pointerToForeign() {
		return getSubtable().getFieldDefinition(
				(String) getSubtable().getFieldNames().elementAt(4));
	}

	/**
	 * works only for ptrOne, set, setComplex, setcharEnum and setintEnum types
	 * 
	 * @return the subtable indicated in set or ptr definition
	 */
	public DataDefinition getSubtable() {
		switch (getIntegerType()) {
		case FieldDefinition._ptrOne:
		case FieldDefinition._setCharEnum:
		case FieldDefinition._setComplex:
		case FieldDefinition._setIntEnum:
			return get_ptrOne_Subtable();
		case FieldDefinition._set:
			return get_set_Subtable();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from setHandler
	public DataDefinition get_set_Subtable() {
		return (DataDefinition) this.extra1;
	}

	/**
	 * works only for all pointer and set types
	 *  
	 */
	public DataDefinition getPointedType() {
		switch (getIntegerType()) {
		case FieldDefinition._ptrIndex:
			return get_ptrIndex_PointedType();
		case FieldDefinition._ptrOne:
		case FieldDefinition._setCharEnum:
		case FieldDefinition._setComplex:
		case FieldDefinition._setIntEnum:
			return get_ptrOne_PointedType();
		case FieldDefinition._ptrRel:
		case FieldDefinition._ptr:
		case FieldDefinition._set:
			return get_ptrRel_PointedType();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from ptrIndexHandler
	public DataDefinition get_ptrIndex_PointedType() {
		return getDataDefinition();
	}

	//moved from ptrOneHandler
	public DataDefinition get_ptrOne_PointedType() {
		return getSubtable();
	}

	//moved from ptrRelHandler
	public DataDefinition get_ptrRel_PointedType() {
		return getForeignTable();
	}

	/**
	 * works only for ptr and set types
	 * 
	 * @return title field of the record in the foreign table, as indicated in
	 *         this field definition or in the respective foreign table record
	 *         definition
	 * @see #hasTitleFieldIndicated()
	 */
	public String getTitleField() {
		switch (getIntegerType()) {
		case FieldDefinition._ptr:
		case FieldDefinition._set:
			return get_ptr_TitleField();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from ptrHandler
	public String get_ptr_TitleField() {
		if (hasTitleFieldIndicated())
			return (String) this.extra2;
		return getForeignTable().getTitleFieldName();
	}

	/**
	 * works only for ptr and set types
	 * 
	 * @return wether the definition indicates a titile field
	 * @exception ClassCastException
	 *                for other types
	 */
	public boolean hasTitleFieldIndicated() {
		switch (getIntegerType()) {
		case FieldDefinition._ptr:
		case FieldDefinition._set:
			return has_ptr_TitleFieldIndicated();
		default:
			throw new RuntimeException("Shouldn't be here");
		}
	}

	//moved from ptrHandler
	public boolean has_ptr_TitleFieldIndicated() {
		return this.extra2 != null;
	}

	//moved from FieldHandler
	protected Object normalCheck(Object value) {
		if (!getJavaType().isInstance(value))
			throw new org.makumba.InvalidValueException(this, getJavaType(),
					value);
		return value;
	}
}
