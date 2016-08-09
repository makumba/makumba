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
import java.io.*;
import org.makumba.*;

public class RecordParser extends RecordHandler
{
  OrderedProperties text;
  OrderedProperties fields= new OrderedProperties();
  OrderedProperties subfields= new OrderedProperties();
  DataDefinitionParseError mpe;

  Properties definedTypes;

  RecordParser()
  {
    definedTypes= new Properties();
  }

  // for parsing of subtalbes
  RecordParser(RecordInfo sbtbl, RecordParser rp)
  {
    super(sbtbl);
    text= new OrderedProperties();
    definedTypes= rp.definedTypes;
    mpe= rp.mpe;
  }

  void parse(RecordInfo ri) 
  {
      this.ri= ri;
      text= new OrderedProperties();
      mpe= new DataDefinitionParseError();

      try{
	read(text, ri.origin);
      }catch(IOException e){ throw fail(e); }
      try{      
	// make the default pointers resulted from the table name
	ri.addStandardFields(ri.name.substring(ri.name.lastIndexOf('.')+1));
	parse();
      }catch(RuntimeException e) {throw new MakumbaError(e, "Internal error in parser while parsing "+ri.getName()); }
      if(!mpe.isSingle() && !ri.isSubtable())
	throw mpe;
  }
  
  
  RecordInfo parse(java.net.URL u, String path) 
  {
    ri= new RecordInfo(u, path);
    parse(ri);
    return ri;
  }

  RecordInfo parse(String txt) 
  {
    ri= new RecordInfo();
    text= new OrderedProperties();
    mpe= new DataDefinitionParseError();
    
    try{
      read(text, txt);
    }catch(IOException e){ throw fail(e); }
    try{      
      // make the default pointers resulted from the table name
      ri.addStandardFields(ri.name.substring(ri.name.lastIndexOf('.')+1));
      parse();
    }catch(RuntimeException e) {throw new MakumbaError(e, "Internal error in parser while parsing "+ri.getName()); }
    if(!mpe.isSingle() && !ri.isSubtable())
      throw mpe;
    
    return ri;
  }
  
  void parse()
  {
    // include all the files and add them to the text, delete the
    // !include command
    solveIncludes();

    // put fields in the fields table, subfields in subfields
    separateFields();

    // determine the title field, delete !title
    setTitle();

    // read predefined types, delete all !type.*
    readTypes();

    // commands should be finished at this point
    if(text.size()!=0)
      mpe.add(fail("unrecognized commands", text.toString()));

    // make a FieldParser for each field, let it parse and substitute
    // itself
    treatMyFields();

    // send info from the subfield table to the subfields
    configSubfields();

    // call solveAll() on all subfields
    treatSubfields();
  }

  void separateFields()
  {
    for(Enumeration e= text.keys(); e.hasMoreElements();)
    {
      String k= (String) e.nextElement();
      if(k.indexOf('!')==0)
        continue;

      if(k.indexOf("->")==-1)
        fields.putLast(k, text.getOriginal(k), text.remove(k));
      else
        subfields.putLast(k, text.getOriginal(k), text.remove(k));
    }
  }

  void setTitle()
  {
    String origCmd= (String)text.getOriginal("!title");
    String ttl= (String)text.remove("!title");
    String ttlt= null;
    if(ttl!=null)
      {
        if(fields.get(ttlt=ttl.trim())==null)
	  {
	    mpe.add(fail("no such field for title",
			 makeLine(origCmd, ttl)));
	    return;
	  }
      }
    else
      if(fields.get("name")!=null)
        ttlt= "name";
      else
        // if there are any relations, we skip their fields as
        // titles...
	if(fields.size()>0)
	  ttlt= fields.keyAt(0);
    ri.title= ttlt;
  }

  static java.net.URL getResource(String s)
  {
    return org.makumba.util.ClassResource.get(s);
  }

  static public java.net.URL findDataDefinition(String s, String ext)
  {
    String s1=null;
    java.net.URL u= null;
    if(s.endsWith(".") || s.endsWith("//"))
      return null;
    // OLDSUPPORT >>
    u= getResource("metadata.files/"+s.replace('.','/'));
    if(u==null)
      {
	//<<
	u= getResource(s.replace('.', '/')+"."+ext);
	if(u== null){
	  u= getResource("dataDefinitions/"+s.replace('.','/')+"."+ext);
	  if(u== null){
	    u= getResource(s1="dataDefinitions/"+s.replace('.','/'));
	    if(u==null){
	      u= getResource(s1=s.replace('.','/'));
	    }
	  }
	}
      }
    return u;
  }

  void solveIncludes()
  {
     int line= 0;
     OrderedProperties inclText;

     for(Enumeration e= text.keys(); e.hasMoreElements();line++)
     {
        String st= (String)e.nextElement();

        if(st.startsWith("!include"))
          {
	    String ok= text.getOriginal(st);
            String incl=(String)text.remove(st);
            line--;
            String s=incl;
	    java.net.URL u= findDataDefinition(s, "idd");
	    String n="."+ri.name;
	    //if(u==null && s.indexOf('.')==-1)
			//	u=findTable(n.substring(1, n.lastIndexOf('.')+1));
	    
	    if(u== null)
	      {
		mpe.add(fail("could not find include file "+s, ok+"="+incl));
		return;
	      }
            try{
	      inclText=new OrderedProperties();
	      read(inclText, u);
	    }
	    catch(IOException ioe)
	      {
		mpe.add(fail("could not find include file "+s+" "+ioe, ok+"="+incl)); ;
		return;
	      }

            for(Enumeration k= inclText.keys(); k.hasMoreElements(); )
              {
                String key= (String)k.nextElement();
                String val= text.getProperty(key);
                if(val==null)
		  text.putAt(++line, key, inclText.getOriginal(key), inclText.getProperty(key));
              }
          }
    }

    // now we remove all empty fields
    for(Enumeration k= text.keys(); k.hasMoreElements(); )
    {
      String key= (String) k.nextElement();
      if(((String)text.get(key)).trim().length()==0)
        text.remove(key);
    }
  }

  void readTypes()
  {
    for(Enumeration e= text.keys(); e.hasMoreElements(); )
      {
        String s= (String)e.nextElement();
        if(s.startsWith("!type."))
          {
            String nm= s.substring(6);
            definedTypes.put(nm, text.remove(s));
          }
      }
  }

  FieldCursor currentRowCursor;

  void treatMyFields()
  {
    FieldInfo fi;
    String nm;

    int line=0;
    for(Enumeration e= fields.keys(); e.hasMoreElements(); line++ )
      {
        nm= (String)e.nextElement();
	fi= new FieldInfo(ri, nm);
	ri.addField1(fi);
	FieldHandler fh;
	try{
	  handlers.put(nm, fh=new FieldParser(fi).parse
		       (new FieldCursor(this, makeLine(fields, nm))));
	}catch(DataDefinitionParseError pe){ mpe.add(pe); continue; }
	handlerOrder.addElement(fh);
      }
  }

  void configSubfields()
  {
    String nm;
    int p;
    for(Enumeration e= subfields.keys(); e.hasMoreElements(); )
      {
        nm= (String)e.nextElement();

        p= nm.indexOf("->");
        FieldParser fp= (FieldParser)handlers.get(nm.substring(0, p));
        if(fp==null)
	  {
	    mpe.add(fail("no such field in subfield definition",
			 makeLine(subfields, nm)));
	    continue;
	  }

        String s;
        if((s=fp.addText(nm.substring(p+2), subfields.getOriginal(nm), subfields.getProperty(nm)))!=null)
	  mpe.add(fail(s, makeLine(subfields, nm)));
      }
  }


  void treatSubfields()
  {
    try{
      callAll(getHandlerMethod("parseSubfields"), noArgs);
    }
    catch(java.lang.reflect.InvocationTargetException e)
      {
	e.getTargetException().printStackTrace();
	throw (RuntimeException)e.getTargetException();
      }
  }

  static String makeLine(String origKey, String value)
  {
    return origKey+"="+value;
  }

  static String makeLine(OrderedProperties p, String k)
  {
    return p.getOriginal(k)+"="+p.getProperty(k);
  }

  public static Vector commaString2Vector(String s)
  {
    Vector v= new Vector();

    while(true)
      {
        int p= s.indexOf(',');
        if(p== -1)
           {
              String toAdd= s.trim();
              if(toAdd.length()>0)
                v.addElement(toAdd);
              return v;
           }
        else
           {
              String toAdd= s.substring(0, p).trim();
              if(toAdd.length()>0)
                v.addElement(toAdd);
              s=s.substring(p+1);
           }
      }
  }

  static String listArguments(Vector v)
  {
    StringBuffer sb= new StringBuffer();
    if(v!=null && v.size()>0)
      {
        sb.append('(').append(v.elementAt(0));
        for(int i=1; i<v.size(); i++)
          sb.append(", ").append(v.elementAt(i));
        sb.append(')');
      }
    return sb.toString();
  }

  DataDefinitionParseError fail(String why, String where)
  {
    return new DataDefinitionParseError(ri.getName(), why, where, where.length());
  }

  DataDefinitionParseError fail(IOException ioe)
  {
    return new DataDefinitionParseError(ri.getName(), ioe);
  }


  void read(OrderedProperties op, String txt)
       throws IOException
  {
    read(op, new BufferedReader(new StringReader(txt)));
  }

  void read(OrderedProperties op, java.net.URL u)
       throws IOException
  {
    read(op, new BufferedReader(new InputStreamReader((InputStream)u.getContent())));
  }

  void read(OrderedProperties op, BufferedReader rd)
       throws IOException
    {
    while(true)
      {
	String s= null;
	s=rd.readLine();
	if(s== null)
	  break;

	String st= s.trim();
	int l = s.indexOf('=');
	if(st.length()==0 || st.charAt(0)=='#')
	  continue;
	if(l==-1)
	  {
	    mpe.add(fail("non-empty, non-comment line without =", s));
	    continue;
	  }
	String k= s.substring(0, l);
	String kt= k.trim();
	if(kt.length()== 0)
	  {
	    mpe.add(fail("zero length key", s));
	    continue;
	  }
	if(kt.charAt(0)=='!' && kt.length()==1)
	  {
	    mpe.add(fail("zero length command", s));
	    continue;
	  }

	if(kt.startsWith("!include"))
	  {
	    if(kt.length()>8)
	      {
		mpe.add(fail("unknown command: "+kt, s));
		continue;
	      }
	    while(op.get(kt)!=null)
	      kt= kt+"_";
	  }
	if(op.putLast(kt, k, s.substring(l+1))!=null)
	  mpe.add(fail("ambiguous key "+kt, s));
      }
    rd.close();
  }

}

class OrderedProperties extends Dictionary
{
  Vector ks= new Vector();
  Hashtable orig= new Hashtable();
  Hashtable content= new Hashtable();

  OrderedProperties(){}

  public String toString(){
    StringBuffer sb= new StringBuffer("{");
    Enumeration e= keys();
    if(e.hasMoreElements())
    {
        Object o= e.nextElement();
        sb.append(o).append("=").append(get(o));
        while(e.hasMoreElements())
        {
            o= e.nextElement();
            sb.append(", ").append(o).append("= ").append(get(o));
        }
    }
    return sb.append('}').toString();
  }

  public Enumeration elements(){ return ((Hashtable)content.clone()).elements(); }
  public Object get(Object key){ return content.get(key); }

  public Enumeration keys()
  { return ((Vector)ks.clone()).elements(); }

  public String getOriginal(String key)
  { return (String)orig.get(key); }

  public String keyAt(int i){ return (String)ks.elementAt(i); }

  public Object remove(Object key)
  {
    ks.removeElement(key);
    orig.remove(key);
    return content.remove(key);
  }

  public Object putAt(int n, Object key, Object origKey, Object value)
  {
    ks.insertElementAt(key, n);
    orig.put(key, origKey);
    return content.put(key, value);
  }

  public synchronized Object putLast(Object key, Object origKey, Object value)
  {
    Object o= content.put(key, value);
    if(o!=null)
      ks.removeElement(key);
    ks.addElement(key);
    orig.put(key, origKey);
    return o;
  }

  public Object put(Object key, Object value)
  {
    return putLast(key, key, value);
  }

  public String getProperty(String s){ return (String)get(s); }
  public int size(){ return content.size(); }
  public boolean isEmpty(){ return content.isEmpty(); }
}
