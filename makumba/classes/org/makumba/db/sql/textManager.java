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

package org.makumba.db.sql;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.makumba.Text;

/** this deals with SQL uncontrolled-length fields */
public class textManager extends FieldManager
{
  /** returns text */
  protected String getDBType()
  {
      return "LONG VARBINARY";
  }
  
  protected int getSQLType()
  {
    return java.sql.Types.LONGVARBINARY;
  }

  /** does apostrophe escape */
  public String writeConstant(Object o)
  { return org.makumba.db.sql.Database.SQLEscape(o.toString()); }

  /** get the java value of the recordSet column corresponding to this field. This method should return null if the SQL field is null */
  public Object getValue(ResultSet rs, int i)
       throws SQLException
  {
    Object o= super.getValue(rs, i);
    if(o==null )
      return o;
    return Text.getText(o);

    /*
    InputStream is= rs.getBinaryStream(i);
    if(is==null )
      return null;
    return new Text(is);
    */
  }

  public void setArgument(PreparedStatement ps, int n, Object o)
       throws SQLException
  {
    Text t= Text.getText(o);
    ps.setBinaryStream(n, t.toBinaryStream(), t.length());
    //ps.setBytes(n, t.getBytes());
  }

  public boolean shouldIndex(){ return false; }

}
