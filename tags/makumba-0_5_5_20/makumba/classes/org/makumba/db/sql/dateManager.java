package org.makumba.db.sql;
import java.sql.*;
import org.makumba.abstr.FieldHandler;

/** This handler just replaces itself with two handlers, one for the date part, the other for the time part
  */
public class dateManager extends FieldManager
{
  /** returns a dateSQL and a time */
  public Object replaceIn(org.makumba.abstr.RecordHandler rh)
  { 
    FieldHandler[] ret;
    Database d= ((RecordManager)rh).getSQLDatabase();
    if(d.getConfiguration("separateDate")!=null)
      {
	ret= new FieldHandler[1];
    	ret[0]= rh.makeHandler("dateTime");
      }
    else
      {
	ret= new FieldHandler[2];
	ret[0]= rh.makeHandler("dateSQL");
	ret[1]= rh.makeHandler("time");
      }
    return ret;

  }
}
