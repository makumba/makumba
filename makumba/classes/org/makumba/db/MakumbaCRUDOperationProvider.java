package org.makumba.db;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.Configuration;
import org.makumba.providers.CRUDOperationProvider;
import org.makumba.providers.DataDefinitionProvider;

/**
 * Makumba-specific implementation of the {@link CRUDOperationProvider}
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaCRUDOperationProvider.java,v 1.1 02.11.2007 12:29:23 Manuel Exp $
 */
public class MakumbaCRUDOperationProvider implements CRUDOperationProvider {
    
    private DataDefinitionProvider ddp = new DataDefinitionProvider(new Configuration());

    public void delete(Transaction t, Pointer ptr) {
        ((DBConnection)t).delete1(ptr);
    }

    public Pointer insert(Transaction t, String type, Dictionary data) {
        Table table = (((DBConnection)t).db.getTable(ddp.getDataDefinition(type).getName()));
        return table.insertRecord((DBConnection)t, data);
    }

    public Dictionary read(Transaction t, Pointer ptr, Object fields) {
        return t.read(ptr, fields);
    }

    public void update(Transaction t, Pointer ptr, Dictionary data) {
        t.update(ptr, data);

    }
    
    public void updateSet(Transaction t, Pointer base, FieldDefinition fi, Object val) {
        
        if (!fi.getType().equals("set") && !fi.getType().equals("setintEnum") && !fi.getType().equals("setcharEnum"))
            throw new InvalidFieldTypeException(fi, "set");

        // we empty the existing set
        deleteSet(t, base, fi);
        
        // if the new value is empty, we simply return
        if (val == null || val == Pointer.NullSet || ((Vector) val).size() == 0)
            return;
        
        // we update the set with the new values
        Vector values = (Vector) val;

        Dictionary<String, Object> data = new Hashtable<String, Object>(10);
        data.put(fi.getSubtable().getSetOwnerFieldName(), base);
        
        for (Enumeration e = values.elements(); e.hasMoreElements();) {
            data.put(fi.getSubtable().getSetMemberFieldName(), e.nextElement());
            ((DBConnection)t).db.getTable(fi.getSubtable()).insertRecord((DBConnection)t, data);
        }
    }
    
    public void deleteSet(Transaction t, Pointer base, FieldDefinition fi) {
        t.update(fi.getSubtable().getName() + " this", null, "this." + fi.getSubtable().getSetOwnerFieldName()
                + "=$1", base);
    }

    public void checkInsert(Transaction t, String type, Dictionary fieldsToCheck, Dictionary fieldsToIgnore, Dictionary allFields) {
        Table table = (((DBConnection)t).db.getTable(ddp.getDataDefinition(type).getName()));
        table.checkInsert(fieldsToCheck, fieldsToIgnore, allFields);
    }

    public void checkUpdate(Transaction t, String type, Pointer pointer, Dictionary fieldsToCheck, Dictionary fieldsToIgnore, Dictionary allFields) {
        Table table = (((DBConnection)t).db.getTable(ddp.getDataDefinition(type).getName()));
        table.checkUpdate(pointer, fieldsToCheck, fieldsToIgnore, allFields);
    }

}
