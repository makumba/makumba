package org.makumba.test.jsf;

import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class ListBean {
    static int n = 0;

    public List<Object> getList() {

        List<Object> v = new ArrayList<Object>();
        v.add(n++);
        v.add(n++);
        v.add(n++);
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
}
