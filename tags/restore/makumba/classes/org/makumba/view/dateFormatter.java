package org.makumba.view;
import org.makumba.abstr.*;
import java.util.*;
import java.text.*;

public class dateFormatter extends FieldFormatter
{
  static String[] params= { "format" };
  static String[][] paramValues= { null };
  public String[] getAcceptedParams(){ return params; }
  public String[][] getAcceptedValue(){ return paramValues; }

  public String formatNotNull(Object o, Dictionary formatParams) 
  {
    DateFormat formatter=yearDate;
    String s=(String)formatParams.get("format");
    if(s!=null)
      {
	formatter= new SimpleDateFormat(s, org.makumba.MakumbaSystem.getLocale());
	formatter.setCalendar(calendar);
      }      

    return formatter.format((java.util.Date)o); 
  }

  public static final Calendar calendar;
  
  public static final DateFormat yearDate;
  public static final DateFormat debugTime;

  static {
    calendar= new GregorianCalendar(org.makumba.MakumbaSystem.getTimeZone());
    yearDate= new SimpleDateFormat("dd MMMM yyyy",org.makumba.MakumbaSystem.getLocale());
    debugTime= new SimpleDateFormat("d MMMM yyyy HH:mm:ss zzz",org.makumba.MakumbaSystem.getLocale());
    yearDate.setCalendar(calendar);
    debugTime.setCalendar(calendar);
  }
}
