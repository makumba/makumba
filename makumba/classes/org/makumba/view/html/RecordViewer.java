package org.makumba.view.html;
import org.makumba.*;
import org.makumba.abstr.*;
import org.makumba.view.*;
import java.util.*;

public class RecordViewer extends RecordFormatter
{
  public RecordViewer(ComposedQuery q) { super(q); }
  public RecordViewer(RecordInfo ri, Hashtable h) { super(ri, h); }  

  protected String applyParameters(FieldFormatter ff, Dictionary formatParams, String s)
  {
    if(formatParams.get("urlEncode")!=null)
      return java.net.URLEncoder.encode(s);
    return s;
  }

}
