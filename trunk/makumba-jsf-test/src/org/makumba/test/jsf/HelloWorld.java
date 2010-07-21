package org.makumba.test.jsf;

import java.util.Vector;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class HelloWorld {

    public Vector<String> getList() {
        Vector<String> v = new Vector<String>();
        v.add("apple");
        v.add("banana");
        v.add("kiwi");
        return v;
    }

}
