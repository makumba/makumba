package org.makumba.test.component;

import java.util.Arrays;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.makumba.Pointer;
import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;
import org.makumba.test.MakumbaTestSetup;


/**
 * Tests methods that manipulate sets in the {@link Transaction} interface, specifically
 * {@link Transaction#readExternalSetValues(Pointer, String)} and
 * {@link Transaction#updateSet(Pointer, String, java.util.Collection, java.util.Collection)}.<br>
 * FIXME: maybe this should be together with {@link TableTest}
 * 
 * @author Rudolf Mayer
 * @version $Id: ExternalSetAPITest.java 5201 2010-05-23 15:39:32Z manuel_gay $
 */
public class ExternalSetAPITest extends TestCase {
    private static final String setName = "speaks";

    private Pointer person = null;

    private Vector<Pointer> speaks;

    private static MakumbaTestSetup setup;

    public static Test suite() {
        return setup = new MakumbaTestSetup(new TestSuite(ExternalSetAPITest.class), "oql");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        speaks = new Vector<Pointer>(setup.getTestData().languages);
        speaks.addAll(setup.getTestData().languages);
        person = setup.getTestData().brother;
    }

    public void testReadingExternalSet() {
        Transaction t = TransactionProvider.getInstance().getConnectionToDefault();
        try {
            // compare newly read with already known set elements
            assertEquals(t.readExternalSetValues(person, setName), speaks);
        } finally {
            if (t != null) {
                t.close();
            }
        }
    }

    public void testRemovingFromExternalSet() {
        Transaction t = TransactionProvider.getInstance().getConnectionToDefault();
        try {
            // remove speaks entry from the set in the database
            final Pointer removedLanguage = speaks.get(0);
            t.updateSet(person, setName, null, Arrays.asList(new Pointer[] { removedLanguage }));

            // remove also from the comparison object
            final Vector<Pointer> speaksNew = new Vector<Pointer>(speaks);
            speaksNew.remove(removedLanguage);

            assertEquals(t.readExternalSetValues(person, setName), speaksNew);
        } finally {
            if (t != null) {
                t.close();
            }
        }
    }

    public void testAddingToExternalSet() {
        Transaction t = TransactionProvider.getInstance().getConnectionToDefault();
        try {
            // add speaks entry back to the set
            final Pointer removedLanguage = speaks.get(0);
            t.updateSet(person, setName, Arrays.asList(new Pointer[] { removedLanguage }), null);

            // prepare the comparison object (needs a different order)
            final Vector<Pointer> speaksNew = new Vector<Pointer>(speaks);
            speaksNew.remove(removedLanguage);
            speaksNew.add(removedLanguage);

            assertEquals(t.readExternalSetValues(person, setName), speaksNew);
        } finally {
            if (t != null) {
                t.close();
            }
        }
    }
}
