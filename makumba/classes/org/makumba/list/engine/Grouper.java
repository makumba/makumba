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

package org.makumba.list.engine;

import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.commons.ArrayMap;
import org.makumba.commons.MultipleKey;

/**
 * This class groups data coming in an Enumeration of Dictionaries. Grouping is done in more levels, each level is
 * defined by a set of keys of the dictionary. Elements of each group come in a Vector that is guaranteed to respect the
 * order in the original enumeration.
 * 
 * @author Cristian Bogdan
 */
public class Grouper extends Hashtable {

    private static final long serialVersionUID = 1L;

    Vector<Vector<Integer>> keyNameSets;

    /**
     * Groups the given data according to the given key sets.
     * 
     * @param keyNameSets
     *            a Vector of Vectors of Strings that represents key names
     * @param e
     *            the Enumeration of dictionaries containing the data
     */
    public Grouper(Vector<Vector<Integer>> keyNameSets, Enumeration<Dictionary<String, Object>> e) {
        this.keyNameSets = keyNameSets;
        long l = new Date().getTime();

        // for all read records
        while (e.hasMoreElements()) {
            ArrayMap data = (ArrayMap) e.nextElement();
            Hashtable h = this;
            Hashtable h1;
            int i = 0;
            int _max = keyNameSets.size() - 1;
            MultipleKey mk;

            // find the subresult where this record has to be inserted
            for (; i < _max; i++) {
                // make a keyset value
                mk = getKey(i, data.data);

                // get the subresult associated with it, make a new one if none exists
                h1 = (Hashtable) h.get(mk);
                if (h1 == null) {
                    h1 = new Hashtable();
                    h.put(mk, h1);
                }
                h = h1;
            }

            // insert the data in the subresult
            mk = getKey(i, data.data);
            Vector v = (Vector) h.get(mk);
            if (v == null) {
                v = new Vector();
                h.put(mk, v);
            }
            v.addElement(data);
        }

        max = keyNameSets.size() - 1;
        stack = new Hashtable[max + 1];
        keyStack = new MultipleKey[max];
        stack[0] = this;

        long diff = new Date().getTime() - l;

        // if(diff>20)
        java.util.logging.Logger.getLogger("org.makumba.db.query.performance.grouping").fine("grouping " + diff + " ms");
    }

    int max;

    Hashtable[] stack;

    MultipleKey[] keyStack;

    /**
     * Gets the Vector associated with the given keysets. The returned data is deleted from the Grouper.
     * 
     * @param keyData
     *            a Vector of Dictionaries representing a set of key values each
     * @return A Vector associated with the given keysets
     */
    public Vector<ArrayMap> getData(Vector<Dictionary<String, Object>> keyData) {
        int i = 0;
        for (; i < max; i++) {
            keyStack[i] = getKey(i, ((ArrayMap) keyData.elementAt(i)).data);
            stack[i + 1] = (Hashtable) stack[i].get(keyStack[i]);
            if (stack[i + 1] == null) {
                return null;
            }
        }
        Vector v = (Vector) stack[i].remove(getKey(i, ((ArrayMap) keyData.elementAt(i)).data));
        for (; i > 0 && stack[i].isEmpty(); i--) {
            stack[i - 1].remove(keyStack[i - 1]);
        }
        return v;
    }

    /**
     * Get the bunch of values associated with the keyset with the given index
     * 
     * @param n
     *            the index
     * @param data
     *            an object array containing the interesting data
     * @return A MultipleKey holding the values
     */
    protected MultipleKey getKey(int n, Object[] data) {
        return new MultipleKey((Vector) keyNameSets.elementAt(n), data);
    }
}
