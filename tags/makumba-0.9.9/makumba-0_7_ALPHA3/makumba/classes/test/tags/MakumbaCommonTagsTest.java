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
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;

import junit.extensions.TestSetup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.Request;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.commons.tags.MakumbaVersionTag;
import org.makumba.db.hibernate.HibernateTransactionProvider;
import org.makumba.providers.TransactionProvider;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebResponse;

import test.util.MakumbaJspTestCase;

public class MakumbaCommonTagsTest extends MakumbaJspTestCase {
    
    static Suite setup;
    private String output;
    private boolean record = false;
    
    static Pointer person;

    static Pointer brother;

    static Pointer address;
    
    static ArrayList languages = new ArrayList();

    static Object[][] languageData = { { "English", "en" }, { "French", "fr" }, { "German", "de" },
            { "Italian", "it" }, { "Spanish", "sp" } };
    
    private static final class Suite extends TestSetup {
        private Suite(Test arg0) {
            super(arg0);
        }

        protected void setUp() {
            TransactionProvider tp = new TransactionProvider(new HibernateTransactionProvider());
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
            
            TransactionProvider tp = new TransactionProvider(new HibernateTransactionProvider());
            Transaction db =  tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
            
            deletePerson(db);
            deleteLanguages(db);
            db.close();
            
        }
    }
    
    public static Test suite() {
        setup = new Suite(new TestSuite(MakumbaCommonTagsTest.class));
        return setup;
    }

    public void testVersionTag() throws JspException, IOException {
        MakumbaVersionTag versionTag = new MakumbaVersionTag();
        versionTag.setPageContext(pageContext);
        versionTag.doStartTag();
        session.setAttribute("version", "0.0");
        Assert.assertEquals(1, versionTag.doStartTag());
        BodyContent bodyContent = pageContext.pushBody();
        bodyContent.println("Makumbaaaaaaaaaaa");
        bodyContent.print("Version 0");
        versionTag.doAfterBody();
        versionTag.doEndTag();
        pageContext.popBody();
    }
    
    public void beginLogin(Request request) throws MalformedURLException, IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebResponse   resp = wc.getResponse( System.getProperty("cactus.contextURL") + "/login/loginTest.jsp" );

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // we try to login
        form.setParameter("username","manu");
        form.setParameter("password", "secret");
        // submit the form
        form.submit();
    }
    public void testLogin() throws ServletException, IOException {
        pageContext.include("login/loginTest.jsp");
    }
    public void endLogin(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, true);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        
        assertTrue(compareTest(output));
    }
    
    // TODO this test isn't actually testing the chooser logic, it just helps triggering it
    public void beginHibernateTransactionProviderChooserLogic(Request request) throws MalformedURLException, IOException, SAXException {
        WebConversation wc = new WebConversation();
        WebResponse   resp = wc.getResponse( System.getProperty("cactus.contextURL") + "/transactionProviderChooser/beginHibernateTransactionProviderChooser.jsp");

        // we get the first form in the jsp
        if(resp.getForms().length==0)
            fail("forms expected\n"+resp.getText());
        WebForm form = resp.getForms()[0];
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email","bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }
    
    public void testHibernateTransactionProviderChooserLogic() throws ServletException, IOException {
        pageContext.include("transactionProviderChooser/testHibernateTransactionProviderChooser.jsp");
    }
    public void endHibernateTransactionProviderChooserLogic(WebResponse response) throws Exception {
        try {
            output = response.getText(); fetchValidTestResult(output, record);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }


}
