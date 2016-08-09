package org.makumba;

/** Information about a field from a makumba data definition as obtained from an MDD file.
 * This class is provided for makumba programs to be able to introspect makumba data structures. Such introspection is not needed usually, as the application programmer knows the makumba data structure.
 */
public interface FieldDefinition
{
  /** The name of this field, normally the same with the name of the field */
  public String getName();

  /** The data definition that contains this field definition */
  public DataDefinition getDataDefinition();

  /** The value returned in case there is no value in the database and 
    no default value is indicated */
  public Object getEmptyValue();

  /** Tells wether this field has a description in the MDD */
  public boolean hasDescription();

  /** Returns field's description*/
  public String getDescription();

  /** Returns field's internal makumba type. Can be: 
   * ptr: normal pointer
   * ptrRel: relational pointer (in automatically generated types such as middle types in set)
   * ptrOne: pointer to type defined on-the-spot 
   * ptrIndex: primary key, automatically added
   * int: normal integer
   * intEnum: integer defined by enumeration
   * char: normal character
   * charEnum: character defined by enumeration
   * text: normal text
   * date: normal date
   * dateCreate: creation date, automcatically added
   * dateModify: last modification date, automatically added
   * set: normal set in another table
   * setComplex: set of type defined on-the-spot
   */
  public String getType();

  /** The data type of this field. For example, intEnum and int both have int as data type */
  public String getDataType();

  /** The Java type of this field. For example, intEnum and int both have java.lang.Integer as data type */
  public Class getJavaType();

  /** tells wether this field is fixed */
  public boolean isFixed();

  /** tells wether this field is not null */
  public boolean isNotNull();

  /** returns the default value of this field */
  public Object getDefaultValue();

  /** Get the default value as a String.
   * Works only for char, text, charEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public String getDefaultString();

  /** Get the default value as an integer.
   * Works only for int, intEnum types
   * @exception ClassCastException for other types
  */
  public int getDefaultInt();

  /** Get the default value as a Date. 
   * Works only for date type
   * @exception ClassCastException for other types
  */
  public java.util.Date getDefaultDate();

  /** Get the values of an enumerated field.
   * Works only for intEnum, charEnum types.
   * @exception ClassCastException for other types
  */
  public java.util.Enumeration getValues();

  /** Get the names of an enumerated field. 
   * Works only for intEnum, charEnum types 
   * @exception ClassCastException for other types
  */
  public java.util.Enumeration getNames();
  
  /** Get the number of the members of an enumerated field. 
   * Works only for intEnum, charEnum types 
   * @exception ClassCastException for other types
  */
  public int getEnumeratorSize();
    
  /** Get the String value at a certain position in an enumerated field. 
   * Works only for intEnum, charEnum types 
   * @exception ClassCastException for other types
  */
  public String getStringAt(int i);

  /** Get the name at a certain position in an enumerated field. 
   * Works only for intEnum, charEnum types 
   * @exception ClassCastException for other types
  */
  public String getNameAt(int i);
  
  /** Get the integer at a certain position in an enumerated type. 
   * Works only for intEnum types 
   * @exception ClassCastException for other types
  */
  public int getIntAt(int i);

  /** Get the maximum character width. 
   * Works only for char, charEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public int getWidth();
  
  /** The type with which the ptr or set relation is defined. 
   * Works only for ptr, ptrRel and set types
   * @return the foreign type indicated in set or ptr definition
   * @exception ClassCastException for other types
   */
  public DataDefinition getRelationType();

  /** The subtype created by an immediate ptr or set definition.
   * Works only for ptrOne, set, setComplex types
   * @return the subtype indicated in set or ptr definition
   * @exception ClassCastException for other types
   */
  public DataDefinition getSubtype();
  
  /** Get the alternative title field, if the title to be used is indicated specifically on a ptr or set. 
   * Works only for ptr and set types
   * @return title field of the record in the foreign table, as indicated in this field definition or in the respective foreign table record definition
   * @exception ClassCastException for other types
   * @see org.makumba.DataDefinition#getTitleFieldName
   */
  public String getTitleField();
  
  /** Tells if the title to be used is indicated specifically on a ptr or set. 
   * works only for ptr and set types
   * @return wether the definition indicates a titile field 
   * @exception ClassCastException for other types
  */
  public boolean hasTitleFieldIndicated();
}


