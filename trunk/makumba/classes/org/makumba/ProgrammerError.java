package org.makumba;

/** this error is thrown when a handler method is missing but it is needed by some controller */
public class ProgrammerError extends MakumbaError
{
  public ProgrammerError(String expl){ super(expl); }
}
