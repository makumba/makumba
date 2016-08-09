package org.makumba.view;
import java.util.Dictionary;

public class intEnumFormatter extends FieldFormatter
{
  public String formatNotNull(Object o, Dictionary formatParams) { return getNameFor(((Integer)o).intValue()); }
}
