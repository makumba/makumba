/*
 * Created on Jan 18, 2006
 *
 */
package test.http;

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

import org.apache.cactus.JspTestCase;
import org.apache.cactus.Request;
import org.makumba.MakumbaSystem;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.Transaction;
import org.makumba.view.jsptaglib.MakumbaVersionTag;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @author Rudolf Mayer
 */
public class TestHibernateTags extends JspTestCase {

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

    static Object[][] languageData = { { "English", "en" }, { "French", "fr" }, { "German", "de" },
            { "Italian", "it" }, { "Spanish", "sp" } };

    private static final class Suite extends TestSetup {
        private Suite(Test arg0) {
            super(arg0);
        }

        protected void setUp() {
            Transaction db = getDB();
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
            for (int i = 0; i < languages.size(); i++)
                db.delete((Pointer) languages.get(i));
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
        setup = new Suite(new TestSuite(TestHibernateTags.class));
        return setup;
    }

    private static Transaction getDB() {
        return MakumbaSystem.getConnectionTo(MakumbaSystem.getDefaultDatabaseName("test/testDatabase.properties"));
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

    protected String removeNewlines(String line) {
        String parsed = line;
        parsed = parsed.replaceAll("[\n\r]*", "");
        return parsed;
    }

    protected String removeMakumbaResponder(String line) {
        String parsed = line;
        parsed = parsed.replaceAll("value=\"-?[0-9]*\"", "");
        return parsed;
    }

    protected String removeMakumbaBase(String line) {
        String parsed = line;
        parsed = parsed.replaceAll("value=\"[0-9a-zA-Z]*\"", "");
        return parsed;
    }

    protected String removeLabelStuff(String line) {
        String parsed = line;
        parsed = parsed.replaceAll("id=\"AutoLabel_[0-9]*\"", "");
        parsed = parsed.replaceAll("<LABEL for=\"AutoLabel_[0-9]*\">", "");
        parsed = parsed.replaceAll("</LABEL>", "");
        return parsed;
    }

    protected String removeTabs(String line) {
        String parsed = line;
        parsed = parsed.replaceAll(">[\t]*<", "><");
        return parsed;
    }

    public void testHibernateMakListTag() throws ServletException, IOException {
        pageContext.include("testHibernateMakListTag.jsp");
    }

    public void endHibernateMakListTag(WebResponse response) {
        try {
            output = response.getText();
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        int i = 0, begin, end;

        while ((begin = output.indexOf("name")) != -1) {
            output = output.substring(begin + 5);
            assertEquals(languageData[i][0], output.substring(0, output.indexOf("<br>")));
            output = output.substring(output.indexOf("isoCode") + 8);
            assertEquals(languageData[i][1], output.substring(0, output.indexOf("<br>")));
            i++;
        }
        try {
            assertEquals(true, response.getText().indexOf("English") != -1);
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }
        return;
    }

}
