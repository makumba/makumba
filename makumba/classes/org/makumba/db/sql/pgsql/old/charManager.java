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

package org.makumba.db.sql.pgsql.old;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public class charManager extends org.makumba.db.sql.pgsql.charManager
{
  /** allows CHAR type, not only varchar. for old databases */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return super.unmodified(rsm, index) || rsm.getColumnType(index)==Types.CHAR;
  }

  /** trims the data at the left. for old databases */
  public Object getValue(ResultSet rs, int i)
    throws SQLException
    {
      Object o=super.getValue(rs, i);
      if(o==null)
	return null;
      String s= o.toString();
      i= s.length()-1;
      while(i>=0 && s.charAt(i)==' ')
	i--;
      return s.substring(0, i+1);
    } 
}
