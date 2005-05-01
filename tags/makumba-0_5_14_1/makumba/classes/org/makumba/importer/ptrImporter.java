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
import java.util.Vector;

import org.makumba.Database;
import org.makumba.Pointer;

public class ptrImporter extends FieldImporter
{
  int index=-1;
  String joinField;
  String select;
  int nchar=-1;

  public boolean isMarked() { return joinField!=null || index!=-1 || select!=null; }

  public void configure(Properties markers)
  {
    super.configure(markers);
    if(ignored)
      return;

    joinField= getMarker("joinField");
    select= getMarker("select");
    try{
      index=Integer.parseInt(getMarker("index"));
    }catch(RuntimeException e) {}
    if(index!=-1)
      if(begin!=null || joinField!=null || select!=null)
	configError= makeError("if pointer index is indicated, begin, end or joinfield are not needed");
      else;
    else 
      if(joinField!=null)
	{
	  if(index!=-1 || select!=null )
	    configError= makeError("if join field is indicated, begin and end are needed, index not");  
	  String s= getMarker("joinChars");
	  if(s!=null)
	    nchar=Integer.parseInt(s);
	}
      else if(select!=null)
	{
	  if(index!=-1 || joinField!=null)
	    configError= makeError("if select is indicated, begin and end are needed, index not"); }
      else
	configError=makeError("join field or pointer index must be indicated for pointers");  
  }

  public Object getValue(String s, Database db, Pointer[] indexes)
  {
    if(index!=-1)
      return indexes[index];
    if(s.length()==0)
      return null;
    String arg=s;
    if(select!=null)
      {
	Vector v= db.executeQuery(select, arg);
	if(v.size()>1)
	  {
	    warning("too many join results for \""+s+"\": "+v);
	    return null;
	  }
	
	if(v.size()==1)
	  return (Pointer)((Dictionary)v.elementAt(0)).get("col1");

	warning("no join results for \""+s+"\"");
	return null;
      }
    String query=null;

    query="SELECT p, p."+joinField+" FROM "+getForeignTable().getName()+ " p WHERE p."+joinField+"=$1";

    Vector v= db.executeQuery(query, arg);

    if(v.size()>1)
      {
	warning("too many join results for \""+s+"\": "+v);
	return null;
      }

    if(v.size()==1)
      return (Pointer)((Dictionary)v.elementAt(0)).get("col1");

    if(nchar==-1)
      {
	warning("no join results for \""+s+"\"");
	return null;
      }
    
    query="SELECT p, p."+joinField+" FROM "+getForeignTable().getName()+ " p WHERE p."+joinField+" like $1";
    if(s.length() <nchar)
      arg=s;
    else
      arg=s.substring(0, nchar)+"%";
    
    v= db.executeQuery(query, arg);
    
    if(v.size()>1)
      {
	warning("too many join results for \""+s+"\": "+v);
	return null;
      }
    if(v.size()==0)
      {
	warning("no join results for \""+s+"\"");
	return null;
      }
    return (Pointer)((Dictionary)v.elementAt(0)).get("col1");
  }
}
