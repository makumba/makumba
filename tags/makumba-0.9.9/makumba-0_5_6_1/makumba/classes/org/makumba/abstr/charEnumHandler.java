package org.makumba.abstr;
import java.util.*;

public class charEnumHandler extends charHandler implements Enumerator
{
  public Enumeration getValues() { return ((Vector)fi.extra1).elements(); }
  
  public Enumeration getNames() { return ((Vector) fi.extra1).elements(); }
    
  public int getEnumeratorSize(){ return ((Vector)fi.extra1).size(); }
  
  public String getStringAt(int i){ return ((Vector)fi.extra1).elementAt(i).toString(); }
  
  public String getNameAt(int i){ return (String)((Vector)fi.extra1).elementAt(i); }

  public Object checkValueImpl(Object value) 
  {
    super.checkValueImpl(value);

    Vector names=(Vector)fi.extra1; 

    for(int i=0; i<names.size(); i++)
      if(names.elementAt(i).equals(value))
	return value;
    throw new org.makumba.InvalidValueException(getFieldInfo(), "value set to char enumerator ("+value+") is not a member of "+names);
  }
}
