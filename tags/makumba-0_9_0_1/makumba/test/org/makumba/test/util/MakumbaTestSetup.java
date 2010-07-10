package org.makumba.test.util;

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

        Transaction db = getTransaction();
        if (db == null) {
            return;
        }
        testData.insertLanguages(db);
        testData.insertPerson(db);

        /*
         * This is a dummy select, so the test_Person__charSet_ is mentioned in the client side part of the tests. If
         * this is not done, the server side and the client side will attempt to insert the same primary key in the
         * catalog table (because they use the same DBSV, because they use the same database connection file).
         * The last table that an insert is going to be attempted on needs to be queried for here, this may change if more tables are 
         * added.
         */
        // String query = "SELECT p.extraData.something FROM test.Person p WHERE 1=0";
        // String query1 = "SELECT l.name FROM test.Person p, p.address a, a.languages l WHERE 1=0";

        String query2 = "SELECT count(*) FROM test.Person p, p.charSet c WHERE 1=0";
        db.executeQuery(query2, null);

        db.close();
    }

    @Override
    public void tearDown() {

        Transaction db = getTransaction();
        if (db == null) {
            return;
        }
        testData.deletePersonsAndIndividuals(db);
        testData.deleteLanguages(db);
        db.close();

        System.err.println("cleaning caches");
        NamedResources.cleanStaticCache("Databases open");
    }

    public Transaction getTransaction() {
        if (transactionProviderType == null) {
            return null;
        }
        if (transactionProviderType.equals("oql")) {
            return TransactionProvider.getInstance().getConnectionToDefault();
        } else if (transactionProviderType.equals("hql")) {
            return HibernateTransactionProvider.getInstance().getConnectionTo("testDatabaseHibernate");
        }
        return null;
    }

}
