package org.makumba.db.hibernate;

import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.NameResolver;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.db.TransactionImplementation;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryAnalysisProvider;
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
    
    private String dataSource;
    
    private NameResolver nr = new NameResolver();
    
    public HibernateTransaction(TransactionProviderInterface tp) {
        super(tp);
    }
    
    public HibernateTransaction(String dataSource, DataDefinitionProvider ddp, TransactionProviderInterface tp) {
        this(tp);
        this.dataSource = dataSource;
        this.ddp = ddp;
        this.s = ((SessionFactory) ((HibernateTransactionProvider)tp).getHibernateSessionFactory(dataSource)).openSession();
        s.setCacheMode(CacheMode.IGNORE);
        beginTransaction();
    }

    @Override
    public void close() {
        t.commit();
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
        sb.append(" FROM " + nr.arrowToDoubleUnderscore(p.getType()) + " p WHERE p.id=?");
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
        
        // I have no idea if giving the type directly will work...
        if(set == null) {
            hql = "DELETE FROM "+type.replaceAll("->", "__")+" WHERE "+where;
        } else {
            hql = "UPDATE "+type.replaceAll("->", "__")+" SET "+set+" WHERE "+where;
        }
        //System.out.println("HQL: "+hql);
        
        org.hibernate.Query q = s.createQuery(hql);
        q.setCacheable(false);
        
        // FIXME this needs type analysis to accept e.g. Pointers in String (external) form
        
        // FIXME a wild quess to detect whether the query has positional or named parameters
        if(set!=null && set.indexOf('?')!=-1||where!=null && where.indexOf('?')!=-1){
            Object[] argsArray = treatParam(args);
            for(int i=0; i<argsArray.length; i++) {
                q.setParameter(i, weaklyTreatParamType(argsArray[i]));
            }
        }
        else{
            Map<String, Object> args1= paramsToMap(args);
            for(Iterator<String> i= args1.keySet().iterator(); i.hasNext();){
                String key= i.next();
                q.setParameter(key, weaklyTreatParamType(args1.get(key)));

            }
        }
                
        
        return q.executeUpdate();
    }

    private Object weaklyTreatParamType(Object object) {
        if(object instanceof Pointer)
            return new Integer(((Pointer)object).getId());
        return object;
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

    
    /**
     * Executes a query with the given parameters.
     * @param query the HQL query
     * @param args the parameters of the query. Can be a Map containing named parameters, or a Vector, Object[] or Object for not named parameters.
     * @param offset the offset from which the results should be returned
     * @param limit the maximum number of results to be returned
     * @return a Vector of Dictionaries containing the results
     */
    @Override
    public Vector executeQuery(String query, Object args, int offset, int limit) {
        return execute(query, args, offset, limit);
    }

    /**
     * Executes a query with the given parameters.
     * @param query the HQL query
     * @param args the parameters of the query. Can be a Map containing named parameters, or a Vector, Object[] or Object for not named parameters.
     * @return a Vector of Dictionaries containing the results
     */
    @Override
    public Vector executeQuery(String query, Object parameterValues) {
        return execute(query, parameterValues, 0, -1);
    }
    
    public Vector execute(String query, Object args, int offset, int limit) {
        MakumbaSystem.getLogger("hibernate.query").fine("Executing hibernate query " + query);
        QueryAnalysisProvider qap= null;
        try {
                qap=(QueryAnalysisProvider) Class.forName(HQLQueryProvider.HQLQUERY_ANALYSIS_PROVIDER).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        QueryAnalysis analyzer = qap.getQueryAnalysis(query);

        DataDefinition dataDef = analyzer.getProjectionType();
        DataDefinition paramsDef = analyzer.getParameterTypes();
        
        // check the query for correctness (we do not allow "select p from Person p", only "p.id")
        for (int i = 0; i < dataDef.getFieldNames().size(); i++) {
            FieldDefinition fd = dataDef.getFieldDefinition(i);
            if (fd.getType().equals("ptr")) { // we have a pointer
                if (!(fd.getDescription().equalsIgnoreCase("ID"))) {
                    throw new ProgrammerError("Invalid HQL query - you must not select the whole object '"
                            + fd.getDescription() + "' in the query '" + query + "'!\nYou have to select '"
                            + fd.getDescription() + ".id' instead.");
                }
            }
        }
    
        // workaround for Hibernate bug HHH-2390
        // see http://opensource.atlassian.com/projects/hibernate/browse/HHH-2390
        query = analyzer.getPreProcessedQuery(query);

        org.hibernate.Query q = s.createQuery(query);

        q.setCacheable(false); // we do not cache queries

        q.setFirstResult(offset);
        if (limit != -1) { // limit parameter was specified
            q.setMaxResults(limit);
        }
        if (args != null && args instanceof Map) {
            setNamedParameters((Map)args, paramsDef, q);
        } else if(args != null) {
            setOrderedParameters(args, paramsDef, q);
        }

        Vector results = getConvertedQueryResult(analyzer, q.list());
        return results;
    }


    /**
     * TODO: find a way to not fetch the results all by one, but row by row, to reduce the memory used in both the
     * list returned from the query and the Vector composed out of.
     * see also bug
     *  
     * @param analyzer
     * @param list
     * @return
     */
    private Vector getConvertedQueryResult(QueryAnalysis analyzer, List list) {
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
                            resultFields[j] = new HibernatePointer(ddName, ((Pointer) resultFields[j]).getId());
                        } else if (resultFields[j] instanceof Integer) { // we have an integer
                            resultFields[j] = new HibernatePointer(ddName, ((Integer) resultFields[j]).intValue());
                        } else if(resultFields[j] instanceof Long) { // we have a Long
                            resultFields[j] = new HibernatePointer(ddName, (Long)resultFields[j]);
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

    private void setOrderedParameters(Object parameterValues, DataDefinition paramsDef, org.hibernate.Query q) {
        Object[] argsArray = treatParam(parameterValues);
        for(int i=0; i<argsArray.length; i++) {
            
            Object paramValue = argsArray[i];
            
            FieldDefinition paramDef = paramsDef.getFieldDefinition(i);
            
            if (paramValue instanceof Date) {
                q.setDate(i, (Date)paramValue);
            } else if (paramValue instanceof Integer) {
                q.setInteger(i, (Integer)paramValue);
            } else if (paramValue instanceof Pointer) {
                q.setParameter(i, new Integer(((Pointer)argsArray[i]).getId()));
            } else { // we have any param type (most likely String)
                if(paramDef != null) {
                    if(paramDef.getIntegerType()==FieldDefinition._ptr && paramValue instanceof String){
                        Pointer p= new Pointer(paramDef.getPointedType().getName(), (String)paramValue);
                        q.setInteger(i, new Integer((int) p.longValue()));
                    } else {
                      q.setParameter(i, paramValue);
                    } 
                } else {
                    q.setParameter(i, paramValue);
                }
            }
        }
    }
    

    private void setNamedParameters(Map args, DataDefinition paramsDef, org.hibernate.Query q) {
        String[] queryParams = q.getNamedParameters();
        for (int i = 0; i < queryParams.length; i++) {
            String paramName = queryParams[i];               
            Object paramValue = args.get(paramName);
            
            FieldDefinition paramDef= paramsDef.getFieldDefinition(paramName);
            
            //FIXME: check if the type of the actual parameter is in accordance with paramDef
            if (paramValue instanceof Vector) {
                q.setParameterList(paramName, (Collection) paramValue);
            } else if (paramValue instanceof Date) {
                q.setParameter(paramName, paramValue, Hibernate.DATE);
            } else if (paramValue instanceof Integer) {
                q.setParameter(paramName, paramValue, Hibernate.INTEGER);
            } else if (paramValue instanceof Pointer) {
                q.setParameter(paramName, new Integer(((Pointer) paramValue).getId()),
                    Hibernate.INTEGER);
            } else { // we have any param type (most likely String)
                if(paramDef.getIntegerType()==FieldDefinition._ptr && paramValue instanceof String){
                    Pointer p= new Pointer(paramDef.getPointedType().getName(), (String)paramValue);
                    q.setParameter(paramName, new Integer(p.getId()),
                        Hibernate.INTEGER);
                }else
                    q.setParameter(paramName, paramValue);
            }
        }
    }
    
    @Override
    public int insertFromQuery(String type, String OQL, Object parameterValues) {
        throw new MakumbaError("Not implemented");
    }

    @Override
    public String transformTypeName(String name) {
        
        return nr.arrowToDoubleUnderscore(name);
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
    
    @Override
    public String getDataSource() {
        return this.dataSource;
    }
    

    public org.hibernate.Transaction beginTransaction() {
        return this.t = s.beginTransaction();
    }
}
