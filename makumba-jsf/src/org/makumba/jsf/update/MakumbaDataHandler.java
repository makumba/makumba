package org.makumba.jsf.update;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.makumba.FieldDefinition;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;

public class MakumbaDataHandler implements DataHandler {

    private ThreadLocal<ArrayList<ObjectInputValue>> values = new ThreadLocal<ArrayList<ObjectInputValue>>() {
        @Override
        protected ArrayList<ObjectInputValue> initialValue() {
            return new ArrayList<ObjectInputValue>();
        }
    };

    /* (non-Javadoc)
     * @see org.makumba.jsf.update.DataHandler#addSimpleObjectInputValue(org.makumba.jsf.update.OIV)
     */
    public void addSimpleObjectInputValue(ObjectInputValue v) {
        values.get().add(values.get().size(), v);

    }

    /* (non-Javadoc)
     * @see org.makumba.jsf.update.DataHandler#addPointerObjectInputValue(org.makumba.jsf.update.OIV, java.lang.String, java.lang.String)
     */
    public void addPointerObjectInputValue(ObjectInputValue v, String label, String field) {
        int i = findMostRecentlyAddedObjectInputValueIndex(label);
        if (i < 0) {
            throw new RuntimeException("Invalid label encountred while adding ObjectInputValue: " + label);
        }

        // add a placeholder InputValue to the ObjectInputValue of "label"
        // this placeholder will be used when inserting the ObjectInputValue "label", when the pointer to the record of
        // this ObjectInputValue will be known

        values.get().get(i).addField(field, new InputValue(v));

        // add this ObjectInputValue before the most recently inserted ObjectInputValue having as label the passed label
        values.get().add(i, v);

        // this ObjectInputValue will be treated first and hence the pointer inserted before the record depending on it
        // is inserted

    }

    /* (non-Javadoc)
     * @see org.makumba.jsf.update.DataHandler#addSetObjectInputValue(org.makumba.jsf.update.OIV, java.lang.String, java.lang.String)
     */
    public void addSetObjectInputValue(ObjectInputValue v, String label, String field) {

        int i = findMostRecentlyAddedObjectInputValueIndex(label);
        if (i < 0) {
            throw new RuntimeException("Invalid label encountred while adding ObjectInputValue: " + label);
        }

        // we add a placeholder to this ObjectInputValue for the base pointer of the object to which we add a set
        // this placeholder will be used when inserting the set values in the field "field" of label "label", once the
        // record "label" will have been created
        v.setAddReference(values.get().get(i));

        // we insert this ObjectInputValue at the bottom of the list
        addSimpleObjectInputValue(v);

        // when this ObjectInputValue will be treated, the base record will already have been inserted so set values can
        // be added

    }

    /**
     * Finds the most recently added ObjectInputValue in the list
     * 
     * @param label
     *            the label of the OIV to find
     * @return the index of the OIV with the largest index
     */
    private int findMostRecentlyAddedObjectInputValueIndex(String label) {
        int m = -1;
        for (int i = 0; i < values.get().size(); i++) {
            if (values.get().get(i).equals(label)) {
                m = i;
            }
        }
        return m;
    }

    @Override
    public void process() {

        Transaction t = null;

        try {
            // TODO list db attribute
            t = TransactionProvider.getInstance().getConnectionToDefault();

            for (ObjectInputValue v : values.get()) {

                try {

                    switch (v.getCommand()) {
                        case CREATE:
                            Pointer p = t.insert(v.getType().getName(), toDictionary(v.getFields()));
                            // update the pointer of this ObjectInputValue
                            v.setPointer(p);
                            break;
                        case UPDATE:
                            t.update(v.getPointer(), toDictionary(v.getFields()));
                            break;
                        case ADD:
                            // what kind of field do we add to?
                            FieldDefinition fd = v.getAddReference().getType().getFieldOrPointedFieldDefinition(
                                v.getAddFieldPath());

                            switch (fd.getIntegerType()) {
                                case FieldDefinition._ptrOne:
                                    // FIXME probably this won't work as it will try to add to a non-existing record
                                    // special treatment of ptrOne (different of ptr) is required
                                    t.update(v.getAddReference().getPointer(), toDictionary(v.getFields()));
                                    break;
                                case FieldDefinition._setComplex:
                                    t.insert(v.getAddReference().getPointer(), v.getAddFieldPath(),
                                        toDictionary(v.getFields()));
                                    break;
                                case FieldDefinition._ptr:
                                    // insert the new record
                                    Pointer newRecord = t.insert(v.getType().getName(), toDictionary(v.getFields()));

                                    // update our Pointer field so records having us as child will be able to find us
                                    v.setPointer(newRecord);

                                    break;
                                case FieldDefinition._set:
                                    // TODO
                                    break;
                            }
                            break;
                    }

                    t.commit();

                } catch (InvalidValueException e) {

                    t.rollback();
                    // TODO: store the type (MDD) in the InvalidValueException
                    // TODO: store the offending value in the IVE
                    // TODO: from the type, and the field name, find the label.ptr.field that edits such a field
                    // after that, find the clientId of the UIInput(s) that produced the value
                    // for each such input, register a message

                    // TODO: if the above is hard, at least include the clientId of the form, as below.
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
                    throw new ELException(e);

                } catch (Throwable tr) {
                    t.rollback();
                    // TODO: we cannot detect which field provoked this, but we could insert the clientId of the
                    // form
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, tr.getMessage(), tr.getMessage()));
                    throw new ELException(tr);
                }

            }

        } finally {
            if (t != null) {
                t.close();
            }

            // clear everything
            values.get().clear();

        }

    }

    private Dictionary<String, Object> toDictionary(Map<String, InputValue> fields) {
        Hashtable<String, Object> dic = new Hashtable<String, Object>();
        for (String key : fields.keySet()) {
            InputValue v = fields.get(key);
            Object val = v.getValue();
            if (v.isPlaceholder()) {
                // fetch the pointer which is now known
                val = ((ObjectInputValue) v.getValue()).getPointer();
            }
            dic.put(key, val);
        }
        return dic;
    }

}
