package org.makumba.db.sql;
import java.sql.*;
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








