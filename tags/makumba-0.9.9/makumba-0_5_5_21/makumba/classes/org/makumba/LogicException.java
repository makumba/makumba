package org.makumba;

/** An exception thrown during the execution of some business logic code */
public class LogicException extends org.makumba.util.WrappedException
{
  public LogicException(Throwable t) { super(t); }
  public LogicException(String s){ super(s); }
}
