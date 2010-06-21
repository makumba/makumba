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
 * TODO read class annotations<br>
 * 
 * @author Manuel Gay
 * @version $Id: MetadataAspectReader.java,v 1.1 Jun 21, 2010 3:14:06 PM manu Exp $
 */
public class MetadataAspectReader {

    private static MetadataAspectReader instance = null;

    public static MetadataAspectReader getInstance() {
        if (instance == null) {
            instance = new MetadataAspectReader();
        }
        return instance;
    }

    private AbstractClassReader r;

    private MetadataAspectReader() {
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

        Object v = null;

        switch (a.getType()) {
            case SIMPLE:
            case ARRAY:
                v = r.getAnnotationValue(a.getAnnotationClass(), a.getAttributeName(), m.getName(), clazz);
                break;
            case ANNOTATION_ARRAY:
                // @MakumbaEnum({@E(key = 5, value = "some"), @E(key = 6, value ="someother", deprecated=true)})
                v = r.getAnnotationValue(a.getAnnotationClass(), a.getAttributeName(), m.getName(), clazz);

                // TODO pluggable mechanism for reading the object model of the annotation
                // here we get AbstractAnnotation-s

                break;
        }

        return v;
    }

}
