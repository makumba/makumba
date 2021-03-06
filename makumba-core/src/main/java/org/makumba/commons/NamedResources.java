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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * An instance of this class holds a cache of resources. If a resource is requested but is not present, it is produced
 * using the associated NamedResourceFactory, in a thread-safe way
 * 
 * @author Cristian Bogdan
 * @version $Id$
 * @see org.makumba.commons.NamedResourceFactory
 */
public class NamedResources implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    static boolean soft_static_caches;
    static {
        String soft = null;
        try {
            soft = System.getProperty("makumba.soft-static-caches");
        } catch (SecurityException se) {
        } // for applets
        if (soft != null) {
            soft = soft.trim();
        }
        soft_static_caches = "true".equals(soft);
    }

    NamedResourceFactory f;

    String name;

    Map<Object, Object> values = new HashMap<Object, Object>();

    int hits, misses;

    static List<NamedResources> staticCaches = new ArrayList<NamedResources>();

    static Vector<WeakReference<NamedResources>> allCaches = new Vector<WeakReference<NamedResources>>();

    /**
     * Cleans-up all the static and soft caches
     */
    static public void cleanup() {
        if(staticCaches != null) {
            for (int i = 0; i < staticCaches.size(); i++) {
                staticCaches.get(i).close();
            }
            staticCaches.clear();
        }
        if(allCaches != null) {
            for (int i = 0; i < allCaches.size(); i++) {
                ((java.lang.ref.WeakReference<?>) allCaches.elementAt(i)).clear();
            }
            allCaches.clear();
        }
        staticCaches = null;
        allCaches = null;
    }

    /** Clean all static caches, so their content will be re-initialized when needed. */
    public static void cleanupStaticCaches() {
        for (NamedResources r : staticCaches) {
            String name = r.getName();
            r.close();
            java.util.logging.Logger.getLogger("org.makumba.system").fine("Cleaned '" + name + "' cache.");
        }

    }

    /** Cleans the cache with the specified name */
    public static void cleanupStaticCache(String cacheName) {
        for (NamedResources r : staticCaches) {
            String name = r.getName();
            if (name.equals(cacheName)) {
                r.close();
                java.util.logging.Logger.getLogger("org.makumba.system").fine("Cleaned '" + name + "' cache.");
            }
        }
    }

    /** Returns the names of the currently used caches */
    public static ArrayList<String> getActiveCacheNames() {
        ArrayList<String> result = new ArrayList<String>();
        for (NamedResources r : staticCaches) {
            result.add(r.getName());
        }
        return result;
    }

    /**
     * Creates a static cache
     * 
     * @param name
     *            the name of the cache
     * @param fact
     *            the {@link NamedResourceFactory} used to create the cache
     * @param soft
     *            <code>true</code> if this should be a soft cache
     * @return The identifier of this cache
     */
    public synchronized static int makeStaticCache(String name, NamedResourceFactory fact, boolean soft) {
        staticCaches.add(soft ? new SoftNamedResources(name, fact) : new NamedResources(name, fact));
        return staticCaches.size() - 1;
    }

    /**
     * Creates a static cache
     * 
     * @param name
     *            the name of the cache
     * @param fact
     *            the NamdedResourceFactory used to create the cache
     * @return The identifier of this cache
     */
    public synchronized static int makeStaticCache(String name, NamedResourceFactory fact) {
        return makeStaticCache(name, fact, soft_static_caches);
    }

    /**
     * Gets the static cache identified by n
     * 
     * @param n
     *            the index of the cached object in the cache
     * @return The cache of resources
     */
    public static NamedResources getStaticCache(int n) {
        return staticCaches.get(n);
    }

    /**
     * Cleans the given cache. Use this for developing purposes.
     * 
     * @param n
     *            the index of the cache to be cleaned
     */
    public static void cleanStaticCache(int n) {
        staticCaches.get(n).values = new HashMap<Object, Object>();
    }

    public static void cleanStaticCache(String name) {
        for (NamedResources r : staticCaches) {
            if (r.getName().equals(name)) {
                // r.values.clear();
                r.close();
                java.util.logging.Logger.getLogger("org.makumba.system").fine("Cleaned '" + name + "' cache.");
            }
        }
    }

    /**
     * Wraps information about the cache into a Map
     * 
     * @return A Map having as keys the names of the caches and as value an array of int containing the size, hits and
     *         misses of each cache
     */
    public static Map<String, int[]> getCacheInfo() {
        Map<String, int[]> m = new HashMap<String, int[]>();
        for (int i = 0; i < allCaches.size(); i++) {
            NamedResources nr = allCaches.elementAt(i).get();
            if (nr == null) {
                continue;
            }
            int[] n = m.get(nr.getName());
            if (n == null) {
                m.put(nr.getName(), n = new int[3]);
            }
            n[0] += nr.size();
            n[1] += nr.hits;
            n[2] += nr.misses;
        }
        return m;
    }

    /**
     * Initializes using the given factory
     * 
     * @param name
     *            the name of the NamedResources object to initalise
     * @param f
     *            the {@link NamedResourceFactory} used to construct the NamedResource
     */
    public NamedResources(String name, NamedResourceFactory f) {
        this.name = name;
        this.f = f;
        allCaches.addElement(new java.lang.ref.WeakReference<NamedResources>(this));
    }

    /**
     * Checks if a resource is known
     * 
     * @param name
     *            the name of the resource to be checked
     * @return <code>true</code> if the resource is known, <code>false</code> otherwise
     */
    public boolean knowResource(Object name) {
        try {
            return values.get(f.getHashObject(name)) != null;
        } catch (Throwable t) {
            throw new RuntimeWrappedException(t);
        }
    }

    /**
     * Whatever supplementary stuff the factory wants to keep
     * 
     * @return An object with the supplementary things the factory may need
     */
    public Object getSupplementary() {
        return f.supplementary;
    }

    /**
     * Gets a specific resource. If it doesn't exist, calls the NamedResourceFactory to produce it
     * 
     * @param name
     *            the name of the resource to get
     * @return The resource
     */
    public Object getResource(Object name) {
        NameValue nv = null;

        synchronized (this) {
            Object hash = null;
            try {
                hash = f.getHashObject(name);
            } catch (RuntimeWrappedException t) {
                throw t;
            } catch (Throwable t) {
                throw new RuntimeWrappedException(t);
            }
            nv = getNameValue(name, hash);
        }
        return nv.getResource();
    }

    protected NameValue getNameValue(Object name, Object hash) {
        NameValue nv = (NameValue) values.get(hash);
        if (nv == null) {
            misses++;
            values.put(hash, nv = new NameValue(name, hash, f));
        } else {
            hits++;
        }
        return nv;
    }

    public int size() {
        return values.size();
    }

    public String getName() {
        return name;
    }

    /**
     * Closes each contained object by calling its close() method, if any
     */
    void closeContent() {
        for (Object element : values.keySet()) {
            Object nvo = values.get(element);
            if (nvo instanceof java.lang.ref.Reference) {
                nvo = ((java.lang.ref.Reference<?>) nvo).get();
                if (nvo == null) {
                    continue;
                }
            }

            if (!(((NameValue) nvo).returner instanceof NameValue)) {
                continue;
            }

            Object o = ((NameValue) nvo).getResource();

            java.lang.reflect.Method m = null;
            if (o == null) {
                continue;
            }
            try {
                m = o.getClass().getMethod("close", ((java.lang.Class[]) null));
            } catch (NoSuchMethodException e) {
                // we assume homogeneous caches
                return;
            }
            try {
                m.invoke(o, ((java.lang.Object[]) null));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    /**
     * Closes each contained object by calling its close() method, if any, then de-references all contained objects so
     * they can be garbage collected.
     */
    public void close() {
        closeContent();
        values.clear();
    }
}
