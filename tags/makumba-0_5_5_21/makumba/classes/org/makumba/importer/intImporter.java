package org.makumba.importer;
import java.util.*;
import org.makumba.*;

public class intImporter extends FieldImporter
{
  public void configure(Properties markers)
  {
    super.configure(markers);
  }

  public boolean shouldEscape() {return false; }

  public Object getValue(String s)
  {
    s=(String)super.getValue(s);
    if(s.trim().length()==0)
      return null;
    try{
      return new Integer(Integer.parseInt((String)super.getValue(s)));
    }catch(Exception e) { warning(e); return null; }
  }
}
