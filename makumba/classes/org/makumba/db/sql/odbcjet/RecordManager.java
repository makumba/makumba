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

package org.makumba.db.sql.odbcjet;
/** the ODBC RecordHandler, identical with the sql RecordHandler. Due to RecordHandler field generation conventions, it will have different FieldHandler types, according to the org.makumba.db.sql/odbc/redirectManager.properties file: 
<pre>
dateCreate=date
dateModify=date
</pre> 
 * More specifically, dates are written out differently,  with the special dateManager provided in this package. The other handlers are the general SQL handlers. */
public class RecordManager extends org.makumba.db.sql.RecordManager
{
  protected void create(java.sql.Statement st, String tblname, boolean really)
       throws java.sql.SQLException
  {
    super.create(st, tblname, really);
    if(really)
      {
	try{
	  Thread.currentThread().sleep(500);
	}catch(InterruptedException t){}
      }
  }
}

