/*
 * Created on Aug 12, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf.update;

import org.makumba.DataDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;

public class CreateInputValue extends ObjectInputValue {

    public CreateInputValue(DataHandler dh, String label, DataDefinition type) {
        super(dh, label);
        this.type = type;
    }

    private Pointer result;

    private DataDefinition type;

    @Override
    public DataDefinition getType() {
        return this.type;
    }

    @Override
    protected void process(Transaction t) {
        result = t.insert(this.type.getName(), this.getFields());
    }

    @Override
    public Pointer getPointer() {
        return this.result;
    }
}
