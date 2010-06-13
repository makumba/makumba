package org.makumba.providers;

import java.lang.reflect.Member;
import java.lang.reflect.Method;

import javax.persistence.Column;

/**
 * Register for properties of (simple) fields.<br>
 * 
 * @author manu
 * @version $Id: ModifierRegister.java,v 1.1 Jun 12, 2010 1:22:58 AM manu Exp $
 */
public class FieldPropertyRegister extends AbstractBooleanAspectRegister {

    @Override
    public void registerAspects() {
        registerAspect("unique", null, Column.class, "unique", true);
    }

    public boolean isUnique(Member m) {
        return match("unique", m);
    }

    public static void main(String... args) throws Exception {
        Class<?> c = Class.forName("test.Language");
        System.out.println(c.getName());
        Method m = c.getDeclaredMethod("getIsoCode", null);
        FieldPropertyRegister mr = new FieldPropertyRegister();
        mr.registerAspects();
        System.out.println(mr.isUnique(m));

    }
}
