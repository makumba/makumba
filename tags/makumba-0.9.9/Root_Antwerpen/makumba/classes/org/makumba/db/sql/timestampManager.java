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
import java.sql.Timestamp;

/** Represents a timestamp. Used by dateCreate and dateModify for their DB representation */
public class timestampManager extends FieldManager
{
  /** returns timestamp */
  protected String getDBType()
  {
    return "TIMESTAMP";
  }

  /** writes the date between apostrophes */
  public String writeConstant(Object o)
  { 
    return "\'"+super.writeConstant(o)+"\'"; 
  }

  public int getSQLType()
  {
    return java.sql.Types.TIMESTAMP;
  }

  public void setInsertArgument(PreparedStatement ps, int n, java.util.Dictionary d) throws SQLException
  {
    Object o= d.get(getName());
    if(o instanceof java.util.Date && !(o instanceof Timestamp))
      d.put(getName(), new Timestamp(((java.util.Date)o).getTime()));
    super.setInsertArgument(ps, n,d);
  }

  public Object checkValueImpl(Object value)
    {
	Object o=super.checkValueImpl(value);	
	if(o instanceof java.util.Date && !(o instanceof Timestamp))
	   o= new Timestamp(((java.util.Date)o).getTime());
	return o;
    }

    public Object getValue(ResultSet rs, int i)
       throws SQLException
    {
	Object o= rs.getTimestamp(i);
	if(rs.wasNull())
	    return null;
	//  return getDefaultValue();
	//        if(o instanceof java.lang.BigDecimal)

	// System.out.println(o.getClass());
	return o;
    }

}
