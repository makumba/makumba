package org.makumba.test.jsf;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.faces.bean.ManagedBean;

@ManagedBean
public class HelloWorld {
	
	public String getName() {
		return "John";
	}

    public Vector<String> getList() {
        Vector<String> v = new Vector<String>();
        v.add("apple");
        v.add("banana");
        v.add("kiwi");
        return v;
    }
    
    public Map<String, Object> getMaps() {
    	Map<String, Object> m = new HashMap<String, Object>();
    	m.put("key1", "level1val1");
    	m.put("key2", "level1val2");
    	Map<String, Object> m1 = new HashMap<String, Object>();
    	m1.put("key11", "level2val1");
    	m1.put("key22", "level2val2");
    	m.put("key3", m1);
    	return m;
    }
    
}
