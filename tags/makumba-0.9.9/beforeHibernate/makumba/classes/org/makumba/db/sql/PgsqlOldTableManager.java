/*
 * Created on 18-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.db.sql;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Bart
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PgsqlOldTableManager extends PgsqlTableManager
{
	//Moved from pgsql.old.RecordManager
	static{
	    System.out.println(""+(char)7+"old Postgres driver in use");
	  }
	
	//moved from pgsql.old.charManager
	 /** trims the data at the left. for old databases */
	  public Object get_char_Value(String fieldName, ResultSet rs, int i)
	    throws SQLException
	    {
	      Object o=super.get_char_Value(fieldName, rs, i);
	      if(o==null)
		return null;
	      String s= o.toString();
	      i= s.length()-1;
	      while(i>=0 && s.charAt(i)==' ')
		i--;
	      return s.substring(0, i+1);
	    } 
}
