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

package org.makumba;
import java.util.Vector;

/** Deletes records from a database. The database configuration must have admin# confirmations that match each of the indicated types. 
 * <code>  java org.makumba.delete destinationDb type1 [type2 ...]</code>
 * Deletion is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.delete"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
*/
public class delete
{
  public static void main(String[]argv) 
  {
    int exit=0;
    if(argv.length<1)
      {
	usage();
	exit=1;
      }
    else
      {
	Transaction db1=null;

	try{
	  String [] types;
	  if(argv.length==1)
	    {
	      Vector v= org.makumba.MakumbaSystem.mddsInDirectory("dataDefinitions");
	      types= new String[v.size()];
	      for(int i=0; i<v.size(); i++)
		types[i]= (String)v.elementAt(i);
	    }
	  else
	    {
	      types= new String[argv.length-1];
	      System.arraycopy(argv, 1, types, 0, types.length);
	    }
	  MakumbaSystem._delete(argv[0], argv[0], types);
	}catch(Throwable tr){ tr.printStackTrace(); exit=1; usage(); }
      }
    System.exit(exit);
  }
  
  static void usage()
  {
    System.err.println("org.makumba.delete deletes data of several types and their subtypes from  the database. ONLY data originating in the local database (data with the DBSV indicated in the connection file) is deleted. All types must have an admin# confirmation in the database connection file.\r\nUsage: \r\n java org.makumba.delete db [type1 type2 ...]\r\nIndicate the database as hostname_jdbcsubprotocol_databasename. If no type is indicated, data of all known types is deleted.");
  }
  
}
