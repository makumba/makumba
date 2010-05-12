package org.makumba.db;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * Class which enables it to perform "super-CRUD" operations, i.e. composite inserts and updates on subrecords The data
 * is passed as a dictionary, and its keys look like "a", "a.b", "a.b.c" ...
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id: DBConnection.java 1938 2007-10-25 14:55:27Z manuel_gay $
 */
public class DataHolder {

    /** the Transaction of this DataHolder */
    private Transaction t;

    /** the TransactionProvider of the transaction of this DataHolder */
    private TransactionProvider tp;

    /** dictionary holding the data used for the operation, and on which operations are performed */
    Dictionary<String, Object> dictionnary = new Hashtable<String, Object>();

    /** dictionary holding subrecords, i.e. each key gives access to a hashtable of fields */
    Dictionary<String, DataHolder> subrecords = new Hashtable<String, DataHolder>(); // contains data holders

    /** dictionary holding the data which has to be performed on sets */
    Dictionary<String, Object> sets = new Hashtable<String, Object>(); // contains vectors

    /** all the fields to be processed */
    private Dictionary<String, Object> fullData;

    private DataDefinitionProvider ddp;

    /** the type of the base object to be worked on */
    private String type;

    /** the DataDefinition of the base object to be worked on */
    private DataDefinition typeDef;

    public DataHolder(Transaction t, Dictionary<String, Object> data, String type) {
        this.t = t;
        this.fullData = data;
        this.type = type;

        this.ddp = DataDefinitionProvider.getInstance();
        this.tp = t.getTransactionProvider();
        this.typeDef = ddp.getDataDefinition(type);

        // we populate our dictionary with the given data
        for (Enumeration<String> e = data.keys(); e.hasMoreElements();) {
            String o = e.nextElement();
            dictionnary.put(o, data.get(o));
        }

        Hashtable<String, Hashtable<String, Object>> subfieldsTemp = new Hashtable<String, Hashtable<String, Object>>();

        for (Enumeration<String> e = data.keys(); e.hasMoreElements();) {
            Object o = e.nextElement();

            // we check if the key of the dictionary is a string, if not, we complain
            if (!(o instanceof String)) {
                throw new org.makumba.NoSuchFieldException(typeDef,
                        "Dictionaries passed to makumba DB operations should have String keys. Key <" + o
                                + "> is of type " + o.getClass() + typeDef.getName());
            }

            // we figure out the content of our dictionary. if dots are found, this means we refer to subtypes
            String s = (String) o;
            int dot = s.indexOf(".");

            // if there's no dot, this is a field of the current object (the "type" parameter)
            if (dot == -1) {
                FieldDefinition fi = typeDef.getFieldDefinition(s);

                // if there was no field definition found, then this field doesn't exist and we complain
                if (fi == null) {
                    throw new org.makumba.NoSuchFieldException(typeDef, (String) o);
                }

                // if this field is a set, we add it to our dictionary of sets
                if (fi.getType().equals("set") || fi.getType().equals("setintEnum")
                        || fi.getType().equals("setcharEnum")) {
                    Object v = dictionnary.remove(s); // remove from our dictionary, as it was treated
                    fi.checkValue(v);
                    sets.put(s, v);
                }
            } else { // if there's a dot, we place it in our dictionary of subrecords
                String fld = s.substring(0, dot);
                Hashtable<String, Object> oth = subfieldsTemp.get(fld);
                if (oth == null) {
                    oth = new Hashtable<String, Object>();
                    subfieldsTemp.put(fld, oth);
                }
                oth.put(s.substring(dot + 1), dictionnary.remove(s)); // we keep only the field name after the dot
            }
        }

        // we check what is left (in the subrecords)
        for (Enumeration<String> e = subfieldsTemp.keys(); e.hasMoreElements();) {

            String fld = e.nextElement();
            FieldDefinition fd = typeDef.getFieldDefinition(fld);

            if (fd == null) {
                throw new org.makumba.NoSuchFieldException(typeDef, fld);
            }

            // both a field and its subrecords were indicated
            if (dictionnary.get(fld) != null) {
                throw new org.makumba.InvalidValueException(fd,
                        "you cannot indicate both a subfield and the field itself. Values for " + fld + "."
                                + subrecords.get(fld) + " were also indicated");
            }

            // if this field is a ptrOne (in the same table), i.e. a subrecord (not external record)
            if (!fd.getType().equals("ptrOne") && (!fd.isNotNull() || !fd.isFixed())) {
                throw new InvalidFieldTypeException(fd,
                        "subpointer or base pointer, so it cannot be used for composite insert/edit");
            }

            // we recursively add the subfield to our fields
            subrecords.put(fld, new DataHolder(t, subfieldsTemp.get(fld), fd.getPointedType().getName()));
        }
    }

    @Override
    public String toString() {
        return "data: " + dictionnary + " others: " + subrecords;
    }

    /**
     * Checks if it is possible to insert data for all subrecords.<br>
     * Does not check if same-table duplicate exists; duplicate errors are to be dealt with after an insertion attempt
     * has been made by the implementation of {@link #insert()}
     */
    public void checkInsert() {
        for (Enumeration<DataHolder> e = subrecords.elements(); e.hasMoreElements();) {
            e.nextElement().checkInsert();
        }
        tp.getCRUD().checkInsert(t, type, dictionnary, subrecords, fullData);
    }

    /**
     * Checks if it is possible to update for all subrecords.<br>
     * Does not check if same-table duplicate exists; duplicate errors are to be dealt with after an update attempt has
     * been made by the implementation of {@link #update(Pointer)}
     * 
     * @param pointer
     *            the pointer to the record to be updated
     */
    void checkUpdate(Pointer pointer) {
        for (Enumeration<DataHolder> e = subrecords.elements(); e.hasMoreElements();) {
            e.nextElement().checkUpdate(pointer);
        }
        tp.getCRUD().checkUpdate(t, type, pointer, dictionnary, subrecords, fullData);
    }

    public Pointer insert() {
        // first we insert the other pointers, i.e. the subrecords
        for (Enumeration<String> e = subrecords.keys(); e.hasMoreElements();) {
            String fld = e.nextElement();
            dictionnary.put(fld, subrecords.get(fld).insert());
        }
        // then we insert the record, and we know all the pointers to the subrecords
        Pointer p = tp.getCRUD().insert(t, type, dictionnary);

        // insert the sets
        for (Enumeration<String> e = sets.keys(); e.hasMoreElements();) {
            String fld = e.nextElement();
            FieldDefinition fi = ddp.getDataDefinition(p.getType()).getFieldDefinition(fld);
            tp.getCRUD().updateSet(t, p, fi, sets.get(fld));
        }
        return p;
    }

    int update(Pointer p) {
        // see if we have to read some pointers
        Vector<Object> ptrsx = new Vector<Object>();
        // we have to read the "other" pointers
        for (Enumeration<String> e = subrecords.keys(); e.hasMoreElements();) {
            ptrsx.addElement(e.nextElement());
        }
        // we might have to read the ptrOnes that are nullified
        for (Enumeration<String> e = dictionnary.keys(); e.hasMoreElements();) {
            String s = e.nextElement();
            if (dictionnary.get(s).equals(Pointer.Null) && typeDef.getFieldDefinition(s).getType().equals("ptrOne")) {
                ptrsx.addElement(s);
            }
        }
        // read the pointers if there are any to read
        Dictionary<String, Object> ptrs = null;
        if (ptrsx.size() > 0) {
            ptrs = t.read(p, ptrsx);
        }

        // update others
        for (Enumeration<String> e = subrecords.keys(); e.hasMoreElements();) {
            String fld = e.nextElement();
            Pointer ptr = (Pointer) ptrs.remove(fld);
            if (ptr == null || ptr == Pointer.Null) {
                dictionnary.put(fld, subrecords.get(fld).insert());
            } else {
                subrecords.get(fld).update(ptr);
            }
        }

        // rest of ptrs should be ptrOnes to delete
        if (ptrs != null) {
            for (Enumeration<Object> e = ptrs.elements(); e.hasMoreElements();) {
                tp.getCRUD().delete(t, (Pointer) e.nextElement());
            }
        }

        // we update the record
        tp.getCRUD().update1(t, p, typeDef, dictionnary);

        for (Enumeration<String> e = sets.keys(); e.hasMoreElements();) {
            String fld = e.nextElement();
            FieldDefinition fi = ddp.getDataDefinition(p.getType()).getFieldDefinition(fld);
            tp.getCRUD().updateSet(t, p, fi, sets.get(fld));
        }

        return 1;
    }

}