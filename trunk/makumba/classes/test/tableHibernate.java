///////////////////////////////
//  Makumba, Makumba tag library
//  Copyright (C) 2000-2003  http://www.makumba.org
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//
//  -------------

package test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.db.hibernate.HibernateTransactionProvider;
import org.makumba.db.makumba.MakumbaTransactionProvider;
import org.makumba.providers.TransactionProvider;

/**
 * Testing table operations
 * 
 * @author Cristian Bogdan
 */
public class tableHibernate extends TestCase {

    static Transaction db;

    static long epsilon = 2000;

    public tableHibernate(String name) {
        super(name);
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        return new TestSuite(tableHibernate.class);
    }

    @Override
    public void setUp() {
        tp = HibernateTransactionProvider.getInstance();
        db = tp.getConnectionTo("testDatabaseHibernate");
    }

    @Override
    public void tearDown() {
        db.close();
    }

    private TransactionProvider tp;

    static Pointer ptr, ptr1;

    static Pointer fptr1, fptr2, fptr;

    static Date create;

    static String[] personFields = { "TS_modify", "TS_create", "extraData", "birthdate" };

    static String[] ptrOneFields = { "something" };

    static String[] subsetFields = { "description" };

    static Dictionary<String, Object> pc, pc1;

    static Date now;

    static Pointer ptrOne;

    static Pointer set1, set2;

    String readPerson = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.birthdate AS birthdate, p.TS_modify as TS_modify, p.TS_create as TS_create, p.extraData.something as something, p.extraData.id as extraData FROM test.Person p WHERE p.id= ?";

    String readPerson1 = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.birthdate AS birthdate, p.weight as weight, p.TS_modify as TS_modify, p.TS_create as TS_create, p.extraData.something as something, p.extraData.id as extraData, p.comment as comment, p.picture AS picture FROM test.Person p WHERE p.id = ?";

    String readPerson2 = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.birthdate AS birthdate, p.weight as weight, p.brother.id as brother, p.TS_modify as TS_modify, p.TS_create as TS_create, p.extraData.something as something, p.extraData.id as extraData, p.comment as comment, p.picture AS picture FROM test.Person p WHERE p.id= ?";

    String readIntSet = "SELECT i.enum_ as member FROM test.Person p JOIN p.intSet i WHERE p.id=? ORDER BY i.enum_";

    String readCharSet = "SELECT c.enum_ as member FROM test.Person p JOIN p.charSet c WHERE p.id=? ORDER BY c.enum_";

    static InputStream getExampleData() {
        try {
            return new BufferedInputStream(new FileInputStream("lib/core/antlr-2.7.6.jar".replace('/',
                File.separatorChar)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
     * public void testQueryValidMdds() { Vector v = org.makumba.MakumbaSystem.mddsInDirectory("test/validMdds"); Vector
     * errors = new Vector(); for (int i = 0; i < v.size(); i++) { try { Vector v1 =
     * db.executeQuery("SELECT t.id FROM test.validMdds." + (String) v.elementAt(i) + " t", null); Vector fields =
     * ddp.getDataDefinition( "test.validMdds." + (String) v.elementAt(i)) .getFieldNames(); String what = ""; for
     * (Enumeration e = fields.elements(); e.hasMoreElements();) { String fname = (String) e.nextElement(); String ftype
     * = ddp.getDataDefinition( "test.validMdds." + (String) v.elementAt(i)) .getFieldDefinition(fname).getDataType();
     * // System.out.println(fname+": "+ftype); if (ftype != null && !ftype.equals("null") && !ftype.startsWith("set"))
     * // skip setComplex // fields what = what + (what.length() > 0 ? ", " : "") + "t." + fname; } //
     * System.out.println(what); if (what.length() > 0) v1 = db.executeQuery("SELECT " + what + " FROM test.validMdds."
     * + (String) v.elementAt(i) + " t", null); } catch (Exception e) { errors.add("\n ." + (errors.size() + 1) +
     * ") Error querying valid MDD <" + (String) v.elementAt(i) + ">:\n\t " + e); } } if (errors.size() > 0)
     * fail("\n  Tested " + v.size() + " valid MDDs, of which " + errors.size() + " cant be used for DB queries:" +
     * errors.toString()); }
     */

    public void testInsert() {
        Hashtable<String, Object> p = new Hashtable<String, Object>();
        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(1977, 2, 5);
        Date birth = c.getTime();

        Text comment = new Text("SomeComment");

        p.put("birthdate", birth);
        p.put("comment", comment);
        p.put("picture", new Text(getExampleData()));

        p.put("weight", new Double(85.7d));

        p.put("indiv.name", "john");
        p.put("indiv.surname", "doe");
        p.put("extraData.something", "else");

        Vector<Integer> setintElem = new Vector<Integer>();
        setintElem.addElement(new Integer(1));
        setintElem.addElement(new Integer(0));

        Vector<String> setcharElem = new Vector<String>();
        setcharElem.addElement("f");
        setcharElem.addElement("e");

        p.put("intSet", setintElem);
        p.put("charSet", setcharElem);

        ptr = db.insert("test.Person", p);
        assertNotNull(ptr);
        assertEquals(ptr.getType(), "test.Person");

        now = new Date();

        Vector<Dictionary<String, Object>> v = db.executeQuery(readPerson1, ptr);

        assertEquals(1, v.size());

        pc = v.elementAt(0);

        create = (Date) pc.get("TS_create");
        ptrOne = (Pointer) pc.get("extraData");
        assertEquals("Name", "john", pc.get("name"));
        assertEquals("Surname", "doe", pc.get("surname"));
        assertEquals("Weight(real)", new Double(85.7d), pc.get("weight"));
        assertEquals("Birthdate", birth, pc.get("birthdate"));
        assertEquals("Something else", "else", pc.get("something"));
        assertEquals("Comment text", pc.get("comment").toString(), comment.getString());
        assertEquals("Picture", pc.get("picture"), new Text(getExampleData()));
        assertNotNull(ptrOne);

        v = db.executeQuery(readIntSet, ptr);
        assertEquals(2, v.size());
        assertEquals(new Integer(0), (v.elementAt(0)).get("member"));
        assertEquals(new Integer(1), (v.elementAt(1)).get("member"));

        v = db.executeQuery(readCharSet, ptr);
        assertEquals(v.size(), 2);
        assertEquals("e", (v.elementAt(0)).get("member"));
        assertEquals("f", (v.elementAt(1)).get("member"));

        assertEquals(create, pc.get("TS_modify"));
        assertTrue(now.getTime() - create.getTime() < 3 * epsilon);

    }

    public void testForeignKeys() {
        // FIXME assertTrue(org.makumba.db.sql.Database.supportsForeignKeys());

        // try to delete brother = that ID
        // try to delete the other brother

        // insert the first person
        Hashtable<String, Object> p = new Hashtable<String, Object>();

        Text comment = new Text("Hello world!!!!");

        p.put("comment", comment);

        p.put("indiv.name", "john");
        p.put("indiv.surname", "doe_1");
        p.put("extraData.something", "else");

        fptr = db.insert("test.Person", p);

        // check if he got inserted
        assertNotNull(fptr);
        assertEquals(fptr.getType(), "test.Person");

        Vector<Dictionary<String, Object>> v = db.executeQuery(readPerson2, fptr);
        // System.out.println(v.size());
        assertEquals(1, v.size());

        // insert the second person (brother)
        p = new Hashtable<String, Object>();

        comment = new Text("SomeComment");

        p.put("comment", comment);
        p.put("brother", fptr);
        p.put("indiv.name", "john");
        p.put("indiv.surname", "doe_2");
        p.put("extraData.something", "else");

        fptr1 = db.insert("test.Person", p);
        assertNotNull(fptr1);
        assertEquals(fptr.getType(), "test.Person");

        // check if it links to the first one correctly
        v = db.executeQuery(readPerson2, fptr1);

        assertEquals(1, v.size());

        pc = v.elementAt(0);

        fptr2 = (Pointer) pc.get("brother");
        assertNotNull(fptr2);
        assertEquals("Brother", fptr2, fptr);

        // try to delete the first guy (who was set as a brother. should fail)
        try {
            db.delete(fptr);
            // we could delete him... the foreign keys don't work
            assertTrue(false);
        } catch (org.makumba.DBError e) {
        }

        // try to delete the second guy
        db.delete(fptr1);

        // delete the first guy again, this time he shouldn't be linked to from anywhere
        db.delete(fptr);

    }

    static String subsetQuery = "SELECT a.description, a.id, a.description, a.sth.aaa FROM test.Person p JOIN p.address a WHERE p.id=? ORDER BY a.description";

    public void testSetInsert() {
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        p.put("description", "home");
        p.put("sth.aaa", "bbb");

        set1 = db.insert(ptr, "address", p);

        assertNotNull(set1);
        Vector<Dictionary<String, Object>> v = db.executeQuery(subsetQuery, ptr);
        assertEquals(1, v.size());
        assertEquals("home", (v.elementAt(0)).get("col1"));
        assertEquals(set1, (v.elementAt(0)).get("col2"));
        assertEquals("home", (v.elementAt(0)).get("col3"));

        p.put("description", "away");

        set2 = db.insert(ptr, "address", p);
        assertNotNull(set2);
        assertEquals("away", db.read(set2, subsetFields).get("description"));
        v = db.executeQuery(subsetQuery, ptr);
        assertEquals(2, v.size());
        assertEquals("away", (v.elementAt(0)).get("col1"));
        assertEquals(set2, (v.elementAt(0)).get("col2"));
        assertEquals("home", (v.elementAt(1)).get("col1"));
        assertEquals(set1, (v.elementAt(1)).get("col2"));
    }

    public void testSetMemberUpdate() {
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        p.put("description", "somewhere");

        db.update(set2, p);

        Vector<Dictionary<String, Object>> v = db.executeQuery(subsetQuery, ptr);

        assertEquals("somewhere", db.read(set2, subsetFields).get("description"));
        v = db.executeQuery(subsetQuery, ptr);
        assertEquals(v.size(), 2);
        assertEquals("home", (v.elementAt(0)).get("col1"));
        assertEquals(set1, (v.elementAt(0)).get("col2"));
        assertEquals("somewhere", (v.elementAt(1)).get("col1"));
        assertEquals(set2, (v.elementAt(1)).get("col2"));
    }

    public void testSetMemberDelete() {
        db.delete(set1);
        assertNull(db.read(set1, subsetFields));
        Vector<Dictionary<String, Object>> v = db.executeQuery(subsetQuery, ptr);
        assertEquals(1, v.size());
        assertEquals("somewhere", (v.elementAt(0)).get("col1"));
        assertEquals(set2, (v.elementAt(0)).get("col2"));

        // we put it back
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        p.put("description", "home");

        set1 = db.insert(ptr, "address", p);
    }

    public void testSubrecordUpdate() {
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        p.put("something", "else2");

        db.update(ptrOne, p);

        Dictionary<String, Object> d = db.read(ptr, personFields);
        assertNotNull(d);
        assertEquals(ptrOne, d.get("extraData"));

        d = db.read(ptrOne, ptrOneFields);
        assertNotNull(d);
        assertEquals("else2", d.get("something"));
    }

    static Object[][] languageData = { { "English", "en" }, { "French", "fr" }, { "German", "de" },
            { "Italian", "it" }, { "Spanish", "sp" } };

    static String[] toInsert = { "German", "Italian" };

    static String langQuery = "SELECT l.id FROM test.Language l WHERE l.name=?";

    static String speaksQuery = "SELECT l.id as k, l.name as name FROM test.Person p JOIN p.speaks l WHERE p.id=?";

    static String checkSpeaksQuery = "SELECT l.id FROM test.Person s JOIN s.speaks l WHERE s.id=?";

    void workWithSet(String[] t) {
        Vector<Object> v = new Vector<Object>();
        for (String element : t) {
            Vector<Dictionary<String, Object>> getLanguagesFromDb = db.executeQuery(langQuery, element);
            Dictionary<String, Object> resultDic = getLanguagesFromDb.elementAt(0);
            v.addElement(resultDic.get("col1"));
        }

        Hashtable<String, Object> dt = new Hashtable<String, Object>();
        dt.put("speaks", v);
        db.update(ptr, dt);

        Vector<Dictionary<String, Object>> result = db.executeQuery(speaksQuery, ptr);
        Vector<Dictionary<String, Object>> result1 = db.executeQuery(checkSpeaksQuery, ptr);

        assertEquals(t.length, result.size());
        assertEquals(t.length, result1.size());

        for (String element : t) {
            for (int j = 0; j < result.size(); j++) {
                Dictionary<String, Object> d = result.elementAt(j);
                if (d.get("name").equals(element)) {
                    for (int k = 0; j < result1.size(); k++) {
                        if ((result1.elementAt(k)).get("col1").equals(d.get("k"))) {
                            result1.removeElementAt(k);
                            break;
                        }
                    }
                    result.removeElementAt(j);
                    break;
                }
            }
        }
        assertEquals(0, result.size());
        assertEquals(0, result1.size());
    }

    public void testSetUpdate() {
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        if (db.executeQuery("SELECT l.id FROM test.Language l", null).size() == 0) {
            for (Object[] element : languageData) {
                p.put("name", element[0]);
                p.put("isoCode", element[1]);
                db.insert("test.Language", p);
            }
        }
        p = new Hashtable<String, Object>();

        workWithSet(toInsert);
    }

    static String[] toInsert2 = { "English", "Italian", "French" };

    public void testSetUpdate2() {
        workWithSet(toInsert2);
    }

    static String[] toInsert3 = { "English", "German", "French" };

    public void testSetDelete() {
        Hashtable<String, Object> dt = new Hashtable<String, Object>();
        dt.put("speaks", new Vector<Object>());

        db.update(ptr, dt);
        Vector<Dictionary<String, Object>> result = db.executeQuery(speaksQuery, ptr);
        assertEquals(0, result.size());

        assertEquals(0, db.executeQuery("SELECT l.id FROM test.Person p JOIN p.speaks l WHERE p.id=?", ptr).size());
        workWithSet(toInsert3);
        // db.delete("test.Language l", "1=1", null);
        // delete garbage

    }

    public void testPtrOneDelete() {
        db.delete(ptrOne);

        Dictionary<String, Object> d = db.read(ptr, personFields);
        assertNotNull(d);
        assertNull(d.get("extraData"));

        assertNull(db.read(ptrOne, ptrOneFields));
    }

    public void testPtrOneReInsert() {
        Dictionary<String, Object> p = new Hashtable<String, Object>();
        p.put("extraData.something", "else2");
        db.update(ptr, p);
        Dictionary<String, Object> d = db.read(ptr, personFields);
        ptrOne = (Pointer) d.get("extraData");
        assertNotNull(ptrOne);
        Dictionary<String, Object> read;
        assertNotNull(read = db.read(ptrOne, ptrOneFields));
        assertEquals("else2", read.get("something"));
    }

    public void testUpdate() {
        Hashtable<String, Object> pmod = new Hashtable<String, Object>();
        String val = "A completely new guy";
        pmod.put("indiv.name", val);

        Vector<Integer> setintElem = new Vector<Integer>();
        setintElem.addElement(new Integer(2));

        Vector<String> setcharElem = new Vector<String>();
        setcharElem.addElement("d");

        pmod.put("intSet", setintElem);
        pmod.put("charSet", setcharElem);

        int updates = db.update(ptr, pmod);

        now = new Date();
        Vector<Dictionary<String, Object>> v = db.executeQuery(readPerson, ptr);
        assertEquals(1, v.size());
        assertEquals(1, updates);

        Dictionary<String, Object> modc = v.elementAt(0);

        assertNotNull(modc);
        create = (Date) modc.get("TS_create");
        assertEquals(val, modc.get("name"));
        assertEquals("doe", modc.get("surname"));
        assertTrue(now.getTime() - ((Date) modc.get("TS_modify")).getTime() < epsilon);
        assertNotNull(db.read(ptrOne, ptrOneFields));

        v = db.executeQuery(readIntSet, ptr);
        assertEquals(1, v.size());
        assertEquals(new Integer(2), (v.elementAt(0)).get("member"));

        v = db.executeQuery(readCharSet, ptr);
        assertEquals(1, v.size());
        assertEquals("d", (v.elementAt(0)).get("member"));
    }

    public void testDelete() {
        Hashtable<String, Vector<Object>> dt = new Hashtable<String, Vector<Object>>();
        dt.put("speaks", new Vector<Object>());

        db.delete(ptr);

        assertNull(db.read(ptr, personFields));
        assertNull(db.read(ptrOne, ptrOneFields));
        assertEquals(0, db.executeQuery(subsetQuery, ptr).size());
        assertNull(db.read(set1, subsetFields));
        assertNull(db.read(set2, subsetFields));
        assertEquals(0, db.executeQuery(speaksQuery, ptr).size());
        assertEquals(0, db.executeQuery(readIntSet, ptr).size());
        assertEquals(0, db.executeQuery(readCharSet, ptr).size());
        assertEquals(0, db.executeQuery("SELECT l.id FROM  test.Person p JOIN p.speaks l WHERE p.id=?", ptr).size());
        assertEquals(0, db.executeQuery("SELECT l.enum_ FROM  test.Person p JOIN p.intSet l WHERE p.id=?", ptr).size());
        assertEquals(0, db.executeQuery("SELECT l.enum_ FROM  test.Person p JOIN p.charSet l WHERE p.id=?", ptr).size());

        /*
         * delete all entries, bug 673: db .delete("test.validMdds.CharWithLength name", "name.name='bla'", null);
         * db.delete("test.validMdds.CharWithLength t", "5=5", null); db.delete("test.validMdds.CharWithLength	t",
         * "t.name LIKE \"www\"", null); db.delete("test.validMdds.CharWithLength bla", "'x'=bla.name", null);
         */
    }

    public void testCopy() {
        Hashtable<String, Object> p = new Hashtable<String, Object>();
        p.put("birthdate", new java.util.GregorianCalendar(1977, 7, 7).getTime());
        p.put("indiv.name", "john");
        p.put("indiv.surname", "Copy");
        p.put("extraData.something", "else");

        Calendar c = Calendar.getInstance();
        c.clear();
        c.set(1976, 2, 9);

        Date cr = c.getTime();
        p.put("TS_create", cr);

        c.clear();
        c.set(1976, 2, 10);

        Date mod = c.getTime();
        p.put("TS_modify", mod);

        ptr1 = db.insert("test.Person", p);
        assertNotNull(ptr1);

        now = new Date();
        Vector<Dictionary<String, Object>> v = db.executeQuery(readPerson, ptr1);
        assertEquals(1, v.size());

        pc1 = v.elementAt(0);
        assertNotNull(pc1);

        assertEquals("john", pc1.get("name"));
        assertEquals("Copy", pc1.get("surname"));
        assertEquals(cr, new Date(((Date) pc1.get("TS_create")).getTime()));
        assertEquals(mod, new Date(((Date) pc1.get("TS_modify")).getTime()));
        db.delete(ptr1);
        db.delete("test.Individual i", "i.surname='Copy'", null);
        db.delete("test.Language l", "1=1", null);
    }

    @Override
    public void run(TestResult r) {
        try {
            super.run(r);

        } catch (Throwable t) {
            t.printStackTrace();
        }

        /*
         * very shitty solution, more JUnit should be studied for a better one... we want to find out whether we just
         * finished the last test if yes, we do cleanup
         */
        if (toString().equals("testCopy(test.table)")) {
            String nm = "testDatabaseHibernate";

            System.out.println("\nworked with: "
                    + MakumbaTransactionProvider.getDatabaseProperty(nm, "sql_engine.name") + " version: "
                    + MakumbaTransactionProvider.getDatabaseProperty(nm, "sql_engine.version") + "\njdbc driver: "
                    + MakumbaTransactionProvider.getDatabaseProperty(nm, "jdbc_driver.name") + " version: "
                    + MakumbaTransactionProvider.getDatabaseProperty(nm, "jdbc_driver.version")
                    + "\njdbc connections allocated: "
                    + MakumbaTransactionProvider.getDatabaseProperty(nm, "jdbc_connections") + "\ncaches: "
                    + org.makumba.commons.NamedResources.getCacheInfo()

            );
            java.util.logging.Logger.getLogger("org.makumba.system").info("destroying makumba caches");
        }
    }
}
