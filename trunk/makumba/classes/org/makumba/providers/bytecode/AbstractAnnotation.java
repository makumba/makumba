package org.makumba.providers.bytecode;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An abstract annotation
 */
public class AbstractAnnotation {

    private String name;

    private Map<String, Object> attributes;

    public AbstractAnnotation(String name) {
        this.name = name;
        this.attributes = new LinkedHashMap<String, Object>();
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
    public Map<String, Object> getAttribues() {
        return this.attributes;
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
     * Adds a nested annotation to a given attribute. Currently only one nested annotation per attribute is supported!
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