package org.makumba.view;
import org.makumba.*;
import java.util.Dictionary;

public class ptrFormatter extends FieldFormatter
{
  public String formatNotNull(Object o, Dictionary formatParams) 
  {return ((Pointer)o).toExternalForm(); }
}
