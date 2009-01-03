// /////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003 http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------
//  $Id: timestampFormatter.java 2568 2008-06-14 01:06:21Z rosso_nero $
//  $Name$
/////////////////////////////////////
package org.makumba.db;

import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.makumba.Attributes;
import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.LogicException;
import org.makumba.NoSuchFieldException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

/**
 * @version $Id: TransactionImplementation.java,v 1.1 Jun 15, 2008 3:31:07 PM rudi Exp $
 */
public abstract class TransactionImplementation implements Transaction {

    protected DataDefinitionProvider ddp;

    protected QueryProvider qp;

    protected TransactionProvider tp;

    private Attributes contextAttributes;

    public TransactionImplementation(TransactionProvider tp) {
        this.tp = tp;
        this.ddp = DataDefinitionProvider.getInstance();
    }

    public abstract void close();

    public abstract void commit();

    public abstract void rollback();

    public abstract String getName();

    public abstract void lock(String symbol);

    public abstract void unlock(String symbol);

    /**
     * Executes an UPDATE statement or a DELETE FROM statement, depending on the value of set.
     * 
     * @param type
     *            the type on which to perform the operation
     * @param set
     *            the SET part of the query. if null, this performs a DELETE FROM statement
     * @param where
     *            the WHERE part of the query
     * @param args
     *            the query arguments
     * @return either (1) the row count for <code>INSERT</code>, <code>UPDATE</code>, or <code>DELETE</code>
     *         statements or (2) 0 for SQL statements that return nothing
     */
    protected abstract int executeUpdate(String type, String set, String where, Object args);

    /**
     * Delete the record pointed by the given pointer. If the pointer is a 1-1, the oringinal is set to null. All the
     * subrecords and subsets are automatically deleted.
     */
    public void delete(Pointer ptr) {
        DataDefinition ri = ddp.getDataDefinition(ptr.getType());
        FieldDefinition fi = ri.getParentField();

        // if this is a ptrOne, we nullify the pointer in the parent record
        if (fi != null && fi.getType().equals("ptrOne")) {
            executeUpdate(transformTypeName(fi.getDataDefinition().getName()) + " this", "this." + fi.getName() + "="
                    + getNullConstant(), "this." + fi.getName() + getPrimaryKeyName() + "=" + getParameterName(), ptr);
        }

        // then we do the rest of the delete job
        try {
            delete1(ptr);
        } catch (Throwable e) {
            if(e.getClass().getName().endsWith("ConstraintViolationException"))
                throw new DBError(e);
            if(e instanceof Error)
                throw (Error)e;
            if(e instanceof RuntimeException)
                throw (RuntimeException)e;
            throw new RuntimeWrappedException(e);
        }

    }

    /**
     * Deletes in the form delete("general.Person p", "p=$1", params) NOTE that this method does not delete subsets and
     * subrecords
     * 
     * @return the number of records affected
     */
    public int delete(String from, String where, Object parameters) {
        return executeUpdate(from, null, where, parameters);
    }

    public abstract Vector<Dictionary<String, Object>> executeQuery(String OQL, Object parameterValues, int offset,
            int limit);

    public abstract Vector<Dictionary<String, Object>> executeQuery(String OQL, Object parameterValues);

    public TransactionProvider getTransactionProvider() {
        return this.tp;
    }

    public Pointer insert(String type, Dictionary<String, Object> data) {

        // TODO: this does not support the DataTransformer possibility as for the Makumba DB.
        // Probably all those Makumba DB features should be placed in another place than the makumba DB.

        DataHolder dh = new DataHolder(this, data, type);
        dh.checkInsert();
        return dh.insert();

    }

    /**
     * Insert a record in a subset (1-N set) or subrecord (1-1 pointer) of the given record. For 1-1 pointers, if
     * another subrecord existed, it is deleted.
     * 
     * @return a Pointer to the inserted record
     */
    public Pointer insert(Pointer base, String field, Dictionary<String, Object> data) {
        FieldDefinition fi = ddp.getDataDefinition(base.getType()).getFieldDefinition(field);
        if (fi == null) {
            throw new NoSuchFieldException(ddp.getDataDefinition(base.getType()), field);
        }
        if (fi.getType().equals("setComplex")) {
            data.put(fi.getSubtable().getSetOwnerFieldName(), base);
            return insert(fi.getSubtable().getName(), data);
        } else {
            throw new InvalidFieldTypeException(fi, "subset");
        }
    }

    public abstract int insertFromQuery(String type, String OQL, Object parameterValues);

    protected abstract StringBuffer writeReadQuery(Pointer p, Enumeration<String> e);

    /** change the record pointed by the given pointer. Only fields indicated are changed to the respective values */
    public int update(Pointer ptr, java.util.Dictionary<String, Object> fieldsToChange) {
        DataHolder dh = new DataHolder(this, fieldsToChange, ptr.getType());
        dh.checkUpdate(ptr);
        return dh.update(ptr);
    }

    /**
     * updates in the form update("general.Person p", "p.birthdate=$1", "p=$2", params) NOTE that this method does not
     * delete subrecords if their pointers are nullified
     * 
     * @return the number of records affected
     */
    public int update(String from, String set, String where, Object parameters) {
        return executeUpdate(from, set, where, parameters);
    }

    public Dictionary<String, Object> read(Pointer p, Object flds) {

        Enumeration<String> e = extractReadFields(p, flds);

        StringBuffer sb = writeReadQuery(p, e);

        Vector<Dictionary<String, Object>> v = executeReadQuery(p, sb);

        if (v.size() == 0) {
            return null;
        }
        if (v.size() > 1) {
            throw new org.makumba.MakumbaError("MAKUMBA DATABASE INCOSISTENT: Pointer not unique: " + p);
        }
        Dictionary<String, Object> d = v.elementAt(0);
        Hashtable<String, Object> h = new Hashtable<String, Object>(13);
        for (Enumeration<String> en = d.keys(); en.hasMoreElements();) {
            Object o = en.nextElement();
            h.put((String) o, d.get(o));
        }
        return h;
    }

    protected Enumeration<String> extractReadFields(Pointer p, Object flds) throws ProgrammerError {
        Enumeration<String> e = null;
        if (flds == null) {
            DataDefinition ri = ddp.getDataDefinition(p.getType());
            Vector<String> v = new Vector<String>();
            for (String s : ri.getFieldNames()) {
                if (!ri.getFieldDefinition(s).getType().startsWith("set")) {
                    v.addElement(s);
                }
            }
            e = v.elements();
        } else if (flds instanceof Vector) {
            e = ((Vector) flds).elements();
        } else if (flds instanceof Enumeration) {
            e = (Enumeration) flds;
        } else if (flds instanceof String[]) {
            Vector<String> v = new Vector<String>();
            String[] fl = (String[]) flds;
            for (String element : fl) {
                v.addElement(element);
            }
            e = v.elements();
        } else if (flds instanceof String) {
            Vector<String> v = new Vector<String>();
            v.add((String) flds);
            e = v.elements();
        } else {
            throw new ProgrammerError("read() argument must be Enumeration, Vector, String[], String or null");
        }
        return e;
    }

    protected abstract Vector<Dictionary<String, Object>> executeReadQuery(Pointer p, StringBuffer sb);

    public void delete1(Pointer ptr) {
        String ptrDD = ptr.getType();
        DataDefinition ri = ddp.getDataDefinition(ptrDD);
        Object param[] = { ptr };

        // FIXME: deleting the ptrOnes and set entries could potentially be skipped, by automatically creating
        
        // delete the ptrOnes
        Vector<String> ptrOnes = new Vector<String>();

        for (String s : ri.getFieldNames()) {
            if (ri.getFieldDefinition(s).getType().equals("ptrOne")) {
                ptrOnes.addElement(s);
            }
        }

        if (ptrOnes.size() > 0) {
            Dictionary<String, Object> d = read(ptr, ptrOnes);
            for (Enumeration<Object> e = d.elements(); e.hasMoreElements();) {
                delete((Pointer) e.nextElement());
            }
        }
        // delete all the subfields
        for (String string : ri.getFieldNames()) {
            FieldDefinition fi = ri.getFieldDefinition(string);
            if (fi.getType().startsWith("set")) {
                if (fi.getType().equals("setComplex")) {
                    // recursively process all set entries, to delete their subSets and ptrOnes
                    Vector<Dictionary<String, Object>> v = executeQuery("SELECT pointedType"+ getPrimaryKeyName()+ " as pointedType FROM " + ptr.getType() + " ptr "+getSetJoinSyntax()+" ptr." + fi.getName() + " pointedType WHERE ptr"+getPrimaryKeyName()+"="+getParameterName(), ptr);
                    for (Dictionary<String, Object> dictionary : v) {
                        Pointer p = (Pointer) dictionary.get("pointedType");
                        delete1(p);
                    }
                    executeUpdate(transformTypeName(fi.getSubtable().getName()) + " this", null, "this."
                            + transformTypeName(fi.getSubtable().getFieldDefinition(3).getName()) + getPrimaryKeyName()
                            + "= " + getParameterName(), param);
                } else {
                    tp.getCRUD().deleteSet(this, ptr, fi);
                }
            }
        }
        // delete the record
        executeUpdate(transformTypeName(ptrDD) + " this", null, "this." + getPrimaryKeyName(ptrDD) + "="
                + getParameterName(), ptr);
    }

    public String getSetJoinSyntax() {
        return ",";
    }

    protected Map<String, Object> paramsToMap(Object args) {
        final Map<String, Object> m= paramsToMap1(args);
        if(contextAttributes==null)
            return m;
        return new EasyMap<String, Object>(){
            public Object get(Object key){
                Object o= m.get(key);
                if(o!=null)
                    return o;
                try{
                    o= contextAttributes.getAttribute((String)key);
                    if(o==null && contextAttributes.hasAttribute(""+key+"_null"))
                        o=Pointer.Null;
                    return o;
                }catch(LogicException e){
                    return null;
                }
            }

        };
    }
    
    protected Map<String, Object> paramsToMap1(Object args) {
        if (args instanceof Map) {
            return (Map<String, Object>) args;
        }
        Map<String, Object> ret = new HashMap<String, Object>();
        if (args == null) {
            return ret;
        }
        if (args instanceof List) {
            args = ((List) args).toArray();
        }
        if (args instanceof Object[]) {
            for (int j = 0; j < ((Object[]) args).length; j++) {
                ret.put("" + (j + 1), ((Object[]) args)[j]);
            }
            return ret;
        }
        ret.put("1", args);
        return ret;
    }

    protected Object[] treatParam(Object args) {
        if (args == null) {
            return new Object[] {};
        } else if (args instanceof Vector) {
            Vector v = (Vector) args;
            Object[] param = new Object[v.size()];
            v.copyInto(param);
            return param;
        } else if (args instanceof Object[]) {
            return (Object[]) args;
        } else {
            Object p[] = { args };
            return p;
        }
    }

    public String transformTypeName(String name) {
        return name;
    }

    public String getParameterName() {
        return "$1";
    }

    public String getPrimaryKeyName() {
        return "";
    }

    public String getPrimaryKeyName(String ptrDD) {
        return ddp.getDataDefinition(ptrDD).getIndexPointerFieldName();
    }

    public abstract String getNullConstant();

    public abstract String getDataSource();

    public void setContext(Attributes a) {
        contextAttributes = a;
    }
    
    static class EasyMap<K, V>implements Map<K,V>{

        public void clear() {            
        }

        public boolean containsKey(Object key) {
            return false;
        }

        public boolean containsValue(Object value) {
            return false;
        }

        public Set<java.util.Map.Entry<K, V>> entrySet() {
            return null;
        }

        public V get(Object key) {
            return null;
        }

        public boolean isEmpty() {
            return false;
        }

        public Set<K> keySet() {
            return null;
        }

        public V put(K key, V value) {
            return null;
        }

        public void putAll(Map<? extends K, ? extends V> t) {            
        }

        public V remove(Object key) {
            return null;
        }

        public int size() {
            return 0;
        }

        public Collection<V> values() {
            return null;
        }
        
    }
}
