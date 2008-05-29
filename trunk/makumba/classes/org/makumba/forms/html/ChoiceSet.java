package org.makumba.forms.html;

import java.io.Serializable;

/**
 * An ordered set of chooser choices
 * @author Cristian Bogdan
 */
public class ChoiceSet extends java.util.ArrayList {

    private static final long serialVersionUID = 1L;

    public static final String PARAMNAME = "org.makumba.ChoiceSet";

    public class Choice implements Serializable{
        Object value;

        String title;

        boolean forceSelection;

        boolean forceDeselection;

        public Object getValue() {
            return value;
        }

        public String getTitle() {
            return title;
        }
        
        @Override
        public String toString() {
            return "Choice, title: '" + getTitle() + "', value: '" + getValue() + "'";
        }
    }

    java.util.Map<Object, Choice> m = new java.util.HashMap<Object, Choice>();

    public void add(Object value, String title, boolean forceSelection, boolean forceDeselection) {
        // only Pointer.Null values can be repeated, rest are ignored
        // null values are not a choice, they are simply a text between choices
        if (value != org.makumba.Pointer.Null && value != null && m.get(value) != null)
            return;
        Choice c = new Choice();
        c.value = value;
        c.title = title;
        c.forceSelection = forceSelection;
        c.forceDeselection = forceDeselection;
        if (value != null)
            m.put(value, c);
        add(c);
    }
}
