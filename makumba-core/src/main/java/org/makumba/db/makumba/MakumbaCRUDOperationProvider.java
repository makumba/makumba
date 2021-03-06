package org.makumba.db.makumba;

import java.sql.Timestamp;
import java.util.Dictionary;
import java.util.Enumeration;

import org.makumba.DataDefinition;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.db.DataHolder;
import org.makumba.providers.CRUDOperationProvider;

/**
 * Makumba-specific implementation of the {@link CRUDOperationProvider}
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: MakumbaCRUDOperationProvider.java,v 1.1 02.11.2007 12:29:23 Manuel Exp $
 */
public class MakumbaCRUDOperationProvider extends CRUDOperationProvider {

    @Override
    public Pointer insert(Transaction t, String type, Dictionary<String, Object> data) {
        Table table = ((DBConnection) t).db.getTable(ddp.getDataDefinition(type).getName());
        return table.insertRecord((DBConnection) t, data);
    }

    @Override
    public void checkInsert(Transaction t, String type, Dictionary<String, Object> fieldsToCheck,
            Dictionary<String, DataHolder> fieldsToIgnore, Dictionary<String, Object> allFields) {
        Table table = ((DBConnection) t).db.getTable(ddp.getDataDefinition(type).getName());
        checkFieldNames(fieldsToCheck, table.dd);
        table.checkInsert(fieldsToCheck, fieldsToIgnore, allFields);
    }

    @Override
    public void checkUpdate(Transaction t, String type, Pointer pointer, Dictionary<String, Object> fieldsToCheck,
            Dictionary<String, DataHolder> fieldsToIgnore, Dictionary<String, Object> allFields) {

        DataDefinition dd = checkUpdate(type, fieldsToCheck, fieldsToIgnore);

        Table table = ((DBConnection) t).db.getTable(dd.getName());
        table.checkUpdate(pointer, allFields);
    }

    @Override
    public int update1(Transaction t, Pointer p, DataDefinition typeDef, Dictionary<String, Object> dic) {
        boolean added = false;
        if (!((org.makumba.db.makumba.sql.Database) ((DBConnection) t).db).automaticUpdateTimestamp()
                && dic.get("TS_modify") == null) {
            dic.put("TS_modify", new Timestamp(System.currentTimeMillis()));
            added = true;
        }
        Object[] params = new Object[dic.size() + 1];
        params[0] = p;
        int n = 1;
        String set = "";
        String comma = "";
        for (Enumeration<String> upd = dic.keys(); upd.hasMoreElements();) {
            set += comma;
            String s = upd.nextElement();
            params[n++] = dic.get(s);
            set += "this." + s + "=$" + n;
            comma = ",";
        }
        if (added) {
            dic.remove("TS_modify");
        }
        if (set.trim().length() > 0) {
            return t.update(typeDef.getName() + " this", set, "this."
                    + ddp.getDataDefinition(p.getType()).getIndexPointerFieldName() + "=$1", params);
        }

        return 0;
    }

}
