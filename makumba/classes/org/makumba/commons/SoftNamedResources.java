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

package org.makumba.commons;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * A NamedResources that keeps its resources as soft references
 * 
 * @see org.makumba.commons.NamedResources
 */
public class SoftNamedResources extends NamedResources {
    private static final long serialVersionUID = 1L;
    private ReferenceQueue<NameValue> queue= new ReferenceQueue<NameValue>();
    private int diff=0;
    
    public SoftNamedResources(String name, NamedResourceFactory f) {
        super(name, f);
    }

    @Override
    protected NameValue getNameValue(Object name, Object hash) {
        NameValue nv = null;
        SoftReference<NameValue> sr = (SoftReference<NameValue>) values.get(hash);
        if (sr == null || (nv = sr.get()) == null){ 
            if(sr!=null && nv==null)
                diff--;
            values.put(hash, new SoftReference<NameValue>(nv = new NameValue(name, hash, f), queue));
            misses++;
        } else {
            hits++;
        }
        return nv;
    }

    @Override
    public String getName() {
        return name + " (soft cache)";
    }

    @Override
    public synchronized int size() {
        while(queue.poll()!=null){
            try {
                queue.remove();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            diff++;
        }
        
        // sanity check
//        int diff2=0;
//        for (Object o:values.values())
//            if (((SoftReference<NameValue>) o).get() == null) 
//                diff2++;
//        if(diff2!=diff)
//            throw new IllegalStateException(diff+ "<>"+diff2+" out of " +super.size());
        // end check
    
        return super.size()-diff;
    }
}