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

package org.makumba.abstr;
import java.util.*;
import java.lang.reflect.*;
import org.makumba.util.*;

/**
 * This class is the root of all handler classes for records.
 * The handlers share a RecordInfo structure, there can (and should)
 * be many RecordHandlers for a certain RecordInfo.
 * <p>
 * A record handler contains zero, one or more FieldHandlers per field. The field order
 * in the RecordHandler can be changed to differ from the one in the original RecordInfo. <p>
 * A record handler and its FieldHandlers form a <b>handler family</b>. A family can be a parser, a HTML formatter, 
 * a RTF formatter, a SQL database manager, etc. A simple <a href= Package-org.makumba.abstr.printer.html>printer family</a> is given as example.
 * A family can inherit from another family (e.g. different flavours of SQL)  by extending its RecordHandler and some of its FieldHandlers.<p>
 * Upon creation, a RecordHandler creates FieldHandlers for each field according to the following family rules: <ul>
 * <li> the FieldHandlers of a family are in the same package with the RecordHandler
 * <li> the family name is determined by the suffix of the
 * RecordHandler name in the family, e.g. Formatter for RecordFormatter, Parser for RecordParser, etc
 * <li> first, a redirection for the type is looked up in the redirectFamilyname.properties in the same package. The file has entries as <br>
 * <code>ptrIndex=ptr<br>
 * intEnum=int<br>
 * date=simple<br>
 * text=simple
 * </code><br>
 * which denote that instead of looking for a handler of a left-side type, the record handler should look for a handler of the corresponding right-side type. The process starts from the beginning for the right-side type.
 * For the example above, if the family name is Manager, int will be handled by intManager (since there is no redirection), so will be intEnum. The field handler class name where the type is redirected does not have to start with a type name (e.g. in the example above, for a RecordParser, the date and text types are treated the same way by a simpleParser handler)
 * <li> if there is no redirection, the FieldHandler class for a certain field type is of class typenameFamilyname (e.g. ptrParser, intEnumFormatter, etc)
 * <li> if at this point no field handler class could be found, the same procedure is attempted for the supercalss (i.e. the handler class is "inherited")
 * <li> after creation, the field handler can replace itself with another handler (or a number of handlers) that it finds more approprieate while looking at the field it is supposed to handle (e.g. a ptrParser replaces itself with a ptrOneParser when it detects the actual type during parsing, a ptrSQLManager could replace itself with 2 kinds of intSQLManager , etc)
 * </ul>
 * The above mechanism can be changed by overriding the makeHandler method and other associated methods (gerHandlerFamily, getHandlerPackage, etc)
 * <p>
 * The field types have root classes provided (the org.makumba corresponding type and the java type are described in brackets):
 * <ul>
 * <li><a href=org.makumba.abstr.intHandler.html>int</a> (an int, java.lang.Integer),
 * <li><a href=org.makumba.abstr.intEnumHandler.html>intEnum</a> (an int with enumerated values, java.lang.Integer),
 * <li><a href=org.makumba.abstr.charHandler.html>char</a> (a char, java.lang.String),
 * <li><a href=org.makumba.abstr.charEnumHandler.html>charEnum</a> (a char with enumerated values, java.lang.String),
 * <li><a href=org.makumba.abstr.textHandler.html>text</a> (a text, java.lang.String),
 * <li><a href=org.makumba.abstr.dateHandler.html>date</a> (a date, java.util.Date),
 * <li><a href=org.makumba.abstr.dateCreateHandler.html>dateCreate</a> (a creation timestamp, java.sql.Timestamp),
 * <li><a href=org.makumba.abstr.dateModifyHandler.html>dateModify</a> (a modification timestamp, java.sql.Timestamp),
 * <li><a href=org.makumba.abstr.ptrHandler.html>ptr</a> (a pointer, org.makumba.Pointer),
 * <li><a href=org.makumba.abstr.ptrRelHandler.html>ptrRel</a> (a pointer from a subtable to the main table, and other 1:n or m:n relational pointers, org.makumba.abstr.Pointer),
 * <li><a href=org.makumba.abstr.ptrIndexHandler.html>ptrIndex</a> (a unique index pointer- dbsvuid, org.makumba.abstr.Pointer),
 * <li><a href=org.makumba.abstr.ptrOneHandler.html>ptrOne</a> (a 1:1 pointer, org.makumba.abstr.Pointer),
 * <li><a href=org.makumba.abstr.setHandler.html>set</a> (a set in another table, no data in this table),
 * <li><a href=org.makumba.abstr.setcharEnumHandler.html>setcharEnum</a> (a set of enumerated char values, no data in this table),
 * <li><a href=org.makumba.abstr.setintEnumHandler.html>setintEnum</a> (a set of enumerated int values, no data in this table),
 * <li><a href=org.makumba.abstr.setComplexHandler.html>setComplex</a> (a set in a subtable, no data in this table)
 * but family implementations of field handlers can choose not to extend them. They can always retrieve them by calling getDefault()
 * <p>
 * The class also provides a generic mechanism of calling a method on all contained field handlers. 
 * To simplify the process of obtaining the Method objects, when each RecordHanlder class is first loaded, 
 * it stores the method names of the generic field handler of the family, 
 * having the name FieldFamilyname (e.g. FieldFormatter, FieldParser, etc).
 * All field handlers are supposed to extend/implement that class. The methods can then be accessed via getMethod()
 * <p> The generic field handler name can be changed by overriding getGenericHandler().
 * 
 * @author Cristian Bogdan
 * @see org.makumba.abstr.RecordInfo
 * @see org.makumba.abstr.FieldHandler
 */

public abstract class RecordHandler
{
  RecordInfo ri;
  protected Hashtable handlers= new Hashtable();
  protected Vector handlerOrder;

  protected RecordHandler()
  { 
    handlerOrder= new Vector(); 
  }

  /** Sets the RecordInfo for this handler.
   * It will create FieldHandlers for each field, using the 
   * makeHandler factory method.
   * 
   * @param ri the RecordInfo to be handled by this handler
   * @see org.makumba.abstr.FieldHandler
   * @see makeHandler()
   * @see org.makumba.abstr.RecordInfo#getRecordInfo(String)
   * @see org.makumba.abstr.RecordInfo#getRecordInfo(String, String)
   */
  protected void setRecordInfo(RecordInfo ri)
  {
    this.ri= ri; 

    Vector ho= ri.getFieldNames();

    for(int i= 0; i< ho.size();i++)
    {
        FieldInfo fi= (FieldInfo)ri.fields.get(ho.elementAt(i));
	if(fi==null)
	  throw new RuntimeException(ho.elementAt(i).toString());
        FieldHandler fh= makeHandler(fi.type);
        fh.setFieldInfo(fi);
        addFieldHandler(fh, fi);
    }    
  }

  /**
   * Creates a RecordHandler from the given RecordInfo.
   * It will create a FieldHandler for each field, using the 
   * makeHandler factory method.
   * 
   * @param ri the RecordInfo to be handled by this handler
   * @see org.makumba.abstr.FieldHandler
   * @see makeHandler()
   * @see org.makumba.abstr.RecordInfo#getRecordInfo(String)
   * @see org.makumba.abstr.RecordInfo#getRecordInfo(String, String)
   */
  public RecordHandler(RecordInfo ri)
  { 
    this();
    setRecordInfo(ri);
  }
  
  /** From which RecordInfo was this handler made ? */
  public RecordInfo getRecordInfo(){ return ri; }

  void addFieldHandler(FieldHandler fh, FieldInfo fi) 
  {
    String nm= fi.name;
    
    Object o= fh.replaceIn(this);
    if(o== null)
      return;

    if(o instanceof FieldHandler)
      {
	handlers.put(nm, o);
	if(o!=fh)
	  {
	    ((FieldHandler)o).setFieldInfo(fi);
	    addFieldHandler((FieldHandler)o, fi);
	    return;
	  }
	handlerOrder.addElement(o);
	return;
      }

    /*
    if(o instanceof String)
      {
	FieldHandler fhn= makeHandler((String)o);
	fhn.setFieldInfo(fi);
	addFieldHandler(fhn, fi);
	return;
      }

    if(o instanceof String[])
      {
	String s[]= (String[])o;
	for(int j= 0; j<s.length; j++)
	{
	  FieldHandler fhn= makeHandler((String)s[j]);
	  fhn.setFieldInfo(fi);
	  nm= fhn.getName();
	  handlerOrder.addElement(fhn);
	  handlers.put(nm, fhn);
	}
	return;
      }
      */
    FieldHandler fha[]= (FieldHandler[])o;
    for(int j= 0; j<fha.length; j++)
      {
	if(fha[j]!=fh)
	  fha[j].setFieldInfo(fi);
	nm= fha[j].getName();
	handlerOrder.addElement(fha[j]);
	handlers.put(nm, fha[j]);
      }
  }
  
  static int redirectors= NamedResources.makeStaticCache
  ("Handler family redirectors", 
   new NamedResourceFactory()
   {
     protected Object makeResource(Object nm) throws java.io.IOException
       {
	 String name= (String)nm;
	 int n= name.indexOf('*');
	 String pkg= name.substring(0, n);
	 String suffix= name.substring(n+1);
	 
	 Properties p= new Properties();

	 java.net.URL u= org.makumba.util.ClassResource.get
	   (pkg.replace('.','/')+"redirect"+suffix+".properties");

	 if(u==null)
	   return null;

	 java.io.InputStream is= u.openStream();

	 if(is==null)
	   return null;

        p.load(is);
	return p;
       }
   });

  static Hashtable getRedirectors(String pkg, String suffix)
  {
    return (Hashtable)NamedResources.getStaticCache(redirectors).
      getResource(pkg+'*'+suffix);
  }
  
  /** this is only called if the RecordHandler subclass name 
   * does not include the word "Record"; 
   */
  static int handlerClasses= NamedResources.makeStaticCache
  ("Handler family classes",
   new NamedResourceFactory()
   {
     protected Object makeResource(Object nm)
       {
	 final Class cl=(Class)nm;
	 final String n=cl.getName();
	 final String pkg= n.substring(0, n.lastIndexOf('.')+1);
	 final String suffix= getFamilyName(n);
	 
	 return new NamedResources
	     ("Handler family classes",
	    new NamedResourceFactory()
	    {
	      protected Object makeResource(Object nml)
		{
		  String type= (String)nml;
		  try{
		    type= ((String)getRedirectors(pkg, suffix).get(type))
		      .trim();
		  } catch(NullPointerException g)
		    { 
		      try
			{ return Class.forName(pkg+type+suffix);}
		      catch(ClassNotFoundException f)
			{ return getHandlerClass(cl.getSuperclass(), type); }
		    }
		  return getHandlerClass(cl, type);
		}  
	    });
       }
   });

  static Class getHandlerClass(Class cl, String type)
  {
    return (Class)((NamedResources)
		   NamedResources.getStaticCache(handlerClasses).
		   getResource(cl))
      .getResource(type);
  }
  
  
  /** makes a handler for the gived field, given its type. Types can go beyond the standard types here */
  public FieldHandler makeHandler(String type)
  {
    try{
      return (FieldHandler)getHandlerClass(getClass(), type).newInstance();
    }
    catch(NullPointerException npe){ return null; }
    catch(RuntimeWrappedException rwe){ throw rwe; }
    catch(Exception e) 
      { throw new RuntimeWrappedException(e); }  
  }

  static String getFamilyName(String className)
  {
    int i= className.indexOf("Record");
    return i==-1?"Handler":className.substring(i+6);
  }

  //************************************************************

  static int handlerMethods= NamedResources.makeStaticCache
  ( "Handler family methods", 
    new NamedResourceFactory()
    {
      public Object makeResource(Object nm) 
	{
	  final Class cl= (Class)nm;
	  final String n=cl.getName();
	  final String pkg= n.substring(0, n.lastIndexOf('.')+1);
	  final String suffix= getFamilyName(n);

	  Class c= null;
	  
	  try{
	    c= Class.forName(pkg+ "Field"+suffix);
	  } catch(ClassNotFoundException e){}

	  if(c==null)
	    return null;

	  Hashtable ret= new Hashtable();

	  Method[] mt= c.getMethods();
	  for(int i= 0; i<mt.length; i++)
	    ret.put(mt[i].getName(), mt[i]);
	  return ret;
	}
    });

  protected static Object[] noArgs= new Object[0];

  /**
   * Returns a method of the general field handler
   * class or interface associated with this record handler.
   * <br>
   * Methods are read on RecordHandler creation, to give easy
   * access to field handler methods that can be used in callAll, 
   * concatAll, writeAll and streamAll
   * 
   * @param m the name of the method. Only one method with a name is supported 
   * (i.e. no name overloading). This is simple and powerful enough
   * @see getHandlerAncestor()
   * @see callAll(Method, Object[])
   * @see writeAll(Method, Object[], String)
   * @see streamAll(Method, Object[], byte[])
   * @see concatAll(Method, Object[], String)
   */
  protected Method getHandlerMethod(String m)
  {
    return (Method)((Hashtable)
		    NamedResources.getStaticCache(handlerMethods).
		    getResource(getClass())).get(m);
  }
  
   /** call a method on all handlers
    */
  public void callAll(Method m, Object[] args)
  throws InvocationTargetException
  {
    Enumeration e= handlerOrder.elements();
    
    try{   
      while(e.hasMoreElements()){
	m.invoke(e.nextElement(), args);
      }
    }
    catch(IllegalAccessException f){}
  }

   /** call a method on all handlers and concatenate the string results
    * The separator is added after each handler's output, except the last.
    */
  public StringBuffer concatAll(Method m, Object[] args, String separator)
  throws InvocationTargetException
  {
    Enumeration e= handlerOrder.elements();
    StringBuffer ret= new StringBuffer();

    if(e.hasMoreElements())
       try
       {
          ret.append(m.invoke(e.nextElement(), args));
          while(e.hasMoreElements())
            ret.append(separator).append(m.invoke(e.nextElement(), args));
       }
       catch(IllegalAccessException f){}
    return ret;
  }

  static Method toStr;
  static
  {
    try{
      toStr= java.lang.Object.class.getMethod("toString", new Class[0]);
      } catch(Exception e){}
  }
  /** toString() separator. The system line separator by default */
  protected String toStringSeparator(){ return System.getProperty("line.separator"); }

  /** toString() preamble. Line separator by default. */
  protected String toStringBefore(){ return System.getProperty("line.separator"); }  
  
  /** toString() postamble. Line separator by default */
  protected String toStringAfter(){ return System.getProperty("line.separator"); }  
  
  /** call the toString method of all FieldHandlers, preceded by 
   * toStringBefore(), separated by toStringSeparator(), 
   * and followed by toStringAfter() */
  public String toString()  
  {
    try{
      return toStringBefore()+
        concatAll(toStr, noArgs, toStringSeparator())
        .append(toStringAfter());
      } catch(InvocationTargetException e){ e.getTargetException().printStackTrace(); return null;}
  }

  /**
   * Call a method on all handlers for concatenation in a Writer.
   * The first argument in args has to be a PrintWriter.
   * The separator is added after each handler's output, except the last.
   * 
   * @param m
   * @param args
   * @param separator
   */
  public void writeAll(java.lang.reflect.Method m, Object[] args, String separator)
  throws InvocationTargetException
  {
    Enumeration e= handlerOrder.elements();
    java.io.PrintWriter w= (java.io.PrintWriter)args[0];

    if(e.hasMoreElements())
       try
       {
          m.invoke(e.nextElement(), args);
          while(e.hasMoreElements())
          {
            w.write(separator);
            m.invoke(e.nextElement(), args);
          }
       }
       catch(IllegalAccessException f){}
  }

  /** Call a method on all handlers for concatenation in an OutputStream.
    * The first argument in args has to be an OutputStream.
    * The separator is added after each handler's output, except the last
    */
  public void streamAll(Method m, Object[] args, byte[]separators)
  throws InvocationTargetException
  {
    Enumeration e= handlerOrder.elements();
    java.io.OutputStream w= (java.io.OutputStream)args[0];
    
    if(e.hasMoreElements())
       try
       {
          m.invoke(e.nextElement(), args);
          while(e.hasMoreElements())
          {
            w.write(separators);
            m.invoke(e.nextElement(), args);
          }
       }
       catch(IllegalAccessException f){}
       catch(java.io.IOException ioe) { throw new InvocationTargetException(ioe); }

  }

  /** return the field handler with the given name */
  public FieldHandler getFieldHandler(String name)
    { return (FieldHandler)handlers.get(name); }
  
  public void checkInsert(Dictionary d, Dictionary except)
  {
    getRecordInfo().checkFieldNames(d);
    for(Enumeration e= handlerOrder.elements(); e.hasMoreElements(); )
      {
	FieldHandler fh=(FieldHandler)e.nextElement();
	if(except.get(fh.getName())==null)
	  fh.checkInsert(d);
      }
  }

  public void checkUpdate(Dictionary d, Dictionary except)
  {
    getRecordInfo().checkFieldNames(d);
    for(Enumeration e= handlerOrder.elements(); e.hasMoreElements(); )
      {
	FieldHandler fh=(FieldHandler)e.nextElement();
	if(except.get(fh.getName())==null)
	  fh.checkUpdate(d);
      }
  }

}

