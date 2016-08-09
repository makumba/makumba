package org.makumba.db;
import java.util.*;
import org.makumba.abstr.*;
import org.makumba.db.*;
import java.io.*;

/** Copies one database to the other.
*/
public class open
{
  public static void main(String[]argv)  
  {
    Database d=null;
    try{
      d= Database.getDatabase(argv[0]);
      String[] tables= new String[argv.length-1];
      System.arraycopy(argv, 1, tables, 0, tables.length);
      d.openTables(tables);
    }
    catch(Throwable t)
      {
    	t.printStackTrace();
      }
    finally
      {
	if(d!=null)
	  d.close();
      }
  }
}
