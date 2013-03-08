package org.makumba.jsf.component.el;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.context.FacesContext;
import javax.faces.model.ListDataModel;

import org.makumba.jsf.MakumbaDataContext;

/**
 * makumba EL functions
 * 
 * @author manu
 * @author cristi
 */
public class MakumbaFunctions {

    public static Object expr(String expr) {
        FacesContext faces = FacesContext.getCurrentInstance();
        ELContext el = faces.getELContext();
        ExpressionFactory ef = faces.getApplication().getExpressionFactory();
        return MakumbaDataContext.getDataContext().getCurrentList().getExpressionValue(expr);
    }

    public static Object from(String expr) {
        ArrayList<String> list = new ArrayList<String>();
        Collections.addAll(list, "a" + new Date(), "b" + new Date(), "c" + new Date());

        return new ListDataModel<String>(list) {
            @Override
            public void setRowIndex(int i) {
                super.setRowIndex(i);
                System.out.println(Integer.toString(System.identityHashCode(this), 16) + " " + i);
            }
        };
    }

    private static ArrayList<String> makeList() {
        ArrayList<String> list = new ArrayList<String>() {
            @Override
            public Iterator<String> iterator() {
                final Iterator<String> orig = super.iterator();
                final int hc = System.identityHashCode(this);
                return new Iterator<String>() {

                    @Override
                    public boolean hasNext() {
                        boolean b = orig.hasNext();
                        if (!b) {
                            System.out.println("end of iteration " + hc);
                        }
                        return b;
                    }

                    @Override
                    public String next() {
                        System.out.println("iteration " + hc);
                        return orig.next();
                    }

                    @Override
                    public void remove() {
                        orig.remove();
                    }

                };
            }

            @Override
            public String get(int i) {
                System.out.println("get " + i + " " + System.identityHashCode(this));
                return super.get(i);
            }
        };

        return list;
    }
}
