/*
 *
 */
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
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.providers.TransactionProvider;
import org.xml.sax.SAXException;

import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @author Rudolf Mayer
 * @author Manuel Gay
 */
public class FormsHQLTest extends MakumbaJspTestCase {
    
    private boolean record = false;

    static Pointer person;

    static Pointer brother;

    static Pointer address;

    static Dictionary pc;

    static Suite setup;

    static Vector v;

    static String readPerson = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.gender AS gender, p.uniqChar AS uniqChar, p.uniqInt AS uniqInt, p.birthdate AS birthdate, p.weight AS weight, p.TS_modify AS TS_modify, p.TS_create AS TS_create, p.comment AS comment, a.description AS description, a.email AS email, a.usagestart AS usagestart FROM test.Person p, p.address a WHERE p= $1";

    private String output;

    static ArrayList languages = new ArrayList();

    static Object[][] languageData = { { "English", "en" }, { "French", "fr" }, { "German", "de" },
            { "Italian", "it" }, { "Spanish", "sp" } };

    private static final class Suite extends TestSetup {
        private Suite(Test arg0) {
            super(arg0);
        }

        protected void setUp() {
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction db =  tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
            
            insertLanguages(db);
            insertPerson(db);

            /*
             * Just a dummy select, so the test_Person__extraData_ is mentioned in the client side part of the tests. If
             * this is not done, the server side and the client side will attempt to insert the same primary key in the
             * catalog table (because they use the same DBSV, because they use the same database connection file).
             */
            db.executeQuery("SELECT p.extraData.something FROM test.Person p WHERE 1=0", null);
            db.close();
        }

        protected void insertPerson(Transaction db) {
            Properties p = new Properties();

            p.put("indiv.name", "bart");
            brother = db.insert("test.Person", p);

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
            address = db.insert(person, "address", p);

        }

        protected void deletePerson(Transaction db) {
            db.delete(address);
            db.delete(person);
            // brother is referenced by person so we delete it after person
            db.delete(brother);
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
            for (int i = 0; i < languages.size(); i++)
                db.delete((Pointer) languages.get(i));
        }

        public void tearDown() {
            // do your one-time tear down here!
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction db =  tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
            
            deletePerson(db);
            deleteLanguages(db);
            db.close();
        }
    }

    public static Test suite() {
        setup = new Suite(new TestSuite(FormsHQLTest.class));
        return setup;
    }

    public void beginTomcat(Request request) {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            WebResponse resp = wc.getResponse(req);
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            setup.tearDown();
            System.err.println("\n\n\n\n\nYou should run tomcat first! Use mak-tomcat to do that.\n\n");
            System.exit(1);
        } catch (SAXException e) {
        }
    }

    public void testTomcat() {
    }
  
    
    public void testHibernateMakNewForm() throws ServletException, IOException {
        pageContext.include("forms-hql/testHibernateMakNewForm.jsp");
    }
    public void endHibernateMakNewForm(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
        
        }

    public void beginHibernateMakAddForm(Request request) throws MalformedURLException, IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebResponse   resp = wc.getResponse( System.getProperty("cactus.contextURL") + "/forms-hql/beginHibernateMakAddForm.jsp");

        // we get the first form in the jsp
        if(resp.getForms().length==0)
            fail("forms expected\n"+resp.getText());
        WebForm form = resp.getForms()[0];
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email","bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }
    
    public void testHibernateMakAddForm() throws ServletException, IOException {
        pageContext.include("forms-hql/testHibernateMakAddForm.jsp");
    }
    public void endHibernateMakAddForm(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }
    
    public void testHibernateMakEditForm() throws ServletException, IOException {
       pageContext.include("forms-hql/testHibernateMakEditForm.jsp");
    }
    public void endHibernateMakEditForm(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        
        assertTrue(compareTest(output));
                
    }
    
    public void testHibernateMakForm() throws ServletException, IOException, SAXException {
        pageContext.include("forms-hql/testHibernateMakForm.jsp");
    }
    public void endHibernateMakForm(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        
        assertTrue(compareTest(output));
        
    }      
}