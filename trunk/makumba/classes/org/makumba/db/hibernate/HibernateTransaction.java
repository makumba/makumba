package org.makumba.db.hibernate;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Vector;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.makumba.DataDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.MakumbaError;
import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.db.DataHolder;
import org.makumba.db.Query;
import org.makumba.db.TransactionImplementation;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProviderInterface;

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
        this.s = HibernateSFManager.getSF().openSession();
    }

    @Override
    public void close() {
        t.commit();
        s.close();
    }

    @Override
    public void commit() {
        t.commit();
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
            if (r.getFieldDefinition((String) o) == null)
                throw new org.makumba.NoSuchFieldException(r, (String) o);
            String s = (String) o;
            sb.append(separator).append("p.").append(s).append(" as ").append(s);
            separator = ",";
        }
        sb.append(" FROM " + p.getType() + " p WHERE p=:ptr");
        return sb;
    }
    
    @Override
    protected Vector executeReadQuery(Pointer p, StringBuffer sb) {
        org.hibernate.Query query = s.createQuery(sb.toString());
        query.setCacheable(false);
        query.setParameter("ptr", new Integer((int) ((Pointer) p).longValue()), Hibernate.INTEGER);
        
        Vector v = new Vector(query.list());
        
        t.commit();
        
        return v;
    }

    

    @Override
    protected int executeUpdate(String type, String set, String where, Object args) {
        
        // in the current implementation, executeUpdate is also used to execute delete-s, depending on the value of "set"
        
        String hql = new String();
        
        HibernateUtils utils = new HibernateUtils();
        
        // I have no idea if giving the type directly will work...
        if(set == null) {
            hql = "DELETE FROM "+type+" WHERE "+where;
        } else {
            hql = "UPDATE "+type+" SET "+set+" WHERE "+where;
        }
        System.out.println("HQL: "+hql);
        
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
        throw new MakumbaError("Not implemented");
    }

    @Override
    public Vector executeQuery(String OQL, Object parameterValues) {
        throw new MakumbaError("Not implemented");
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

}
