package org.makumba.providers.bytecode;

import java.util.Vector;

/**
 * A class capable of reading class bytecode. Useful for code inspection.
 * 
 * @author Manuel Gay
 * @version $Id: AbstractClassReader.java,v 1.1 Jun 18, 2010 12:01:55 PM manu Exp $
 */
public abstract class AbstractClassReader {

    public abstract Clazz getClass(String fullyQualifiedClassName) throws ClassNotFoundException;

    public abstract Vector<AbstractAnnotation> getAnnotations(String methodName, Clazz clazz);

    public abstract Object getAnnotationValue(Class<?> annotationClass, String attributeName, String methodName,
            Clazz clazz);

}
