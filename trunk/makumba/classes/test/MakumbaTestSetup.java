package test;

import junit.extensions.TestSetup;
import junit.framework.Test;

import org.makumba.Transaction;
import org.makumba.commons.NamedResources;
import org.makumba.db.hibernate.HibernateTransactionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * @version $Id: MakumbaTestSetup.java,v 1.1 5 May 2010 13:51:13 rudi Exp $
 */
public class MakumbaTestSetup extends TestSetup {

    private final String transactionProviderType;

    private final MakumbaTestData testData = new MakumbaTestData();

    public MakumbaTestSetup(Test test, String transactionProviderType) {
        super(test);
        this.transactionProviderType = transactionProviderType;
    }

    public MakumbaTestData getTestData() {
        return testData;
    }

    @Override
    protected void setUp() {

        TransactionProvider tp = null;
        Transaction db = null;

        if (transactionProviderType == null)
            return;
        if (transactionProviderType.equals("oql")) {
            System.err.println("cleaning caches");
            NamedResources.cleanStaticCache("Databases open");

            tp = TransactionProvider.getInstance();
            db = tp.getConnectionTo(tp.getDefaultDataSourceName());
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

    @Override
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
