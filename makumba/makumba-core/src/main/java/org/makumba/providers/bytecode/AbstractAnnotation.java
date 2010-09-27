package org.makumba.providers.bytecode;

import org.apache.commons.collections.map.MultiValueMap;

/**
 * An abstract annotation object, useful to read/write annotations<br>
 * 
 * @author manu
 * @version $Id: AbstractAnnotation.java,v 1.1 Jun 21, 2010 3:24:36 PM manu Exp $
 */
public class AbstractAnnotation {

    private String name;

    private MultiValueMap attributes;

    public AbstractAnnotation(String name) {
        this.name = name;
        this.attributes = new MultiValueMap();
    }

    /**
     * Gets the name of the annotation
     * 
     * @return the name of the annotation
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the raw attribute map
     * 
     * @return the map of attributes, where the key is the name of the attribute. the value can also be another
     *         {@link AbstractAnnotation}
     */
    public MultiValueMap getAttribues() {
        return this.attributes;
    }

    /**
     * Adds an annotation attribute, with support for multiple values
     * 
     * @param name
     *            the name of the attribute
     * @param value
     *            the value of the attribute. Can be a collection.
     * @return the {@link AbstractAnnotation} to which the attribute was added
     */
    public AbstractAnnotation addAttribute(String name, Object value) {
        this.attributes.put(name, value);
        return this;
    }

    /**
     * Adds a nested annotation to a given attribute.
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

}