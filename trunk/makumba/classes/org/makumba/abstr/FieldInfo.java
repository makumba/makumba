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
import org.makumba.*;

/** This is a structure containing the elementary data about a field: name, 
 * type, attributes, description, and other type-specific extra info. 
 * All this information is available through the associated <a href=org.makumba.abstr.FieldHandler.html#_top_>FieldHandler</a>
 */
public class FieldInfo implements java.io.Serializable, FieldDefinition
{
  RecordInfo ri;

  public RecordInfo getRecordInfo() { return ri; }
  public DataDefinition getDataDefinition() { return getRecordInfo(); }

  public static FieldInfo getFieldInfo(String name, Object type, 
				       boolean typeSearch)
  {
    if (type instanceof FieldInfo)
      return new FieldInfo(name, (FieldInfo)type);
    String t=((String)type).trim();
    
    if(!typeSearch || t.indexOf(" ")==-1)
      return new FieldInfo(name, t);
    
    t= name+"="+t;

    return new RecordParser().parse(t).getField(name);
  }
  
  public FieldInfo(RecordInfo ri, String name) { this.ri=ri; this.name= name; }

  public FieldInfo(FieldInfo fi) 
  { 
    this(fi.ri, fi.name);
    type= fi.type;
    fixed= fi.fixed;
    notNull= fi.notNull;
    unique= fi.unique;
    defaultValue= fi.defaultValue;
    description= fi.description;
  }

  /** for temporary field info */
  public FieldInfo(String name, FieldInfo fi) 
  { 
    this.name=name;
    type= fi.type;
    fixed= fi.fixed;
    notNull= fi.notNull;
    unique= fi.unique;
    defaultValue= fi.defaultValue;
    description= fi.description;
    extra1= fi.extra1;
    extra2=fi.extra2;
    extra3=fi.extra3;
    if(type.equals("ptrIndex"))
      {
	type="ptr";
	extra1=fi.getRecordInfo();
      }
  }
  
  public FieldInfo(String name, String t) 
  { 
    try{
      this.name=name;
      this.type= t;
      fixed= false;
      notNull= false;
      unique= false;
      if(type.equals("char"))
	extra2= new Integer(255);
      else if (type.startsWith("char"))
	{
	  int n= type.indexOf("[");
	  int m= type.indexOf("]");
	  if(!type.endsWith("]") || type.substring(3, n).trim().length()>1)
	    throw new InvalidValueException("invalid char type "+type);
	  
	  extra2=new Integer(Integer.parseInt(type.substring(n+1, m)));
	  type="char";
	}
    }catch(StringIndexOutOfBoundsException e){throw new InvalidValueException("bad type "+type); }
    catch(NumberFormatException f){throw new InvalidValueException("bad char[] size "+type); }

  }

  public boolean isAssignableFrom(FieldDefinition fi) { return defa().isAssignableFrom((FieldInfo)fi); }
  public String toString(){ return defa().toString(); }

  String name;
  String type;
  boolean fixed;
  boolean notNull;
  boolean unique;
  Object defaultValue;
  String description;
  
  // those fields are only used by some types
  Object extra1, extra2, extra3;
  
  //////////// a very dirty hack to be able to access field info outside field handlers
  FieldHandler _defa;
  FieldHandler defa()
  {
    if(_defa==null)
      try{
      Class c= Class.forName("org.makumba.abstr."+getType()+"Handler");
      _defa= (FieldHandler)c.newInstance();
      _defa.fi= this;
    }catch(Exception e){e.printStackTrace();}
    return _defa;
  }

  /** check if the value can be assigned */
  public Object checkValue(Object value)
  {
    return defa().checkValue(value);
  }

  public void checkInsert(Dictionary d)
  {
    defa().checkInsert(d);
  }

  public void checkUpdate(Dictionary d)
  {
    defa().checkUpdate(d);
  }

  /** the value returned in case there is no value in the database and no defa()ult value is indicated */
  public Object getEmptyValue() { return null; }

  /** the name of this handler, normally the same with the name of the field */
  public String getName(){ return getDataName(); }
  
  /** the data field this handler is associated to */
  public final String getDataName(){ return name; }

  /** tells wether this field has a description originally */
  public boolean hasDescription(){ return !description.equals(name); }

  /** returns field's description, if present. If not present (null or "") it returns field name. */
  public String getDescription()
  {
    if(description==null) return name;
    if(description.trim().equals("")) return name;
    return description; 
  }

  /** returns field's type */
  public String getType(){ return type; }

  // should be set while parsing
  // intEnum has int, set has null, etc
  public String getDataType() { return defa().getDataType(); }

  // intEnum has int, set has null, etc
  public Class getJavaType() { return defa().getJavaType(); }

  /** tells wether this field is fixed */
  public boolean isFixed() { return fixed; }

  /** tells wether this field is not null */
  public boolean isNotNull(){ return notNull; }

  /** tells wether this field is unique */
  public boolean isUnique(){ return unique; }

  /** returns the defa()ult value of this field */
  public Object getDefaultValue()
    { 
      if(defaultValue==null)
	return defa().getEmptyValue();
      return defaultValue;
    }

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public  Enumeration getValues()
    {return ((Enumerator)defa()).getValues(); }

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public Enumeration getNames()
    {return ((Enumerator)defa()).getNames(); }
  
  /** works only for intEnum, charEnum, setintEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public int getEnumeratorSize()
    {return ((Enumerator)defa()).getEnumeratorSize(); }
    
  /** works only for intEnum, charEnum, setintEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public String getStringAt(int i)
    {return ((Enumerator)defa()).getStringAt(i);}

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public String getNameFor(int i)
    {return ((Enumerator)defa()).getNameFor(i);}

  /** works only for intEnum, charEnum, setintEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public String getNameAt(int i)
    {return ((Enumerator)defa()).getNameAt(i);}
  
  /** works only for int, intEnum, setintEnum types 
   * @exception ClassCastException for other types
  */
  public int getDefaultInt(){ return ((intType)defa()).getDefaultInt(); }

  /** works only for intEnum, setintEnum types 
   * @exception ClassCastException for other types
  */
  public int getIntAt(int i) {return ((intEnumerator)defa()).getIntAt(i);}

  /** works only for char, text, charEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public String getDefaultString(){ return ((stringType)defa()).getDefaultString(); }

  /** works only for char, charEnum, setcharEnum types 
   * @exception ClassCastException for other types
  */
  public int getWidth() { return ((stringTypeFixed)defa()).getWidth(); }
  
  /** works only for date type
   * @exception ClassCastException for other types
  */
  public Date getDefaultDate(){ return ((dateHandler)defa()).getDefaultDate(); }

  /** works only for ptr, ptrRel and set types
    * @return the foreign table indicated in set or ptr definition
    * @exception ClassCastException for other types
  */
  public RecordInfo getForeignTable() { return ((ptrIndexHandler)defa()).getForeignTable(); } 

  public DataDefinition getRelationType(){return getForeignTable(); }

  /** works only for ptrOne, set, setComplex,  setcharEnum and setintEnum types
    * @return the subtable indicated in set or ptr definition
    * @exception ClassCastException for other types
  */
  public RecordInfo getSubtable() { return ((subtableHandler)defa()).getSubtable(); } 

  public DataDefinition getSubtype() { return getSubtable();}

  public DataDefinition getReferredType(){ return getPointedType(); }

  public DataDefinition getPointedType()
  { return ((ptrIndexHandler)defa()).getPointedType(); }

  /** works only for ptr and set types
    * @return title field of the record in the foreign table, as indicated in this field definition or in the respective foreign table record definition
    * @exception ClassCastException for other types
    * @see hasTitleFieldIndicated
  */
  public String getTitleField() { return ((ptrHandler)defa()).getTitleField(); } 
  
  /** works only for ptr and set types
    * @return wether the definition indicates a titile field 
    * @exception ClassCastException for other types
  */
  public boolean hasTitleFieldIndicated(){ return((ptrHandler)defa()).hasTitleFieldIndicated(); }

}
