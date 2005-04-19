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
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.makumba.Text;

/** the odbc text is called "longbinary" and works with streams instead of large byte arrays */
public class textManager extends org.makumba.db.sql.textManager
{
  protected String getDBType()
  {
    return "LONGBINARY";
  }

  /** get the java value of the recordSet column corresponding to this field. This method should return null if the SQL field is null */
  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    InputStream in=rs.getBinaryStream(i);
    if(rs.wasNull())
      return null;
    return new Text(in);
  }

  public void setArgument(PreparedStatement ps, int n, Object o)
       throws SQLException
  {    
    Text t= (Text)o;
    if(t.length()==0)
      ps.setBytes(n, new byte[0]);
    else
      ps.setBinaryStream(n, t.toBinaryStream(), t.length());
  }

  public void setNullArgument(PreparedStatement ps, int n)
       throws SQLException
  {
    ps.setNull(n, Types.LONGVARCHAR);
  }
}

