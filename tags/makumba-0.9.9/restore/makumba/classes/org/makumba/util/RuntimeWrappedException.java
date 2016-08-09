package org.makumba.util;
import java.io.*;

/** wrap an exception to throw it further as a RuntimeException. stacktraces of this exception will actually print the stracktrace of the wrapped exception */
public class RuntimeWrappedException extends RuntimeException
{
  Throwable t;

  /** wrap the given exception */
  public RuntimeWrappedException(Throwable e)
  {
    super(e.toString());
    this.t=e;
  }
  
  /** return the wrapped exception */
  public Throwable getReason() { return t; }

  /** wrappee's stacktrace */
  public void printStackTrace() { t.printStackTrace();  }

  /** wrappee's stacktrace */
  public void printStackTrace(PrintStream ps){ t.printStackTrace(ps);}

  /** wrappee's stacktrace */
  public void printStackTrace(PrintWriter ps){ t.printStackTrace(ps);}
  
  /** toString of the wrapee */
  public String toString(){ return "RuntimeWrappedException: "+t.toString();}
}
