package org.makumba.jsf.update;

import org.makumba.DataDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;

public class DeleteInputValue extends ObjectInputValue {

    private Pointer pointer;

    public DeleteInputValue(DataHandler dh, String label, Pointer ptr) {
        super(dh, label);
        this.pointer = ptr;
    }

    @Override
    public Pointer getPointer() {
        return this.pointer;
    }

    @Override
    public DataDefinition getType() {
        return DataDefinitionProvider.getInstance().getDataDefinition(pointer.getType());
    }

    @Override
    protected void process(Transaction t) {
        t.delete(pointer);
    }

}
