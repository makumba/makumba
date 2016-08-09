package org.makumba.util;
import java.util.*;

/** the values for a group of fields */
public class MultipleKey extends Vector
{
  static private final Object theNull=new Object(){
    public String toString(){ return "null key member"; }
  };

  public MultipleKey(int size){ super(size); }

  public MultipleKey(Vector v){ this(v, v.size()); }
  public MultipleKey(Vector v, int size)
  {
    super(size);
    for(; elementCount<v.size(); elementCount++)
      elementData[elementCount]= checkNull(v.elementAt(elementCount));
  }

  public MultipleKey(Object o[]) { this(o, o.length); }
  public MultipleKey(Object o[], int size)
  {
    super(size);
    for(; elementCount<o.length; elementCount++)
      elementData[elementCount]= checkNull(o[elementCount]);
  }

  /** keep together the values associated with the give keys */
  public MultipleKey(Vector keys, Dictionary data){ this(keys, data, keys.size()); }
  /** keep together the values associated with the give keys */
  public MultipleKey(Vector keys, Dictionary data, int size)
  {
    super(size);
    for(; elementCount<keys.size(); elementCount++)
      elementData[elementCount]= checkNull(data.get(keys.elementAt(elementCount)));
  }

  /** keep together the values associated with the give keys */
  public MultipleKey(Vector indexes, Object[]data){ this(indexes, data, indexes.size()); }
  /** keep together the values associated with the give keys */
  public MultipleKey(Vector indexes, Object[] data, int size)
  {
    super(size);
    for(; elementCount<indexes.size(); elementCount++)
      elementData[elementCount]= 
	checkNull(data[((Integer)indexes.elementAt(elementCount)).intValue()]);
  }
  
  public void setAt(Object o, int n)
  {
    if(elementCount<=n)
      elementCount=n+1;
    elementData[n]=checkNull(o);
  }

  protected Object checkNull(Object o)
  {
    return o==null?theNull:o; 
  }

  /** see if this bunch is equal with another one */
  public boolean equals(Object o)
  {
    for(int i= 0; i< elementCount; i++)
      if(!((MultipleKey)o).elementAt(i).equals(elementData[i]))
	return false;
    return true;
  }
  
  /** compute a hash code of this bunch by adding the individual hashcodes */
  public int hashCode()
  {
    int ret= 0;
    for(int i= 0; i< elementCount; i++)
      ret+=elementData[i].hashCode();
    return ret;
  }
}


