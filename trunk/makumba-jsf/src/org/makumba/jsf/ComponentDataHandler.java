package org.makumba.jsf;

import org.makumba.jsf.component.MakumbaDataComponent;
import org.makumba.jsf.update.InputValue;

/**
 * This interface defines the contract for registering values and components that contain these values within a
 * component tree. Each {@link MakumbaDataComponent} should register itself via the
 * {@link ComponentDataHandler#popDataComponent()} and {@link ComponentDataHandler#popDataComponent()} methods.<br>
 * New values can be registered via the {@link ComponentDataHandler#addInputValue(MakumbaDataComponent, InputValue)}
 * method.
 * 
 * @see MakumbaDataComponent
 * @see InputValue
 * @author manu
 */
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