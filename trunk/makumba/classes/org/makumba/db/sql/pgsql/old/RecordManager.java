package org.makumba.db.sql.pgsql.old;

/** just a surogate class to indicate there is a small handler sub-family here
 * There is one type redirectation in this family: charEnum=char 
 * RecordHandler should have loaded org.makumba.db.sql.pgsql.charManager when it saw the similar redirection in the upper family, so there is a small bug here */
public class RecordManager extends org.makumba.db.sql.pgsql.RecordManager
{
  static{
    System.out.println(""+(char)7+"old Postgres driver in use");
  }
}
