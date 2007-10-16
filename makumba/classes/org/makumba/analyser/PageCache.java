package org.makumba.analyser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Cache for the page analysis. It is passed along during analysis and holds useful caches. This class provides two
 * methods to add/retrieve caches throughout the analysis process.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @author Rudolf Mayer
 */
public class PageCache {

    private HashMap<String, HashMap<Object, Object>> caches = new HashMap<String, HashMap<Object, Object>>();

    private HashMap<String, HashSet<Object>> setCaches = new HashMap<String, HashSet<Object>>();

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
        HashMap<Object, Object> cache = (HashMap<Object, Object>) caches.get(cacheName);
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
        HashMap cache = (HashMap) caches.get(cacheName);
        if (cache == null)
            return null;
        return cache.get(key);
    }

    /**
     * Returns all elements of a cache in an ordered way
     * 
     * @param cacheName
     *            the name of the cache we want to work with
     * @return a List containing all the elements of the cache in the order of addition to the cache
     */
    public List retrieveElements(String cacheName) {
        HashMap cache = (HashMap) caches.get(cacheName);
        if (cache == null)
            return null;

        List orderedElements = new LinkedList();

        Iterator i = cache.keySet().iterator();
        while (i.hasNext()) {
            orderedElements.add(cache.get(i.next()));
        }
        return orderedElements;
    }

    /**
     * Caches several objects in a specific cache, using sets, i.e. not keeping duplicate values.
     */
    public void cacheSetValues(String cacheName, Object[] value) {
        HashSet<Object> hashSet = setCaches.get(cacheName);
        if (hashSet == null) {
            hashSet = new HashSet<Object>();
            setCaches.put(cacheName, hashSet);
        }

        hashSet.addAll(Arrays.asList(value));
    }

    /** Retrieves a set from a specific set cache. */
    public HashSet<Object> retrieveSetValues(String cacheName) {
        return setCaches.get(cacheName);
    }
}
