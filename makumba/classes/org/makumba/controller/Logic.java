///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.controller;
import java.util.*;
import org.makumba.util.*;
import java.lang.reflect.*;
import org.makumba.*;

/** busines logic administration */
public class Logic
{
  static Properties controllerConfig;
  static java.net.URL controllerURL;
  static
  {
    controllerConfig= new Properties();
    try{
      controllerURL=org.makumba.util.ClassResource.get("MakumbaController.properties");
      controllerConfig.load(controllerURL.openStream());
    }catch(Exception e){ controllerConfig=null; }
  }

  static public String getSearchMessage(String cls)
  {
    return (String)((Hashtable)NamedResources.getStaticCache(logix)
		    .getSupplementary()).get(cls);
  }

  static int logix= NamedResources.makeStaticCache("JSP pages that have mak: tags", 
						   new NamedResourceFactory()
   {
     {
       supplementary=new Hashtable();
     }
     protected Object makeResource(Object p)
       {
	 String path= (String)p;
	 String msg= "Searching for business logic for "+path+":";
	 String className="";
	 int n= path.lastIndexOf(".");
	 if(n!=-1)
	   path=path.substring(0, n);

	 String defa="";
	 String maxKey="";

	 if(controllerConfig!=null)
	   {
	     msg+="\nfollowing rules from MakumbaController.properties found at:\n\t"+getFilePath(controllerURL);
	     for(Enumeration e= controllerConfig.keys(); e.hasMoreElements(); )
	       {
		 String k=(String)e.nextElement();

		 if(k.equals("default") && maxKey.length()==0)
		   {
		     defa= controllerConfig.getProperty(k);
		     continue;
		   }
		 if(path.startsWith(k) &&
		    k.length()> maxKey.length())
		   {
		       maxKey=k;
		       className= controllerConfig.getProperty(k);
		       if(className.length()>0 && className.lastIndexOf(".")!=className.length()-1)
			 className+=".";
		     }
	       }

	     if(maxKey.length()==0 && defa.length()>0)
	       {
		 msg+="\nfollowing default rule from MakumbaController.properties";
		 className=defa+".";
	       }
	     else if(maxKey.length()>0)
	       {
		 msg+="\nfollowing rule based on longest matching key from MakumbaController.properties\n\tkey is: \""+maxKey+"\"";
	       }
	     path=path.substring(maxKey.length());
	   }
	 else
	   {
	     msg+="\ncould not find MakumbaController.properties in CLASSPATH";
	   }
	 
	 msg+="\ndetermined base: \""+className+"\"";
	 StringTokenizer st= new StringTokenizer(path, "/");

	 Object lastFound= null;
	 String dir=" ";

       loop:
	 while(true)
	   {
	     String base=className;
	     for(int i=1; i<=dir.length(); i++)
	       if(i==dir.length() || Character.isUpperCase(dir.charAt(i)))
		  {
		    className=base+dir.substring(0, i).trim();
		    try{
		      msg+="\ntrying \""+className+"Logic\"";
		      lastFound=Class.forName(className+"Logic").newInstance();
		      msg+="... found.";
		    }catch(ClassNotFoundException e) {msg+="... not found";}
		    catch(IllegalAccessException f) {msg+="... no public constructor";}
		    catch(InstantiationException f) {msg+="... abstract class";}
		  }
	     while(st.hasMoreTokens())
	       {
		 dir= st.nextToken();
		 if(dir.length()==0)
		   continue;
		 else
		   {
		     dir= firstUpper(dir);
		     continue loop;
		   }
	       }

	     
	     break; 
	   }
 
	 if(lastFound==null)
	   {
	     msg+="\nNo matching class found for "+p+"!";
	     lastFound=new LogicNotFoundException(msg);
	   }
	 else
	   msg+="\nFound class "+lastFound.getClass().getName();
	 
	 MakumbaSystem.getMakumbaLogger("controller").info(msg);
	 ((Hashtable)supplementary).put(p, msg);
	 
	 return lastFound;
       }
   });


  static String[]separators= { ".", "->" };

  static String upperCase(String a) 
  {
    String ret="";
    while(true)
      {
	int minimum= a.length();
	int imin=-1;
	for(int i=0; i<separators.length; i++)
	  {
	    int j= a.indexOf(separators[i]);
	    if(j!=-1 && j<minimum)
	      {
		minimum=j;
		imin=i;
	      }
	  }
	
	ret+= firstUpper(a.substring(0, minimum));
	if(imin==-1)
	  return ret;
	a=a.substring(minimum+separators[imin].length());
      }
  }

  static String firstUpper(String a)
  {
    char f=Character.toUpperCase(a.charAt(0));
    if(a.length()>1)
      return f+a.substring(1);
    return ""+f;
  }

  /** gets the logic associated with the given package and path according to directory rules */
  public static Object getLogic(String path)
  {
    return NamedResources.getStaticCache(logix).getResource(path);
  }

  static Class[] argDb= { Attributes.class, Database.class };

  public static Object getAttribute(Object controller, String attname, Attributes a, String db) 
       throws NoSuchMethodException, LogicException
  {
    Database d=MakumbaSystem.getConnectionTo(db);
    Object [] args= {a, d};
    try{
      return (controller.getClass().getMethod("find"+firstUpper(attname), argDb)).invoke(controller, args);
    }catch(IllegalAccessException e) { throw new NoSuchMethodException(e.getMessage()); }
    catch(InvocationTargetException f)
      { 
	Throwable g= f.getTargetException();
	if(g instanceof LogicException)
	  throw (LogicException)g;
	throw new LogicInvocationError(g);
      }
    finally{ d.close(); }
  }

  static Class[] editArgs= { Pointer.class, Dictionary.class, Attributes.class, Database.class };
  static Class[] opArgs= { Dictionary.class, Attributes.class, Database.class };
  static Class[]noClassArgs= {};
  static Object[]noObjectArgs= {};

  public static String getControllerFile(Object controller)
  {
    return getFilePath(org.makumba.util.ClassResource.get(controller.getClass().getName().replace('.', '/')+".java"));
  }

  public static String getFilePath(java.net.URL u)
  {
    try{
      return new java.io.File((u.getFile())).getCanonicalPath();
    }catch(java.io.IOException ioe){throw new org.makumba.util.RuntimeWrappedException(ioe); }
  }

  public static Method getMethod(String name, Class[] args, Object controller)
  {
    try{
      Method m= controller.getClass().getMethod(name, args);
      if(!Modifier.isPublic(m.getModifiers()))
	return null;
      return m;
    }catch(NoSuchMethodException e){return null;}
  }

  public static void doInit(Object controller, Attributes a, String dbName)
       throws LogicException
  {
    Database db=MakumbaSystem.getConnectionTo(dbName);
    try{
      if(!(controller instanceof LogicNotFoundException))
	{
	  Method init= getMethod("checkAttributes", argDb, controller);
	  Method oldInit= getMethod("requiredAttributes", noClassArgs, controller);
	  if(init==null && oldInit==null)
	    return;
	  if(init!=null)
	    {
	      Object [] args= {a, db};
	      try{
		init.invoke(controller, args);
	      }
	      catch(IllegalAccessException g){ throw new LogicInvocationError(g);}
	      catch(InvocationTargetException f)
		{ 
		  Throwable g= f.getTargetException();
		  if(g instanceof LogicException)
		    throw (LogicException)g;
		  throw new LogicInvocationError(g);
		}	
	    }
	  else
	    {
	      MakumbaSystem.getMakumbaLogger("controller").warning("requiredAttributes() is deprecated. Use checkAttributes(Attributes a, Database db) instead");
	      Object attrs=null;
	      try{
		attrs=oldInit.invoke(controller, noObjectArgs);
	      }
	      catch(IllegalAccessException g){ throw new LogicInvocationError(g);}
	      catch(InvocationTargetException f)
		{ 
		  Throwable g= f.getTargetException();
		  if(g instanceof LogicException)
		    throw (LogicException)g;
		  throw new LogicInvocationError(g);
		}	
	      if(attrs==null)
		return;
	      if(attrs instanceof String)
		{
		  a.getAttribute((String)attrs);
		  return;
		}
	      if(attrs instanceof String[])
		{
		  for(int i=0; i<((String[])attrs).length; i++)
		    a.getAttribute(((String[])attrs)[i]);
		  return;
		}
	      return;
	    }
	}
    }finally{db.close(); }
  }

  public static Object doOp(Object controller, String opName, 
			       Dictionary data, Attributes a, String dbName) 
       throws LogicException
  {
    if(opName==null)
      return null;

    Database db=MakumbaSystem.getConnectionTo(dbName);
    try{
      Object [] editArg= {data, a, db};
      Method op=null;
      if((controller instanceof LogicNotFoundException) )
	throw new ProgrammerError("there is no controller object to look for the Form handler method "+opName);
      
      op=getMethod(opName, opArgs, controller);
      if(op==null)
	throw new 
	  ProgrammerError("Class "+controller.getClass().getName()+
			  " ("+getControllerFile(controller)+ ")\n"+
			   "does not define the method\n"+
			  opName+"(Dictionary, Attributes, Database)\n"+
			  "The method is declared as a makumba form handler, so it has to be defined");
      
      try{
	return op.invoke(controller, editArg);
      }
      catch(IllegalAccessException g){ throw new LogicInvocationError(g);}
      catch(InvocationTargetException f)
	{ 
	  Throwable g= f.getTargetException();
	  if(g instanceof LogicException)
	    throw (LogicException)g;
	  throw new LogicInvocationError(g);
      }
    }finally{db.close(); }
  }

  public static Pointer doEdit(Object controller, String typename, 
			       Pointer p, Dictionary data, Attributes a, String dbName) 
       throws LogicException
  {
    Database db=MakumbaSystem.getConnectionTo(dbName);
    try{
      Object [] editArg= {p, data, a, db};
      Method edit=null;
      String upper= upperCase(typename);
      if(!(controller instanceof LogicNotFoundException))
	{
	  edit=getMethod("on_edit"+upper, editArgs, controller);
	  if(edit==null)
	    throw new 
	      ProgrammerError("Class "+controller.getClass().getName()+
			      " ("+getControllerFile(controller)+ ")\n"+
			      "does not define the method\n"+
			      "on_edit"+upper+"(Pointer, Dictionary, Attributes, Database)\n"+
			      "so it does not allow EDIT operations on the type "+typename +
			      "\nDefine that method (even with an empty body) to allow such operations.");
	}
      
      try{
	if(edit!=null)
	  edit.invoke(controller, editArg);
	db.update(p, data);  
	return p;
      }
      catch(IllegalAccessException g){ throw new LogicInvocationError(g);}
      catch(InvocationTargetException f)
	{ 
	  Throwable g= f.getTargetException();
	  if(g instanceof LogicException)
	    throw (LogicException)g;
	  throw new LogicInvocationError(g);
	}
    }finally{db.close(); }
  }

  static Class[] deleteArgs= { Pointer.class, Attributes.class, Database.class };

  public static Pointer doDelete(Object controller, String typename, 
				 Pointer p, Attributes a, String dbName) 
       throws LogicException
  {
    Database db=MakumbaSystem.getConnectionTo(dbName);
    try{
      Object [] deleteArg= {p, a, db};
      Method delete=null;
      String upper= upperCase(typename);
      
      if(!(controller instanceof LogicNotFoundException))
	{
	  delete=getMethod("on_delete"+upper, deleteArgs, controller);
	  if(delete==null)
	    throw new 
	      ProgrammerError("Class "+controller.getClass().getName()+
			      " ("+
			      getControllerFile(controller)+ ")\n"+
			      "does not define the method\n"+
			      "on_delete"+upper+"(Pointer, Attributes, Database)\n"+
			      "so it does not allow DELETE operations on the type "+typename +
			      "\nDefine that method (even with an empty body) to allow such operations.");
	}
      
      try{
	if(delete!=null)
	  delete.invoke(controller, deleteArg);
	db.delete(p);  
	return null;
    }
    catch(IllegalAccessException g){ throw new LogicInvocationError(g);}
    catch(InvocationTargetException f)
      { 
	Throwable g= f.getTargetException();
	if(g instanceof LogicException)
	  throw (LogicException)g;
	throw new LogicInvocationError(g);
      }
    }finally{db.close(); }
  }

  public static Pointer doAdd(Object controller, String typename, 
			       Pointer p, Dictionary data, Attributes a, String dbName) 
       throws LogicException
  {
    Database db=MakumbaSystem.getConnectionTo(dbName);
    try{
      Object [] addArg= {p, data, a, db};
      Method on=null;
      Method after=null;
      String upper= upperCase(typename);
      int n=typename.lastIndexOf("->");
      String field=typename.substring(n+2);
      typename=typename.substring(0, n);
      
      if(!(controller instanceof LogicNotFoundException))
	{
	  on=getMethod("on_add"+upper, editArgs, controller);
	  after=getMethod("after_add"+upper, editArgs, controller);
	  
	  if(on==null && after==null)
	    throw new 
	      ProgrammerError("Class "+controller.getClass().getName()+
			      " ("+
			      getControllerFile(controller)+ ")\n"+
			      "does not define neither of the methods\n"+
			      "on_add"+upper+"(Pointer, Dictionary, Attributes, Database)\n"+
			      "after_add"+upper+"(Pointer, Dictionary, Attributes, Database)\n"+
			      "so it does not allow ADD operations on the type "+typename +
			      ", field "+field+
			      "\nDefine any of the methods (even with an empty body) to allow such operations.");
	}
      
      try{
	if(on!=null)
	  on.invoke(controller, addArg);
	addArg[0]=db.insert(p, field, data);  
	if(after!=null)
	  after.invoke(controller, addArg);
	return (Pointer)addArg[0];
      }
      catch(IllegalAccessException g){ throw new LogicInvocationError(g);}
      catch(InvocationTargetException f)
	{ 
	  Throwable g= f.getTargetException();
	  if(g instanceof LogicException)
	    throw (LogicException)g;
	  throw new LogicInvocationError(g);
	}
    }finally{db.close(); }
  }

  static Class[] newArgs= { Dictionary.class, Attributes.class, Database.class };

  public static Pointer doNew(Object controller, String typename, Dictionary data, Attributes a, String dbName) 
       throws LogicException
  { 
    Database db=MakumbaSystem.getConnectionTo(dbName);
    try{
      Object [] onArgs= {data, a, db};
      Object [] afterArgs= {null, data, a, db};
      Method on=null;
      Method after=null;
      String upper= upperCase(typename);
      
      if(!(controller instanceof LogicNotFoundException))
	{
	  on=getMethod("on_new"+upper, newArgs, controller);
	  after=getMethod("after_new"+upper, editArgs, controller);
	  if(on==null && after==null)
	    throw new 
	      ProgrammerError("Class "+controller.getClass().getName()+
			      " ("+
			      getControllerFile(controller)+ ")\n"+
			      "does not define neither of the methods\n"+
			      "on_new"+upper+"(Dictionary, Attributes, Database)\n"+
			      "after_new"+upper+"(Pointer, Dictionary, Attributes, Database)\n"+
			      "so it does not allow NEW operations on the type "+typename +
			      ".\nDefine any of the methods (even with an empty body) to allow such operations.");
	}
      try{
	if(on!=null)
	  on.invoke(controller, onArgs);
	afterArgs[0]=db.insert(typename, data);  
	if(after!=null)
	  after.invoke(controller, afterArgs);
	return (Pointer)afterArgs[0];
      }
      catch(IllegalAccessException g){ throw new LogicInvocationError(g);}
      catch(InvocationTargetException f)
	{ 
	  Throwable g= f.getTargetException();
	  if(g instanceof LogicException)
	    throw (LogicException)g;
	  throw new LogicInvocationError(g);
	}
    }finally{db.close(); }
  }
}

