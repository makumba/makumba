package org.makumba;

/** This error occurs when an undesired business logic exception comes up. It usually denotes a programmer error */
public class LogicInvocationError extends Error
{
  Throwable t;
  public LogicInvocationError(Throwable t){ this.t=t; }
  public Throwable getReason(){ return t; }
  public void printStackTrace(java.io.PrintWriter pw){ t.printStackTrace(pw); }
}
