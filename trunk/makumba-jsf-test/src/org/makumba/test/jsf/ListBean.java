package org.makumba.test.jsf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class ListBean {
    static int n = 0;
    static List<HashMap<String, Object>> v = new ArrayList<HashMap<String, Object>>();

    static {
        addMap(v);
        addMap(v);
    }

    public List<HashMap<String, Object>> getList() {

        return v;

        /*
         * List<Object> apples = new ArrayList<Object>();
         * apples.add("a-1-red apple"); apples.add("a-2-orange apple");
         * apples.add("a-3-yellow apple"); v.add(apples);
         * 
         * List<Object> bananas = new ArrayList<Object>();
         * //bananas.add("b-1-red banana"); //bananas.add("b-2-orange banana");
         * //bananas.add("b-3-yellow banana"); v.add(bananas);
         * 
         * List<Object> oranges = new ArrayList<Object>();
         * oranges.add("o-1-red orange"); oranges.add("o-2-orange orange");
         * oranges.add("o-3-yellow orange"); v.add(oranges);
         * 
         * return v;
         */

    }

    static private void addMap(List<HashMap<String, Object>> v) {
        HashMap<String, Object> h = new HashMap<String, Object>();
        h.put("a", "" + n++);
        v.add(h);
    }
}
