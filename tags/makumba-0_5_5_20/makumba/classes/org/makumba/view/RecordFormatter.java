package org.makumba.view;
import org.makumba.abstr.*;
import java.util.*;

public class RecordFormatter extends RecordHandler
{
  public RecordFormatter(RecordInfo ri) 
  {
    super(ri); 
  }
  
  public RecordFormatter(ComposedQuery q) 
  {
    super((RecordInfo)q.getResultType()); 
    for(int i=0; i<handlerOrder.size(); i++)
      ((FieldFormatter)handlerOrder.elementAt(i)).initExpr(q.getProjectionAt(i));
  }

  public RecordFormatter(RecordInfo ri, java.util.Hashtable names) 
  {
    super(ri);
    for(int i=0; i<handlerOrder.size(); i++)
      {
	FieldFormatter ff= (FieldFormatter)handlerOrder.elementAt(i);
	ff.initExpr((String)names.get(ff.getName()));
      }
  }

  protected String applyParameters(FieldFormatter ff, Dictionary formatParams, String s)
  { return s; }

  public String format(int i, Object value, Dictionary formatParams)
  {
    FieldFormatter ff= (FieldFormatter)handlerOrder.elementAt(i); 
    ff.checkParams(formatParams);
    return applyParameters(ff, formatParams, ff.format(value, formatParams));
  }
}
