package org.makumba.util;
import java.io.*;

/** wrap an exception to throw it further as a desired exception type. stacktraces of this exception will actually print the stracktrace of the wrapped exception */
public class WrappedException extends Exception
{
  Throwable t;

  /** wrap the given exception */
  public WrappedException(Throwable e)
  {
    super(e.toString());
    this.t=e;
  }

  /** wrap the given exception */
  public WrappedException(Throwable e, String s)
  {
    super(e.toString()+(s!=null?s:""));
    this.t=e;
  }
  
  /** wrap nothing */
  public WrappedException(){};
  
  /** wrap nothing, store a message */
  public WrappedException(String s){ super(s); }

  /** return the wrapped exception */
  public Throwable getReason() { return t; }

  /** print the stacktrace of the wrapped exception */
  public void printStackTrace() 
  {
    try{
      t.printStackTrace();  
    }catch(NullPointerException npe){ super.printStackTrace(); }
  }

  /** print the stacktrace of the wrapped exception */
  public void printStackTrace(PrintStream ps)
  { 
    try{
      t.printStackTrace(ps);
    }catch(NullPointerException npe){ super.printStackTrace(ps); }
  }

  /** print the stacktrace of the wrapped exception */
  public void printStackTrace(PrintWriter ps)
  { 
    try{
      t.printStackTrace(ps);
    }catch(NullPointerException npe){ super.printStackTrace(ps); }
  }

  /** the class name of this exception, then the toString of the wrapped */
  public String toString()
  { 
    try{
      return getClass().getName()+": "+t.toString();
    }catch(NullPointerException npe){ return super.toString(); }
  }
}
