package org.makumba.importer;
import java.util.*;
import org.makumba.*;

public class noneImporter extends FieldImporter
{
  public void configure(Properties markers)
  {
    super.configure(markers);

    if(begin!=null)
      throw new MakumbaError("You cannot have markers for fields of type "+getType());
  }
  
  public boolean isIgnored() { return true; }
}
