package org.makumba.importer;
import java.util.*;
import org.makumba.*;
import java.text.*;

public class dateImporter extends FieldImporter
{
  Vector formats=new Vector();

  public void configure(Properties markers)
  {
    super.configure(markers);
    for(Enumeration e=markers.keys(); e.hasMoreElements(); )
      {
	String s=(String)e.nextElement();
	if(s.startsWith(getName()+".format"))
	    {
		SimpleDateFormat dateFormat = new SimpleDateFormat(markers.getProperty(s).trim(), MakumbaSystem.getLocale());
		dateFormat.setTimeZone(MakumbaSystem.getTimeZone());
		dateFormat.setLenient(false);
		formats.addElement(dateFormat);
	    }
      }
    if(formats.size()==0)
      configError= makeError("has no format indicated. Use \""+getName()+".format=MM yy dd\" in the marker file.\nSee the class java.text.SimpleDateFormat to see how to compose the formatter");
  }

  public Object getValue(String s)
  {
    if(s.trim().length()==0)
      return null;
    ParseException lastpe=null;

    for(Enumeration e= formats.elements(); e.hasMoreElements(); )
	{
	    SimpleDateFormat f=(SimpleDateFormat)e.nextElement();
	    try{
		
		return f.parse(s); 
	    } catch(ParseException pe) {lastpe=pe; }
	}
    warning(lastpe);
    return null;
  }
}




