package org.makumba.importer;
import java.util.*;
import org.makumba.*;

public class intEnumImporter extends FieldImporter
{
  public Object getValue(String s)
  {
    s=(String)super.getValue(s);
    if(s.trim().length()==0)
      return null;
    Enumeration f=getValues();
    for(Enumeration e=getNames(); e.hasMoreElements();)
      {
	String v=(String)e.nextElement();
	Integer i=(Integer)f.nextElement();
	if(v.equals(s))
	  return i;
      }
    warning("illegal value: \""+s+"\"");
    return null;
  }
}
