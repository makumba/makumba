/*
 * Created on Apr 29, 2005
 *
 */
package test.newtags;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletException;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.cactus.Request;
import org.xml.sax.SAXException;

import test.MakumbaTestSetup;
import test.util.MakumbaJspTestCase;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebForm;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

/**
 * @author Johannes Peeters
 * @author Manuel Gay
 * @version $Id: ListOQLTest.java,v 1.1 25.09.2007 15:58:58 Manuel Exp $
 */
public class FormsOQLTest extends MakumbaJspTestCase {

    private boolean record = false;

    static Suite setup;

    private String output;

    private static final class Suite extends MakumbaTestSetup {
        private Suite(Test arg0) {
            super(arg0, "oql");
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
        WebResponse resp = wc.getResponse(System.getProperty("cactus.contextURL") + "/forms-oql/beginMakAddForm.jsp");

        // we get the first form in the jsp
        WebForm form = resp.getForms()[0];
        // set the input field "email" to "bartolomeus@rogue.be"
        form.setParameter("email", "bartolomeus@rogue.be");
        // submit the form
        form.submit();
    }

    public void testFormAddForm() throws ServletException, IOException {
        pageContext.include("forms-oql/testMakAddForm.jsp");
    }

    public void endFormAddForm(WebResponse response) throws Exception {
        try {
            output = response.getText();
            fetchValidTestResult(output, record);
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
            fetchValidTestResult(output, record);
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
            fetchValidTestResult(output, record);
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

}
