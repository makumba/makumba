package test.tags;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.Request;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.xml.sax.SAXException;

import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * Tests the HQL list engine.
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id: ListOQLTest.java,v 1.1 25.09.2007 15:58:58 Manuel Exp $
 */
public class ListHQLTest extends MakumbaJspTestCase {
    
    static Pointer person;
    static Pointer brother;
    static Pointer address;
    static Dictionary pc;
    static Suite setup;
    static Vector v;
    static String readPerson = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.gender AS gender, p.uniqChar AS uniqChar, p.uniqInt AS uniqInt, p.birthdate AS birthdate, p.weight AS weight, p.TS_modify AS TS_modify, p.TS_create AS TS_create, p.comment AS comment, a.description AS description, a.email AS email, a.usagestart AS usagestart FROM test.Person p, p.address a WHERE p= $1";
    private String output;
    private String line;

    static ArrayList languages = new ArrayList();
    static Object[][] languageData = { { "English", "en" }, { "French", "fr" },
            { "German", "de" }, { "Italian", "it" }, { "Spanish", "sp" } };

    private static final class Suite extends TestSetup {
        private Suite(Test arg0) {
            super(arg0);
        }

        protected void setUp() {
            Transaction db = getDB();
            insertLanguages(db);
            insertPerson(db);
            
            /* Just a dummy select, so the test_Person__extraData_ is mentioned in the client side part of the tests.
             * If this is not done, the server side and the client side will attempt to insert the same primary key
             * in the catalog table (because they use the same DBSV, because they use the same database connection file). */
            db.executeQuery("SELECT p.extraData.something FROM test.Person p WHERE 1=0", null);
            db.close();
        }

        protected void insertPerson(Transaction db) {
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
            address=db.insert(person, "address", p);

        }

        protected void deletePerson(Transaction db) {
            db.delete(address);
            db.delete(brother);
            db.delete(person);
        }

        protected void insertLanguages(Transaction db) {
            languages.clear();
            Dictionary p = new Hashtable();
            for (int i = 0; i < languageData.length; i++) {
                p.put("name", languageData[i][0]);
                p.put("isoCode", languageData[i][1]);
                languages.add(db.insert("test.Language", p));
            }               
        }

        protected void deleteLanguages(Transaction db) {
            for (int i = 0; i<languages.size(); i++)
                db.delete((Pointer)languages.get(i));
        }

        public void tearDown() {
            // do your one-time tear down here!
            Transaction db = getDB();
            deletePerson(db);
            deleteLanguages(db);
            db.close();
        }
    }

    
    public static Test suite() {
        setup = new Suite(new TestSuite(ListHQLTest.class));
        return setup;
    }
    
    private static Transaction getDB() {
        return MakumbaSystem.getConnectionTo(MakumbaSystem.getDefaultDatabaseName("test/testDatabase.properties"));
    }
    
    public void beginTomcat(Request request) {
        WebConversation wc = new WebConversation();
        WebRequest     req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            WebResponse   resp = wc.getResponse( req );
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            setup.tearDown();
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n");            
            System.exit(1);
        } catch (SAXException e) {
        }       
    }
    public void testTomcat(){}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    public void testHibernateMakObjectTag() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakObjectTag.jsp");        
    }
    public void endHibernateMakObjectTag(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }

    public void testHibernateMakListTag() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakListTag.jsp");
    }

    public void endHibernateMakListTag(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateMakValueChar() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueChar.jsp");        
    }   

    public void endHibernateMakValueChar(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
    public void testHibernateMakValueDate() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueDate.jsp");        
    }   
    
    public void endHibernateMakValueDate(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testHibernateMakValueInt() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueInt.jsp"); 
    }
    
    public void endHibernateMakValueInt(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }
    
    public void testHibernateMakValueDouble() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueDouble.jsp");
    }
    public void endHibernateMakValueDouble(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }
    
    public void testHibernateMakValueText() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueText.jsp");
    }
    public void endHibernateMakValueText(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }
    
    public void testHibernateMakValueSet() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueSet.jsp");
    }
    public void endHibernateMakValueSet(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }
    
    public void testHibernateMakValueTS_create() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueTS_create.jsp");       
    }
    public void endHibernateMakValueTS_create(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }
    
    public void testHibernateMakValueTS_modify() throws ServletException, IOException {
        pageContext.include("list-hql/testHibernateMakValueTS_modify.jsp");
    }
    public void endHibernateMakValueTS_modify(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));

    }
    
    public void testHibernateMakIf() throws ServletException, IOException, SAXException {
        pageContext.include("list-hql/testHibernateMakIfTag.jsp");
    }
    
    public void endHibernateMakIf(WebResponse response) throws Exception {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
        
    }  

}
