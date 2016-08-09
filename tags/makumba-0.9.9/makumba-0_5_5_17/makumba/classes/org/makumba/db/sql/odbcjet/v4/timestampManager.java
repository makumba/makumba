package org.makumba.db.sql.odbcjet.v4;
import java.text.*; 
import java.util.*;
import java.sql.*;

/** Because of odbc4 bug with ts literals
	*	we use format without tenths of seconds for timestamp 
	*	"dd-MM-yyyy hh:mm:ss"
	**/
public class timestampManager extends org.makumba.db.sql.odbcjet.timestampManager
{
  static DateFormat odbcDate
  =new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.UK);
 
  public String writeConstant(Object o)
  { return "\'"+odbcDate.format( new Timestamp(((java.util.Date)o).getTime() )) +"\'"; }

}