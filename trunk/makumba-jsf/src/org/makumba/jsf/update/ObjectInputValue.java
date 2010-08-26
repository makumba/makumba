package org.makumba.jsf.update;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.DataDefinitionNotFoundError;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.jsf.MakumbaDataContext;
import org.makumba.providers.DataDefinitionProvider;

public abstract class ObjectInputValue {

    private String label;

    private Dictionary<String, Object> fields = new Hashtable<String, Object>();

    private Map<String, List<Pointer>> setFields = new Hashtable<String, List<Pointer>>();

    private Map<String, String> clientIds = new HashMap<String, String>();

    /**
     * Makes an input value for creation of a new object
     * 
     * @param label
     *            the label of the object
     * @param definition
     *            the definition of the object
     * @return an {@link ObjectInputValue} that can be used to register values
     */
    public static ObjectInputValue makeCreationInputValue(String label, String definition) {
        DataDefinition t = null;
        try {
            t = DataDefinitionProvider.getInstance().getDataDefinition(definition);
        } catch (DataDefinitionNotFoundError dne) {
            // ignore
        }

        if (t != null) {
            return new CreateInputValue(MakumbaDataContext.getDataContext().getDataHandler(), label, t);
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

            return ReferenceInputValue.makeReferenceObjectInputValue(
                MakumbaDataContext.getDataContext().getDataHandler(), label, baseLabel, fieldPath);
        }

    }

    /**
     * Makes an input value for update of an existing object
     * 
     * @param label
     *            the label of the object
     * @param ptr
     *            the {@link Pointer} to the existing object instance
     * @return an {@link ObjectInputValue} that can be used to register values
     */
    public static ObjectInputValue makeUpdateInputValue(String label, Pointer ptr) {
        return new UpdateInputValue(MakumbaDataContext.getDataContext().getDataHandler(), label, ptr);
    }

    /**
     * Makes an input value for deletion of an existing object
     * 
     * @param label
     *            the label of the object to delete
     * @param ptr
     *            the {@link Pointer} to the object instance
     * @return an ObjectInputValue instance
     */
    public static ObjectInputValue makeDeleteInputValue(String label, Pointer ptr) {
        return new DeleteInputValue(MakumbaDataContext.getDataContext().getDataHandler(), label, ptr);
    }

    protected ObjectInputValue(DataHandler dh, String label) {
        this(dh, label, null);
    }

    protected ObjectInputValue(DataHandler dh, String label, Integer referenceIndex) {
        this.label = label;
        addToDataHandler(dh, referenceIndex);
    }

    public void addField(String path, Object value, String clientId) {
        this.fields.put(path, value);
        this.clientIds.put(path, clientId);
    }

    public void addSetField(String path, List<Pointer> value, String clientId) {
        this.setFields.put(path, value);
        this.clientIds.put(path, clientId);
    }

    protected void addToDataHandler(DataHandler dh, Integer referenceIndex) {
        dh.getValues().add(this);
    }

    private String findClientId(String name) {

        String s = clientIds.get(name);
        if (s != null) {
            return s;
        }
        // FIXME: this is a bug in the makumba db layer. when the field is indicated as indiv.surname, the invalid value
        // error refers just to surname.
        // this fix is only approximative because there may be fields that end with the same string
        for (String p : clientIds.keySet()) {
            if (p.endsWith(name)) {
                return clientIds.get(p);
            }
        }
        return null;

    }

    public Dictionary<String, Object> getFields() {
        return fields;
    }

    public Map<String, List<Pointer>> getSetFields() {
        return setFields;
    }

    public String getLabel() {
        return label;
    }

    public abstract Pointer getPointer();

    public abstract DataDefinition getType();

    /**
     * the command pattern
     * 
     * @param t
     */
    protected abstract void process(Transaction t);

    public void processAndTreatExceptions(Transaction t) {
        try {
            process(t);
        } catch (InvalidValueException e) {
            treatInvalidValue(e);
        } catch (CompositeValidationException f) {
            for (InvalidValueException e : f.getExceptions()) {
                treatInvalidValue(e);
            }
        } catch (Throwable tr) {
            // TODO: we cannot detect which field provoked this, but we could insert the clientId of the
            // form
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, tr.getMessage(), tr.getMessage()));
        }
    }

    @Override
    public String toString() {
        return getClass().getName() + " [label=" + label + "]";
    }

    private void treatInvalidValue(InvalidValueException e) {
        FacesContext.getCurrentInstance().addMessage(findClientId(e.getFieldName()),
            new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
    }
}
