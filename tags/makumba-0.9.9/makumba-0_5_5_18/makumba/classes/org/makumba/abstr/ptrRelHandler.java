package org.makumba.abstr;
import org.makumba.*;

public class ptrRelHandler extends ptrIndexHandler
{
  public RecordInfo getForeignTable(){ return (RecordInfo)fi.extra1; }

  public boolean compatible(FieldInfo fi){ return fi.getType().equals(getType()) && fi.extra1 instanceof RecordInfo && ((RecordInfo)fi.extra1).getName().equals(getForeignTable().getName()); }

  public RecordInfo getPointedType() 
  {
    return getForeignTable();
  }
}
