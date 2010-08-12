package org.makumba.jsf.update;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;

public abstract class ObjectInputValue {

    protected ObjectInputValue(DataHandler dh, String label) {
        this(dh, label, null);
    }

    protected ObjectInputValue(DataHandler dh, String label, Integer referenceIndex) {
        this.label = label;
        addToDataHandler(dh, referenceIndex);
    }

    protected void addToDataHandler(DataHandler dh, Integer referenceIndex) {
        dh.getValues().add(this);
    }

    private String label;

    private Dictionary<String, Object> fields = new Hashtable<String, Object>();

    private Map<String, String> clientIds = new HashMap<String, String>();

    public String getLabel() {
        return label;
    }

    public void addField(String path, Object value, String clientId) {
        this.fields.put(path, value);
        this.clientIds.put(path, clientId);
    }

    public Dictionary<String, Object> getFields() {
        return fields;
    }

    @Override
    public String toString() {
        return getClass().getName() + " [label=" + label + "]";
    }

    public void processAndTreatExceptions(Transaction t) {
        try {
            process(t);
        } catch (InvalidValueException e) {
            FacesContext.getCurrentInstance().addMessage(clientIds.get(e.getFieldName()),
                new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
        } catch (Throwable tr) {
            // TODO: we cannot detect which field provoked this, but we could insert the clientId of the
            // form
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, tr.getMessage(), tr.getMessage()));
        }
    }

    /** Factory method */
    public static ObjectInputValue makeCreationInputValue(DataHandler dataHandler, String label, String definition) {
        DataDefinition t = null;
        try {
            t = DataDefinitionProvider.getInstance().getDataDefinition(definition);
        } catch (DataDefinitionNotFoundError dne) {
            // ignore
        }

        if (t != null) {
            return new CreateInputValue(dataHandler, label, t);
        } else {
            // find base label
            String baseLabel = null;
            String fieldPath = null;
            int n = definition.indexOf(".");
            if (n > 0) {
                baseLabel = definition.substring(0, n);
                fieldPath = definition.substring(n + 1, definition.length());
            } else {
                // this will not happen because otherwise the query analysis would have flopped
                throw new RuntimeException("should not be here");
            }

            return ReferenceInputValue.makeReferenceObjectInputValue(dataHandler, label, baseLabel, fieldPath);
        }

    }

    /**
     * the command pattern
     * 
     * @param t
     */
    protected abstract void process(Transaction t);

    public abstract Pointer getPointer();

    public abstract DataDefinition getType();
}
