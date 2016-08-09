package org.makumba.providers.bytecode;

import java.util.Vector;

/**
 * A class capable of reading class bytecode. Useful for code inspection.<br>
 * TODO method -> member<br>
 * TODO read class annotations<br>
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: AbstractClassReader.java,v 1.1 Jun 18, 2010 12:01:55 PM manu Exp $
 */
public abstract class AbstractClassReader {

    public abstract Clazz getClass(String fullyQualifiedClassName) throws ClassNotFoundException;

    public abstract Vector<AbstractAnnotation> getAnnotations(String methodName, Clazz clazz);

    public abstract Object getAnnotationAttributeValue(Class<?> annotationClass, String attributeName, String methodName,
            Clazz clazz);

}
