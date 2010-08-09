package org.makumba.jsf.component;

import org.makumba.jsf.ComponentDataHandler;

/**
 * A makumba component that performs data handling operations
 * 
 * @author manu
 */
public interface MakumbaDataComponent {

    /**
     * Sets the data handler that will take care of data saving
     */
    public void setDataHandler(ComponentDataHandler handler);

    /**
     * Unique key of this component
     */
    public String getKey();

}
