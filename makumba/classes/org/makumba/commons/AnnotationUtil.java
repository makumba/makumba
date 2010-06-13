package org.makumba.commons;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Vector;

import javax.persistence.Column;

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
        Vector<Long> times = new Vector<Long>();

        for (int i = 0; i < 100000; i++) {
            long start = System.currentTimeMillis();
            Class<?> c = Class.forName("test.Language");
            Method m = c.getDeclaredMethod("getIsoCode", null);
            Object v = readAttributeValue(m, Column.class, "unique");
            long end = System.currentTimeMillis();
            times.add(end - start);
        }

        long sum = 0;
        for (Long l : times) {
            sum += l;
        }
        System.out.println("Average/read for 100000 reads: " + sum / 1000 + " ms");

    }

}
