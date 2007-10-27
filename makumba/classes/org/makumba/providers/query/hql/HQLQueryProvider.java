package org.makumba.providers.query.hql;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.hibernate.CacheMode;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.ProgrammerError;
import org.makumba.Text;
import org.makumba.commons.ArrayMap;
import org.makumba.commons.Configuration;
import org.makumba.commons.NamedResourceFactory;
import org.makumba.commons.NamedResources;
import org.makumba.db.hibernate.HibernatePointer;
import org.makumba.db.hibernate.hql.HqlAnalyzer;
import org.makumba.db.sql.SQLPointer;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

public class HQLQueryProvider extends QueryProvider {

    private Session session;

    private SessionFactory sf;

    private Transaction transaction;
    
    private Configuration config = new Configuration();
    
    private TransactionProvider tp;
    
    public static int parsedHqlQueries = NamedResources.makeStaticCache("Hibernate HQL parsed queries",
    new NamedResourceFactory() {
        private static final long serialVersionUID = 1L;
    
        protected Object makeResource(Object nm, Object hashName) throws Exception {
            return new HqlAnalyzer((String) nm);
        }
    }, true);

    @Override
    public void init(String db) {
        super.init(db);
        tp = new TransactionProvider(config);
        sf = (SessionFactory) tp.getHibernateSessionFactory(db);
        // FIXME: we might want to open the session in a constructor, to re-use it for more than one exection
        session = sf.openSession();
        session.setCacheMode(CacheMode.IGNORE);
        transaction = session.beginTransaction();
    }

    @Override
    public Vector execute(String query, Map args, int offset, int limit) throws LogicException {
        MakumbaSystem.getLogger("hibernate.query").fine("Executing hibernate query " + query);

        HqlAnalyzer analyzer = HQLQueryProvider.getHqlAnalyzer(query);
        DataDefinition dataDef = analyzer.getProjectionType();
        DataDefinition paramsDef = analyzer.getParameterTypes();

        // check the query for correctness (we do not allow "select p from Person p", only "p.id")
        for (int i = 0; i < dataDef.getFieldNames().size(); i++) {
            FieldDefinition fd = dataDef.getFieldDefinition(i);
            if (fd.getType().equals("ptr")) { // we have a pointer
                if (!(fd.getDescription().equalsIgnoreCase("ID") || fd.getDescription().startsWith("hibernate_"))) {
                    throw new ProgrammerError("Invalid HQL query - you must not select the whole object '"
                            + fd.getDescription() + "' in the query '" + query + "'!\nYou have to select '"
                            + fd.getDescription() + ".id' instead.");
                }
            }
        }

        // workaround for Hibernate bug HHH-2390
        // see http://opensource.atlassian.com/projects/hibernate/browse/HHH-2390
        query = analyzer.getHackedQuery(query);

         Query q = session.createQuery(query);

        q.setCacheable(false); // we do not cache queries

        q.setFirstResult(offset);
        if (limit != -1) { // limit parameter was specified
            q.setMaxResults(limit);
        }
        if (args != null) {
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
                    q.setParameter(paramName, new Integer((int) ((Pointer) paramValue).longValue()),
                        Hibernate.INTEGER);
                } else { // we have any param type (most likely String)
                    if(paramDef.getIntegerType()==FieldDefinition._ptr && paramValue instanceof String){
                        Pointer p= new Pointer(paramDef.getPointedType().getName(), (String)paramValue);
                        q.setParameter(paramName, new Integer((int) p.longValue()),
                            Hibernate.INTEGER);
                    }else
                        q.setParameter(paramName, paramValue);
                }
            }
        }

        // TODO: find a way to not fetch the results all by one, but row by row, to reduce the memory used in both the
        // list returned from the query and the Vector composed out of.
        // see also bug
        List list = q.list();
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
                            throw new org.makumba.LogicException(
                                    "Internal Makumba error: Detected an unknown type returned by a query. "
                                            + "The projection index is " + j + ", the result class is "
                                            + resultFields[j].getClass() + ", it's content " + "is '" + resultFields[j]
                                            + "'and type analysis claims its type is " + fd.getPointedType().getName(),
                                    true);
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
    public void close() {
        if (session != null) {
            transaction.commit();
            session.close();
        }
    }

    /**
     * Method for testing the query runner outside a JSP
     */
    public static void main(String[] args) throws LogicException {
        Configuration config = new Configuration();
        
        TransactionProvider tp = new TransactionProvider(config);

        HQLQueryProvider qr = new HQLQueryProvider();
        qr.init("test/localhost_mysql_makumba");
        
        //populateDatabase(getDB());
        
        
        

        Vector v = new Vector();
        v.add(new Integer(1));
        v.add(new Integer(2));
        v.add(new Integer(3));
        v.add(new Integer(4));
        Map params = new HashMap();
        params.put("date", new Timestamp(new GregorianCalendar(1970, 1, 1).getTimeInMillis()));
        params.put("name", "Cristian");
        params.put("someInt", new Integer(1));
        params.put("someSet", v);
        params.put("testPerson", new SQLPointer("test.Person", 345678));
        params.put("someDouble", new Double(2.0));

        String query1 = "SELECT p.id as ID, p.name as name, p.surname as surname, p.birthdate as date, p.T_shirt as shirtSize FROM general.Person p where p.name = :name AND p.birthdate is not null AND p.birthdate > :date AND p.T_shirt = :someInt";
        String query2 = "SELECT p.id as ID, p.name as name, p.surname as surname, p.birthdate as date, p.T_shirt as shirtSize FROM general.Person p where p.name = :name AND p.birthdate is not null AND p.birthdate > :date and p.T_shirt in (:someSet) order by p.surname DESC";
        String query3 = "SELECT e.subject as subject, e.spamLevel AS spamLevel from general.archive.Email e WHERE e.spamLevel = :someDouble";
        String query4 = "SELECT case when 1>2 then 1.5 else 2.0 end, i.id FROM test.Individual i";
        String query5 = "SELECT lbg.id as col0, history.id as col1, history.status as col2, history.event.start as col3 from best.internal.Lbg lbg join lbg.membershipHistory history order by col3 DESC";
        String query6 = "SELECT lbg.id as col0, lbg.name As col1, lbg.id AS col2, lbg.name aS col3 from best.internal.Lbg lbg order by col3, col2,col1 DESC";
        String query7 = "SELECT p.id AS ID, p.driver AS col3, p.birthdate AS col4 FROM test.Person p";
        String query8 = "SELECT 1 from test.Person p join p.indiv i WHERE i.name = 'john'";
        String query9 = "SELECT p.id from test.Person p WHERE p = :testPerson";
        String query10 = "SELECT p.indiv.name FROM test.Person p WHERE p.gender = 1";
        String query11 = "SELECT p.indiv.person.indiv.name FROM test.Person p WHERE p.gender = 1";
        String query12 = "SELECT myIndiv.person.indiv.name FROM test.Person p join p.indiv as myIndiv";

        String[] queries = new String[] { query8, query7 };
        /*for (int i = 0; i < queries.length; i++) {
            System.out.println("Query " + queries[i] + " ==> \n"
                    + printQueryResults(qr.execute(queries[i], params, 0, 50)) + "\n\n");
        }*/
        System.out.println("Query  ==> \n"
            + printQueryResults(qr.execute(query12, params, 0, 50)) + "\n\n");
    }
    
    static Pointer person;
    static Pointer brother;
    static Pointer address;
    static Dictionary pc;
    static Vector v;
    static String readPerson = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.gender AS gender, p.uniqChar AS uniqChar, p.uniqInt AS uniqInt, p.birthdate AS birthdate, p.weight AS weight, p.TS_modify AS TS_modify, p.TS_create AS TS_create, p.comment AS comment, a.description AS description, a.email AS email, a.usagestart AS usagestart FROM test.Person p, p.address a WHERE p= $1";
    static ArrayList languages = new ArrayList();
    static Object[][] languageData = { { "English", "en" }, { "French", "fr" },
            { "German", "de" }, { "Italian", "it" }, { "Spanish", "sp" } };
    
    private static boolean populated = false;
    
    private static void populateDatabase(org.makumba.Transaction db) {
        if(populated) return;
        populated = true;
        
        languages.clear();
        Dictionary language = new Hashtable();
        for (int i = 0; i < languageData.length; i++) {
            language.put("name", languageData[i][0]);
            language.put("isoCode", languageData[i][1]);
            languages.add(db.insert("test.Language", language));
        }  
        
        Properties p = new Properties();
        
        p.put("indiv.name", "bart");
        brother=db.insert("test.Person", p);

        p.clear();
        p.put("indiv.name", "john");
        
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(1977, 2, 5);
        Date birthdate = c.getTime();
        p.put("birthdate", birthdate);
                
        p.put("uniqDate", birthdate);
        p.put("gender", new Integer(1));
        p.put("uniqChar", new String("testing \" character field"));
        
        p.put("weight", new Double(85.7d));
        
        p.put("comment", new Text("This is a text field. It's a comment about this person."));

        p.put("uniqInt", new Integer(255));             
        
        Vector intSet = new Vector();
        intSet.addElement(new Integer(1));
        intSet.addElement(new Integer(0));
        p.put("intSet", intSet);

        p.put("brother", brother);
        p.put("uniqPtr", languages.get(0));
        person = db.insert("test.Person", p);
        
        p.clear();
        p.put("description", "");
        p.put("usagestart", birthdate);
        p.put("email", "email1");
        System.out.println(address=db.insert(person, "address", p));
        
                  
    }
    
    private static org.makumba.Transaction getDB() {
        Configuration config = new Configuration();
        TransactionProvider tp = new TransactionProvider(config);
        return tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
    }

    public static String printQueryResults(Vector v) {
        String result = "";
        for (int i = 0; i < v.size(); i++) {
            result += "Row " + i + ":" + v.elementAt(i) + "\n";
        }
        return result;
    }

    @Override
    public QueryAnalysis getQueryAnalysis(String query) {
        return HQLQueryProvider.getHqlAnalyzer(query);
    }

    @Override
    public String getPrimaryKeyNotation(String label) {
        // this is specific to Hibernate: we add '.id' in order to get the id as in makumba
        if (label.indexOf('.') == -1)
            label += ".id";
        return label;
    }

    @Override
    public boolean selectGroupOrOrderAsLabels() {
        return false;
    }

    @Override
    public FieldDefinition getAlternativeField(DataDefinition dd, String fn) {
        if (fn.equals("id"))
            return dd.getFieldDefinition(dd.getIndexPointerFieldName());
        else if (fn.startsWith("hibernate_"))
            return dd.getFieldDefinition(fn.substring("hibernate_".length()));
        return null;

    }

    @Override
    public String transformPointer(String ptrExpr, String fromSection) {
        if (getQueryAnalysis("SELECT " + ptrExpr + " as gigi FROM " + fromSection).getProjectionType().getFieldDefinition(
            "gigi").getType().equals("ptr")) {
            if(ptrExpr.endsWith(".id"))  // FIXME query type analysis does not return ptrIndex for label.id but clearly there we don't need the hibernate_id 
                return ptrExpr;
            int dot = ptrExpr.lastIndexOf('.') + 1;
            return ptrExpr.substring(0, dot) + "hibernate_" + ptrExpr.substring(dot);
        }
        return ptrExpr;
    }

    /**
     * Get the Hibernate HQL analyzer for the indicated query
     */
    static public HqlAnalyzer getHqlAnalyzer(String hqlQuery) {
        return (HqlAnalyzer) NamedResources.getStaticCache(HQLQueryProvider.parsedHqlQueries).getResource(hqlQuery);
    }
}
