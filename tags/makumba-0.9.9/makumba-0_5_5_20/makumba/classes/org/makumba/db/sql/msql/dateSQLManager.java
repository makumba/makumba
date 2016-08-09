package org.makumba.db.sql.msql;
import java.text.*;
import java.util.*;

/** msql writes date constants quite differrently */
public  class dateSQLManager extends org.makumba.db.sql.dateSQLManager
{
  static DateFormat msqlDate
  =new SimpleDateFormat("dd-MMM-yyyy", Locale.UK);

  public String writeConstant(Object o)
    { 
      return "\'"+msqlDate.format((java.util.Date)o)+"\'";
    }
}
