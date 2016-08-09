package org.makumba.abstr;
import java.util.*;
import org.makumba.*;

public class dateHandler extends FieldHandler
{
  static final Date empty ;
  public Object getEmptyValue() { return empty; }
  public Date getDefaultDate(){ return (Date)getDefaultValue(); }
  public String getDataType() { return "datetime"; }
  public Class getJavaType() { return java.util.Date.class; }
  public Object getNull() { return Pointer.NullDate; }
  public Object checkValueImpl(Object value) { return normalCheck(value); }

    static{
	Calendar c=  new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
	c.clear();
	c.set(1900, 0, 1);
	empty=c.getTime();
    }

}



