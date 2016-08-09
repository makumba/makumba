package org.makumba;

/** An unauthorized exception, due to mismatch of attribute values */
public class UnauthorizedException extends LogicException
{
  public UnauthorizedException(){super("");}
  public UnauthorizedException(String message){super(message);}
}
