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
import java.sql.*;

/** this deals with SQL chars */
public class charManager extends FieldManager
{
  /** returns char */
  protected String getDBType()
  {
    return "VARCHAR";
  }

  protected int getSQLType()
  {
    return java.sql.Types.VARCHAR;
  }

  /** Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this returns true. Some SQL drivers allocate more anyway. */
  protected boolean unmodified(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    return (super.unmodified(rsm, index) || rsm.getColumnType(index)== java.sql.Types.CHAR)&& checkWidth(rsm, index);
  }

  /** check the char width */
  protected boolean checkWidth(ResultSetMetaData rsm, int index)
       throws SQLException
  {
    // some drivers might allocate more, it's their business
    return rsm.getColumnDisplaySize(index)>=getWidth();
  }

  /** Checks if the type is java.sql.Types.CHAR. Then, if the size of the SQL column is still large enough, this returns true. Some SQL drivers allocate more anyway. */
  protected boolean unmodified(int type, int size, java.util.Vector columns, int index)
       throws SQLException
  {
    return (super.unmodified(type, size, columns, index) 
	    || type== java.sql.Types.CHAR)&& checkWidth(size);
  }

  /** check the char width */
  protected boolean checkWidth(int width)
       throws SQLException
  {
    // some drivers might allocate more, it's their business
    return width>=getWidth();
  }

  /** write in CREATE, in the form name char[size] */
  public String inCreate(Database d)
    {
        String s= Database.getEngineProperty(d.getEngine()+"."+"charBinary");
        if(s!=null && s.equals("true"))
            s=" BINARY";
        else
            s="";
        //should width be computed by getDBType() instead?
        return getDBName()+" "+getDBType(d)+"("+getWidth()+")"+s+(isUnique()?" UNIQUE":"");
        //return super.inCreate(d)+"("+getWidth()+")"+s;
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
    if(o instanceof byte[])
        return new String((byte[])o);
    return o;
  }

}
