///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id$
//  $Name$
/////////////////////////////////////

package org.makumba.db;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.NoSuchFieldException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.Configuration;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * This is the Makumba-specific implementation of a {@link Transaction}
 * 
 * @author Cristian Bogdan
 * @author Manuel Gay
 * @version $Id$
 */
public abstract class DBConnection implements Transaction {

    protected org.makumba.db.Database db;

    protected Configuration config = new Configuration();

    protected DataDefinitionProvider ddp;

    private TransactionProvider tp;

    protected DBConnection() {
        this.ddp = new DataDefinitionProvider(config);
    }

    protected DBConnection(TransactionProvider tp) {
        this();
        this.tp = tp;
    } // for the wrapper

    public DBConnection(Database database, TransactionProvider tp) {
        this.db = database;
        this.tp = tp;
        this.ddp = new DataDefinitionProvider(config);
    }

    public org.makumba.db.Database getHostDatabase() {
        return db;
    }

    /** Get the name of the database in the form host[_port]_dbprotocol_dbname */
    public String getName() {
        return db.getName();
    }

    public abstract void close();

    public abstract void commit();

    public abstract void rollback();

    Map<String, Pointer> locks = new HashMap<String, Pointer>(13);

    Hashtable<String, String> lockRecord = new Hashtable<String, String>(5);

    public void lock(String symbol) {
        lockRecord.clear();
        lockRecord.put("name", symbol);
        locks.put(symbol, insert("org.makumba.db.Lock", lockRecord));
    }

    public void unlock(String symbol) {
        Pointer p = (Pointer) locks.get(symbol);
        if (p == null)
            throw new ProgrammerError(symbol + " not locked in connection " + this);
        deleteLock(symbol);
    }

    protected void deleteLock(String symbol) {
        locks.remove(symbol);
        // we need to delete after the lock name instead of the pointer
        // in order not to produce deadlock
        delete("org.makumba.db.Lock l", "l.name=$1", symbol);
    }

    protected void unlockAll() {
        for (Iterator i = locks.keySet().iterator(); i.hasNext();) {
            deleteLock((String) i.next());
        }
    }

    /** change the record pointed by the given pointer. Only fields indicated are changed to the respective values */
    public void update(Pointer ptr, java.util.Dictionary fieldsToChange) {
        DataHolder dh = new DataHolder(this, fieldsToChange, ptr.getType());
        dh.checkUpdate(ptr);
        dh.update(ptr);
    }

    public Dictionary read(Pointer p, Object flds) {
        Enumeration e = null;
        if (flds == null) {
            DataDefinition ri = ddp.getDataDefinition(p.getType());
            Vector<String> v = new Vector<String>();
            for (Enumeration f = ri.getFieldNames().elements(); f.hasMoreElements();) {
                String s = (String) f.nextElement();
                if (!ri.getFieldDefinition(s).getType().startsWith("set"))
                    v.addElement(s);
            }
            e = v.elements();
        } else if (flds instanceof Vector)
            e = ((Vector) flds).elements();
        else if (flds instanceof Enumeration)
            e = (Enumeration) flds;
        else if (flds instanceof String[]) {
            Vector<String> v = new Vector<String>();
            String[] fl = (String[]) flds;
            for (int i = 0; i < fl.length; i++)
                v.addElement(fl[i]);
            e = v.elements();
        } else if (flds instanceof String) {
            Vector<String> v = new Vector<String>();
            v.add((String) flds);
            e = v.elements();
        } else {
            throw new ProgrammerError("read() argument must be Enumeration, Vector, String[], String or null");
        }
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        String separator = "";
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            DataDefinition r = ddp.getDataDefinition(p.getType());
            if (!(o instanceof String))
                throw new org.makumba.NoSuchFieldException(r,
                        "Dictionaries passed to makumba DB operations should have String keys. Key <" + o
                                + "> is of type " + o.getClass() + r.getName());
            if (r.getFieldDefinition((String) o) == null)
                throw new org.makumba.NoSuchFieldException(r, (String) o);
            String s = (String) o;
            sb.append(separator).append("p.").append(s).append(" as ").append(s);
            separator = ",";
        }
        sb.append(" FROM " + p.getType() + " p WHERE p=$1");
        Object[] params = { p };
        Vector v = executeQuery(sb.toString(), params);
        if (v.size() == 0)
            return null;
        if (v.size() > 1)
            throw new org.makumba.MakumbaError("MAKUMBA DATABASE INCOSISTENT: Pointer not unique: " + p);
        Dictionary d = (Dictionary) v.elementAt(0);
        Hashtable<Object, Object> h = new Hashtable<Object, Object>(13);
        for (Enumeration en = d.keys(); en.hasMoreElements();) {
            Object o = en.nextElement();
            h.put(o, d.get(o));
        }
        return h;
    }

    /** insert a record */
    public Pointer insert(String type, Dictionary data) {
        Table t = db.getTable(type);
        t.computeInsertHook();

        if (t.insertHook != null) {
            Hashtable<Object, Object> h = new Hashtable<Object, Object>();
            for (Enumeration e = data.keys(); e.hasMoreElements();) {
                Object k = e.nextElement();
                h.put(k, data.get(k));
            }
            data = h;
        }

        if (t.insertHook == null || t.insertHook.transform(data, this)) {
            DataHolder dh = new DataHolder(this, data, type);
            dh.checkInsert();
            return dh.insert();
        }
        return null;
    }

    Object[] treatParam(Object args) {
        if (args instanceof Vector) {
            Vector v = (Vector) args;
            Object[] param = new Object[v.size()];
            v.copyInto(param);
            return param;
        } else if (args instanceof Object[])
            return (Object[]) args;
        else {
            Object p[] = { args };
            return p;
        }
    }

    /**
     * Execute a parametrized OQL query.
     * 
     * @return a Vector of Dictionaries
     */
    public java.util.Vector executeQuery(String OQL, Object args, int offset, int limit) {
        Object[] k = { OQL, "" };
        return ((Query) getHostDatabase().queries.getResource(k)).execute(treatParam(args), this, offset, limit);
    }

    public int insertFromQuery(String type, String OQL, Object args) {
        Object[] k = { OQL, type };
        return ((Query) getHostDatabase().queries.getResource(k)).insert(treatParam(args), this);
    }

    public java.util.Vector executeQuery(String OQL, Object args) {
        return executeQuery(OQL, args, 0, -1);
    }

    /**
     * Execute a parametrized update or delete. A null set means "delete"
     * 
     * @return a Vector of Dictionaries
     */
    public int executeUpdate(String type, String set, String where, Object args) {
        Object[] multi = { type, set, where };

        return ((Update) getHostDatabase().updates.getResource(multi)).execute(this, treatParam(args));
    }

    /**
     * Insert a record in a subset (1-N set) or subrecord (1-1 pointer) of the given record. For 1-1 pointers, if
     * another subrecord existed, it is deleted.
     * 
     * @return a Pointer to the inserted record
     */
    public Pointer insert(Pointer base, String field, java.util.Dictionary data) {
        FieldDefinition fi = ddp.getDataDefinition(base.getType()).getFieldDefinition(field);
        if (fi == null) {
            throw new NoSuchFieldException(ddp.getDataDefinition(base.getType()), field);
        }
        if (fi.getType().equals("setComplex")) {
            data.put(fi.getSubtable().getSetOwnerFieldName(), base);
            return insert(fi.getSubtable().getName(), data);
        } else
            throw new InvalidFieldTypeException(fi, "subset");
    }

    /**
     * Delete the record pointed by the given pointer. If the pointer is a 1-1, the oringinal is set to null. All the
     * subrecords and subsets are automatically deleted.
     */
    public void delete(Pointer ptr) {
        DataDefinition ri = ddp.getDataDefinition(ptr.getType());
        FieldDefinition fi = ri.getParentField();

        // if this is a ptrOne, we nullify the pointer in the parent record
        if (fi != null && fi.getType().equals("ptrOne"))
            executeUpdate(fi.getDataDefinition().getName() + " this", "this." + fi.getName() + "=nil", "this."
                    + fi.getName() + "=$1", ptr);

        // then we do the rest of the delete job
        delete1(ptr);
    }

    void delete1(Pointer ptr) {
        DataDefinition ri = ddp.getDataDefinition(ptr.getType());
        Object param[] = { ptr };

        // delete the ptrOnes
        Vector ptrOnes = new Vector();

        for (Enumeration e = ri.getFieldNames().elements(); e.hasMoreElements();) {
            String s = (String) e.nextElement();
            if (ri.getFieldDefinition(s).getType().equals("ptrOne"))
                ptrOnes.addElement(s);
        }

        if (ptrOnes.size() > 0) {
            Dictionary d = read(ptr, ptrOnes);
            for (Enumeration e = d.elements(); e.hasMoreElements();)
                delete1((Pointer) e.nextElement());
        }
        // delete all the subfields
        for (Enumeration e = ri.getFieldNames().elements(); e.hasMoreElements();) {
            FieldDefinition fi = ri.getFieldDefinition((String) e.nextElement());
            if (fi.getType().startsWith("set"))
                if (fi.getType().equals("setComplex"))
                    executeUpdate(fi.getSubtable().getName() + " this", null, "this."
                            + fi.getSubtable().getFieldDefinition(3).getName() + "= $1", param);
                else
                    tp.getCRUD().deleteSet(this, ptr, fi);
        }
        // delete the record
        executeUpdate(ptr.getType() + " this", null, "this."
                + ddp.getDataDefinition(ptr.getType()).getIndexPointerFieldName() + "=$1", ptr);
    }

    /*
     * update in the form update("general.Person p", "p.birthdate=$1", "p=$2", params) NOTE that this method does not
     * delete subrecords if their pointers are nullified @return the number of records affected
     */
    public int update(String from, String set, String where, Object parameters) {
        return executeUpdate(from, set, where, parameters);
    }

    /*
     * delete in the form delete("general.Person p", "p=$1", params) NOTE that this method does not delete subsets and
     * subrecords @return the number of records affected
     */
    public int delete(String from, String where, Object parameters) {
        return executeUpdate(from, null, where, parameters);
    }

    public Query getQuery(String OQL) {
        Object[] k = { OQL, "" };
        return ((Query) getHostDatabase().queries.getResource(k));
    }

    public TransactionProvider getTransactionProvider() {
        return this.tp;
    }
}