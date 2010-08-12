/*
 * Created on Aug 12, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf.update;

import org.makumba.FieldDefinition;
import org.makumba.Transaction;

public class AddInputValue extends ReferenceInputValue {

    AddInputValue(DataHandler dh, String label, Integer referenceIndex, String fieldPath, FieldDefinition fd) {
        super(dh, label, referenceIndex, fieldPath, fd);
    }

    @Override
    protected void process(Transaction t) {
        switch (fd.getIntegerType()) {
            case FieldDefinition._ptrOne:
                // FIXME probably this won't work as it will try to add to a non-existing record
                // special treatment of ptrOne (different of ptr) is required
                t.update(this.getReference().getPointer(), this.getFields());
                break;
            case FieldDefinition._setComplex:
                result = t.insert(this.getReference().getPointer(), fieldPath, this.getFields());
                break;
            case FieldDefinition._set:
                // TODO
                break;

        }
    }

}
