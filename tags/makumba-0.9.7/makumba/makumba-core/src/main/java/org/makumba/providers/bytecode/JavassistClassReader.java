package org.makumba.providers.bytecode;

import java.util.Set;
import java.util.Vector;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import org.makumba.MakumbaError;

/**
 * TODO optimize memory consumption if possible, read {@link ClassPool} documentation<br>
 * TODO method reading --> combined method & field reading<br>
 * 
 * @author Manuel Bernhardt (manuel@makumba.org)
 * @version $Id: JavassistClassReader.java,v 1.1 Jun 18, 2010 4:10:53 PM manu Exp $
 */
public class JavassistClassReader extends AbstractClassReader {

    @Override
    public Clazz getClass(String fullyQualifiedClassName) throws ClassNotFoundException {

        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        CtClass cc;

        try {
            cc = cp.get(fullyQualifiedClassName);
        } catch (NotFoundException e) {
            throw new ClassNotFoundException(e.getMessage());
        }

        Clazz clazz = new Clazz(fullyQualifiedClassName);
        clazz.setClassObjectReference(cc);
        return clazz;
    }

    @Override
    public Vector<AbstractAnnotation> getAnnotations(String methodName, Clazz clazz) {

        Vector<AbstractAnnotation> result = new Vector<AbstractAnnotation>();

        for (Annotation annotation : readVisibleAnnotations(methodName, clazz)) {
            readAnnotations(result, annotation);
        }
        return result;
    }

    private Annotation[] readVisibleAnnotations(String methodName, Clazz clazz) {
        CtClass cc = (CtClass) clazz.getClassObjectReference();
        ClassFile cf = cc.getClassFile();

        CtMethod m = null;
        try {
            m = cc.getDeclaredMethod(methodName);
        } catch (NotFoundException e) {
            throw new MakumbaError("Method " + methodName + " not found in class " + clazz.getName());
        }

        AnnotationsAttribute attr = (AnnotationsAttribute) m.getMethodInfo().getAttribute(
            AnnotationsAttribute.visibleTag);
        return attr.getAnnotations();
    }

    private void readAnnotations(Vector<AbstractAnnotation> result, Annotation annotation) {
        AbstractAnnotation aa = new AbstractAnnotation(annotation.getTypeName());
        result.add(aa);
        Set<?> members = annotation.getMemberNames();
        for (Object m : members) {

            Object v = annotation.getMemberValue((String) m);
            if (v == null) {
                throw new MakumbaError("Attribute '" + m + "' not found");
            }
            Object value = readAttributeValue(v);
            aa.addAttribute((String) m, value);
        }
    }

    private Object readAttributeValue(Object v) throws MakumbaError {
        Object value = null;

        if (v instanceof StringMemberValue) {
            value = ((StringMemberValue) v).getValue();
        } else if (v instanceof IntegerMemberValue) {
            value = ((IntegerMemberValue) v).getValue();
        } else if (v instanceof AnnotationMemberValue) {
            // nested annotations, oh joy!
            Annotation na = ((AnnotationMemberValue) v).getValue();
            Vector<AbstractAnnotation> nested = new Vector<AbstractAnnotation>();
            readAnnotations(nested, na);
            value = nested;
        } else if (v instanceof EnumMemberValue) {
            value = ((EnumMemberValue) v).getValue();
        } else if (v instanceof ClassMemberValue) {
            value = ((ClassMemberValue) v).getValue();
        } else if (v instanceof BooleanMemberValue) {
            value = ((BooleanMemberValue) v).getValue();
        } else if (v instanceof ArrayMemberValue) {
            ArrayMemberValue amv = (ArrayMemberValue) v;
            MemberValue[] mvs = amv.getValue();
            Object[] val = new Object[mvs.length];
            for (int i = 0; i < mvs.length; i++) {
                val[i] = readAttributeValue(mvs[i]);
            }
            value = val;
        } else {
            throw new MakumbaError("Unimplemented type " + v.getClass());
        }
        return value;
    }

    @Override
    public Object getAnnotationAttributeValue(Class<?> annotationClass, String attributeName, String methodName,
            Clazz clazz) {
        for (Annotation a : readVisibleAnnotations(methodName, clazz)) {
            if (a.getTypeName().equals(annotationClass.getName())) {
                Object v = a.getMemberValue(attributeName);
                if (v == null) {
                    throw new MakumbaError("Attribute '" + attributeName + "' not found");
                }
                return readAttributeValue(v);
            }
        }
        return null;
    }

    public static void main(String... args) {
        System.out.println("Reading...");
        AbstractClassReader acv = new JavassistClassReader();
        try {
            Clazz clazz = acv.getClass("test.Person");
            Vector<AbstractAnnotation> v = acv.getAnnotations("getBirthdate", clazz);
            for (AbstractAnnotation abstractAnnotation : v) {
                System.out.println("Found annotation '" + abstractAnnotation.getName() + "' with following attributes");
                for (Object m : abstractAnnotation.getAttribues().keySet()) {
                    System.out.println("== Attribute '" + m + "' with value '"
                            + abstractAnnotation.getAttribues().get(m) + "'");
                }

            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
