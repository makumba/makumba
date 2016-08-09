package org.makumba;

/** A database error */
public class DBError extends MakumbaError
{
  public DBError(Throwable reason){ super(reason); }
  public DBError(Throwable reason, String command)
  {
    super(reason, command!=null?"\nin db command "+command:""); 
  }
}
