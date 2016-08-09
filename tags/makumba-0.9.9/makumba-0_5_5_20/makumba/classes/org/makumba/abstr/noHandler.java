package org.makumba.abstr;

/** This type is used when a certain type is not treated by a certain handler
 */
public class noHandler extends FieldHandler
{
  /** returns null, i.e. don't add any field handler for this field */
  public Object replaceIn(org.makumba.abstr.RecordHandler rh){ return null; }
}
