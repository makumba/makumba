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

package org.makumba.view;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.Transaction;
import org.makumba.LogicException;
import org.makumba.util.ArgumentReplacer;

/** Map $name to $n */
public class AttributeParametrizer
{
  /** names of all arguments, to keep an order */
  Vector argumentNames= new Vector();
  
  String oql;

  /** build a parametrizer from an OQL query, in the given database and with the given example arguments */
  public AttributeParametrizer(String oql)
       throws LogicException
  {
    ArgumentReplacer ar= new ArgumentReplacer(oql);
    
    for(Enumeration e=ar.getArgumentNames(); e.hasMoreElements(); )
      argumentNames.addElement(e.nextElement());

    Dictionary d= new Hashtable();
    for(int i=0; i<argumentNames.size(); i++)
      d.put(argumentNames.elementAt(i), "$"+(i+1));
    
    this.oql=ar.replaceValues(d);
  }

  /** execute the query */
  public Vector execute(AbstractQueryRunner db, Dictionary a, int offset, int limit) 
       throws LogicException
  {
    Object args[]= new Object[argumentNames.size()];
    for(int i=0; i<args.length; i++)
      args[i]=a.get((String)argumentNames.elementAt(i));
    return db.execute(oql, args, offset, limit);
  }
}


