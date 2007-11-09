package org.makumba.providers.query.hql;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.makumba.DataDefinition;
import org.makumba.FieldDefinition;
import org.makumba.LogicException;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.commons.Configuration;
import org.makumba.db.hibernate.HibernateTransaction;
import org.makumba.db.sql.SQLPointer;
import org.makumba.providers.QueryAnalysis;
import org.makumba.providers.QueryProvider;
import org.makumba.providers.TransactionProvider;

public class HQLQueryProvider extends QueryProvider {

    private org.makumba.Transaction transaction;
    
    private Configuration config = new Configuration();
    
    private TransactionProvider tp;

    @Override
    public void init(String db) {
        super.init(db);
        config.setDefaultTransactionProvider("org.makumba.db.hibernate.HibernateTransactionProvider");
        tp = new TransactionProvider(config);
        transaction = tp.getConnectionTo(db);
    }

    @Override
    public Vector execute(String query, Map args, int offset, int limit) throws LogicException {
        return transaction.executeQuery(query, args, offset, limit);
    }

    @Override
    public void close() {
        transaction.close();
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
        return HibernateTransaction.getHqlAnalyzer(query);
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
     * Method for testing the query runner outside a JSP
     */
    public static void main(String[] args) throws LogicException {
        Configuration config = new Configuration();
        
        config.setTransactionProvider("org.makumba.db.hibernate.HibernateTransactionProvider");
        TransactionProvider tp = new TransactionProvider(config);

        HQLQueryProvider qr = new HQLQueryProvider();
        qr.init("test/localhost_mysql_makumba");
        
        org.makumba.Transaction t = tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
        
        populateDatabase(t);
        
        
        

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
    
}
