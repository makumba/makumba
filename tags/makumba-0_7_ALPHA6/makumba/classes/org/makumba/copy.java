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

import org.makumba.commons.NamedResources;
import org.makumba.providers.TransactionProvider;

/** Copies one database to the other. Usage: 
 *  <code>java org.makumba.copy source dest type1 [type2 ...]</code>
   * Copying is logged (see {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the <b><code>"db.admin.copy"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
*/
public class copy
{
  public static void main(String[]argv) 
  {
    int exit=0;
	int nargs= argv.length;
	int firstArg=0;
	boolean ignoreDbsv=false;
	
	while(nargs>0 && argv[firstArg].startsWith("-")){
		if(argv[firstArg].equals("-ignoreDbsv")){
			ignoreDbsv=true;
		}
		else{
			usage();
			System.exit(1);
		}
		nargs--;
		firstArg++;
	}
    if(nargs<2)
      {
		usage();
		exit=1;
      }
    else
      {
		try{
		  String [] types;
		  if(nargs==2)
		    {
		      Vector v= org.makumba.MakumbaSystem.mddsInDirectory("dataDefinitions");
		      types= new String[v.size()];
		      for(int i=0; i<v.size(); i++)
				  types[i]= (String)v.elementAt(i);
		    }
		  else
		    {
		      types= new String[nargs-2];
		      System.arraycopy(argv, firstArg+2, types, 0, types.length);
		    }
		  copy._copy(argv[firstArg], argv[firstArg+1], types, ignoreDbsv);
		  java.util.logging.Logger.getLogger("org.makumba." + "system").info("destroying makumba caches");
        NamedResources.cleanup();
		}catch(Throwable tr){ tr.printStackTrace(); exit=1;usage(); }
      }
    System.exit(exit);
  }
  
  static void usage()
  {
    System.err.println("org.makumba.copy copies data of several types and their subtypes from one database to another. All types must have an admin# confirmation in the destination database connection file.All information copied previously from the source database is deleted.\r\nUsage: \r\n java org.makumba.copy source destination [type1 type2 ...]\r\nIndicate the databases as hostname_jdbcsubprotocol_databasename. If no types are indicated, data of all known types is copied\n"+
			"\t java org.makumba.copy [-ignoreDbsv] host1_subprotocol1_db1 host2_subprotocol2_db2 [type1 type2...]\n"+
			"if -ignoreDbsv is not indicated, only records with the dbsv indicated in host1_subprotocol1_db1.properties will be copied"
			);
  }

/**
 * Copies records of certain types (and their subtypes) from a database to another. The destination database must
 * have admin# confirmations that match each of the indicated types Copying is logged (see
 * {@link java.util.logging.Logger}, {@link org.makumba.MakumbaSystem#setLoggingRoot(java.lang.String)}) in the
 * <b><code>"db.admin.copy"</code></b> logger, with {@link java.util.logging.Level#INFO} logging level.
 */
public static void _copy(String sourceDB, String destinationDB, String[] typeNames, boolean ignoreDbsv) {
    (TransactionProvider.getInstance())._copy(sourceDB, destinationDB, typeNames, ignoreDbsv);
}
  
}
