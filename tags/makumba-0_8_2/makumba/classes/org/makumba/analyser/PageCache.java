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

package org.makumba.analyser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.set.ListOrderedSet;

/**
 * Cache for the page analysis. It is passed along during analysis and holds useful caches. This class provides two
 * methods to add/retrieve caches throughout the analysis process.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @author Rudolf Mayer
 * @version $Id$
 */
public class PageCache {

    private HashMap<String, HashMap<Object, Object>> caches = new HashMap<String, HashMap<Object, Object>>();

    private HashMap<String, ListOrderedSet> setCaches = new HashMap<String, ListOrderedSet>();

    /**
     * Caches an object in a specific cache
     * 
     * @param cacheName
     *            the name of the cache we want to work with
     * @param key
     *            the key of the object in this cache
     * @param value
     *            the value of the object to be cached
     */
    public void cache(String cacheName, Object key, Object value) {
        HashMap<Object, Object> cache = caches.get(cacheName);
        if (cache == null) {
            cache = new HashMap<Object, Object>();
            caches.put(cacheName, cache);
        }
        cache.put(key, value);
    }

    /**
     * Retrieves an object of a specific cache
     * 
     * @param cacheName
     *            the name of the cache we want to work with
     * @param key
     *            the key of the object in this cache
     * @return the object corresponding to the cache entry, <code>null</code> if no entry exists
     */
    public Object retrieve(String cacheName, Object key) {
        HashMap<Object, Object> cache = caches.get(cacheName);
        if (cache == null) {
            return null;
        }
        return cache.get(key);
    }

    /**
     * Gets a whole cache
     * 
     * @param cacheName
     *            the name of the cache
     * @return a Map with the content of the cache
     */
    public Map<Object, Object> retrieveCache(String cacheName) {
        return caches.get(cacheName);
    }

    /**
     * Caches several objects in a specific cache, using sets, i.e. not keeping duplicate values.
     */
    public void cacheSetValues(String cacheName, Object[] value) {
        ListOrderedSet hashSet = setCaches.get(cacheName);
        if (hashSet == null) {
            hashSet = new ListOrderedSet();
            setCaches.put(cacheName, hashSet);
        }

        hashSet.addAll(Arrays.asList(value));
    }

    /** Retrieves a set from a specific set cache. */
    public ListOrderedSet retrieveSetValues(String cacheName) {
        return setCaches.get(cacheName);
    }

    @Override
    public String toString() {
        String result = "== Simple caches\n";
        for (String key : caches.keySet()) {
            result += "  == Key " + key + "\n";
            HashMap<Object, Object> cache = caches.get(key);
            for (Object key2 : cache.keySet()) {
                result += "    - " + key2 + " => " + cache.get(key2) + "\n";
            }
        }

        result += "== Set caches\n";
        for (String key : setCaches.keySet()) {
            result += "  == Key " + key + "\n";
            ListOrderedSet cache = setCaches.get(key);
            for (Object entry : cache) {
                result += "    - " + entry + "\n";
            }
        }

        return result;
    }

    public String toString(String key) {
        String result = "== Content of cache " + key + "\n";
        HashMap<Object, Object> cache = caches.get(key);

        Iterator<Object> it2 = cache.keySet().iterator();
        while (it2.hasNext()) {
            Object key2 = it2.next();
            if (key2 != null) {
                result += "  key: " + key2.toString() + "\n    value: " + cache.get(key2).toString() + "\n";
            }
        }

        return result;
    }
}
