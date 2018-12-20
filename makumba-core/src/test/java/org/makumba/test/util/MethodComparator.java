package org.makumba.test.util;

// code from http://intellijava.blogspot.com/2012/05/junit-and-java-7.html

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;
import java.util.Comparator;

import jdk.nashorn.internal.ir.annotations.Ignore;

public class MethodComparator<T> implements Comparator<T> {

    // private static final char[] METHOD_SEPARATORS = { 1, 7 };

    private MethodComparator() {
    }

    public static MethodComparator<Method> getMethodComparatorForJUnit3() {
        return new MethodComparator<Method>();
    }

    @Override
    public int compare(T o1, T o2) {
        final MethodPosition methodPosition1 = this.getIndexOfMethodPosition(o1);
        final MethodPosition methodPosition2 = this.getIndexOfMethodPosition(o2);
        return methodPosition1.compareTo(methodPosition2);
    }

    private MethodPosition getIndexOfMethodPosition(final Object method) {

        if (method instanceof Method) {
            return this.getIndexOfMethodPosition((Method) method);
        } else {
            System.err.println("getIndexOfMethodPosition(): Given object is not a method! Object class is "
                    + method.getClass().getCanonicalName());
            return new NullMethodPosition();
        }
    }

    private MethodPosition getIndexOfMethodPosition(final Method method) {
        final Class<?> aClass = method.getDeclaringClass();
        if (method.getAnnotation(Ignore.class) == null) {
            return getIndexOfMethodPosition(aClass, method.getName());
        } else {
            // logger.debug("getIndexOfMethodPosition(): Method is annotated as Ignored: " + method.getName()
            // + " in class: " + aClass.getCanonicalName());
            return new NullMethodPosition();
        }
    }

    private MethodPosition getIndexOfMethodPosition(final Class<?> aClass, final String methodName) {
        final InputStream inputStream = aClass.getResourceAsStream(aClass.getSimpleName() + ".class");
        final LineNumberReader lineNumberReader = new LineNumberReader(new InputStreamReader(inputStream));
        try {
            try {
                String line;
                while ((line = lineNumberReader.readLine()) != null) {
                    if (line.contains(methodName)) {
                        return new MethodPosition(lineNumberReader.getLineNumber(), line.indexOf(methodName));
                    }
                }
            } finally {
                lineNumberReader.close();
            }
        } catch (IOException e) {
            System.err.println("getIndexOfMethodPosition(): Error while reading byte code of class "
                    + aClass.getCanonicalName() + " " + e);
            return new NullMethodPosition();
        }
        System.err.println("getIndexOfMethodPosition(): Can't find method " + methodName + " in byte code of class "
                + aClass.getCanonicalName());
        return new NullMethodPosition();
    }

    private static class MethodPosition implements Comparable<MethodPosition> {
        private final Integer lineNumber;

        private final Integer indexInLine;

        public MethodPosition(int lineNumber, int indexInLine) {
            this.lineNumber = lineNumber;
            this.indexInLine = indexInLine;
        }

        @Override
        public String toString() {
            return lineNumber + ":" + indexInLine;
        }

        @Override
        public int compareTo(MethodPosition o) {

            // If line numbers are equal, then compare by indexes in this line.
            if (this.lineNumber.equals(o.lineNumber)) {
                return this.indexInLine.compareTo(o.indexInLine);
            } else {
                return this.lineNumber.compareTo(o.lineNumber);
            }
        }
    }

    private static class NullMethodPosition extends MethodPosition {
        public NullMethodPosition() {
            super(-1, -1);
        }
    }
}