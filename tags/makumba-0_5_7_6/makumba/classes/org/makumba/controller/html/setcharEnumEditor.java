package org.makumba.controller.html;

public class setcharEnumEditor extends choiceEditor
{
  public Object getOptions(){return null; }

  public int getOptionsLength(Object opts){ return getEnumeratorSize(); }

  public Object getOptionValue(Object options, int i)
  { return getStringAt(i); }

  public String formatOptionValue(Object opts, int i, Object val)
  { return val.toString(); }
  
  public String formatOptionTitle(Object options, int i)
  { return getNameAt(i); } 

  public String getMultiple() { return " multiple"; }

  public int getDefaultSize() { return getEnumeratorSize(); }
}
