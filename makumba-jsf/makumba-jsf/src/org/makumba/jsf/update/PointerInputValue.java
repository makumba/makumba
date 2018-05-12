/*
 * Created on Aug 12, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf.update;

import org.makumba.FieldDefinition;
import org.makumba.Transaction;

public class PointerInputValue extends ReferenceInputValue {
    public PointerInputValue(DataHandler dh, String label, Integer refernceIndex, String fieldPath, FieldDefinition fd) {
        super(dh, label, refernceIndex, fieldPath, fd);
    }

    @Override
    protected void addToDataHandler(DataHandler dh, Integer referenceIndex) {
        dh.getValues().add(referenceIndex, this);
    }

    @Override
    protected ObjectInputValue computeReference(DataHandler dh, Integer referenceIndex) {
        // we've just inserted, so now the reference object is at our right
        return dh.getValues().get(referenceIndex + 1);
    }

    @Override
    protected void process(Transaction t) {
        // insert the new record
        result = t.insert(this.fd.getPointedType().getName(), this.getFields());
        this.getReference().getFields().put(fieldPath, result);
    }
}
