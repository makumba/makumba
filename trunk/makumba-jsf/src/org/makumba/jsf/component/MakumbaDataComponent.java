package org.makumba.jsf.component;

import javax.faces.component.UIComponent;

import org.makumba.jsf.update.DataHandler;
import org.makumba.jsf.update.ObjectInputValue;
import org.makumba.list.engine.ComposedQuery;

/**
 * A makumba component that performs data handling operations
 * 
 * @author manu
 */
public interface MakumbaDataComponent {

    /**
     * Sets the data handler that will take care of data saving
     */
    public void setDataHandler(DataHandler handler);

    /**
     * Adds a new value in the component tree. The component receiving this value is then responsible for adding it to
     * the {@link ObjectInputValue} of the component declaring the base label of path.
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

    public ComposedQuery getComposedQuery();

    class Util {
        public static MakumbaDataComponent findLabelDefinitionComponent(UIComponent current, String label) {
            UIComponent parent = current;
            MakumbaDataComponent candidate = null;
            while (parent != null) {
                if (parent instanceof MakumbaDataComponent) {
                    MakumbaDataComponent c = (MakumbaDataComponent) parent;
                    if (c.getComposedQuery().getFromLabelTypes().containsKey(label)) {
                        candidate = c;
                    }
                }
                parent = parent.getParent();
            }
            return candidate;
        }
    }

}
