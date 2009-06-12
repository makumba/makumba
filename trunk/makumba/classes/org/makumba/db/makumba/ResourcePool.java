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

package org.makumba.db.makumba;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Keeps a number of identical creation-expensive resources. Avoids resource re-creation/deletion
 * 
 * TODO: this class should be replaced by one more generic pool that exists out there
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 */
public abstract class ResourcePool {
    // a stack of resources
    Stack<Object> stack = new Stack<Object>();

    // a parallel stack that keeps the time when each resource was last used
    Stack<Date> timeStack = new Stack<Date>();

    // we keep a reference to all our resources to prevent them from being finalized when they are out of the pool
    Vector<Object> all = new Vector<Object>();

    /** re-define this method to express how to create a resource */
    public abstract Object create() throws Exception;

    /** create a new resource */
    Object createAndCount() throws Exception {
        Object o = create();
        all.addElement(o);
        java.util.logging.Logger.getLogger("org.makumba.util.pool").fine("pool size: " + all.size());
        return o;
    }

    /** initialize the pool with n resources */
    public void init(int n) throws Exception {
        for (; n > 0; n--)
            put(createAndCount());
    }

    /** get one resource */
    public Object get() throws Exception {
        synchronized (stack) {
            if (stack.isEmpty())
                return createAndCount();
            timeStack.pop();
            java.util.logging.Logger.getLogger("org.makumba.util.pool.member").fine("pool members: " + timeStack.size());
            
            Object r = stack.pop();
            if(!check(r)) {
                // resource is not valid anymore, we have to get another one
                return get();
            }
            
            return r;
        }
    }

    /** put back one resource */
    public void put(Object o) {
        // FIXME: this leaves a door open for resources to be added to the pool
        // without having been created by the pool.
        // we may want this or we may not.
        // if we want it, these resources should be added to "all"
        // if not, they should be rejected (and then "all" should probably be a hashmap or so
        synchronized (stack) {
            stack.push(o);
            timeStack.push(new Date());
            java.util.logging.Logger.getLogger("org.makumba.util.pool.member").fine("pool members: " + timeStack.size());
        }
    }

    /** a weak reference to ourselves, for usage by foreign objects */
    WeakReference<ResourcePool> poolRef = new WeakReference<ResourcePool>(this);

    /** clear all resource containers. if we have a stale prevention thread, we interrupt it */
    public void close() {
        poolRef.clear();
        synchronized (stack) {
            if (stalePreventionTimer != null)
                stalePreventionTimer.cancel();

            stack.clear();
            timeStack.clear();
            for (int i = 0; i < all.size(); i++)
                close(all.elementAt(i));
            all.clear();
        }
    }

    protected void finalize() {
        close();
    }

    // stale is the time after which a resource becomes old and invalid
    // sleeping is the stale prevention thread sleeping period
    // typically sleeping= stale/2
    long sleeping, stale;
    
    Timer stalePreventionTimer;

    /** 
     * refresh a resource that was unused for a long time to prevent it from staling.
     * returns false if resource could not be renewed
     */
    public abstract boolean renew(Object o);

    /** close a resource */
    public abstract void close(Object o);
    
    /** check if a resource is valid **/
    public abstract boolean check(Object o);
    
    /** start a stale prevention timer */
    public void startStalePreventionTimer(long sleepingTime, long staleTime) {
        this.sleeping = sleepingTime;
        this.stale = staleTime;
        
        stalePreventionTimer = new Timer(true);
        stalePreventionTimer.scheduleAtFixedRate(new StalePreventionTask(poolRef), new Date(), staleTime);
    }

    /** check for stale resources and renew the rotten ones */
    protected void renewAll() {
        synchronized (stack) {
            for (int i = 0; i < timeStack.size(); i++) {
                // if the resource can stale by the end of the next sleeping period
                if (((Date) timeStack.elementAt(i)).getTime() + stale < (new Date()).getTime() + sleeping) {
                    java.util.logging.Logger.getLogger("org.makumba.util.pool").fine(
                        "renewing resource " + stack.elementAt(i) + " not used since " + timeStack.elementAt(i));
                    
                    // make sure the renewal works, if not, remove the resource from the pool
                    if(renew(stack.elementAt(i))) {
                        timeStack.setElementAt(new Date(), i);
                    } else {
                        java.util.logging.Logger.getLogger("org.makumba.util.pool").fine(
                            "removing resource " + stack.elementAt(i) + " as it could not be renewed. was last used at "+ timeStack.get(i));
                        stack.remove(i);
                        timeStack.remove(i);
                    }
                }
            }
        }
    }

    public int getSize() {
        return stack.size();
    }

}

/** a task that asks the pool to look for stale resources */
class StalePreventionTask extends TimerTask {
    // we only keep a weak reference to the pool
    // otherwise the system (which keeps a reference to every thread)
    // would not allow the resource pool to be garbage-collected.
    WeakReference<ResourcePool> poolRef;

    StalePreventionTask(WeakReference<ResourcePool> poolRef) {
        this.poolRef = poolRef;
    }

    public void run() {
        ResourcePool rp = (ResourcePool) poolRef.get();

        // if the weak reference was cleared, GC and finalization occured,
        // so we return
        if (rp == null)
            return;

        rp.renewAll();

        // lose the reference to allow for garbage collection of the pool
        rp = null;
    }
}
