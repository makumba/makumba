package org.makumba.commons;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.persistence.Column;

import org.makumba.providers.bytecode.AbstractClassReader;
import org.makumba.providers.bytecode.Clazz;
import org.makumba.providers.bytecode.JavassistClassReader;

public class AnnotationUtil {

    private final static Class<?>[] emptyClassArray = new Class<?>[] {};

    private final static Object[] emptyObjectArray = new Object[] {};

    public static Object readAttributeValue(Method m, Class<? extends Annotation> annotationClass, String attributeName) {
        Annotation a = m.getAnnotation(annotationClass);
        Method propertyGetter;
        Object propertyValue = null;

        try {
            propertyGetter = a.getClass().getMethod(attributeName, emptyClassArray);
            propertyValue = propertyGetter.invoke(a, emptyObjectArray);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return propertyValue;
    }

    public static void main(String... args) throws Exception {

        final int READS = 100000;

        System.out.println("==== Annotation reading via classloader & reflection");

        Vector<Long> times = new Vector<Long>();

        for (int i = 0; i < READS; i++) {
            long start = System.currentTimeMillis();
            Class<?> c = Class.forName("test.Language");
            Method m = c.getDeclaredMethod("getIsoCode", (Class<?>[]) null);
            Object v = readAttributeValue(m, Column.class, "unique");
            long end = System.currentTimeMillis();
            times.add(end - start);
        }

        long sum = 0;
        for (Long l : times) {
            sum += l;
        }
        System.out.println("Average/read for " + READS + " reads: " + sum / READS + " ms");
        System.out.println("Total time for " + READS + " reads: " + sum + " ms");

        times.clear();

        System.out.println("==== Annotation reading via javassist");

        for (int i = 0; i < READS; i++) {
            long start = System.currentTimeMillis();
            AbstractClassReader acv = new JavassistClassReader();
            Clazz clazz = acv.getClass("test.Language");
            Object value = acv.getAnnotationAttributeValue(Column.class, "unique", "getIsoCode", clazz);
            long end = System.currentTimeMillis();
            times.add(end - start);
        }

        long sum2 = 0;
        for (Long l : times) {
            sum2 += l;
        }
        System.out.println("Average/read for " + READS + " reads: " + sum2 / READS + " ms");
        System.out.println("Total time for " + READS + " reads: " + sum2 + " ms");

    }

}
