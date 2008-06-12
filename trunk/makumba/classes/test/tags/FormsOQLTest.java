/*
 * Created on Apr 29, 2005
 *
 */
package test.tags;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.Request;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.forms.responder.ResponderFactory;
import org.makumba.providers.TransactionProvider;
import org.xml.sax.SAXException;

import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HTMLElement;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id$
 */
public class FormsOQLTest extends MakumbaJspTestCase {

    private boolean record = false;

    static Pointer address;

    static Suite setup;

    static String readPerson = "SELECT p.indiv.name AS name, p.indiv.surname AS surname, p.gender AS gender, p.uniqChar AS uniqChar, p.uniqInt AS uniqInt, p.birthdate AS birthdate, p.weight AS weight, p.TS_modify AS TS_modify, p.TS_create AS TS_create, p.comment AS comment, a.description AS description, a.email AS email, a.usagestart AS usagestart FROM test.Person p, p.address a WHERE p= $1";

    private String output;

    static ArrayList<Pointer> languages = new ArrayList<Pointer>();

    static String[][] languageData = { { "English", "en" }, { "French", "fr" }, { "German", "de" },
            { "Italian", "it" }, { "Spanish", "sp" } };

    private final static String namePersonIndivName_AddToNew = "addToNewPerson";

    private static final String namePersonIndivName_Bart = "bart";

    private static final String namePersonIndivName_John = "john";

    /** All names of individuals to be deleted. bart is referenced by john, so we delete him afterwards. */
    private static final String[] namesPersonIndivName = { namePersonIndivName_John, namePersonIndivName_Bart,
            namePersonIndivName_AddToNew };

    private WebResponse submissionResponse;

    private static final Integer uniqInt = new Integer(255);

    private static final String uniqChar = new String("testing \" character field");

    private static Date birthdate;

    private static final class Suite extends TestSetup {

        private Suite(Test arg0) {
            super(arg0);
        }

        protected void setUp() {
            TransactionProvider tp = TransactionProvider.getInstance();
            Transaction db = tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));

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
                Vector<Dictionary<String, Object>> v = db.executeQuery(
                    "SELECT p AS p, p.indiv as i FROM test.Person p WHERE p.indiv.name=$1", namesPersonIndivName[i]);
                db.delete((Pointer) v.firstElement().get("p"));
                db.delete((Pointer) v.firstElement().get("i"));
            }
        }

        protected void insertLanguages(Transaction db) {
            languages.clear();
            Dictionary<String, String> p = new Hashtable<String, String>();
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
            Transaction db = tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));

            deletePersonsAndIndividuals(db);
            deleteLanguages(db);
            db.close();
        }
    }

    public static Test suite() {
        setup = new Suite(new TestSuite(FormsOQLTest.class));
        return setup;
    }

    public void beginTomcat(Request request) {
        WebConversation wc = new WebConversation();
        WebRequest req = new GetMethodWebRequest(System.getProperty("cactus.contextURL"));
        try {
            wc.getResponse(req);
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

    public void testMakNewForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakNewForm.jsp");
    }

    public void endMakNewForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakAddForm(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/forms-oql/beginMakAddForm.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email", "bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }

    public void testMakAddForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakAddForm.jsp");
    }

    public void endMakAddForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakEditForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakEditForm.jsp");
    }

    public void endMakEditForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testMakForm() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakForm.jsp");
    }

    public void endMakForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testBug946() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testBug946.jsp");
    }

    public void endBug946(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testFormMultipleForms() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakMultipleForms.jsp");
    }

    public void endFormMultipleForms(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }

    public void testFormNestedForms() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakNestedForms.jsp");
    }

    public void endFormNestedForms(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void testFormMakNewFile() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testMakNewFormFile.jsp");
    }

    public void endFormMakNewFile(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakAddToNewForm(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginMakAddToNewForm.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", namePersonIndivName_AddToNew);
        form.setParameter("description_1", "addToNewDescription");
        form.setParameter("email_1", "addToNew@makumba.org");
        // submit the form
        form.submit();
    }

    public void testMakAddToNewForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakAddToNewForm.jsp");
    }

    public void endMakAddToNewForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginMakSearchForm(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/forms-oql/beginMakSearchForm.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", "a");

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause he WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testMakSearchForm() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endMakSearchForm(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginFormAnnotation(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginFormAnnotation.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the inputs in the add-to-new form
        form.setParameter("indiv.name", "name");
        form.setParameter("indiv.surname", "surname");
        form.setParameter("age", "invalidInt");
        form.setParameter("weight", "invalidReal");
        form.setParameter("email", "invalidEmail");
        final Date first = new Date(90, 0, 1);
        form.setParameter("firstSex_0", String.valueOf(first.getDate()));
        form.setParameter("firstSex_1", String.valueOf(first.getMonth()));
        form.setParameter("firstSex_2", String.valueOf(first.getYear() + 1900));
        form.setParameter("birthdate_0", String.valueOf(birthdate.getDate()));
        form.setParameter("birthdate_1", String.valueOf(birthdate.getMonth()));
        form.setParameter("birthdate_2", String.valueOf(birthdate.getYear() + 1800));
        form.setParameter("uniqDate_0", String.valueOf(birthdate.getDate()));
        form.setParameter("uniqDate_1", String.valueOf(birthdate.getMonth()));
        form.setParameter("uniqDate_2", String.valueOf(birthdate.getYear() + 1900));
        form.setParameter("hobbies", " ");
        form.setParameter("uniqInt", uniqInt.toString());
        form.setParameter("uniqChar", uniqChar);

        // TODO: read HTTP unit documents carefully.
        // not sure if that is the most elegant / intended solution
        // but, we want to save this specific form submission for later evaluation
        // cause he WebResponse passed in endMakSearchForm is not from this submission
        // we could also do the comparison here, though, and leave the endMakSearchForm method empty
        submissionResponse = form.submit();
    }

    public void testFormAnnotation() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endFormAnnotation(WebResponse response) throws Exception {
        try {
            output = submissionResponse.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

    public void beginFormResponderOrder(Request request) throws Exception {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL")
                + "/forms-oql/beginMakNestedNewForms.jsp");

        // first, compare that the form generated is ok
        try {
            output = resp.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + resp.getResponseMessage());
        }
        assertTrue(compareTest(output));

        // read all the inputs with responder codes, store them in an array
        HTMLElement[] responderElements = resp.getElementsWithAttribute("name", "__makumba__responder__");
        String[] responderCodesString = new String[responderElements.length];
        for (int i = 0; i < responderElements.length; i++) {
            responderCodesString[i] = responderElements[i].getAttribute("value");

        }

        // we will have subsequently a new instance of responderFactory (the one used until now is in tomcat-mak)
        // thus, we need to prepare the responder working dir
        // we don't have an HTTPServletRequest at hand, so we have to do this manually / partly hardcoded
        String contextPath = "tests";
        String tempDir = new File(getClass().getResource("/").toURI()).getParent() + "/tomcat/work/Catalina/localhost/"
                + contextPath;
        ResponderFactory responderFactory = ResponderFactory.getInstance();
        responderFactory.setResponderWorkingDir(tempDir, contextPath);

        // we need the codes as iterator; we could do an iterator ourselves, but let's do it as if we got them from the
        // attributes, i.e. as vector
        List<String> list = Arrays.asList(responderCodesString);
        Vector<String> v = new Vector<String>();
        v.addAll(list);
        Iterator<String> responderCodes = responderFactory.getResponderCodes(v);

        Iterator<String> orderedResponderCodes = responderFactory.getOrderedResponderCodes(list.iterator());

        // debug info
        System.out.println("Responder codes read from form inputs: " + Arrays.toString(responderCodesString));

        ArrayList<String> responderCodesAsList = new ArrayList<String>();
        CollectionUtils.addAll(responderCodesAsList, responderCodes);
        System.out.println("Responder codes as passed through responderFactory.getResponderCodes(..): "
                + ArrayUtils.toString(responderCodesAsList));

        ArrayList<String> orderedResponderCodesAsList = new ArrayList<String>();
        CollectionUtils.addAll(orderedResponderCodesAsList, orderedResponderCodes);
        System.out.println("Ordered responder codes:" + ArrayUtils.toString(orderedResponderCodesAsList));

        // TODO
        // - define an Iterator / something else with the expected responder codes
        // - define an Iterator / something else with the expected ordered responder codes
        // - compare them
    }

    public void testFormResponderOrder() throws ServletException, IOException {
        // we need to have this method, even if it is empty; otherwise, the test is not run
    }

    public void endFormResponderOrder(WebResponse response) throws Exception {
    }

    public void beginLogin(Request request) throws MalformedURLException, IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/login/loginTest.jsp");

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // we try to login
        form.setParameter("username", "manu");
        form.setParameter("password", "secret");
        // submit the form
        form.submit();
    }

    public void testLogin() throws ServletException, IOException {
        pageContext.include("login/loginTest.jsp");
    }

    public void endLogin(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        assertTrue(compareTest(output));
    }

}
