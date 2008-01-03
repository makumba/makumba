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

package org.makumba;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Vector;

/**
 * Information about a field from a makumba data definition as obtained from an MDD file. This class is provided for
 * makumba programs to be able to introspect makumba data structures. Such introspection is not needed usually, as the
 * application programmer knows the makumba data structure.
 */
public interface FieldDefinition {
    public static final int _ptr = 0;

    public static final int _ptrRel = 1;

    public static final int _ptrOne = 2;

    public static final int _ptrIndex = 3;

    public static final int _int = 4;

    public static final int _intEnum = 5;

    public static final int _char = 6;

    public static final int _charEnum = 7;

    public static final int _text = 8;

    public static final int _date = 9;

    public static final int _dateCreate = 10;

    public static final int _dateModify = 11;

    public static final int _set = 12;

    public static final int _setComplex = 13;

    public static final int _nil = 14;

    public static final int _real = 15;

    public static final int _setCharEnum = 16;

    public static final int _setIntEnum = 17;

    public static final int _binary = 18;

    public static final String ERROR_NOT_NULL = "A non-null value is needed for notnull fields";

    public static final String ERROR_NOT_UNIQUE = "This field needs to be unique. Try another value";

    /** The name of this field, normally the same with the name of the field */
    public String getName();

    /** The data definition that contains this field definition */
    public DataDefinition getDataDefinition();

    /**
     * The value returned in case there is no value in the database and no default value is indicated
     */
    public Object getEmptyValue();

    /** The null value for this type */
    public Object getNull();

    /** Tells wether this field has a description in the MDD */
    public boolean hasDescription();

    /** Returns field's description */
    public String getDescription();

    /**
     * Returns field's internal makumba type. Can be:
     * 
     * <ul>
     * <li>ptr: normal pointer</li>
     * <li>ptrRel: relational pointer (in automatically generated types such as middle types in set)</li>
     * <li>ptrOne: pointer to type defined on-the-spot</li>
     * <li>ptrIndex: primary key, automatically added</li>
     * <li>int: normal integer</li>
     * <li>intEnum: integer defined by enumeration</li>
     * <li>char: normal character</li>
     * <li>charEnum: character defined by enumeration</li>
     * <li>text: normal text</li>
     * <li>date: normal date</li>
     * <li>dateCreate: creation date, automatically added</li>
     * <li>dateModify: last modification date, automatically added</li>
     * <li>set: normal set in another table</li>
     * <li>setComplex: set of type defined on-the-spot</li>
     * <li>binary: binary data type (images, documents...)</li>
     * </ul>
     * 
     * TODO nil and real and timeStamp need to be added???
     */
    public String getType();

    /** returns the integer value associated with the field's internal makumba type. */
    public int getIntegerType();

    /** The data type of this field. For example, intEnum and int both have int as data type */
    public String getDataType();

    /** The Java type of this field. For example, intEnum and int both have java.lang.Integer as data type */
    public Class getJavaType();

    /** tells whether this field is fixed */
    public boolean isFixed();

    /** tells whether this field is not null */
    public boolean isNotNull();

    /** tells whether this field is unique */
    public boolean isUnique();

    /** returns the default value of this field */
    public Object getDefaultValue();

    /**
     * Get the default value as a String. Works only for char, text, charEnum, setcharEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public String getDefaultString();

    /**
     * Get the default value as an integer. Works only for int, intEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public int getDefaultInt();

    /**
     * Get the default value as a Date. Works only for date type
     * 
     * @exception ClassCastException
     *                for other types
     */
    public java.util.Date getDefaultDate();

    /**
     * Get the values of an enumerated field. Works only for intEnum, charEnum types.
     * 
     * @exception ClassCastException
     *                for other types
     */
    public java.util.Enumeration getValues();

    /**
     * Get the names of an enumerated field. Works only for intEnum, charEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public java.util.Enumeration getNames();

    /**
     * Get the number of the members of an enumerated field. Works only for intEnum, charEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public int getEnumeratorSize();

    /**
     * Get the String value at a certain position in an enumerated field. Works only for intEnum, charEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public String getStringAt(int i);

    /**
     * Get the name at a certain position in an enumerated field. Works only for intEnum, charEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public String getNameAt(int i);

    /**
     * Get the name for a certain value of the enumerated field Works only for intEnum
     * 
     * @exception ClassCastException
     *                for other types
     */
    public String getNameFor(int i);

    /**
     * Get the integer at a certain position in an enumerated type. Works only for intEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public int getIntAt(int i);

    /**
     * Get the maximum character width. Works only for char, charEnum, setcharEnum types
     * 
     * @exception ClassCastException
     *                for other types
     */
    public int getWidth();

    /**
     * The type with which the ptr or set relation is defined. Works only for ptr, ptrRel and set types
     * 
     * @return the foreign type indicated in set or ptr definition
     * @exception ClassCastException
     *                for other types
     */
    public DataDefinition getForeignTable();

    /**
     * The subtype created by an immediate ptr or set definition. Works only for ptrOne, set, setComplex types
     * 
     * @return the subtype indicated in set or ptr definition
     * @exception ClassCastException
     *                for other types
     */
    public DataDefinition getSubtable();

    /**
     * The type referred. Will return getRelationType() for ptr, ptrRel and set types and getSubtype() for ptrOne, set,
     * setComplex types. Works only for ptrOne, set, setComplex types
     * 
     * @return the subtype indicated in set or ptr definition
     * @exception ClassCastException
     *                for other types
     */
    public DataDefinition getPointedType();

    /**
     * Get the alternative title field, if the title to be used is indicated specifically on a ptr or set. Works only
     * for ptr and set types
     * 
     * @return title field of the record in the foreign table, as indicated in this field definition or in the
     *         respective foreign table record definition
     * @exception ClassCastException
     *                for other types
     * @see org.makumba.DataDefinition#getTitleFieldName
     */
    public String getTitleField();

    /**
     * Tells if the title to be used is indicated specifically on a ptr or set. works only for ptr and set types
     * 
     * @return whether the definition indicates a title field
     * @exception ClassCastException
     *                for other types
     */
    public boolean hasTitleFieldIndicated();

    /** check compatibility with the given type */
    public boolean isAssignableFrom(FieldDefinition fd);

    /** check if the value can be assigned */
    public Object checkValue(Object value);

    // inserted 20050418
    public Object checkValueImpl(Object value);

    /** check if the corresponding field from the dictionary can be inserted */
    public void checkInsert(Dictionary d);

    /** check if the corresponding field from the dictionary can be updated */
    public void checkUpdate(Dictionary d);

    /** returns the deprecated values for intEnum */
    public Vector getDeprecatedValues();

    /** retuns whether this field is a default field generated by makumba. */
    public boolean isDefaultField();

    /** Indicates whether this field should be edited by a simple mak:input, or needs it's own form. */
    public boolean shouldEditBySingleInput();

    /** retuns whether this field is a number type, i.e. date, or the default fields dateCreate and dateModify. */
    public boolean isDateType();

    /** retuns whether this field is a number type, i.e. int or real. */
    public boolean isNumberType();

    /** retuns whether this field is an integer type. */
    public boolean isIntegerType();

    /** retuns whether this field is a real type. */
    public boolean isRealType();

    /** retuns whether this field is a set type, i.e. set, setIntEnum or setCharEnum or setComplex. */
    public boolean isSetType();

    /** retuns whether this field is a set enum type, i.e. setIntEnum or setCharEnum. */
    public boolean isSetEnumType();
    
    /** retuns whether this field is a simple enum type, i.e. intEnum or charEnum. */
    public boolean isEnumType();
    
    /**
     * retuns whether this field is any kind of internal set type, i.e. set, setIntEnum, setCharEnum and setComplex, but
     * not set.
     */
    public boolean isInternalSet();

    /** retuns whether this field is an external set, i.e. set ptr, but not setIntEnum, not setCharEnum nor setComplex. */
    public boolean isExternalSet();
    
    /** retuns whether this field is a setComplex. */
    public boolean isComplexSet();

    /** retuns whether this field is a pointer. */
    public boolean isPointer();

    /** retuns whether this field is a string type, i.e. char or text. */
    public boolean isStringType();

    /** Returns all the validation rules connected to this field. */
    public Collection<ValidationRule> getValidationRules();

    /** Adds a new validation rule to this field. */
    public void addValidationRule(ValidationRule rule);

    /** adds several new validation rules to this field. */
    public void addValidationRule(Collection<ValidationRule> rules);

}
