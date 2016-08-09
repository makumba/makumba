package org.makumba;
import java.util.*;
import java.io.*;

/** Copies one database to the other. Usage: 
 *  <code>java org.makumba.copy source dest type1 [type2 ...]</code>
   * Copying is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.copy"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
*/
public class copy
{
  public static void main(String[]argv) 
  {
    int exit=0;
    if(argv.length<3)
      {
	usage();
	exit=1;
      }
    else
      {
	try{
	  String [] types= new String[argv.length-2];
	  System.arraycopy(argv, 2, types, 0, types.length);
	  MakumbaSystem._copy(argv[0], argv[1], types);
	}catch(Throwable tr){ usage(); tr.printStackTrace(); exit=1; }
      }
    System.exit(exit);
  }
  
  static void usage()
  {
    System.err.println("org.makumba.copy copies data of several types and their subtypes from one database to another. All types must have an admin# confirmation in the destination database connection file.All information copied previously from the source database is deleted.\r\nUsage: \r\n java org.makumba.copy source destination type1 [type2 ...]\r\nIndicate the databases as hostname_jdbcsubprotocol_databasename");
  }
  
}
