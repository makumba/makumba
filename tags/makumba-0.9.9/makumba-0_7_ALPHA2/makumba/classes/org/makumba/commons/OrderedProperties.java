package org.makumba.commons;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * FIXME: can this be replaced by some collection from java.util or apache-commons-collections?
 * 
 * @author Cristian Bogdan
 * @version $Id: ReferenceChecker.java,v 1.1 12.10.2007 05:17:31 Rudolf Mayer Exp $
 */
public class OrderedProperties extends Dictionary {
    Vector<Object> ks = new Vector<Object>();

    Hashtable<Object, Object> orig = new Hashtable<Object, Object>();

    Hashtable<Object, Object> content = new Hashtable<Object, Object>();

    public String toString() {
        StringBuffer sb = new StringBuffer("{");
        Enumeration e = keys();
        if (e.hasMoreElements()) {
            Object o = e.nextElement();
            sb.append(o).append("=").append(get(o));
            while (e.hasMoreElements()) {
                o = e.nextElement();
                sb.append(", ").append(o).append("= ").append(get(o));
            }
        }
        return sb.append('}').toString();
    }

    public Enumeration elements() {
        return ((Hashtable) content.clone()).elements();
    }

    public Object get(Object key) {
        return content.get(key);
    }

    public Enumeration keys() {
        return ((Vector) ks.clone()).elements();
    }

    public String getOriginal(String key) {
        return (String) orig.get(key);
    }

    public String keyAt(int i) {
        return (String) ks.elementAt(i);
    }

    public Object remove(Object key) {
        ks.removeElement(key);
        orig.remove(key);
        return content.remove(key);
    }

    public Object putAt(int n, Object key, Object origKey, Object value) {
        ks.insertElementAt(key, n);
        orig.put(key, origKey);
        return content.put(key, value);
    }

    public synchronized Object putLast(Object key, Object origKey, Object value) {
        Object o = content.put(key, value);
        if (o != null)
            ks.removeElement(key);
        ks.addElement(key);
        orig.put(key, origKey);
        return o;
    }

    public Object put(Object key, Object value) {
        return putLast(key, key, value);
    }

    public String getProperty(String s) {
        return (String) get(s);
    }

    public int size() {
        return content.size();
    }

    public boolean isEmpty() {
        return content.isEmpty();
    }

}
