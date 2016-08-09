package org.makumba.view;
import org.makumba.abstr.*;
import java.util.*;
import java.text.*;

public class timestampFormatter extends dateFormatter
{

  public String formatNotNull(Object o, Dictionary formatParams) 
  {
    DateFormat formatter=timestamp;
    String s=(String)formatParams.get("format");
    if(s!=null)
      {
	formatter= new SimpleDateFormat(s, org.makumba.MakumbaSystem.getLocale());
	formatter.setCalendar(calendar);
      }      

    return formatter.format((java.util.Date)o); 
  }


  public static final DateFormat timestamp;

  static {
    timestamp= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S",org.makumba.MakumbaSystem.getLocale());
    timestamp.setCalendar(calendar);
  }

}
