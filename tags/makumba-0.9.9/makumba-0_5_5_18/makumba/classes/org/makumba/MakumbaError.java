package org.makumba;
import java.io.*;

/** A generic Makumba fatal error, due to misconfiguration, bad Data Definition (MDD) syntax, bad OQL syntax, database fatal error, etc. 
  Such an error usually denotes either a configuration mistake from the makumba API user, either a fatal database problem that makes it impossible for the makumba application to work. Like all errors, Makumba errors don't need to be caught, as they occur in "terminal" conditions anyway. Most makumba errors come from exceptions, which can be retrieved calling getReason()
*/
public class MakumbaError extends Error
{
  Throwable t;
  String explanation;

  /**  Build a makumba error and associate it with the given reason */
  public MakumbaError(Throwable reason)
  {
    super(reason.toString());
    this.t=reason;
  }

  /**  Build a makumba error and associate it with the given reason and explanation text*/
  public MakumbaError(Throwable reason, String expl)
  {
    super(reason.toString()+(expl!=null?expl:""));
    this.t=reason;
    this.explanation=expl;
  }
  
  /** Build an empty makumba error */
  public MakumbaError(){};
  
  /** Build a makumba error with the given explanation */
  public MakumbaError(String explanation){ super(explanation); }

  /** Return the reason for this error */
  public Throwable getReason() { return t; }

  /**Print the stacktrace of the reason exception, if any, otherwise print a normal stack trace*/
  public void printStackTrace() 
  {
    if(explanation!=null)
      System.out.println(explanation);
    if(t==null)
      super.printStackTrace(); 
    else
      t.printStackTrace();  
  }

  /**Print the stacktrace of the reason exception, if any, otherwise print a normal stack trace*/
  public void printStackTrace(PrintStream ps)
  { 
    try{
      if(explanation!=null)
	ps.println(explanation);
      t.printStackTrace(ps);
    }catch(NullPointerException npe){ super.printStackTrace(ps); }
  }

  /**Print the stacktrace of the reason exception, if any, otherwise print a normal stack trace*/
  public void printStackTrace(PrintWriter ps)
  { 
    try{
      if(explanation!=null)
	ps.println(explanation);
      t.printStackTrace(ps);
    }catch(NullPointerException npe){ super.printStackTrace(ps); }
  }

  /**A string representation of this error, for debugging */
  public String toString()
  { 
    try{
      return getClass().getName()+": "+t.toString();
    }catch(NullPointerException npe){ return super.toString(); }
  }
}
