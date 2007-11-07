package org.makumba.db;

import java.util.Dictionary;
import java.util.Enumeration;

import org.makumba.DataDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.CRUDOperationProvider;

/**
 * Makumba-specific implementation of the {@link CRUDOperationProvider}
 * 
 * @author Manuel Gay
 * @version $Id: MakumbaCRUDOperationProvider.java,v 1.1 02.11.2007 12:29:23 Manuel Exp $
 */
public class MakumbaCRUDOperationProvider extends CRUDOperationProvider {

    @Override
    public Pointer insert(Transaction t, String type, Dictionary data) {
        Table table = (((DBConnection)t).db.getTable(ddp.getDataDefinition(type).getName()));
        return table.insertRecord((DBConnection)t, data);
    }

    @Override
    public void checkInsert(Transaction t, String type, Dictionary fieldsToCheck, Dictionary fieldsToIgnore, Dictionary allFields) {
        Table table = (((DBConnection)t).db.getTable(ddp.getDataDefinition(type).getName()));
        table.checkInsert(fieldsToCheck, fieldsToIgnore, allFields);
    }

    @Override
    public void checkUpdate(Transaction t, String type, Pointer pointer, Dictionary fieldsToCheck, Dictionary fieldsToIgnore, Dictionary allFields) {
        
        DataDefinition dd = checkUpdate(type, fieldsToCheck, fieldsToIgnore);
        
        // we check the multi-field key uniqueness that span over more than one table
        Table table = (((DBConnection)t).db.getTable(dd.getName()));
        table.checkUpdate(pointer, allFields);
    }
    
    @Override
    public void update1(Transaction t, Pointer p, DataDefinition typeDef, Dictionary dic) {
        Object[] params = new Object[dic.size() + 1];
        params[0] = p;
        int n = 1;
        String set = "";
        String comma = "";
        for (Enumeration upd = dic.keys(); upd.hasMoreElements();) {
            set += comma;
            String s = (String) upd.nextElement();
            params[n++] = dic.get(s);
            set += "this." + s + "=$" + n;
            comma = ",";
        }
        if (set.trim().length() > 0)
            t.update(typeDef.getName() + " this", set, "this."
                    + ddp.getDataDefinition(p.getType()).getIndexPointerFieldName() + "=$1", params);
    }

}
