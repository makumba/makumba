package org.makumba.providers.bytecode;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.AttributeInfo;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.MemberValue;
import javassist.bytecode.annotation.StringMemberValue;

import javax.persistence.ManyToMany;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.makumba.MakumbaError;
import org.makumba.commons.NameResolver;

public class JavassistClassWriter extends AbstractClassWriter {

    @Override
    public void addField(Clazz clazz, String name, String type) {

        CtClass cc = (CtClass) clazz.getClassObjectReference();
        try {
            addField(name, type, cc);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addClassAnnotations(Clazz clazz, Vector<AbstractAnnotation> annotations) {
        CtClass cc = (CtClass) clazz.getClassObjectReference();
        cc.getClassFile().addAttribute(constructAnnotationAttributeInfo(clazz, annotations));

    }

    @Override
    public void addMethodAnnotations(Clazz clazz, String methodName, Vector<AbstractAnnotation> annotations) {
        CtMethod fld = null;
        CtClass cc = (CtClass) clazz.getClassObjectReference();
        for (CtMethod cm : cc.getMethods()) {
            if (cm.getName().equals(methodName)) {
                fld = cm;
            }
        }
        if (fld == null) {
            throw new MakumbaError("Method " + methodName + " not found in class " + clazz.getName());
        }

        fld.getMethodInfo().addAttribute(constructAnnotationAttributeInfo(clazz, annotations));
    }

    private AttributeInfo constructAnnotationAttributeInfo(Clazz clazz, Vector<AbstractAnnotation> annotations) {
        CtClass cc = (CtClass) clazz.getClassObjectReference();
        ClassFile cf = cc.getClassFile();
        ConstPool cp = cf.getConstPool();
        AnnotationsAttribute attr = new AnnotationsAttribute(cp, AnnotationsAttribute.visibleTag);

        for (AbstractAnnotation aa : annotations) {
            Annotation a = addAnnotation(aa.getName(), cp, aa.getAttribues());
            if (attr.getAnnotations().length > 0) {
                Annotation[] anns = (Annotation[]) ArrayUtils.add(attr.getAnnotations(), a);
                attr.setAnnotations(anns);
            } else {
                attr.setAnnotation(a);
            }
        }
        return attr;
    }

    private Annotation addAnnotation(String annotationName, ConstPool cp, Map<String, Object> annotationAttributes) {

        Annotation a = new Annotation(annotationName, cp);

        for (String attribute : annotationAttributes.keySet()) {
            Object v = annotationAttributes.get(attribute);
            MemberValue mv = null;
            if (v instanceof String) {
                mv = new StringMemberValue((String) v, cp);
            } else if (v instanceof AbstractAnnotation) {
                // nested annotations, oh joy!
                AbstractAnnotation nestedAnnotation = (AbstractAnnotation) v;
                AnnotationMemberValue amv = new AnnotationMemberValue(cp);
                Annotation na = addAnnotation(nestedAnnotation.getName(), cp, nestedAnnotation.getAttribues());
                amv.setValue(na);
                // FIXME what if there are several nested annotations for the _same_ attribute?
                // I guess we need to make a ArrayMemberValue
                // for now this can't happen because we get a Map anyway, so we are kind of screwed unless we start
                // using a MultiValueMap
                mv = amv;
            } else if (v instanceof Enum<?>) {
                EnumMemberValue emv = new EnumMemberValue(cp);
                emv.setType(((Enum<?>) v).getClass().getName());
                emv.setValue(((Enum<?>) v).name());
                mv = emv;
            } else if (v instanceof Class<?>) {
                ClassMemberValue cmv = new ClassMemberValue(cp);
                cmv.setValue(((Class<?>) v).getName());
                mv = cmv;
            } else if (v instanceof Boolean) {
                BooleanMemberValue bmv = new BooleanMemberValue(cp);
                bmv.setValue((Boolean) v);
                mv = bmv;
            } else {
                throw new MakumbaError("Error while trying to construct annotation: unhandled type "
                        + v.getClass().getName());
            }
            a.addMemberValue(attribute, mv);
        }
        return a;
    }

    @Override
    public void appendField(String fullyQualifiedClassName, String fieldName, String type, String generatedClassPath) {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        CtClass cc;

        try {
            cc = cp.get(fullyQualifiedClassName);
            cc.defrost();
            addField(fieldName, type, cc);
            cc.writeFile(generatedClassPath);

        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CannotCompileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void addField(String name, String type, CtClass cc) throws CannotCompileException {
        String fieldName = NameResolver.checkReserved(name);
        cc.addField(CtField.make("private " + type + " " + fieldName + ";", cc));
        cc.addMethod(CtNewMethod.getter("get" + StringUtils.capitalize(name), CtField.make("private " + type + " "
                + fieldName + ";", cc)));
        cc.addMethod(CtNewMethod.setter("set" + StringUtils.capitalize(name), CtField.make("private " + type + " "
                + fieldName + ";", cc)));
    }

    @Override
    public void appendAnnotations(String fullyQualifiedClassName, String methodName,
            Vector<AbstractAnnotation> annotations, String generatedClassPath) {
        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        CtClass cc;

        try {
            cc = cp.get(fullyQualifiedClassName);
            cc.defrost();

            Clazz clazz = new Clazz(fullyQualifiedClassName);
            clazz.setClassObjectReference(cc);
            addMethodAnnotations(clazz, methodName, annotations);

            cc.writeFile(generatedClassPath);

        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (CannotCompileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public Clazz createClass(String fullyQualifiedName) {

        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(this.getClass()));
        CtClass cc = cp.makeClass(fullyQualifiedName);
        cc.stopPruning(true);

        try {
            cc.addConstructor(CtNewConstructor.make("public " + getClassName(fullyQualifiedName) + "() { }", cc));
        } catch (CannotCompileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Clazz clazz = new Clazz(fullyQualifiedName);
        clazz.setClassObjectReference(cc);
        return clazz;

    }

    @Override
    public void writeClass(Clazz clazz, String generatedClassPath) {

        try {
            CtClass cc = (CtClass) clazz.getClassObjectReference();
            // ClassFileWriter.print(cc.getClassFile());
            cc.writeFile(generatedClassPath);

        } catch (CannotCompileException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) throws Exception {

        ClassPool cp = ClassPool.getDefault();
        cp.insertClassPath(new ClassClassPath(EntityClassGenerator.class));
        CtClass cc = cp.makeClass("A");
        cc.stopPruning(true);
        String type = null;
        CtField fld = CtField.make("public java.util.List myField;", cc);

        ClassFile cf = cc.getClassFile();
        ConstPool cop = cf.getConstPool();

        AnnotationsAttribute attr = new AnnotationsAttribute(cop, AnnotationsAttribute.visibleTag);
        Annotation anno = new Annotation("javax.persistence.ManyToMany", cop);
        anno.addMemberValue("targetEntity", new ClassMemberValue("java.lang.String", cop));
        attr.setAnnotation(anno);
        fld.getFieldInfo().addAttribute(attr);
        cf.setVersionToJava5();

        cc.addField(fld);
        cc.writeFile("build");

        Class<?> A = Class.forName("A");
        System.out.println(A.getField("myField").getAnnotation(ManyToMany.class));

    }

}
