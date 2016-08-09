package org.makumba.jsf.component.el;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Special list for printing a list of titles in toString() instead of the objects
 * 
 * @author manu
 */
public class SetList<E> extends ArrayList<E> {

    private static final long serialVersionUID = 1L;

    private List<String> titles = new ArrayList<String>();

    public void setTitles(List<String> titleList) {
        this.titles = titleList;
    }

    public List<String> getTiteList() {
        return this.titles;
    }

    @Override
    public String toString() {
        String ret = "";
        Iterator<String> it = titles.iterator();
        while (it.hasNext()) {
            ret += it.next();
            if (it.hasNext()) {
                ret += ", ";
            }
        }
        return ret;
    }

}
