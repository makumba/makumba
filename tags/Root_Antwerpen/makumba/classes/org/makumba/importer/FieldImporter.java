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
import java.util.Dictionary;
import java.util.Properties;

import org.makumba.Database;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.abstr.FieldHandler;

public class FieldImporter extends FieldHandler
{
  boolean canError=true;
  boolean noWarning=false;
  Properties markers;
  String nothing;
  MakumbaError configError;
  Properties replaceFile;

  protected boolean ignored, ignoreNotFound;
  protected String begin, end;
  
  public boolean isIgnored() { return ignored; }
  public boolean isMarked() { return begin!=null; }

  String getMarker(String m)
  {
    String s= markers.getProperty(getName()+"."+m);
    if(s!=null)
      return s.trim();
    return null;
  }

  public String canonicalName()
  {
    return getFieldDefinition().getDataDefinition().getName()+"#"+getName();
  }

  public void configure(Properties markers)
  {
    this.markers=markers;
    String s=getMarker("ignore");
    ignored=s!=null && s.equals("true");
    if(ignored)
      return;

    s=getMarker("ignoreNotFound");
    ignoreNotFound=s!=null && s.equals("true");

    begin= getMarker("begin");
    nothing=getMarker("nothing");
    String cf= getMarker("replaceFile");
    if(cf!=null)
      {
	replaceFile= new Properties();
	try{
	  replaceFile.load(new java.io.FileInputStream(cf));
	}catch(java.io.IOException e){ configError= new MakumbaError(e); }
      }
    //canError=getMarker("canError")!=null;
    noWarning=getMarker("noWarning")!=null;
    end= getMarker("end");
    if(end==null)
      end= markers.getProperty("end");
  }

  void warning(String s)
  {
    String err=canonicalName()+" "+s;
    if(canError)
	MakumbaSystem.getMakumbaLogger("import").warning(err);
    else
	throw new MakumbaError(err);
  }

  void warning(Throwable t)
  {
    String err=canonicalName();
    if(canError)
	MakumbaSystem.getMakumbaLogger("import").warning(err+" "+ t.toString());
    else
      throw new MakumbaError(t, err);
  }

  MakumbaError makeError(String s)
  {
    return new MakumbaError(canonicalName()+" "+s);
  }

  MakumbaError makeError(Throwable t)
  {
    return new MakumbaError(t, canonicalName());
  }

  public void importTo(Dictionary d, String s, Database db, Pointer[] indexes)
  {
    try{
      if(isIgnored() ||!isMarked())
	return;
      
      String val=null;
      if(begin!=null)
	{
	  int beg= s.indexOf(begin);
      
	  if(beg!=-1)
	    {
	      beg+=begin.length();
	      try{
		val=s.substring(beg, s.indexOf(end, beg));
		if(noWarning)
		    warning(" found value for unfrequent field: "+val);
	      }catch(Exception e)
		{
		  warning("no end found");
		  return;
		}
	    }
	  else
	    if(!ignoreNotFound && !noWarning)
	      warning("begin not found");
	}
      Object o=null;
      if(shouldEscape())
	val=escape(val);
      val=replace(val);
      if(shouldDecomposeURL())
	  val=decomposeURL(val);

      if(begin==null || val!=null)
	o= getValue(val, db, indexes); 
      
      if(o!=null)
	if(nothing!=null && o.equals(getValue(nothing)))
	  {
	    return;
	  }
	else
	  d.put(getName(), o);
    }
    catch(RuntimeException e) { throw makeError(e); }
  }

  String replace(String val)
  {
    if(val !=null)
      {
	String transf=getMarker("replace."+val);
	if(transf!=null)
	  {
	    val=transf;
	  }
	else
	  {
	    String val1= val.replace(' ', '_').replace('=', '_');
	    transf=getMarker("replace."+val1);
	    if(transf!=null)
	      val=transf;
	    else
	      if(replaceFile!=null)
		{
		  transf= replaceFile.getProperty(val);
		  if(transf!=null)
		    val= transf;
		  else
		    {
		      transf= replaceFile.getProperty(val1);
		      if(transf!=null)
			val= transf;
		    }
		}
	  }
      }
    return val;
  }

  static String [][] htmlEscape= {
    {"&quot;", "&amp;", "<br>"},
    {"\"", "&", "\n" }
  };

  static String escape(String s)
  {
    if(s==null)
      return null;

    StringBuffer sb= new StringBuffer();
  chars:
    for(int i=0; i<s.length(); i++)
      {
	for(int j=0; j<htmlEscape[0].length; j++)
	  if(s.length()-i>= htmlEscape[0][j].length() 
	     && s.substring(i, i+htmlEscape[0][j].length()).toLowerCase().equals(htmlEscape[0][j]))
	    {
	      sb.append(htmlEscape[1][j]);
	      i+=htmlEscape[0][j].length()-1;
	      continue chars;
	    }
	sb.append(s.charAt(i));
      }
    return sb.toString();
  }

  public boolean shouldEscape() {return true; }

    public boolean shouldDecomposeURL()
    { 
	return getType().equals("char");
    }


  static String decomposeURL(String s)
  {
    if(s==null)
      return null;
    if(!s.startsWith("<a"))
	return s;
    int n=s.indexOf('\"');
    if(n==-1 || s.length()==n+1)
	return s;
    int n1= s.indexOf('\"', n+1);
    if(n1==-1 || s.length()==n1+1)
	return s;
    String s1= s.substring(n+1, n1);
    n=s.indexOf(">");
    if(n==-1 || s.length()==n+1)
	return s;
    n1=s.indexOf("</a>");
    if(n1==-1)
	return s;
    try{
	if(!s1.equals(s.substring(n+1, n1)))
	    return s;
    }catch(StringIndexOutOfBoundsException aio){ MakumbaSystem.getMakumbaLogger("import").severe("EEEE "+s+" "+s1); return s; }
    if(!s1.startsWith("http"))
	s1="http://"+s1;
    return s1;
  }

  public Object getValue(String s, Database db, Pointer[] indexes)
  {
    return getValue(s);
  }

  public Object getValue(String s)
  {
    //FIXME: need to convert back the special HTML characters
    //take out <br> and <font>
    return s;
  }
}


