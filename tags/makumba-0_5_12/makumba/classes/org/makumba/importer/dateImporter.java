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




