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

package org.makumba.db;
import java.util.Vector;

/** Copies one database to the other.
*/
public class open
{
  public static void main(String[]argv)  
  {
    Database d=null;
    try{
      if(argv.length==0)
	d= Database.findDatabase("MakumbaDatabase.properties");
      else
	d= Database.getDatabase(argv[0]);
      String[] tables;
      if(argv.length<2)
	{
	  Vector v= org.makumba.MakumbaSystem.mddsInDirectory("dataDefinitions");
	  tables= new String[v.size()];
	  for(int i=0; i<v.size(); i++)
	    tables[i]= (String)v.elementAt(i);
	}
      else
	{
	  tables= new String[argv.length-1];
	  System.arraycopy(argv, 1, tables, 0, tables.length);
	}
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
