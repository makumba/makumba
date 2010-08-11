package org.makumba.jsf.update;

public interface DataHandler {

    /**
     * Adds a CREATE ObjectInputValue without dependencies, will be added at the end of the list
     * 
     * @param v
     *            the {@link ObjectInputValue} to insert
     */
    public abstract void addSimpleObjectInputValue(ObjectInputValue v);

    /**
     * Adds a ADD ObjectInputValue which holds the data of a pointer to a given field
     * 
     * @param v
     *            the {@link ObjectInputValue} to insert
     * @param label
     *            the label of the most recently inserted ObjectInputValue before v will be added in the data handler
     * @param field
     *            the field to which the pointer is added
     */
    public abstract void addPointerObjectInputValue(ObjectInputValue v, String label, String field);

    /**
     * Adds a ADD ObjectInputValue which holds the data of a set to a given field
     * 
     * @param v
     * @param label
     * @param field
     */
    public abstract void addSetObjectInputValue(ObjectInputValue v, String label, String field);

    public abstract void process();

}