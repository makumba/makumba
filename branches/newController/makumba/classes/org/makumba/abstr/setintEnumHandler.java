package org.makumba.abstr;
import org.makumba.*;
import java.util.*;

public class setintEnumHandler extends ptrOneHandler implements intType, intEnumerator
{
  public String getDataType() { return "setint"; }
  public Class getJavaType() { return java.util.Vector.class; }
  public Object getNull() { return Pointer.NullSet; }

  FieldInfo getEnum(){ return (FieldInfo)((RecordInfo)super.fi.extra1).fields.get("enum"); }
 
  public Enumeration getValues() { return ((Vector)getEnum().extra1).elements(); }
  
  public Enumeration getNames() { return ((Vector) getEnum().extra2).elements(); }
    
  public int getEnumeratorSize(){ return ((Vector)getEnum().extra1).size(); }
  
  public String getStringAt(int i){ return ((Vector)getEnum().extra1).elementAt(i).toString(); }
  
  public String getNameAt(int i){ return (String)((Vector)getEnum().extra2).elementAt(i); }

  public int getIntAt(int i){ return ((Integer)((Vector)getEnum().extra1).elementAt(i)).intValue(); }
  
  public int getDefaultInt(){ return ((Integer)getEnum().defaultValue).intValue(); }

  public Object checkValue(Object value)
  {
    try{
      // may be just an Integer
      Object o= getEnum().checkValue(value);
      Vector v= new Vector();
      if(o!=null && o instanceof Integer)
	v.addElement(o);
      return v;
    }catch(org.makumba.InvalidValueException ive){}
    
    normalCheck(value);
    Vector v=(Vector)value;

    for(int i=0; i<v.size(); i++)
      {
	if(v.elementAt(i)==null || v.elementAt(i).equals(org.makumba.Pointer.NullInteger))
	  throw new org.makumba.InvalidValueException(getFieldInfo(), "set members cannot be null");
	v.setElementAt(getEnum().checkValue(v.elementAt(i)), i);
      }
    return v;
  }
  
}


