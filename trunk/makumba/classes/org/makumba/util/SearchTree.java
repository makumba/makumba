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
import java.util.NoSuchElementException;
import java.util.Vector;

/** This class models a search tree. All keys are transformed into their toString and used to search in the search tree. For performance reasons, it is not thread safe. 
*/
public class SearchTree extends Dictionary
{
  // invariant: if finish is not null, target is the object contained, and letters is null if finish is not ""
  // if letters is not null, connect contains the subtrees and finish is null or ""
  Object target;
  String finish=null;

  String letters;
  Vector connect;

  /** not implemented */
  public Enumeration keys(){ return null; }

  public SearchTree()
  {
    letters="";
    connect=new Vector();
  }

  SearchTree(String name, Object t)
  {
    finish= name; 
    target= t;
  }

  public String toString()
  {
    String ret="{";
    
    try{
      ret+="("+ finish.toString()+ "="+target+")";
    }
    catch(NullPointerException e){}
    try{
      for(int i= 0; ;i++)
	ret+= " "+letters.charAt(i)+connect.elementAt(i);
    }catch(StringIndexOutOfBoundsException sie){}  
    catch(NullPointerException npe){}  
    return ret+"}";
  }

  public int size()
  {
    int ret=((finish==null)?0:1);
    try
      {
	for(int i=0; ; i++)
	  ret+= ((SearchTree)connect.elementAt(i)).size();
      }catch(ArrayIndexOutOfBoundsException e){}
      catch(NullPointerException e){}
      return ret;
  }

  public boolean isEmpty() 
  { 
    return finish==null && (letters==null || letters.length()==0); 
  }


  public Object get(Object nm)
  {
    String name= nm.toString();
    SearchTree current= this;

    char first;
    
  crnt:
    while(true)
      {
	try{
	  if(current.finish.equals(name))
	    return current.target;
	}catch(NullPointerException e){}
	
	try
	  {
	    first= name.charAt(0);
	    for(int i=0; ; i++)
	      if(current.letters.charAt(i)==first)
		{
		  name= name.substring(1);
		  current= (SearchTree)current.connect.elementAt(i);
		  continue crnt;
		}
	  }
	catch(StringIndexOutOfBoundsException e){}
	catch(NullPointerException e){}
	return null; 
      }
  }

  public Object put(Object nm, Object t)
  {
    String name= nm.toString();
    char first;
    String rest;

    try
      {
	if(finish.equals(name))
	  {
	    Object t1=target;
	    target= t;
	    return t1;
	  }

	// we have finish
	char ffin;
	String frest;
	try
	  {
	    ffin= finish.charAt(0);
	  }catch(StringIndexOutOfBoundsException ai)
	    {
	      // letters might not be null
	      first= name.charAt(0);
	      rest= name.substring(1);
	      try{
		for(int i=0; ;i++)
		  if(letters.charAt(i)== first)
		    return ((SearchTree)connect.elementAt(i)).put(rest, t);
	      }catch(NullPointerException npe){
		letters="";
		connect= new Vector();
	      }
	      letters+= first;
	      connect.addElement(new SearchTree(rest, t));
	      return null;
	    }
	    
	letters= ""+ffin;
	connect= new Vector();
	SearchTree cfin;
	connect.addElement(cfin= new SearchTree(finish.substring(1), target));
	try{
	  first= name.charAt(0);
	  rest= name.substring(1);
	}catch(StringIndexOutOfBoundsException ai)
	  {
	    // no letters, we have finish, name is "", finish is not
	    target= t;
	    finish= name;
	    return null;
	  }
	// we have finish, name is not null, letters is null
	if(first== ffin)
	  {
	    cfin.put(rest, t);
	  }
	else
	  {
	    letters+=first;
	    connect.addElement(new SearchTree(rest, t));
	  }
	finish=null;
	return null;
      }
    catch(NullPointerException e)
      {
	// we have letters
	try{
	  first= name.charAt(0);
	  rest= name.substring(1);
	}catch(StringIndexOutOfBoundsException ai)
	  {
	    // name is ""
	    finish= name;
	    target=t;
	    return null;
	  }
	// we have letters, name is not ""
	try
	  {
	    for(int i=0; ;i++)
	      if(letters.charAt(i)==first)
		return ((SearchTree)connect.elementAt(i)).put(rest, t);
	  }
	catch(StringIndexOutOfBoundsException aie)
	  {
	    letters+= first;
	    connect.addElement(new SearchTree(rest, t));
	    return null;
	  }
      }
  }
 
  public Object remove(Object nm)
  {
    String name= nm.toString();
    try
      {
	if(finish.equals(name))
	  {
	    Object t1= target;
	    finish=null;
	    testUnique();
	    return t1;
	  }
      }
    catch(NullPointerException e){}
    char first;
    String rest;
    try
      {
	first= name.charAt(0);
	rest= name.substring(1);
      }
    catch(StringIndexOutOfBoundsException e){return null;}
    
    try
      {
	for(int i=0; ; i++)
	  if(letters.charAt(i)==first)
	    {
	      SearchTree c= (SearchTree)connect.elementAt(i);
	      Object o= c.remove(rest);
	      if(c.isEmpty())
		{
		  connect.removeElementAt(i);
		  letters= letters.substring(0, i)+letters.substring(i+1);
		}
	      if(finish== null )
		testUnique();

	      return o;
	    }
      }catch(StringIndexOutOfBoundsException e){}
      catch(NullPointerException e){}
      return null; 
  }

  void testUnique()
  {
    if(letters!=null && letters.length()==1)
      {
	SearchTree st= (SearchTree)connect.elementAt(0);
	if(st.letters==null || st.letters.length()==0)
	  {
	    finish= letters+st.finish;
	    letters= null;
	    connect=null;
	    target= st.target;
	  }
      }
  }

  Enumeration childrenElements()
  {
    return new Enumeration()
      {
	int n= 0;
	Enumeration currEnum= ((SearchTree)connect.elementAt(0)).elements();

	public boolean hasMoreElements()
	  {
	    return n< connect.size()-1 || currEnum.hasMoreElements() ;
	  }

	public Object nextElement()
	  { 
	    Object o= currEnum.nextElement(); 

	    if(!currEnum.hasMoreElements())
	      try
	      {
		currEnum= ((SearchTree)connect.elementAt(++n)).elements();
	      }catch(ArrayIndexOutOfBoundsException e){}
	    return o;
	  }
      };
  }

  static Enumeration empty= new Enumeration()
  {
    public boolean hasMoreElements()
      {
	return false;
      }
    public Object nextElement()
      {
	throw new NoSuchElementException();
      }
  };

  public Enumeration elements()
  {
    return new Enumeration()
      {
	Enumeration redir= new Enumeration()
	  {
	    public boolean hasMoreElements()
	      {
		return target!=null;
	      }
	    public Object nextElement()
	      {
		if(connect==null)
		  redir=empty;
		redir=childrenElements();
		return target;
	      }
	  };

	public boolean hasMoreElements()
	  {
	    return redir.hasMoreElements();
	  }

	public Object nextElement()
	  {
	    return redir.nextElement();
	  }
      };
  }
}



