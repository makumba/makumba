/*
 * Created on Aug 12, 2010
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.makumba.jsf.update;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;

public class UpdateInputValue extends ObjectInputValue {
    public UpdateInputValue(DataHandler dh, String label, Pointer p) {
        super(dh, label);
        this.pointer = p;
    }

    private Pointer pointer;

    @Override
    public Pointer getPointer() {
        return pointer;
    }

    @Override
    protected void process(Transaction t) {
        // we do nothing if we have no data
        if (!this.getFields().isEmpty()) {
            t.update(this.pointer, this.getFields());
        }
        if (!this.getSetFields().isEmpty()) {
            Dictionary<String, Object> d = new Hashtable<String, Object>();
            for (String key : getSetFields().keySet()) {
                Vector<Pointer> v = new Vector<Pointer>();
                for (Pointer p : getSetFields().get(key)) {
                    v.add(p);
                }
                d.put(key, v);
            }
            t.update(this.pointer, d);
        }
    }

    @Override
    public DataDefinition getType() {
        return DataDefinitionProvider.getInstance().getDataDefinition(pointer.getType());
    }

}
