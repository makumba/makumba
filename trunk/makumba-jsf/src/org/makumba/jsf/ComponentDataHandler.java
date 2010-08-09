package org.makumba.jsf;

import org.makumba.el.InputValue;

public interface ComponentDataHandler {

    /**
     * Pushes a component in the stack for topology analysis
     * 
     * @param c
     *            a {@link MakumbaDataComponent}
     */
    public void pushDataComponent(MakumbaDataComponent c);

    /**
     * Pops the head component from the topology analysis stack
     */
    public void popDataComponent();

    /**
     * Add a new input value for processing
     * 
     * @param c
     *            the {@link MakumbaDataComponent} this value is a child of
     * @param v
     *            the {@link InputValue} for creation
     */
    public void addInputValue(MakumbaDataComponent c, InputValue v);

}