package org.makumba.db.hibernate;

import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.Session;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.db.DataHolder;
import org.makumba.db.Query;
import org.makumba.db.TransactionImplementation;
import org.makumba.db.hibernate.hql.HqlAnalyzer;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProviderInterface;
import org.makumba.providers.query.hql.HQLQueryProvider;

/**
 * Hibernate-specific implementation of a {@link Transaction}
 * 
 * @author Manuel Gay
 * @version $Id: HibernateTransaction.java,v 1.1 02.11.2007 14:08:53 Manuel Exp $
 */
public class HibernateTransaction extends TransactionImplementation {
    
    public org.hibernate.Transaction t;
    
    public Session s;
    
    private DataDefinitionProvider ddp;
    
    public HibernateTransaction(TransactionProviderInterface tp) {
        super(tp);
    }
    
    public HibernateTransaction(DataDefinitionProvider ddp, TransactionProviderInterface tp) {
        this(tp);
        this.ddp = ddp;
        // FIXME this obviously should not use this hardcoded value, but should come from something (like, tp.getConnectionTo?
        this.s = HibernateSFManager.getSF("test/localhost_mysql_makumba.cfg.xml", true).openSession();
        beginTransaction();
    }

    @Override
    public void close() {
        commit();
        s.close();
    }

    @Override
    public void commit() {
        t.commit();
        t=s.beginTransaction();
    }

    @Override
    protected StringBuffer writeReadQuery(Pointer p, Enumeration e) {
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
            FieldDefinition fieldDefinition = r.getFieldDefinition((String) o);
            if (fieldDefinition == null)
                throw new org.makumba.NoSuchFieldException(r, (String) o);
            String s = (String) o;
            sb.append(separator).append("p.").append(s);
            if(fieldDefinition.getType().startsWith(("ptr"))) {
                sb.append(".id");
            }
            sb.append(" as ").append(s);
            separator = ",";
        }
        sb.append(" FROM " + (new HibernateUtils()).arrowToDoubleUnderscore(p.getType()) + " p WHERE p.id=?");
        return sb;
    }
    
    @Override
    protected Vector executeReadQuery(Pointer p, StringBuffer sb) {
        
        return executeQuery(sb.toString(), p);
    }

    

    @Override
    protected int executeUpdate(String type, String set, String where, Object args) {
        
        // in the current implementation, executeUpdate is also used to execute delete-s, depending on the value of "set"
        
        String hql = new String();
        
        HibernateUtils utils = new HibernateUtils();
        
        // I have no idea if giving the type directly will work...
        if(set == null) {
            hql = "DELETE FROM "+type.replaceAll("->", "__")+" WHERE "+where;
        } else {
            hql = "UPDATE "+type.replaceAll("->", "__")+" SET "+set+" WHERE "+where;
        }
        //System.out.println("HQL: "+hql);
        
        org.hibernate.Query q = s.createQuery(hql);
        q.setCacheable(false);
        
        // setting params
        Object[] argsArray = treatParam(args);
        for(int i=0; i<argsArray.length; i++) {
            if(argsArray[i] instanceof Pointer)
                argsArray[i] = new Integer(((Pointer)argsArray[i]).getUid());
            q.setParameter(i, argsArray[i]);
        }
        
        return q.executeUpdate();
    }

    @Override
    public String getName() {
        throw new MakumbaError("Not implemented");
    }

    @Override
    public void lock(String symbol) {
        throw new MakumbaError("Not implemented");
    }

    @Override
    public void rollback() {
        t.rollback();
    }

    @Override
    public void unlock(String symbol) {
        throw new MakumbaError("Not implemented");
        
    }

    @Override
    public Vector executeQuery(String OQL, Object parameterValues, int offset, int limit) {
        return executeQuery(OQL, parameterValues);
    }

    @Override
    public Vector executeQuery(String OQL, Object parameterValues) {
        HqlAnalyzer analyzer = HQLQueryProvider.getHqlAnalyzer(OQL);
        DataDefinition paramsDef = analyzer.getParameterTypes();

        
        org.hibernate.Query q = s.createQuery(OQL);
        //System.out.println("TRYING TO RUN: "+OQL);
        q.setCacheable(false);

        // setting params
        //TODO RefactorMe - I am a weird copy-paste from HQLQueryProvider
        Object[] argsArray = treatParam(parameterValues);
        for(int i=0; i<argsArray.length; i++) {
            
            Object paramValue = argsArray[i];
            
            FieldDefinition paramDef= paramsDef.getFieldDefinition(i);
            
            if (paramValue instanceof Date) {
                q.setDate(i, (Date)paramValue);
            } else if (paramValue instanceof Integer) {
                q.setInteger(i, (Integer)paramValue);
            } else if (paramValue instanceof Pointer) {
                q.setParameter(i, new Integer(((Pointer)argsArray[i]).getUid()));
            } else { // we have any param type (most likely String)
                if(paramDef.getIntegerType()==FieldDefinition._ptr && paramValue instanceof String){
                    Pointer p= new Pointer(paramDef.getPointedType().getName(), (String)paramValue);
                    q.setInteger(i, new Integer((int) p.longValue()));
                }else
                    q.setParameter(i, paramValue);
            }
            
        }
        
        List list = q.list();
        
        // TODO RefactorMe - I am a copy-paste from HQLQueryProvider
        DataDefinition dataDef = analyzer.getProjectionType();
        
        Vector results = new Vector(list.size());

        Object[] projections = dataDef.getFieldNames().toArray();
        Dictionary keyIndex = new java.util.Hashtable(projections.length);
        for (int i = 0; i < projections.length; i++) {
            keyIndex.put(projections[i], new Integer(i));
        }

        int i = 1;
        for (Iterator iter = list.iterator(); iter.hasNext(); i++) {
            Object resultRow = iter.next();
            Object[] resultFields;
            if (!(resultRow instanceof Object[])) { // our query result has only one field
                resultFields = new Object[] { resultRow }; // we put it into an object[]
            } else { // our query had more results ==>
                resultFields = (Object[]) resultRow; // we had an object[] already
            }

            // process each field's result
            for (int j = 0; j < resultFields.length; j++) { // 
                if (resultFields[j] != null) { // we add to the dictionary only fields with values in the DB
                    FieldDefinition fd;
                    if ((fd = dataDef.getFieldDefinition(j)).getType().equals("ptr")) {
                        // we have a pointer
                        String ddName = fd.getPointedType().getName();
                        // FIXME: once we do not get dummy pointers from hibernate queries, take this out
                        if (resultFields[j] instanceof Pointer) { // we have a dummy pointer
                            resultFields[j] = new HibernatePointer(ddName, ((Pointer) resultFields[j]).getUid());
                        } else if (resultFields[j] instanceof Integer) { // we have an integer
                            resultFields[j] = new HibernatePointer(ddName, ((Integer) resultFields[j]).intValue());
                        } else {
                            throw new RuntimeWrappedException(new org.makumba.LogicException(
                                    "Internal Makumba error: Detected an unknown type returned by a query. "
                                            + "The projection index is " + j + ", the result class is "
                                            + resultFields[j].getClass() + ", it's content " + "is '" + resultFields[j]
                                            + "'and type analysis claims its type is " + fd.getPointedType().getName(),
                                    true));
                        }
                    } else {
                        resultFields[j] = resultFields[j];
                    }
                }
            }
            Dictionary dic = new ArrayMap(keyIndex, resultFields);
            results.add(dic);
        }
        
        return results;
    }

    @Override
    public Query getQuery(String OQL) {
        throw new MakumbaError("Not implemented");
    }

    @Override
    public Pointer insert(String type, Dictionary data) {
        
        // TODO: this does not support the DataTransformer possiblilty as for the Makumba DB.
        // Probably all those Makumba DB features should be placed in another place than the makumba DB.
        
        DataHolder dh = new DataHolder(this, data, type);
        dh.checkInsert();
        return dh.insert();
        
    }

    @Override
    public int insertFromQuery(String type, String OQL, Object parameterValues) {
        throw new MakumbaError("Not implemented");
    }

    @Override
    public String transformTypeName(String name) {
        HibernateUtils util = new HibernateUtils();
        
        return util.arrowToDoubleUnderscore(name);
    }
    
    @Override
    public String getParameterName() {
        return "?";
    }
    
    @Override
    public String getPrimaryKeyName() {
        return ".id";
    }
    
    @Override
    public String getPrimaryKeyName(String s) {
        return "id";
    }
    
    @Override
    public String getNullConstant() {
        return "null";
    }
    

    public org.hibernate.Transaction beginTransaction() {
        return this.t = s.beginTransaction();
    }

}
