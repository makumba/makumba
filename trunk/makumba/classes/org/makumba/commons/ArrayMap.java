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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class exports an Object[] array as a dictionary
 * 
 * @author Cristian Bogdan
 */
public class ArrayMap extends Dictionary<String, Object> {
    public Object[] data;

    Dictionary<String, Integer> keyIndex;

    public ArrayMap(Dictionary<String, Integer> d, Object[] o) {
        data = o;
        keyIndex = d;
    }

    public ArrayMap() {
        keyIndex = new Hashtable<String, Integer>();
    }

    Integer index(Object key) {
        return keyIndex.get(key);
    }

    @Override
    public Object get(Object key) {
        Integer i = index(key);
        if (i == null) {
            return null;
        }
        return data[i.intValue()];
    }

    @Override
    public Object put(String key, Object value) {
        Integer i = index(key);
        if (i != null) {
            Object ret = data[i.intValue()];
            data[i.intValue()] = value;
            return ret;
        } else {
            throw new RuntimeException("invalid key: " + key);
        }
    }

    @Override
    public Object remove(Object key) {
        return put((String) key, null);
    }

    @Override
    public Enumeration<String> keys() {
        return new Enumeration<String>() {
            Object nxt;

            Object next;

            Enumeration<String> e;
            {
                e = keyIndex.keys();
                findNext();
            }

            void findNext() {
                next = null;
                while (e.hasMoreElements() && (next = get(nxt = e.nextElement())) == null) {
                    ;
                }
            }

            public boolean hasMoreElements() {
                return next != null;
            }

            public String nextElement() {
                Object o = nxt;
                findNext();
                return (String) o;
            }
        };
    }

    @Override
    public Enumeration<Object> elements() {
        return new Enumeration<Object>() {
            int i;
            {
                i = 0;
                findNext();
            }

            void findNext() {
                while (i < data.length && data[i] == null) {
                    i++;
                }
            }

            public boolean hasMoreElements() {
                return i < data.length;
            }

            public Object nextElement() {
                Object o = data[i++];
                findNext();
                return o;
            }
        };
    }

    @Override
    public int size() {
        int n = 0;
        for (Object element : data) {
            if (element != null) {
                n++;
            }
        }
        return n;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public String toString() {
        StringBuffer ret = new StringBuffer();
        ret.append("{");
        String sep = "";
        Object o;
        for (Enumeration<String> e = keys(); e.hasMoreElements();) {
            ret.append(sep);
            sep = ",";
            ret.append(o = e.nextElement()).append("=").append(get(o));
        }
        ret.append("}");
        return ret.toString();
    }

}
