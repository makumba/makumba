package org.makumba.abstr;
import org.makumba.*;
import java.util.*;

public class setcharEnumHandler extends ptrOneHandler implements stringTypeFixed, Enumerator
{
  public String getDataType() { return "setchar"; }
  public Class getJavaType() { return java.util.Vector.class; }
  public Object getNull() { return Pointer.NullSet; }

  FieldInfo getEnum()
    { return (FieldInfo)((RecordInfo)super.fi.extra1).fields.get("enum"); }
 
  public Enumeration getValues() { return ((Vector)getEnum().extra1).elements(); }
  
  public Enumeration getNames() { return ((Vector) getEnum().extra2).elements(); }
    
  public int getEnumeratorSize(){ return ((Vector)getEnum().extra1).size(); }
  
  public String getStringAt(int i){ return ((Vector)getEnum().extra1).elementAt(i).toString(); }
  
  public String getNameAt(int i){ return (String)((Vector)getEnum().extra1).elementAt(i); }

  public int getWidth(){ return ((Integer)getEnum().extra2).intValue(); } 
  
  public String getDefaultString(){ return (String)getEnum().defaultValue; }

  public Object checkValueImpl(Object value)
  {
    try{
      Object o= getEnum().checkValue(value);
      Vector v= new Vector();
      if(o!=null && o instanceof String)
	v.addElement(o);
      return v;
    }catch(org.makumba.InvalidValueException ive){}
    
    normalCheck(value);

    Vector v=(Vector)value;

    for(int i=0; i<v.size(); i++)
      {
	if(v.elementAt(i)==null || v.elementAt(i).equals(org.makumba.Pointer.NullString))
	  throw new org.makumba.InvalidValueException(getFieldInfo(), "set members cannot be null");
	v.setElementAt(getEnum().checkValue(v.elementAt(i)), i);
      }
    return v;
  }


}
