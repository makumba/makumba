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
    if(argv.length<2)
      {
	usage();
	exit=1;
      }
    else
      {
	try{
	  String [] types;
	  if(argv.length==2)
	    {
	      Vector v= org.makumba.MakumbaSystem.mddsInDirectory("dataDefinitions");
	      types= new String[v.size()];
	      for(int i=0; i<v.size(); i++)
		types[i]= (String)v.elementAt(i);
	    }
	  else
	    {
	      types= new String[argv.length-2];
	      System.arraycopy(argv, 2, types, 0, types.length);
	    }
	  MakumbaSystem._copy(argv[0], argv[1], types);
	}catch(Throwable tr){ tr.printStackTrace(); exit=1;usage(); }
      }
    System.exit(exit);
  }
  
  static void usage()
  {
    System.err.println("org.makumba.copy copies data of several types and their subtypes from one database to another. All types must have an admin# confirmation in the destination database connection file.All information copied previously from the source database is deleted.\r\nUsage: \r\n java org.makumba.copy source destination [type1 type2 ...]\r\nIndicate the databases as hostname_jdbcsubprotocol_databasename. If no types are indicated, data of all known types is copied");
  }
  
}
