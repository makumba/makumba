package org.makumba.view.jsptaglib;
import java.util.*;

public class errorEditor extends FieldEditor
{
  public String formatShow(Object o, Dictionary formatParam)
  {
    throw new org.makumba.view.InvalidValueException(this, "cannot edit fields of type "+getType());
  }
}
