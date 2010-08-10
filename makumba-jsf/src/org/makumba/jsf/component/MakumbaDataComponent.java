package org.makumba.jsf.component;

import javax.faces.component.UIComponent;

import org.makumba.jsf.ComponentDataHandler;
import org.makumba.jsf.update.InputValue;

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

    /**
     * Adds a new value in the component tree. The component receiving this value is then responsible for adding it to
     * the {@link InputValue} of the component declaring the base label of path.
     * 
     * @param label
     *            the base label of this value
     * @param path
     *            the path of the field to be set
     * @param value
     *            the value of the field
     * @param clientId
     *            the clientId of the input for the value
     */
    public void addValue(String label, String path, Object value, String clientId);

    /**
     * Whether this component knows a given label
     * 
     * @param label
     *            a query label
     * @return <code>true</code> if the component knows the label, <code>false</code> otherwise
     */
    public boolean hasLabel(String label);

    class Util {
        public static MakumbaDataComponent findLabelDefinitionComponent(UIComponent current, String label) {
            UIComponent parent = current.getParent();
            while (parent != null) {
                parent = parent.getParent();
                if (parent instanceof MakumbaDataComponent) {
                    MakumbaDataComponent c = (MakumbaDataComponent) parent;
                    if (c.hasLabel(label)) {
                        return c;
                    }
                }
            }
            return null;
        }
    }

}
