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

package org.makumba.db.sql.qed;
import java.sql.*;

/** this deals with SQL chars */
public class charManager extends org.makumba.db.sql.charManager
{
  /** returns char */
   protected String getDBType()
   {
     return "VARCHAR";
   }

 /** Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this returns true. Some SQL drivers allocate more anyway. */
  protected boolean unmodified(int type, int size, java.util.Vector columns, 
			       int index)
       throws java.sql.SQLException
  {
    return super.unmodified(type, size, columns, index) || 
      type==java.sql.Types.VARCHAR;
  }

}
