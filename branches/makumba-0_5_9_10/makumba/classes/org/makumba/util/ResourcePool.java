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
import java.util.*;

/** Keeps a number of identical creation-expensive resources. Avoids resource re-creation/deletion
 * @author cristi
 */
public abstract class ResourcePool
{
  Stack stack= new Stack();
    
    // we keep a reference to all our resources here
    Vector all= new Vector();

  /** re-define this method to express how to create a resource */
  public abstract Object create() throws Exception;

    Object createAndCount() throws Exception
    {
	Object o= create();
	all.addElement(o);
	org.makumba.MakumbaSystem.getMakumbaLogger("util.pool").fine("pool size: "+ all.size());
	return o;	
    }

  /** initialize the pool with n resources */
  public void init(int n) throws Exception
  {
    for(;n>0;n--)
      put(createAndCount());
  }
  
  /** get one resource */
  public Object get() throws Exception
  {
    synchronized(stack)
      {
	if(stack.isEmpty())
	    return createAndCount();
	return stack.pop();
      }
  }
  
  /** put back one resource */
  public void put(Object o)
  { 
      // FIXME: this leaves a door open for resources to be added to the pool
      // without having been created by the pool. 
      // we may want this or we may not. 
      // if we want it, these resources should be added to "all"
      // if not, they should be rejected (and then "all" should probably be a hashmap or so
    synchronized(stack)
      {
	stack.push(o); 
      }
  }
  
  public void reset()
  {
    stack= new Stack();
    all= new Vector();
  }
}
