package org.makumba.importer;
import org.makumba.*;
import org.makumba.abstr.*;
import org.makumba.db.*;
import java.util.*;
import java.lang.reflect.*;

/** This class imports makumba objects from fields of Java objects. 
 * Imported classes have the opportunity to say what not to import (field String[] noImport). 
 * They can also do post processing (method boolean importTransform(Object o)) and decide 
 * (by the boolean result of importTransform) whether the object is written or not in the
 * makumba database*/
public class ObjectToRecord
{
  Method transform, clean;
  RecordInfo type;
  Hashtable fields= new Hashtable();

  public ObjectToRecord(Class c, String type)
  {
    try{
      Class args[]={java.util.Hashtable.class, org.makumba.db.Database.class };
      try{
	transform= c.getMethod("importTransform", args);
      } catch(NoSuchMethodException nsme) {}

      try{
	clean= c.getMethod("importClean", args);
      } catch(NoSuchMethodException nsme) {}

      this.type= RecordInfo.getRecordInfo(type);

      Field no=null;
      try{
	no=c.getField("noImport");
      } catch(java.lang.NoSuchFieldException nsfe) {}

      String[] noImp= {};
      if(no!=null)
	noImp=(String[])no.get(null);

      Object dummy="dummy";
      Hashtable noImport= new Hashtable();
     
      for(int i= 0; i<noImp.length; i++)
	noImport.put(noImp[i], dummy);

      Field acc=null;
      try{
	acc=c.getField("accountedImport");
      } catch(java.lang.NoSuchFieldException nsfe) {}

      String[] accountedImp= {};
      if(acc!=null)
	accountedImp=(String[])acc.get(null);

      Hashtable accountedImport= new Hashtable();
     
      for(int i= 0; i<accountedImp.length; i++)
	accountedImport.put(accountedImp[i], dummy);

      for(Enumeration e= this.type.getDeclaredFields(); e.hasMoreElements(); )
	{
	  String s= (String)e.nextElement(); 
	  Field f= null;
	  try {f=c.getField(s); } catch(java.lang.NoSuchFieldException nsfe1) {}
	  if(f!=null)
	    {
	      if(noImport.get(s)==null)
		fields.put(s, f);
	    }
	  else 
	    if(accountedImport.get(s)==null)
	      MakumbaSystem.getMakumbaLogger("import").severe("No Java correspondent for "+type+"."+s+" in "+c.getName());
	}
      Field flds[]= c.getFields();
      for(int i=0; i< flds.length; i++)
	{
	  try{
	    flds[i].get(null);
	  }catch(NullPointerException e)
	    {
	      String s= flds[i].getName();
    
	      if(this.type.getField(s)==null && noImport.get(s)==null)
		MakumbaSystem.getMakumbaLogger("import").severe("No Makumba correspondent for "+c.getName()+"."+s+" in "+type);
	    }
	}

    }catch(Throwable t){ t.printStackTrace(); throw new RuntimeException();}
  }

  boolean cleaned=false;

  public Hashtable importObject(Object o, org.makumba.db.Database db)
  {
    try{
      Hashtable h= new Hashtable();
      Object args[]={h, db};

      h.put(type.getIndexName(), db.getPointer(type.getName(), o.hashCode()));

      for(Enumeration e= fields.keys(); e.hasMoreElements(); )
	{
	  String s= (String)e.nextElement();
	  Object value=((Field)fields.get(s)).get(o);
	  
	  if(value!=null)
	    {
	      if(!value.getClass().getName().startsWith("java"))
		value= db.getPointer(type.getField(s).getForeignTable().getName(),
				     value.hashCode());
	      h.put(s, value);
	    }
	}

      if(!cleaned && clean!=null && ((Boolean)clean.invoke(o, args)).booleanValue())
	{
	  db.deleteFrom(db.getName(), type.getName());
	  cleaned=true;
	}      
      if(transform==null  || ((Boolean)transform.invoke(o, args)).booleanValue())
	{
	  DBConnection dbc= db.getDBConnection();
	  try{
	    dbc.insert(type.getName(), h);
	  }finally{dbc.close(); }
	}
      return h;
    }catch(InvocationTargetException ite) { ite.getTargetException().printStackTrace(); }
    catch(org.makumba.InvalidValueException ive){ MakumbaSystem.getMakumbaLogger("import").warning(ive.getMessage()); return null; }
    catch(Throwable t){ t.printStackTrace(); }
    throw new RuntimeException(); 
  }

  /** import an integer from  a hashtable */
  public static void importInteger(String java, String mdd, Hashtable hjava, Hashtable hmdd)
  {
    String s= (String)hjava.remove(java);
    if(s==null)
      return;
    Integer i= null;
    try{
      i=new Integer(Integer.parseInt(s.trim()));
    }catch (NumberFormatException nfe) 
      { 
	if(s.trim().length()>0)
	  MakumbaSystem.getMakumbaLogger("import").warning(s);
       return;
      }
    hmdd.put(mdd, i);
  }
  
  /** import an string from  a hashtable */
  public static void importString(String java, String mdd, Hashtable hjava, Hashtable hmdd)
  {
    String s= (String)hjava.remove(java);
    if(s==null)
      return;
    hmdd.put(mdd, s.trim());
  }

  static Integer zero= new Integer(0);
  static Integer one= new Integer(1);

  /** import a boolean choice from a hashtable */
  public static void importBoolean(String java, String mdd, Hashtable hjava, Hashtable hmdd, String on)
  {
    String s= (String)hjava.remove(java);
    hmdd.put(mdd, zero);
    if(s==null)
      return;
    if(s.trim().equals(on))
    hmdd.put(mdd, one);
  }



}






