package org.makumba.abstr;
import org.makumba.*;

public class ptrOneHandler extends ptrIndexHandler implements subtableHandler
{
  public RecordInfo getSubtable(){ return (RecordInfo)fi.extra1; }

  public RecordInfo getPointedType() 
  {
    return getSubtable();
  }
}
