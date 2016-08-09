package org.makumba.importer;
import java.util.*;
import org.makumba.*;

public class textImporter extends FieldImporter
{
  public void configure(Properties markers)
  {
    super.configure(markers);
  }

  public boolean shouldEscape() {return false; }

}
