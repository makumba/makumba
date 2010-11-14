package org.makumba.providers;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.makumba.MetadataAspect;
import org.makumba.providers.bytecode.AbstractClassReader;
import org.makumba.providers.bytecode.Clazz;
import org.makumba.providers.bytecode.JavassistClassReader;

/**
 * Class capable of reading class/member meta-data in a structured/pluggable way.<br>
 * TODO pluggable reader mechanism for complex aspect values<br>
 * TODO read class annotations: wrappers around this Reader that are then used in the code in order to read aspects in a
 * specialised way<br>
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id$
 */
public class AnnotationMetadataReader {

    private static AnnotationMetadataReader instance = null;

    public static AnnotationMetadataReader getInstance() {
        if (instance == null) {
            instance = new AnnotationMetadataReader();
        }
        return instance;
    }

    private AbstractClassReader r;

    private AnnotationMetadataReader() {
        r = new JavassistClassReader();
    }

    public String readStringAspectValue(Member m, MetadataAspect a) {
        return (String) readAspectValue(m, a);
    }

    public Integer readIntegerAspectValue(Member m, MetadataAspect a) {
        return (Integer) readAspectValue(m, a);
    }

    public Boolean readBooleanAspectValue(Member m, MetadataAspect a) {
        return (Boolean) readAspectValue(m, a);
    }

    public Object readAspectValue(Member m, MetadataAspect a) {

        boolean present = true;
        String declaringClassName = null;

        if (m instanceof Method) {
            Method me = (Method) m;
            present = me.isAnnotationPresent(a.getAnnotationClass());
            declaringClassName = me.getDeclaringClass().getName();
        } else if (m instanceof Field) {
            Field f = (Field) m;
            present = f.isAnnotationPresent(a.getAnnotationClass());
            declaringClassName = f.getDeclaringClass().getName();
        }

        if (!present) {
            return null;
        }

        Clazz clazz = null;
        try {
            clazz = r.getClass(declaringClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Object v = r.getAnnotationAttributeValue(a.getAnnotationClass(), a.getAttributeName(), m.getName(), clazz);

        return v;
    }

}
