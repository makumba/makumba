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
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Dictionary;

/** this sets a creation date in inserts, changes it in updates. it creates timestamps at the java level. SQL drivers might provide better implementations*/
public class dateModifyJavaManager extends wrapperManager
{
  protected FieldManager makeWrapped(org.makumba.abstr.RecordHandler rh)
  {
    return (FieldManager)rh.makeHandler("timestamp");
  }

  void nxt(Dictionary d)
  {
    d.put(getName(), new Timestamp(new java.util.Date().getTime()));
  }

  public void checkInsert(Dictionary d)
  {
    Object o=d.get(getName());
    if(o!=null)
      {
	checkCopy("modification date");
	d.put(getName(), checkValue(o));
      }
  }

  public void checkUpdate(Dictionary d)
  {
    Object o=d.get(getName());
    if(o!=null)
      throw new org.makumba.InvalidValueException(getFieldInfo(), "you cannot update a modification date");
  }

  public void setInsertArgument(PreparedStatement ps, int n, Dictionary d) throws SQLException
  {
    if(d.get(getName())!=null)
      {
	super.setInsertArgument(ps, n, d);
	return;
      }
    nxt(d);
    super.setInsertArgument(ps, n, d);
  }

  public String inInsert(Dictionary d)
  {
    if(d.get(getName())!=null)
      {
	return super.inInsert(d);
      }
    nxt(d);
    return super.inInsert(d);
  }

  public void setCopyArgument(PreparedStatement ps, int n, Dictionary d) throws SQLException
  {
    super.setCopyArgument(ps, n, d);
  }

  public String inCopy(Dictionary d)
  {
    return super.inInsert(d);
  }

  public void setUpdateArgument(PreparedStatement ps, int n, Dictionary d) throws SQLException
  {
    nxt(d);
    super.setUpdateArgument(ps, n, d);
  }

  public String inUpdate(Dictionary d)
  {
    nxt(d);
    return super.inUpdate(d);
  }
}
