/*
 * Created on Apr 29, 2005
 *
 */
package test.newtags;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.Request;
import org.makumba.Transaction;
import org.makumba.Pointer;
import org.makumba.Text;
import org.makumba.commons.Configuration;
import org.makumba.providers.TransactionProvider;
import org.xml.sax.SAXException;

import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;


/**
 * @author Johannes Peeters
 * @author Manuel Gay
 */
public class FormsOQLTest extends MakumbaJspTestCase {

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
		setup = new Suite(new TestSuite(FormsOQLTest.class));
		return setup;
	}
	
	private static Transaction getDB() {
        Configuration config = new Configuration();
        TransactionProvider tp = new TransactionProvider(config);
        
		return tp.getConnectionTo(tp.getDataSourceName("test/testDatabase.properties"));
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
	
	public void testFormNewForm() throws ServletException, IOException {
		pageContext.include("forms-oql/testMakNewForm.jsp");
	}
	public void endFormNewForm(WebResponse response) throws Exception {
		try {
			output = response.getText();
		} catch (IOException e) {
			fail("JSP output error: " + response.getResponseMessage());
		}
        assertTrue(compareTest(output));
		
        }
    
	public void beginMakAddForm(Request request) throws MalformedURLException, IOException, SAXException {
		WebConversation wc = new WebConversation();
		WebResponse   resp = wc.getResponse( System.getProperty("cactus.contextURL") + "/forms-oql/beginMakAddForm.jsp" );

		// we get the first form in the jsp
		WebForm form = resp.getForms()[0];
		// set the input field "email" to "bartolomeus@rogue.be"
		form.setParameter("email","bartolomeus@rogue.be");
		// submit the form
		form.submit();
	}
	public void testFormAddForm() throws ServletException, IOException {
		pageContext.include("forms-oql/testMakAddForm.jsp");
	}
	public void endFormAddForm(WebResponse response) throws Exception {
		try {
			output = response.getText();
		} catch (IOException e) {
			fail("JSP output error: " + response.getResponseMessage());
		}
		
        assertTrue(compareTest(output));
	}
	
	public void testFormEditForm() throws ServletException, IOException {
		pageContext.include("forms-oql/testMakEditForm.jsp");
	}
	public void endFormEditForm(WebResponse response) throws Exception {
		try {
			output = response.getText();
		} catch (IOException e) {
			fail("JSP output error: " + response.getResponseMessage());
		}
        assertTrue(compareTest(output));
				
	}
	
	public void testFormForm() throws ServletException, IOException, SAXException {
		pageContext.include("forms-oql/testMakForm.jsp");
	}
	public void endFormForm(WebResponse response) throws Exception {
		try {
			output = response.getText();
		} catch (IOException e) {
			fail("JSP output error: " + response.getResponseMessage());
		}
		
        assertTrue(compareTest(output));
		
	}

    public void testFormBug946() throws ServletException, IOException, SAXException {
        pageContext.include("forms-oql/testBug946.jsp");
    }
    public void endFormBug946(WebResponse response) throws Exception {
        try {
            output = response.getText();
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
        } catch (IOException e) {
            fail("JSP output error: " + response.getResponseMessage());
        }

        assertTrue(compareTest(output));
    }
 
    
    
    
}
