package org.makumba.db.sql;
import java.sql.*;
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
