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

package org.makumba.providers.datadefinition.makumba;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * FIXME: can this be replaced by some collection from java.util or apache-commons-collections?
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class OrderedProperties extends Dictionary<String, String> {
    Vector<String> ks = new Vector<String>();

    Hashtable<String, String> orig = new Hashtable<String, String>();

    Hashtable<String, String> content = new Hashtable<String, String>();

    public String toString() {
        StringBuffer sb = new StringBuffer("{");
        Enumeration<String> e = keys();
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

    public Enumeration<String> elements() {
        return ((Hashtable<String, String>) content.clone()).elements();
    }

    public String get(Object key) {
        return content.get(key);
    }

    public Enumeration<String> keys() {
        return ((Vector) ks.clone()).elements();
    }

    public String getOriginal(String key) {
        return (String) orig.get(key);
    }

    public String keyAt(int i) {
        return (String) ks.elementAt(i);
    }

    public String remove(Object key) {
        ks.removeElement(key);
        orig.remove(key);
        return content.remove(key);
    }

    public Object putAt(int n, String key, String origKey, String value) {
        ks.insertElementAt(key, n);
        orig.put(key, origKey);
        return content.put(key, value);
    }

    public synchronized String putLast(String key, String origKey, String value) {
        String o = content.put(key, value);
        if (o != null)
            ks.removeElement(key);
        ks.addElement(key);
        orig.put(key, origKey);
        return o;
    }

    public String put(String key, String value) {
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
