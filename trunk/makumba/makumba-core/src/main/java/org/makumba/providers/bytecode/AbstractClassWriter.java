package org.makumba.providers.bytecode;

import java.io.File;
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
     * Returns the last generation date in milliseconds, -1 if the file was not generated
     */
    public long getLastGenerationTime(String fullyQualifiedName, String generatedClassesPath) {
        File checkFile = new File(getClassFilePath(fullyQualifiedName, generatedClassesPath));
        return checkFile.exists() ? checkFile.lastModified() : -1;
    }

    /**
     * Adds a field during class construction
     */
    public abstract void addField(Clazz clazz, String name, String type);

    /**
     * Adds annotations to a class
     */
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
     * Appends a field to an existing class, replaces it if it already exists.
     */
    public abstract void appendField(String fullyQualifiedClassName, String fieldName, String type,
            String generatedClassPath);

    /**
     * Appends annotations to a field
     */
    public abstract void appendAnnotations(String fullyQualifiedClassName, String methodName,
            Vector<AbstractAnnotation> annotations, String generatedClassPath);

    /**
     * Creates a new annotation with a given name
     */
    public AbstractAnnotation createAnnotation(String name) {
        return new AbstractAnnotation(name);
    }

}
