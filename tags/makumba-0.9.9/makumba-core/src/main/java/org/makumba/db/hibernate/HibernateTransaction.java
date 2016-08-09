package org.makumba.db.hibernate;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.type.NullableType;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.HibernateSFManager;
import org.makumba.InvalidValueException;
import org.makumba.MakumbaError;
import org.makumba.MakumbaSystem;
import org.makumba.NullObject;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Transaction;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.NameResolver;
import org.makumba.commons.NullNameResolver;
import org.makumba.commons.RuntimeWrappedException;
import org.makumba.commons.StacktraceUtil;
import org.makumba.db.NativeQuery;
import org.makumba.db.NativeQuery.ParameterHandler;
import org.makumba.db.TransactionImplementation;
import org.makumba.providers.DataDefinitionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * Hibernate-specific implementation of a {@link Transaction}
 * 
 * @author Manuel Bernhardt <manuel@makumba.org>
 * @version $Id: HibernateTransaction.java,v 1.1 02.11.2007 14:08:53 Manuel Exp $
 */
@SuppressWarnings("deprecation")
public class HibernateTransaction extends TransactionImplementation {

    public org.hibernate.Transaction t;

    private boolean useCurrentSession = false;

    public Session s;

    private DataDefinitionProvider ddp;

    private String dataSource;

    // private static NameResolver nr = new NameResolver();

    public HibernateTransaction(TransactionProvider tp) {
        super(tp);
    }

    public HibernateTransaction(String dataSource, DataDefinitionProvider ddp,
            HibernateTransactionProvider hibernateTransactionProvider) {
        this(hibernateTransactionProvider);
        this.dataSource = dataSource;
        this.ddp = ddp;

        useCurrentSession = org.makumba.providers.Configuration.getDataSourceConfiguration(dataSource).containsKey(
            HibernateSFManager.HIBERNATE_CURRENT_SESSION_CONTEXT);

        if (useCurrentSession) {
            this.s = ((SessionFactory) hibernateTransactionProvider.getHibernateSessionFactory(dataSource)).getCurrentSession();
        } else {
            this.s = ((SessionFactory) hibernateTransactionProvider.getHibernateSessionFactory(dataSource)).openSession();
            s.setCacheMode(CacheMode.IGNORE);
        }
        beginTransaction();
    }

    @Override
    public void close() {
        setContext(null);
        if (t.isActive()) {
            t.commit();
        }
        if (!useCurrentSession) {
            s.close();
        }

    }

    @Override
    public void commit() {
        if (t.isActive()) {
            t.commit();
        }
        t = s.beginTransaction();
    }

    @Override
    protected StringBuffer writeReadQuery(Pointer p, Enumeration<String> e) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT ");
        String separator = "";
        while (e.hasMoreElements()) {
            Object o = e.nextElement();
            DataDefinition r = ddp.getDataDefinition(p.getType());
            if (!(o instanceof String)) {
                throw new org.makumba.NoSuchFieldException(r,
                        "Dictionaries passed to makumba DB operations should have String keys. Key <" + o
                                + "> is of type " + o.getClass() + r.getName());
            }
            FieldDefinition fieldDefinition = r.getFieldDefinition((String) o);
            if (fieldDefinition == null) {
                throw new org.makumba.NoSuchFieldException(r, (String) o);
            }
            String s = (String) o;
            sb.append(separator).append("p.");
            if (fieldDefinition.getType().equals("ptrIndex")) {
                sb.append("id");
            } else {
                sb.append(s);
                if (fieldDefinition.getType().startsWith("ptr")) {
                    sb.append(".id");
                }
            }

            sb.append(" as ").append(s);
            separator = ",";
        }
        sb.append(" FROM " + NameResolver.arrowToDoubleUnderscore(p.getType()) + " p WHERE p.id=?");
        return sb;
    }

    @Override
    protected Vector<Dictionary<String, Object>> executeReadQuery(Pointer p, StringBuffer sb) {

        return executeQuery(sb.toString(), p);
    }

    @Override
    protected int executeUpdate(String type, String set, String where, Object args) {

        // in the current implementation, executeUpdate is also used to execute delete-s, depending on the value of
        // "set"

        String hql = new String();

        // I have no idea if giving the type directly will work...
        if (set == null) {
            hql = "DELETE FROM " + type.replaceAll("->", "__") + " WHERE " + where;
        } else {
            hql = "UPDATE " + type.replaceAll("->", "__") + " SET " + set + " WHERE " + where;
        }
        // System.out.println("HQL: "+hql);

        org.hibernate.Query q = s.createQuery(hql);
        q.setCacheable(false);

        // FIXME this needs type analysis to accept e.g. Pointers in String (external) form

        // FIXME a wild quess to detect whether the query has positional or named parameters
        if (set != null && set.indexOf('?') != -1 || where != null && where.indexOf('?') != -1) {
            Object[] argsArray = treatParam(args);
            for (int i = 0; i < argsArray.length; i++) {
                q.setParameter(i, weaklyTreatParamType(argsArray[i]));
            }
        } else {
            Map<String, Object> args1 = paramsToMap(args);
            for (String key : args1.keySet()) {
                q.setParameter(key, weaklyTreatParamType(args1.get(key)));

            }
        }

        return q.executeUpdate();
    }

    private Object weaklyTreatParamType(Object object) {
        if (object instanceof Pointer) {
            Pointer p = (Pointer) object;
            // let's figure the correct type for this guy
            // if it is a generated class, it will be an integer
            for (String s : HibernateSFManager.getGeneratedClasses()) {
                if (s.equals(p.getType())) {
                    return new Integer(p.getId());
                }
            }

            // otherwise it may be a long
            Class<?> recordClass = null;
            try {
                recordClass = Class.forName(HibernateSFManager.getFullyQualifiedName(p.getType()));

                String idMethodName = "getprimaryKey";

                if (!HibernateCRUDOperationProvider.isGenerated(recordClass)) {
                    idMethodName = "getId";
                }

                Method m = recordClass.getMethod(idMethodName, new Class[] {});
                if (HibernateCRUDOperationProvider.isInteger(m.getReturnType().getName())) {
                    return new Integer(p.getId());
                } else if (HibernateCRUDOperationProvider.isLong(m.getReturnType().getName())) {
                    return p.longValue();
                }

            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
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
        if (t.isActive()) {
            t.rollback();
        }
    }

    @Override
    public void unlock(String symbol) {
        throw new MakumbaError("Not implemented");

    }

    /**
     * Executes a query with the given parameters.
     * 
     * @param query
     *            the HQL query
     * @param args
     *            the parameters of the query. Can be a Map containing named parameters, or a Vector, Object[] or Object
     *            for not named parameters.
     * @param offset
     *            the offset from which the results should be returned
     * @param limit
     *            the maximum number of results to be returned
     * @return a Vector of Dictionaries containing the results
     */
    @Override
    public Vector<Dictionary<String, Object>> executeQuery(String query, Object args, int offset, int limit) {
        return execute(query, args, offset, limit);
    }

    /**
     * Executes a query with the given parameters.
     * 
     * @param query
     *            the HQL query
     * @param args
     *            the parameters of the query. Can be a Map containing named parameters, or a Vector, Object[] or Object
     *            for not named parameters.
     * @return a Vector of Dictionaries containing the results
     */
    @Override
    public Vector<Dictionary<String, Object>> executeQuery(String query, Object parameterValues) {
        return execute(query, parameterValues, 0, -1);
    }

    static Pattern namedParam = Pattern.compile("\\:[a-zA-Z]\\w*");

    public Vector<Dictionary<String, Object>> execute(String query, Object args, int offset, int limit) {
        MakumbaSystem.getLogger("hibernate.query").fine("Executing hibernate query " + query);

        NativeQuery nat = NativeQuery.getNativeQuery(query, "hql", null, new NullNameResolver());

        // QueryAnalysisProvider qap = QueryProvider.getQueryAnalzyer("oql");

        Map<String, Object> argsMap = paramsToMap(args);

        // analyze the query with MQL!
        // MqlQueryAnalysis analyzer = (MqlQueryAnalysis) qap.getQueryAnalysis(query);

        // analyzer.prepareForHQL();

        // MqlParameterTransformer paramTransformer = MqlParameterTransformer.getSQLQueryGenerator(analyzer, argsMap,
        // "hql");
        // query = paramTransformer.getTransformedQuery(new NullNameResolver(), argsMap);

        if (nat.getConstantValues() != null) {
            // no need to send the query to the sql engine
            return nat.getConstantResult(argsMap, offset, limit);
        }

        org.hibernate.Query q = s.createQuery(nat.getCommand(argsMap));

        q.setCacheable(false); // we do not cache queries

        q.setFirstResult(offset);
        if (limit != -1) { // limit parameter was specified
            q.setMaxResults(limit);
        }

        setOrderedParameters(nat, q, argsMap);

        Vector<Dictionary<String, Object>> results = null;
        try {
            results = getConvertedQueryResult(nat, q.list());

        } catch (Exception e) {

            throw new ProgrammerError("Error while trying to execute query " + q.getQueryString() + ":\n"
                    + StacktraceUtil.getStackTrace(e));
        }
        return results;
    }

    /**
     * TODO: find a way to not fetch the results all by one, but row by row, to reduce the memory used in both the list
     * returned from the query and the Vector composed out of. see also bug
     * 
     * @param analyzer
     * @param list
     * @return
     */
    private Vector<Dictionary<String, Object>> getConvertedQueryResult(NativeQuery nat, List<?> list) {
        Vector<Dictionary<String, Object>> results = new Vector<Dictionary<String, Object>>(list.size());

        // int i = 1;
        for (Object resultRow : list) {
            Object[] resultFields;
            if (!(resultRow instanceof Object[])) { // our query result has only one field
                resultFields = new Object[] { resultRow }; // we put it into an object[]
            } else { // our query had more results ==>
                resultFields = (Object[]) resultRow; // we had an object[] already
            }

            // process each field's result
            for (int j = 0; j < resultFields.length; j++) { //
                if (resultFields[j] != null) { // we add to the dictionary only fields with values in the DB
                    FieldDefinition fd = nat.getProjectionType().getFieldDefinition(j);
                    if (fd.getType().startsWith("ptr")) {
                        // we have a pointer
                        String ddName = fd.getPointedType().getName();
                        // FIXME: once we do not get dummy pointers from hibernate queries, take this out
                        if (resultFields[j] instanceof Pointer) { // we have a dummy pointer
                            resultFields[j] = new HibernatePointer(ddName, ((Pointer) resultFields[j]).getId());
                        } else if (resultFields[j] instanceof Integer) { // we have an integer
                            resultFields[j] = new HibernatePointer(ddName, ((Integer) resultFields[j]).intValue());
                        } else if (resultFields[j] instanceof Long) { // we have a Long
                            resultFields[j] = new HibernatePointer(ddName, (Long) resultFields[j]);
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
            Dictionary<String, Object> dic = new ArrayMap(nat.getKeyIndex(), resultFields);
            results.add(dic);
        }
        return results;
    }

    private void setOrderedParameters(NativeQuery nat, final org.hibernate.Query q, Map<String, Object> argsMap) {
        // Object[] argsArray = treatParam(parameterValues);

        nat.assignParameters(new ParameterHandler() {
            @Override
            public void handle(int i, FieldDefinition paramDef, Object paramValue) {
                try {
                    assignParameter(i, paramValue, paramDef, q);
                } catch (InvalidValueException e) {
                    q.setParameter(i, null);
                }
            }

        }, argsMap);

    }

    // private void setNamedParameters(Map<?, ?> args, DataDefinition paramsDef, org.hibernate.Query q) {
    // String[] queryParams = q.getNamedParameters();
    // for (String paramName : queryParams) {
    // Object paramValue = args.get(paramName);
    // FieldDefinition paramDef = paramsDef.getFieldDefinition(paramName);
    // assignParameter(paramName, paramValue, paramDef, q);
    //
    // }
    // }

    private void assignParameter(Object nameOrPosition, Object paramValue, FieldDefinition paramDef,
            org.hibernate.Query q) {
        // FIXME: check if the type of the actual parameter is in accordance with paramDef
        // FIXME actually we should never have this case given the Multiple attribute parametrizer of MQL
        // so probably we can trash this
        if (paramValue instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> lst = (List<Object>) paramValue;
            for (int i = 0; i < lst.size(); i++) {
                lst.set(i, checkPtr(paramDef, lst.get(i)));
            }

            if (nameOrPosition instanceof String) {
                q.setParameterList((String) nameOrPosition, (Collection<?>) paramValue);
            } else if (nameOrPosition instanceof Integer) {
                // FIXME
                // q.setParameterList((Integer)nameOrPosition, (Collection<?>) paramValue);
            } else {
                throw new MakumbaError("Wrong parameter name / position");
            }

        } else if (paramValue instanceof Date) {
            setParameter(nameOrPosition, paramValue, Hibernate.TIMESTAMP, q);
        } else if (paramValue instanceof Integer) {
            setParameter(nameOrPosition, paramValue, Hibernate.INTEGER, q);
        } else if (paramValue instanceof Pointer) {
            setParameter(nameOrPosition, new Integer(((Pointer) paramValue).getId()), Hibernate.INTEGER, q);
        } else if (paramValue instanceof NullObject) {
            NullObject n = (NullObject) paramValue;
            if (n.equals(Pointer.Null)) {
                setParameter(nameOrPosition, new Integer(-1), Hibernate.INTEGER, q);
            } else if (n.equals(Pointer.NullInteger)) {
                setParameter(nameOrPosition, new Integer(-1), Hibernate.INTEGER, q);
            } else {
                setParameter(nameOrPosition, null, null, q);
            }

        } else { // we have any param type (most likely String)
            boolean assigned = false;
            if (paramDef.getIntegerType() == FieldDefinition._ptr && paramValue instanceof String) {
                Pointer p = new Pointer(paramDef.getPointedType().getName(), (String) paramValue);
                setParameter(nameOrPosition, new Integer(p.getId()), Hibernate.INTEGER, q);
                assigned = true;
            } else if (paramDef.getIntegerType() == FieldDefinition._int) {
                Integer val = paramValue instanceof String ? Integer.parseInt((String) paramValue)
                        : (Integer) paramValue;
                setParameter(nameOrPosition, val, null, q);
                assigned = true;
            } else if (paramDef.getIntegerType() == FieldDefinition._intEnum) {
                if (paramValue instanceof String) {
                    // FIXME if no corresponding value is found, scream
                    for (int k = 0; k < paramDef.getEnumeratorSize(); k++) {
                        if (paramDef.getNameAt(k).equals(paramValue)) {
                            paramValue = paramDef.getIntAt(k);
                            break;
                        }
                    }
                }

                setParameter(nameOrPosition, paramValue, null, q);
                assigned = true;
            } else if (paramDef.getIntegerType() == FieldDefinition._real) {
                Double val = paramValue instanceof String ? Double.parseDouble((String) paramValue)
                        : (Double) paramValue;
                setParameter(nameOrPosition, val, null, q);
                assigned = true;
            } else {
                setParameter(nameOrPosition, paramValue, null, q);
                assigned = true;
            }

            if (!assigned) {
                throw new IllegalStateException("Could not assign paramter " + paramDef + " = " + paramValue);
            }
        }
    }

    private void setParameter(Object nameOrPosition, Object value, NullableType type, org.hibernate.Query q) {
        if (nameOrPosition instanceof String) {
            if (type != null) {
                q.setParameter((String) nameOrPosition, value, type);
            } else {
                q.setParameter((String) nameOrPosition, value);
            }
        } else if (nameOrPosition instanceof Integer) {
            if (type != null) {
                q.setParameter((Integer) nameOrPosition, value, type);
            } else {
                q.setParameter((Integer) nameOrPosition, value);
            }
        } else {
            throw new MakumbaError("Wrong parameter name / position");
        }

    }

    private Object checkPtr(FieldDefinition paramDef, Object paramValue) {
        if (paramDef.getIntegerType() == FieldDefinition._ptr && paramValue instanceof String) {
            return new Pointer(paramDef.getPointedType().getName(), (String) paramValue).getId();
        }
        return paramValue;
    }

    @Override
    public Vector<Pointer> insert(String type, Collection<Dictionary<String, Object>> data) {
        // TODO Auto-generated method stub
        return super.insert(type, data);
    }

    @Override
    protected int insertFromQueryImpl(String type, String OQL, Object parameterValues) {
        throw new MakumbaError("Not implemented");
    }

    @Override
    public String transformTypeName(String name) {

        return NameResolver.arrowToDoubleUnderscore(name);
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
    public String getSetJoinSyntax() {
        return "JOIN";
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

    protected Object[] treatParam(Object args) {
        if (args == null) {
            return new Object[] {};
        } else if (args instanceof List) {
            List<?> v = (List<?>) args;
            Object[] param = new Object[v.size()];
            v.toArray(param);
            return param;
        } else if (args instanceof Object[]) {
            return (Object[]) args;
        } else {
            Object p[] = { args };
            return p;
        }
    }
}
