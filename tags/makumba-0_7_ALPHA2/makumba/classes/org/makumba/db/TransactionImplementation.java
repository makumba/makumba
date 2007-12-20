package org.makumba.db;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.hibernate.exception.ConstraintViolationException;
import org.makumba.DBError;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.InvalidFieldTypeException;
import org.makumba.NoSuchFieldException;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProviderInterface;

public abstract class TransactionImplementation implements Transaction {

    protected DataDefinitionProvider ddp;
    
    protected QueryProvider qp;

    protected TransactionProviderInterface tp;

    public TransactionImplementation(TransactionProviderInterface tp) {
        this.tp = tp;
        this.ddp = new DataDefinitionProvider();
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
        if (fi != null && fi.getType().equals("ptrOne"))
            executeUpdate(transformTypeName(fi.getDataDefinition().getName()) + " this", "this." + fi.getName() + "=" + getNullConstant(), "this."
                    + fi.getName() + getPrimaryKeyName() + "="+getParameterName(), ptr);

        // then we do the rest of the delete job
        try {
            delete1(ptr);
        } catch(ConstraintViolationException e) {
            throw new DBError(e);
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

    public abstract Vector executeQuery(String OQL, Object parameterValues, int offset, int limit);

    public abstract Vector executeQuery(String OQL, Object parameterValues);

    public TransactionProviderInterface getTransactionProvider() {
        return this.tp;
    }
    
    public Pointer insert(String type, Dictionary data) {
        
        // TODO: this does not support the DataTransformer possiblilty as for the Makumba DB.
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

    public abstract int insertFromQuery(String type, String OQL, Object parameterValues);

    protected abstract StringBuffer writeReadQuery(Pointer p, Enumeration e);

    /** change the record pointed by the given pointer. Only fields indicated are changed to the respective values */
    public void update(Pointer ptr, java.util.Dictionary fieldsToChange) {
        DataHolder dh = new DataHolder(this, fieldsToChange, ptr.getType());
        dh.checkUpdate(ptr);
        dh.update(ptr);
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

    public Dictionary read(Pointer p, Object flds) {

        Enumeration e = extractReadFields(p, flds);

        StringBuffer sb = writeReadQuery(p, e);

        Vector v = executeReadQuery(p, sb);

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

    protected Enumeration extractReadFields(Pointer p, Object flds) throws ProgrammerError {
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
        return e;
    }

    protected abstract Vector executeReadQuery(Pointer p, StringBuffer sb);

    public void delete1(Pointer ptr) {
        String ptrDD = ptr.getType();
        DataDefinition ri = ddp.getDataDefinition(ptrDD);
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
                delete((Pointer) e.nextElement());
        }
        // delete all the subfields
        for (Enumeration e = ri.getFieldNames().elements(); e.hasMoreElements();) {
            FieldDefinition fi = ri.getFieldDefinition((String) e.nextElement());
            if (fi.getType().startsWith("set"))
                if (fi.getType().equals("setComplex"))
                    executeUpdate(transformTypeName(fi.getSubtable().getName()) + " this", null, "this."
                            + transformTypeName(fi.getSubtable().getFieldDefinition(3).getName()) + getPrimaryKeyName() + "= "+getParameterName(), param);
                else
                    tp.getCRUD().deleteSet(this, ptr, fi);
        }
        // delete the record
        executeUpdate(transformTypeName(ptrDD) + " this", null, "this."
                + getPrimaryKeyName(ptrDD) + "="+getParameterName(), ptr);
    }

    protected Map<String, Object> paramsToMap(Object args){
        if(args instanceof Map)
            return (Map<String, Object>)args;
        Map<String, Object> ret= new HashMap<String, Object>();
        if(args==null)
            return ret;
        if(args instanceof List)
            args= ((List)args).toArray();
        if(args instanceof Object[]){
            for(int j=0; j<((Object[])args).length; j++){
                ret.put(""+(j+1), ((Object[])args)[j]);
            }
            return ret;
        }
        ret.put("1", args);
        return ret;
    }
    
    protected Object[] treatParam(Object args) {
        if (args == null) {
            return new Object[] {};
        } else if(args instanceof Vector) {
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

    
}
