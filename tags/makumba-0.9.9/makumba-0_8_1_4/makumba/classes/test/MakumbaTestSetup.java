package test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.db.hibernate.HibernateTransactionProvider;
import org.makumba.providers.TransactionProvider;

public class MakumbaTestSetup extends TestSetup {

    public static final String namePersonIndivName_Bart = "bart";

    public static final String namePersonIndivName_John = "john";

    private static Pointer address;

    private static ArrayList<Pointer> languages = new ArrayList<Pointer>();

    private static String[][] languageData = { { "English", "en" }, { "French", "fr" }, { "German", "de" },
            { "Italian", "it" }, { "Spanish", "sp" } };

    public static Date birthdate;

    public static final Integer uniqInt = new Integer(255);

    public static final String uniqChar = new String("testing \" character field");

    public final static String namePersonIndivName_AddToNew = "addToNewPerson";

    public static final String namePersonIndivName_FirstBrother = "firstBrother";

    public static final String namePersonIndivName_SecondBrother = "secondBrother";

    public static final String namePersonIndivName_StepBrother = "stepBrother";

    /** All names of individuals to be deleted. bart is referenced by john, so we delete him afterwards. */
    private static final String[] namesPersonIndivName = { namePersonIndivName_John, namePersonIndivName_Bart,
            namePersonIndivName_AddToNew, namePersonIndivName_SecondBrother, namePersonIndivName_FirstBrother,
            namePersonIndivName_StepBrother };

    private String transactionProviderType;

    public MakumbaTestSetup(Test test, String transactionProviderType) {
        super(test);
        this.transactionProviderType = transactionProviderType;
    }

    protected void setUp() {
        TransactionProvider tp = null;
        Transaction db = null;

        if (transactionProviderType.equals("oql")) {
            tp = TransactionProvider.getInstance();
            db = tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
        } else if (transactionProviderType.equals("hql")) {
            tp = new TransactionProvider(new HibernateTransactionProvider());
            db = tp.getConnectionTo(tp.getDataSourceName("test/testHibernateDatabase.properties"));
        }

        insertLanguages(db);
        insertPerson(db);

        /*
         * Just a dummy select, so the test_Person__extraData_ is mentioned in the client side part of the tests. If
         * this is not done, the server side and the client side will attempt to insert the same primary key in the
         * catalog table (because they use the same DBSV, because they use the same database connection file).
         */
        String query = "SELECT p.extraData.something FROM test.Person p WHERE 1=0";
        db.executeQuery(query, null);
        db.close();
    }

    protected void insertPerson(Transaction db) {
        Hashtable<String, Object> p = new Hashtable<String, Object>();

        p.put("indiv.name", namePersonIndivName_Bart);
        Pointer brother = db.insert("test.Person", p);

        p.clear();
        p.put("indiv.name", namePersonIndivName_John);

        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(1977, 2, 5);
        birthdate = c.getTime();
        p.put("birthdate", birthdate);

        p.put("uniqDate", birthdate);
        p.put("gender", new Integer(1));
        p.put("uniqChar", uniqChar);

        p.put("weight", new Double(85.7d));

        p.put("comment", new Text("This is a text field. It's a comment about this person."));

        p.put("uniqInt", uniqInt);

        Vector<Integer> intSet = new Vector<Integer>();
        intSet.addElement(new Integer(1));
        intSet.addElement(new Integer(0));
        p.put("intSet", intSet);

        p.put("brother", brother);
        p.put("uniqPtr", languages.get(0));
        Pointer person = db.insert("test.Person", p);

        p.clear();
        p.put("description", "");
        p.put("usagestart", birthdate);
        p.put("email", "email1");
        address = db.insert(person, "address", p);

    }

    protected void deletePersonsAndIndividuals(Transaction db) {
        db.delete(address);
        for (int i = 0; i < namesPersonIndivName.length; i++) {
            String query = "SELECT p AS p, p.indiv as i FROM test.Person p WHERE p.indiv.name="+(transactionProviderType.equals("oql")?"$1":"?");
            Vector<Dictionary<String, Object>> v = db.executeQuery(query, namesPersonIndivName[i]);
            if (v.size() > 0) {
                db.delete((Pointer) v.firstElement().get("p"));
                db.delete((Pointer) v.firstElement().get("i"));
            }
        }
    }

    protected void insertLanguages(Transaction db) {
        languages.clear();
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        for (int i = 0; i < languageData.length; i++) {
            p.put("name", languageData[i][0]);
            p.put("isoCode", languageData[i][1]);
            languages.add(db.insert("test.Language", p));
        }
    }

    protected void deleteLanguages(Transaction db) {
        for (int i = 0; i < languages.size(); i++)
            db.delete((Pointer) languages.get(i));
    }

    public void tearDown() {
        // do your one-time tear down here!
        TransactionProvider tp = null;
        Transaction db = null;

        if (transactionProviderType.equals("oql")) {
            tp = TransactionProvider.getInstance();
            db = tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
        } else if (transactionProviderType.equals("hql")) {
            tp = new TransactionProvider(new HibernateTransactionProvider());
            db = tp.getConnectionTo(tp.getDataSourceName("test/testHibernateDatabase.properties"));
        }

        deletePersonsAndIndividuals(db);
        deleteLanguages(db);
        db.close();
    }

}
