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
import java.lang.ref.SoftReference;

/** A NamedResources that keeps its resources as soft references
 *@see org.makumba.util.NamedResources
*/
public class SoftNamedResources extends NamedResources
{
  public SoftNamedResources(String name, NamedResourceFactory f)
    { super(name, f); }

  protected NameValue getNameValue(Object name, Object hash)
  {
    NameValue nv=null;
    cleanCleared();
    SoftReference sr=(SoftReference)values.get(hash);
    if(sr==null || (nv=(NameValue)sr.get())==null){
      values.put(hash, new SoftReference(nv=new NameValue(name, hash, f)));
      misses++;
    }else hits++;
    return nv;
  }

  public String getName() { return name+" (soft cache)"; }

  /** remove the residue data structures which were used to refer the resources that were cleared by the garbage collector */
  void cleanCleared(){
    for(java.util.Iterator i= values.keySet().iterator(); i.hasNext(); ){
      SoftReference sr=(SoftReference)values.get(i.next());
      if(sr.get()==null)
	i.remove();
    }
  }
  
  public synchronized int size() {
    cleanCleared();
    return super.size();
  }
}
