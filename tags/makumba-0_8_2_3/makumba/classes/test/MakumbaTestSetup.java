package test;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.makumba.Transaction;
import org.makumba.db.hibernate.HibernateTransactionProvider;
import org.makumba.providers.TransactionProvider;

public class MakumbaTestSetup extends TestSetup {


    private String transactionProviderType;
    
    private MakumbaTestData testData = new MakumbaTestData();

    public MakumbaTestSetup(Test test, String transactionProviderType) {
        super(test);
        this.transactionProviderType = transactionProviderType;
    }
    
    protected void setUp() {
        
        TransactionProvider tp = null;
        Transaction db = null;

        if (transactionProviderType.equals("oql")) {
            tp = TransactionProvider.getInstance();
            db = tp.getConnectionTo("testDatabase");
        } else if (transactionProviderType.equals("hql")) {
            tp = HibernateTransactionProvider.getInstance();
            db = tp.getConnectionTo("testDatabaseHibernate");
        }

        testData.insertLanguages(db);
        testData.insertPerson(db);

        /*
         * Just a dummy select, so the test_Person__extraData_ is mentioned in the client side part of the tests. If
         * this is not done, the server side and the client side will attempt to insert the same primary key in the
         * catalog table (because they use the same DBSV, because they use the same database connection file).
         */
        String query = "SELECT p.extraData.something FROM test.Person p WHERE 1=0";
        db.executeQuery(query, null);
        db.close();
    }

    

    public void tearDown() {
        
        // do your one-time tear down here!
        TransactionProvider tp = null;
        Transaction db = null;

        if (transactionProviderType.equals("oql")) {
            tp = TransactionProvider.getInstance();
            db = tp.getConnectionTo("testDatabase");
        } else if (transactionProviderType.equals("hql")) {
            tp = HibernateTransactionProvider.getInstance();
            db = tp.getConnectionTo("testDatabaseHibernate");
        }

        testData.deletePersonsAndIndividuals(db);
        testData.deleteLanguages(db);
        db.close();
    }

}