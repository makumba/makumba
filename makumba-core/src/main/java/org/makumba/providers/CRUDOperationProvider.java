package org.makumba.providers;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.CompositeValidationException;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.InvalidValueException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.StringUtils;
import org.makumba.db.DataHolder;
import org.makumba.db.TransactionImplementation;

/**
 * This interface describes the operations that a simple CRUD provider has to implement.
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id$
 */
public abstract class CRUDOperationProvider {

    protected DataDefinitionProvider ddp = DataDefinitionProvider.getInstance();

    /**
     * Inserts data of a given type
     * 
     * @param t
     *            the Transaction handling the insertion
     * @param type
     *            the type of the object to be inserted
     * @param data
     *            the data to be inserted
     * @return a Pointer corresponding to the place where the record has been inserted
     */
    public abstract Pointer insert(Transaction t, String type, Dictionary<String, Object> data);

    /**
     * Reads a record
     * 
     * @param t
     *            the Transaction used to read the record
     * @param ptr
     *            the Pointer corresponding to the place at which we should read
     * @param fields
     *            the fields to read
     * @return a Dictionary holding the values of the fields
     */
    public Dictionary<String, Object> read(Transaction t, Pointer ptr, Object fields) {
        return t.read(ptr, fields);
    }

    /**
     * Updates a record with the given data
     * 
     * @param t
     *            the Transaction handling the update
     * @param ptr
     *            the Pointer corresponding to the place at which we should update
     * @param data
     *            a Dictionary holding the the fields to be updated
     */
    public void update(Transaction t, Pointer ptr, Dictionary<String, Object> data) {
        t.update(ptr, data);
    }

    /**
     * Deletes a record
     * 
     * @param t
     *            the Transaction handling the deletion
     * @param ptr
     *            the Pointer to the record to be deleted
     */
    public void delete(Transaction t, Pointer ptr) {
        ((TransactionImplementation) t).delete1(ptr);
    }

    /**
     * Updates a set of values
     * 
     * @param t
     *            the Transaction handling the update
     * @param base
     *            the Pointer to the base object which has the set
     * @param fi
     *            the FieldDefinition corresponding to the set
     * @param val
     *            the data to be updated, hold in a Vector
     */
    public void updateSet(Transaction t, Pointer base, FieldDefinition fi, Object val) {

        if (!fi.getType().equals("set") && !fi.getType().equals("setintEnum") && !fi.getType().equals("setcharEnum")) {
            throw new InvalidFieldTypeException(fi, "set");
        }

        // we empty the existing set
        deleteSet(t, base, fi);

        // if the new value is empty, we simply return
        if (val == null || val == Pointer.NullSet || ((Vector<?>) val).size() == 0) {
            return;
        }

        updateSet1(t, base, fi, val);
    }

    public void updateSet1(Transaction t, Pointer base, FieldDefinition fi, Object val) {
        // we update the set with the new values
        Vector<?> values = (Vector<?>) val;

        Dictionary<String, Object> data = new Hashtable<String, Object>(10);
        data.put(fi.getSubtable().getSetOwnerFieldName(), base);

        for (Object element : values) {
            data.put(fi.getSubtable().getSetMemberFieldName(), element);
            insert(t, fi.getSubtable().getName(), data);
        }
    }

    /**
     * Deletes a set
     * 
     * @param t
     *            the Transaction handling the deletion
     * @param base
     *            the base pointer to the object to be deleted
     * @param fi
     *            the FieldDefinition of the field containing the set
     */
    public void deleteSet(Transaction t, Pointer base, FieldDefinition fi) {
        TransactionImplementation t1 = (TransactionImplementation) t;
        t.update(t1.transformTypeName(fi.getSubtable().getName()) + " this", null, "this."
                + fi.getSubtable().getSetOwnerFieldName() + t1.getPrimaryKeyName() + "=" + t1.getParameterName(), base);
    }

    /**
     * Checks if a set of values can be updated in the database
     * 
     * @param t
     *            the Transaction to be used
     * @param type
     *            the type of the base object to insert to
     * @param fieldsToCheck
     *            the values to be checked
     * @param fieldsToIgnore
     *            the values of toCheck not to be checked
     * @param allFields
     *            the entire data to be inserted
     */
    public abstract void checkInsert(Transaction t, String type, Dictionary<String, Object> fieldsToCheck,
            Dictionary<String, DataHolder> fieldsToIgnore, Dictionary<String, Object> allFields);

    /**
     * Checks if a set of values can be updated in the database.
     * 
     * @param t
     *            the Transaction to be used
     * @param type
     *            the type of the base object to insert to
     * @param pointer
     *            the pointer to the record to be updated
     * @param fieldsToCheck
     *            the values to be checked
     * @param others
     *            the values of toCheck not to be checked
     * @param allFields
     *            the entire data to be inserted
     */
    public abstract void checkUpdate(Transaction t, String type, Pointer pointer,
            Dictionary<String, Object> fieldsToCheck, Dictionary<String, DataHolder> others,
            Dictionary<String, Object> allFields);

    protected DataDefinition checkUpdate(String type, Dictionary<String, Object> fieldsToCheck,
            Dictionary<String, DataHolder> fieldsToIgnore) {
        DataDefinition dd = ddp.getDataDefinition(type);

        // we check if we can perform the update
        checkFieldNames(fieldsToCheck, dd);
        for (FieldDefinition fd : dd.getFieldDefinitions()) {
            if (fieldsToIgnore.get(fd.getName()) == null) {
                checkUpdate1(fd, fieldsToCheck);
            }
        }
        return dd;
    }

    public abstract int update1(Transaction t, Pointer p, DataDefinition typeDef, Dictionary<String, Object> dic);

    private void checkUpdate1(FieldDefinition fd, Dictionary<String, Object> d) {
        Object o = d.get(fd.getName());
        if (o != null) {
            switch (fd.getIntegerType()) {
                case FieldDefinition._dateCreate:
                    throw new org.makumba.InvalidValueException(fd, "you cannot update a creation date");
                case FieldDefinition._dateModify:
                    throw new org.makumba.InvalidValueException(fd, "you cannot update a modification date");
                case FieldDefinition._ptrIndex:
                    throw new org.makumba.InvalidValueException(fd, "you cannot update an index pointer");
                default:
                    checkUpdate(fd, d);
            }
        }
    }

    private void checkUpdate(FieldDefinition fd, Dictionary<String, Object> d) {
        Object o = d.get(fd.getName());
        if (fd.isNotEmpty() && StringUtils.isEmpty(o)) {
            // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(fd, FieldDefinition.ERROR_NOT_EMPTY));
        }
        if (o == null) {
            return;
        }
        if (fd.isFixed()) {
            // FIXME: call this in RecordEditor.readFrom, to have more possible exceptions gathered at once
            throw new CompositeValidationException(new InvalidValueException(fd, "You cannot update a fixed field"));
        }
        d.put(fd.getName(), fd.checkValue(o));
    }

    public void checkFieldNames(Dictionary<String, Object> d, DataDefinition dd) {
        for (Enumeration<String> e = d.keys(); e.hasMoreElements();) {
            String s = e.nextElement();
            if (dd.getFieldDefinition(s) == null) {
                throw new org.makumba.NoSuchFieldException(dd, s);
            }
        }
    }

}
