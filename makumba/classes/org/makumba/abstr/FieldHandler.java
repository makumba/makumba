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

package org.makumba.abstr;
import java.util.*;

/** The general field handler. Knows how to read the data of its field info. Can replace itself with other FieldHandlers upon RecordHandler construction */
public abstract class FieldHandler
{
  FieldInfo fi;
  FieldHandler defa;

  public FieldInfo getFieldInfo() { return fi; }

  public void setFieldInfo(FieldInfo fi)
  {
   try{
        this.fi= fi;
        Class c= Class.forName("org.makumba.abstr."+getType()+"Handler");
        if(c.isInstance(this))
        {
            defa= this;
            return;
        }
        defa= (FieldHandler)c.newInstance();
        defa.fi= fi;
	defa.init();
    }catch(Exception e){e.printStackTrace();}
  }

  void init(){}

  public boolean isAssignableFrom(FieldInfo fi){ return fi.getType().equals("nil") || getType().equals(fi.getType()); }

  public String toString(){ return getType(); }

  /** check if the value can be assigned */
  public Object checkValue(Object value)
  {
    if(!value.equals(getNull()))
      return checkValueImpl(value);
    return value;
  }
  
  /** check a non-null value */
  public Object checkValueImpl(Object value)
  {
    return defa.checkValueImpl(value);
  }

  protected Object normalCheck(Object value)
  {
    if(!getJavaType().isInstance(value))
      throw new org.makumba.InvalidValueException(getFieldInfo(), getJavaType(), value);
    return value;
  }

  /** check if the value can be assigned */
  public void checkInsert(Dictionary d)
  {
    Object o=d.get(getName());
    if(isNotNull() && (o==null || o.equals(getNull())))
      throw new org.makumba.InvalidValueException(getFieldInfo(), "A non-null value is needed for notnull fields");
    if(o!=null)
      d.put(getName(), checkValue(o));
  }

  /** check if the value can be assigned */
  public void checkUpdate(Dictionary d)
  {
    Object o=d.get(getName());
    if(o==null)
      return;
    if(isFixed())
      throw new org.makumba.InvalidValueException(getFieldInfo(), "You cannot update a fixed field");
    d.put(getName(), checkValue(o));
  }

  /** the value returned in case there is no value in the database and no default value is indicated */
   Object getEmptyValue() { return null; }

  /** This method is called when this FieldHandler is about to be added to a record handler. It passes information about the record handler and provides a chance for this fieldHandler to replace itself in the RecordHandler
    * @return the FieldHandler or FieldHandler[] (in which case, the getName() method should return different names for each handler returned) that this FieldHandler wishes to replace itself with. By default, it returns this object
    */
  public Object replaceIn(RecordHandler rh)
  { return this; }

  /** the name of this handler, normally the same with the name of the field */
   public  String getName(){ return getDataName(); }

  /** the data field this handler is associated to */
   public  final String getDataName(){ return fi.name; }

  /** tells wether this field has a description originally */
   public  boolean hasDescription(){ return !fi.description.equals(fi.name); }

  /** returns field's description*/
   public  String getDescription(){ return fi.description; }

  /** returns field's type */
   public  String getType(){ return fi.type; }

  // should be set while parsing
  // intEnum has int, set has null, etc
  public  String getDataType() { return defa.getDataType(); }

  // intEnum has int, set has null, etc
  public  Class getJavaType() { return defa.getJavaType(); }

  /** which is the null object for this type ? */
  public  Object getNull() { return defa.getNull(); }

  /** tells wether this field is fixed */
   public  boolean isFixed() { return fi.fixed; }

  /** tells wether this field is not null */
   public  boolean isNotNull(){ return fi.notNull; }

  /** returns the default value of this field */
   public  Object getDefaultValue()
    {
      if(fi.defaultValue==null)
	return defa.getEmptyValue();
      return fi.defaultValue;
    }

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public   Enumeration getValues()
    {return ((Enumerator)defa).getValues(); }

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public  Enumeration getNames()
    {return ((Enumerator)defa).getNames(); }

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public  int getEnumeratorSize()
    {return ((Enumerator)defa).getEnumeratorSize(); }

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public  String getStringAt(int i)
    {return ((Enumerator)defa).getStringAt(i);}

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public  String getNameAt(int i)
    {return ((Enumerator)defa).getNameAt(i);}

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public  String getNameFor(int i)
    {return ((Enumerator)defa).getNameFor(i);}

  /** works only for int, intEnum, setintEnum types
   * @exception ClassCastException for other types
  */
   public  int getDefaultInt(){ return ((intType)defa).getDefaultInt(); }

  /** works only for intEnum, setintEnum types
   * @exception ClassCastException for other types
  */
   public  int getIntAt(int i) {return ((intEnumerator)defa).getIntAt(i);}

  /** works only for char, text, charEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public  String getDefaultString(){ return ((stringType)defa).getDefaultString(); }

  /** works only for char, charEnum, setcharEnum types
   * @exception ClassCastException for other types
  */
   public  int getWidth() { return ((stringTypeFixed)defa).getWidth(); }

  /** works only for date type
   * @exception ClassCastException for other types
  */
   public  Date getDefaultDate(){ return ((dateHandler)defa).getDefaultDate(); }

  /** works only for ptr, ptrRel and set types
    * @return the foreign table indicated in set or ptr definition
    * @exception ClassCastException for other types
  */
   public  RecordInfo getForeignTable() { return ((ptrRelHandler)defa).getForeignTable(); }

  /** works only for ptrOne, set, setComplex,  setcharEnum and setintEnum types
    * @return the subtable indicated in set or ptr definition
    * @exception ClassCastException for other types
  */
   public  RecordInfo getSubtable() { return ((subtableHandler)defa).getSubtable(); }

  public RecordInfo getPointedType()
  { return ((ptrIndexHandler)defa).getPointedType(); }

  /** works only for ptr and set types
    * @return title field of the record in the foreign table, as indicated in this field definition or in the respective foreign table record definition
    * @exception ClassCastException for other types
    * @see hasTitleFieldIndicated
  */
   public  String getTitleField() { return ((ptrHandler)defa).getTitleField(); }

  /** works only for ptr and set types
    * @return wether the definition indicates a titile field
    * @exception ClassCastException for other types
  */
   public  boolean hasTitleFieldIndicated(){ return((ptrHandler)defa).hasTitleFieldIndicated(); }
}

