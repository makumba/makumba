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

import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.makumba.commons.ArrayMap;
import org.makumba.commons.MultipleKey;

/**
 * The grouper is a tree, with maps containing other maps, or lists. Lists are tree leaves.
 * 
 * @author cristi
 * @version $Id$
 */
class ListOrMap {

    List<ArrayMap> list;

    Map<MultipleKey, ListOrMap> map;

    public ListOrMap(List<ArrayMap> list) {
        this.list = list;
    }

    public ListOrMap() {
        // map is default
        map = new HashMap<MultipleKey, ListOrMap>();
    }

    @Override
    public String toString() {
        if (list != null) {
            return list.toString();
        }
        return map.toString();
    }

    public String toStringTree(String prefix) {
        StringBuffer lines = new StringBuffer();

        if (list != null) {
            for (ArrayMap x : list) {
                lines.append(prefix);
                for (Object o : x.data) {
                    lines.append(o).append("\t");
                }
                lines.append("\n");
            }
            return lines.toString();
        }
        for (Map.Entry<MultipleKey, ListOrMap> entry : map.entrySet()) {
            lines.append(prefix).append(entry.getKey()).append("\n");
            lines.append(entry.getValue().toStringTree(prefix + "\t")).append("\n");

        }
        return lines.toString();
    }
}

/**
 * This class groups data coming in an Iterator of Dictionaries. Grouping is done in more levels, each level is defined
 * by a set of keys of the dictionary. Elements of each group come in a List that is guaranteed to respect the order in
 * the original Iterator.
 * 
 * @author Cristian Bogdan
 */
public class Grouper {

    private static final long serialVersionUID = 1L;

    List<List<Integer>> keyNameSets;

    ListOrMap content = new ListOrMap();

    /**
     * Groups the given data according to the given key sets.
     * 
     * @param keyNameSets
     *            a List of Lists of Integers that represents indexes in data sets
     * @param e
     *            the Iterator of dictionaries containing the data
     */
    public Grouper(List<List<Integer>> keyNameSets, Iterator<Dictionary<String, Object>> e) {
        this.keyNameSets = keyNameSets;
        max = keyNameSets.size() - 1;
        long l = new Date().getTime();

        // for all read records
        while (e.hasNext()) {
            ArrayMap data = (ArrayMap) e.next();
            ListOrMap h = content;
            ListOrMap h1;
            int i = 0;

            MultipleKey mk;

            // find the subresult where this record has to be inserted
            for (; i < max; i++) {
                // make a keyset value
                mk = getKey(i, data.data);

                // get the subresult associated with it, make a new one if none exists
                h1 = h.map.get(mk);
                if (h1 == null) {
                    h1 = new ListOrMap();
                    h.map.put(mk, h1);
                }
                h = h1;
            }

            // insert the data in the subresult
            mk = getKey(i, data.data);
            ListOrMap lv = h.map.get(mk);
            if (lv == null) {
                lv = new ListOrMap(new ArrayList<ArrayMap>());
                h.map.put(mk, lv);
            }
            lv.list.add(data);
        }

        stack = new ListOrMap[max + 1];
        keyStack = new MultipleKey[max];
        stack[0] = content;

        long diff = new Date().getTime() - l;

        // if(diff>20)
        java.util.logging.Logger.getLogger("org.makumba.db.query.performance.grouping").fine("grouping " + diff + " ms");
    }

    int max;

    ListOrMap[] stack;

    MultipleKey[] keyStack;

    // TODO: provide deletion as an option, to be able to reiterate the grouper.

    /**
     * Gets the List associated with the given keysets. The returned data is deleted from the Grouper.
     * 
     * @param keyData
     *            a List of Dictionaries representing a set of key values each
     * @return A List associated with the given keysets
     */
    public List<ArrayMap> getData(List<Dictionary<String, Object>> keyData) {
        return getData(keyData, true);
    }

    public List<ArrayMap> getData(List<Dictionary<String, Object>> keyData, boolean remove) {

        int i = 0;
        for (; i < max; i++) {
            keyStack[i] = getKeyFromData(keyData, i);
            stack[i + 1] = stack[i].map.get(keyStack[i]);
            if (stack[i + 1] == null) {
                return null;
            }
        }
        MultipleKey key = getKeyFromData(keyData, i);
        ListOrMap lm = stack[i].map.get(key);
        if (remove) {
            stack[i].map.remove(key);
        }
        List<ArrayMap> v = null;
        if (lm != null) {
            v = lm.list;
        }
        if (remove) {
            for (; i > 0 && stack[i].map.isEmpty(); i--) {
                stack[i - 1].map.remove(keyStack[i - 1]);
            }
        }
        return v;
    }

    private MultipleKey getKeyFromData(List<Dictionary<String, Object>> keyData, int i) {
        return getKey(i, ((ArrayMap) keyData.get(i)).data);
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
        return new MultipleKey(keyNameSets.get(n), data);
    }

    @Override
    public String toString() {
        return "Grouper with keys " + keyNameSets + "\n" + content.toStringTree("");
    }
}
