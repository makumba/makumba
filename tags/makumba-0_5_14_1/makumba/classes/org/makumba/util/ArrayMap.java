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

package org.makumba.util;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/** this class exports an Object[] array as a dictionary */
public class ArrayMap extends Dictionary
{
  public Object[] data;
  Dictionary keyIndex;

  public ArrayMap(Dictionary d, Object[]o){ data=o; keyIndex=d; }
  public ArrayMap(){keyIndex=new Hashtable(); }
  Integer index(Object key){ return (Integer)keyIndex.get(key); }
  public Object get(Object key)
  { 
    Integer i= index(key);
    if(i==null)
      return null;
    return data[i.intValue()];
  }
  public Object put(Object key, Object value)
  {
    Integer i= index(key);
    if(i!=null)
      {
	Object ret=data[i.intValue()];
	data[i.intValue()]=value;
	return ret;
      }
    else
      throw new RuntimeException("invalid key: "+key);
  }

  public Object remove(Object key){ return put(key, null); }
  public Enumeration keys()
  {
    return new Enumeration(){
      Object nxt;
      Object next;
      Enumeration e;
      {e=keyIndex.keys();findNext();}
      void findNext()
	{ 
	  next=null;
	  while(e.hasMoreElements() && (next=get(nxt=e.nextElement()))==null); 
	}
      public boolean hasMoreElements(){ return next!=null; }
      public Object nextElement(){  Object o=nxt; findNext(); return o ;}
    }; 
  }
  public Enumeration elements() 
  { 
    return new Enumeration(){
      int i;
      {i=0; findNext();}
      void findNext(){ while(i<data.length && data[i]==null)i++; }
      public boolean hasMoreElements(){ return i<data.length; }
      public Object nextElement(){  Object o=data[i++]; findNext(); return o ;}
    }; 
  }
  public int size() { int n=0; for(int i=0; i<data.length; i++) if(data[i]!=null) n++; return n;}
  public boolean isEmpty(){ return size()==0; }
  public String toString() 
  {
    StringBuffer ret= new StringBuffer();
    ret.append("{");
    String sep="";
    Object o;
    for(Enumeration e=keys(); e.hasMoreElements(); )
      {
	ret.append(sep); sep=",";
	ret.append(o=e.nextElement()).append("=").append(get(o));
      }
    ret.append("}");
    return ret.toString();
  }
  abstract class Enumerator implements Enumeration
  {
    
  }
}

