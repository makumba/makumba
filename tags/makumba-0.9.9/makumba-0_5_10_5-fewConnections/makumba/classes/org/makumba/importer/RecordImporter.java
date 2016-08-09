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

package org.makumba.importer;
import org.makumba.*;
import org.makumba.abstr.*;
import java.util.*;
import java.lang.reflect.*;
import java.io.*;

/** this class imports makumba records from text files based on markers placed in special configuration files with the "mark" extension from the CLASSPATH */
public class RecordImporter extends RecordHandler
{
  Properties markers= new Properties();
  
  public String getMarker(String s){ return markers.getProperty(s); }

  boolean noMarkers=false;
  public RecordImporter(RecordInfo type){this(type, false); }

  public RecordImporter(RecordInfo type, boolean noMarkers)
  {
    super(type);
    this.noMarkers=noMarkers;

    String nm=type.getName().replace('.', '/')+".mark";
    java.net.URL u=null;
    try{
      markers.load((u=org.makumba.util.ClassResource.get("dataDefinitions/"+nm)).openStream());
    }catch(Exception e)
      {
	try{
	  markers.load((u=org.makumba.util.ClassResource.get(nm)).openStream());
	}catch(Exception f){throw new MakumbaError(f); }
      }

    Method m= getHandlerMethod("configure");
    Object[] arg= { markers};
    try{
      callAll(m, arg);
    }catch(InvocationTargetException e)
      {
	e.getTargetException().printStackTrace();
      }
    
    Vector notMarked= new Vector();
    for(Enumeration e= handlerOrder.elements(); e.hasMoreElements(); )
      {
	FieldImporter fi= (FieldImporter)e.nextElement();
	if(!isMarked(fi) && !fi.isIgnored())
	  notMarked.addElement(fi.getName());
      }
    if(notMarked.size()>0)
      MakumbaSystem.getMakumbaLogger("import").warning("marker file "+u+ " does not contain markers for:\n "+notMarked+
					      "\nUse \"<fieldname>.ignore=true\" in the marked file if you are shure you don't want the field to be imported");
    
    boolean hasErrors=false;
    for(Enumeration e= handlerOrder.elements(); e.hasMoreElements(); )
      {
	FieldImporter fi= (FieldImporter)e.nextElement();
	if(fi.configError!=null && !fi.isIgnored())
	  {
	    if(!hasErrors)
	      {
		hasErrors=true;
		MakumbaSystem.getMakumbaLogger("import").warning("marker file "+u+ " contains errors. Erroneous fields will be ignored.");
	      }
	    fi.ignored=true;
	    MakumbaSystem.getMakumbaLogger("import").severe(fi.configError.toString());
	  }
      }
  }

  Object getValue(String name, String s, Database db, Pointer[] indexes)
  {
    FieldImporter fi=((FieldImporter)handlers.get(name));
    if(fi.isIgnored())
      return null;
    return fi.getValue(fi.replace(s), db, indexes);
  }

  protected boolean isMarked(FieldImporter fi)
  {
    if(fi.isMarked())
      return true;
    if(noMarkers)
      {
	String s=fi.begin;
	fi.begin="x";
	boolean b= fi.isMarked();
	fi.begin=s;
	return b;
      }
    return false;
  }


  protected boolean usesHidden(){ return true; }

  /** import data from a text. indexes contains the pointers to other records imported from the same text, at the same time */
  public Dictionary importFrom(String s, Database db, Pointer[] indexes)
  {
    Object[] arg= {  new Hashtable(), s, db, indexes};
    Method m= getHandlerMethod("importTo");
    try{
      callAll(m, arg);
    }catch(InvocationTargetException e)
      {
	e.getTargetException().printStackTrace();
      }
    return (Dictionary)arg[0];
  }

  /** imports all files from a directory */
  public static void main(String argv[]) throws Throwable
  {
    RecordImporter ri= new RecordImporter(RecordInfo.getRecordInfo(argv[0]));
    File dir= new File(argv[1]);
    String[] lst= dir.list();
    char buffer[]= new char[8196];
    for(int i=0; i<lst.length; i++)
      {
	MakumbaSystem.getMakumbaLogger("import").finest(lst[i]);
	Reader r= new FileReader(new File(dir, lst[i]));
	StringWriter sw= new StringWriter();
	int n;
	while((n=r.read(buffer))!=-1)
	  sw.write(buffer, 0, n);
	String content= sw.toString().toString();
	MakumbaSystem.getMakumbaLogger("import").finest(ri.importFrom(content, null, null).toString());
      }
  }

}
