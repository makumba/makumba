package org.makumba.db.sql;
import org.makumba.*;

/** the SQL pointer, represents a pointer as a long, but only an int is needed... */
public class SQLPointer extends Pointer
{
  static long compute(int dbsv, int uid)
  {
    return (dbsv<<MASK_ORDER)+uid;
  }

  static int getMaskOrder() { return MASK_ORDER; }

  private SQLPointer(String type)
  {
    if(type==null)
      throw new NullPointerException();
    this.type=type; 
  }

  SQLPointer(String type, long n){ this(type); this.n= n;  }

  SQLPointer(String type, int dbsv, int uid)
  {
    this(type);
    if(uid > (1<<MASK_ORDER))
      {
	MakumbaSystem.getMakumbaLogger("debug.db").finest("p");
	n= uid;
      }
    else
      n=compute(dbsv, uid);
  }

  public SQLPointer(String type, String s)
  {
    this(type);
    int separator=s.indexOf(":");
    int dbsv=new Integer(s.substring(0,separator)).intValue();
    int uid=new Integer(s.substring(separator+1,s.length())).intValue();
    n= compute (dbsv, uid);
  }
}
