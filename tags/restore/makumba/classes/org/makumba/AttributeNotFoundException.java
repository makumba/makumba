package org.makumba;

/** An exception thrown if an attribute is not found */
public class AttributeNotFoundException extends LogicException
{
  /** construct an exception message from the name of the missing attribute */
  public AttributeNotFoundException(String attName) { super("attribute not found: "+attName); }
}
