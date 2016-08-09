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

package org.makumba.view.jsptaglib;
import org.makumba.*;
import org.makumba.abstr.*;
import org.makumba.view.*;
import java.util.*;
import javax.servlet.jsp.*;

public class RecordEditor extends RecordViewer
{
  String database;

  public RecordEditor(RecordInfo ri, Hashtable h)   {  super(ri, h); }  

  public Dictionary readFrom(PageContext pc)
  {
    Dictionary data= new Hashtable();
    for(Enumeration e=handlerOrder.elements(); e.hasMoreElements(); )
      {
	FieldEditor fe= (FieldEditor)e.nextElement();
	if(fe.getInputName()==null)
	  continue;
	Object o= fe.readFrom(HttpAttributes.getParameters(pc));
	if(o!=null)
	  o=fe.getFieldInfo().checkValue(o);
	else
	  o=fe.getNull();

	pc.setAttribute(fe.getInputName()+"_type", fe.getFieldInfo());

	// FIXME: semantics of EDIT might be wrong here
	if(o!=null)
	  data.put(fe.getInputName(), o);
	HttpAttributes.setAttribute(pc, fe.getInputName(), o);
      }
    return data;
  }

  public void config()
  {
    Object a[]= { this} ;
    try{
      callAll(getHandlerMethod("onStartup"), a);
    }catch(java.lang.reflect.InvocationTargetException e)
      {
	throw new org.makumba.MakumbaError(e.getTargetException());
      }
  }

}
