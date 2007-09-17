package org.makumba.analyser;

import java.util.HashMap;

import org.makumba.MakumbaError;
import org.makumba.list.engine.ComposedQuery;
import org.makumba.list.engine.ComposedSubquery;
import org.makumba.util.MultipleKey;

/**
 * Cache for the page analysis. It is passed along during analysis and holds useful caches. This class provides two
 * methods to add/retrieve caches throughout the analysis process.
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 */
public class PageCache {

    private HashMap caches = new HashMap();

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
        HashMap cache = (HashMap) caches.get(cacheName);
        if (cache == null) {
            cache = new HashMap();
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

}
