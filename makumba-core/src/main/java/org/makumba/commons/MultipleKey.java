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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

/**
 * Holds the values for a group of fields. Every element of a MultipleKey holds an Object. In case of a null value being
 * passed to one of the constructors, the element is replaced by a placeholder.
 * 
 * @author Cristian Bogdan
 * @version $Id$
 */
public class MultipleKey extends ArrayList<Object> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Dummy object holding the place for a null element
     */
    static private final String theNull = "null key member";

    public MultipleKey(int size) {
        super(size);
    }

    public MultipleKey(List<Object> v) {
        this(v, v.size());
    }

    public MultipleKey(List<Object> v, int size) {
        super(size);
        for (Object o : v) {
            add(checkNull(o));
        }
    }

    /**
     * Extends the Vector v and adds the object
     * 
     * @param v
     *            An instance of Vector
     * @param o
     *            An object to be added
     */
    public MultipleKey(List<Object> v, Object o) {
        this(v, v.size() + 1);
        setAt(o, v.size());
    }

    public MultipleKey(Object... o) {
        this(o, o.length);
    }

    public MultipleKey(Object array[], int size) {
        super(size);
        for (Object o : array) {
            add(checkNull(o));
        }
    }

    /**
     * Keeps together the values associated with the give keys
     * 
     * @param keys
     *            A vector of keys
     * @param data
     *            The dictionary containing the values of the keys
     */
    public MultipleKey(List<String> keys, Dictionary<String, Object> data) {
        this(keys, data, keys.size());
    }

    /**
     * Keeps together the values associated with the given keys
     * 
     * @param keys
     *            A vector of keys
     * @param data
     *            The dictionary containing the values of the keys
     * @param size
     *            The size of the MultipleKey object to be constructed
     */
    public MultipleKey(List<String> keys, Dictionary<String, Object> data, int size) {
        super(size);
        for (String key : keys) {
            add(checkNull(data.get(key)));
        }
    }

    /**
     * Keeps together the values associated with the given keys
     * 
     * @param indexes
     *            A vector of keys
     * @param data
     *            The object array containing the values of the keys
     */
    public MultipleKey(List<Integer> indexes, Object[] data) {
        this(indexes, data, indexes.size());
    }

    /**
     * Keep together the values associated with the given keys
     * 
     * @param indexes
     *            A vector of keys
     * @param data
     *            The object array containing the values of the keys
     * @param size
     *            The size of the MultipleKey object to be constructed
     */
    public MultipleKey(List<Integer> indexes, Object[] data, int size) {
        super(size);
        for (Integer i : indexes) {
            add(checkNull(data[i]));
        }
    }

    public void setAt(Object o, int n) {
        while (size() <= n) {
            add(theNull);
        }
        set(n, checkNull(o));
    }

    protected Object checkNull(Object o) {
        return o == null ? theNull : o;
    }

    /**
     * Compares the elements of two MultipleKey objects
     * 
     * @param o
     *            The object to be compared with
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MultipleKey)) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!((MultipleKey) o).get(i).equals(get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Computes a hash code of the MultipleKey by adding the individual hashcodes of each element
     * 
     * @return The hashcode of the MultipleKey
     */
    @Override
    public int hashCode() {
        int ret = 0;
        for (int i = 0; i < size(); i++) {
            ret += get(i).hashCode();
        }
        return ret;
    }
}
