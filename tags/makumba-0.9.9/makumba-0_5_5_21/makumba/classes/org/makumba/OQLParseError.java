package org.makumba;

/** An OQL parse error */
public class OQLParseError extends MakumbaError
{
  /** Construct an OQL parse error with the given explanation */
  public OQLParseError(String explanation){super(explanation); }
}
