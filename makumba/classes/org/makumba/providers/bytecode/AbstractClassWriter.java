package org.makumba.providers.bytecode;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

/**
 * A class capable of writing class bytecode. This abstraction makes client code easier to read.
 * 
 * @author Manuel Gay
 * @version $Id: AbstractClassWriter.java,v 1.1 May 31, 2010 10:29:13 AM manu Exp $
 */
public abstract class AbstractClassWriter {

    /**
     * Creates a class with an empty default constructor
     */
    public abstract Clazz createClass(String fullyQualifiedName);

    /**
     * Returns true if class is already generated
     */
    public boolean wasGenerated(String fullyQualifiedName, String generatedClassesPath) {
        File checkFile = new File(getClassFilePath(fullyQualifiedName, generatedClassesPath));
        return checkFile.exists();
    }

    /**
     * Adds a field during class construction
     */
    public abstract void addField(Clazz clazz, String name, String type);

    public abstract void addClassAnnotations(Clazz clazz, Vector<AbstractAnnotation> annotations);

    /**
     * Adds annotations to a method
     * 
     * @param clazz
     *            the {@link Clazz} to add the annotation to
     * @param methodName
     *            the name of the method to annotate
     * @param annotations
     *            a Vector of {@link AbstractAnnotation}-s
     */
    public abstract void addMethodAnnotations(Clazz clazz, String methodName, Vector<AbstractAnnotation> annotations);

    protected String getClassFilePath(String fullyQualifiedName, String generatedClassesPath) {
        int n = fullyQualifiedName.lastIndexOf('.');
        if (n < 0) {
            return generatedClassesPath + File.separator + fullyQualifiedName + ".class";
        }
        String packageName = fullyQualifiedName.substring(0, n);
        String className = fullyQualifiedName.substring(n + 1);
        String path = generatedClassesPath + File.separator + packageName.replace('.', File.separatorChar);
        return path + File.separator + className + ".class";

    }

    protected String getClassName(String fullyQualifiedName) {
        int n = fullyQualifiedName.lastIndexOf('.');
        if (n < 0) {
            return fullyQualifiedName;
        } else {
            return fullyQualifiedName.substring(n + 1);
        }
    }

    /**
     * Writes the class file to disk
     */
    public abstract void writeClass(Clazz clazz, String generatedClassesPath);

    /**
     * Appends a field to an existing class
     */
    public abstract void appendField(String fullyQualifiedClassName, String fieldName, String type,
            String generatedClassPath);

    /**
     * Appends annotations to a field
     */
    public abstract void appendAnnotations(String fullyQualifiedClassName, String methodName,
            Vector<AbstractAnnotation> annotations, String generatedClassPath);

    class Clazz {

        private String name;

        private Object classObjectReference;

        public Clazz(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public Object getClassObjectReference() {
            return classObjectReference;
        }

        public void setClassObjectReference(Object classObjectReference) {
            this.classObjectReference = classObjectReference;
        }
    }

    public AbstractAnnotation createAnnotation(String name) {
        return new AbstractAnnotation(name);
    }

    /**
     * An abstract annotation to which attributes and nested attributes can be added
     */
    class AbstractAnnotation {
        private String name;

        private Map<String, Object> attributes;

        public AbstractAnnotation(String name) {
            this.name = name;
            this.attributes = new LinkedHashMap<String, Object>();
        }

        /**
         * Adds an annotation attribute
         * 
         * @param name
         *            the name of the attribute
         * @param value
         *            the value of the attribute
         * @return the {@link AbstractAnnotation} to which the attribute was added
         */
        public AbstractAnnotation addAttribute(String name, Object value) {
            this.attributes.put(name, value);
            return this;
        }

        /**
         * Adds a nested annotation to a given attribute. Currently only one nested annotation per attribute is
         * supported!
         * 
         * @param attributeName
         *            the name of the annotation attribute into which the nested annotation should be added
         * @param annotationName
         *            the name of the nested annotation
         * @return the {@link AbstractAnnotation} corresponding to the nested annotation
         */
        public AbstractAnnotation addNestedAnnotation(String attributeName, String annotationName) {
            AbstractAnnotation an = new AbstractAnnotation(annotationName);
            this.attributes.put(attributeName, an);
            return an;
        }

        public String getName() {
            return this.name;
        }

        public Map<String, Object> getAttribues() {
            return this.attributes;
        }
    }

}
