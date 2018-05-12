/*
 * Created on Aug 12, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf.update;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.Pointer;

public abstract class ReferenceInputValue extends ObjectInputValue {

    ReferenceInputValue(DataHandler dh, String label, Integer referenceIndex, String fieldPath, FieldDefinition fd) {
        super(dh, label, referenceIndex);
        this.reference = computeReference(dh, referenceIndex);
        this.fieldPath = fieldPath;
        this.fd = fd;

    }

    protected ObjectInputValue computeReference(DataHandler dh, Integer referenceIndex) {
        return dh.getValues().get(referenceIndex);
    }

    ObjectInputValue reference;

    protected FieldDefinition fd;

    protected String fieldPath;

    protected Pointer result;

    @Override
    public Pointer getPointer() {
        return result;
    }

    protected ObjectInputValue getReference() {
        return reference;
    }

    @Override
    public DataDefinition getType() {
        // FIXME: not sure this one works for setComplex. surely works for pointer.
        return fd.getPointedType();
    }

    protected static int findMostRecentlyAddedObjectInputValueIndex(DataHandler dh, String label) {
        // we start from the end and return when we find the first thing we search for

        for (int i = dh.getValues().size() - 1; i >= 0; i--) {
            if (dh.getValues().get(i).getLabel().equals(label)) {
                return i;
            }
        }
        return -1;
    }

    /** Factory method */
    public static ObjectInputValue makeReferenceObjectInputValue(DataHandler dh, String label, String referenceLabel,
            String fieldPath) {
        int n = findMostRecentlyAddedObjectInputValueIndex(dh, referenceLabel);
        ObjectInputValue reference = dh.getValues().get(n);
        FieldDefinition fd = reference.getType().getFieldOrPointedFieldDefinition(fieldPath);
        if (fd.getIntegerType() == FieldDefinition._ptr) {
            return new PointerInputValue(dh, label, n, fieldPath, fd);
        } else {
            return new AddInputValue(dh, label, n, fieldPath, fd);
        }
    }

}
