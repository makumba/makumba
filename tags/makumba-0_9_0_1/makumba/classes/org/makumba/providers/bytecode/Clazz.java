package org.makumba.providers.bytecode;

/**
 * Abstract representation of a Class
 * 
 * @author Manuel Gay
 * @version $Id: Clazz.java,v 1.1 Jun 18, 2010 12:08:31 PM manu Exp $
 */
public class Clazz {

    private String name;

    private Object classObjectReference;

    public Clazz(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Gets the reference to the class object for the underlying implementation
     */
    public Object getClassObjectReference() {
        return classObjectReference;
    }

    /**
     * Sets thee reference to the class object for the underlying implementation
     */
    public void setClassObjectReference(Object classObjectReference) {
        this.classObjectReference = classObjectReference;
    }
}