package org.makumba;
import java.util.*;
import java.io.*;

/** Deletes records from a database. The database configuration must have admin# confirmations that match each of the indicated types. 
 * <code>  java org.makumba.delete destinationDb type1 [type2 ...]</code>
 * Deletion is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.delete"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
*/
public class delete
{
  public static void main(String[]argv) 
  {
    int exit=0;
    if(argv.length<2)
      {
	usage();
	exit=1;
      }
    else
      {
	Database db1=null;

	try{
	  String [] types= new String[argv.length-1];
	  System.arraycopy(argv, 1, types, 0, types.length);
	  MakumbaSystem._delete(argv[0], argv[0], types);
	}catch(Throwable tr){ usage(); tr.printStackTrace(); exit=1; }
      }
    System.exit(exit);
  }
  
  static void usage()
  {
    System.err.println("org.makumba.delete deletes data of several types and their subtypes from  database. All types must have an admin# confirmation in the database connection file.\r\nUsage: \r\n java org.makumba.delete db type1 [type2 ...]\r\nIndicate the database as hostname_jdbcsubprotocol_databasename");
  }
  
}
