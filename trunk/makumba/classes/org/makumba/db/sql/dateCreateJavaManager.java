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
import java.util.Dictionary;

/** this sets a creation date in inserts, and keeps it fixed in updates. At insert, it creates a timestamp at the java level (which is then used by dateModifyManager too). At update it does nothing. SQL drivers might provide better implementations
 */
public class dateCreateJavaManager extends wrapperManager
{
  String modfield;

  protected FieldManager makeWrapped(org.makumba.abstr.RecordHandler rh)
  {
    return (FieldManager)rh.makeHandler("timestamp");
  }

  protected void setRecord(org.makumba.abstr.RecordHandler rh)
  {
    modfield= rh.getRecordInfo().getModifyName();
  }

  void nxt(Dictionary d)
  {
    d.put(getName(), d.get(modfield));
  }

  public void checkInsert(Dictionary d)
  {
    Object o=d.get(getName());
    if(o!=null)
      {
	checkCopy("creation date");
	d.put(getName(), checkValue(o));
      }
  }

  public void checkUpdate(Dictionary d)
  {
    Object o=d.get(getName());
    if(o!=null)
      throw new org.makumba.InvalidValueException(getFieldInfo(), "you cannot update a creation date");
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

  public String inUpdate(Dictionary d)
  {
    throw new RuntimeException("shouldn't be called");
  }

  public void setUpdateArgument(PreparedStatement ps, int n, Dictionary d) 
  {
    throw new RuntimeException("shouldn't be called");
  }
}








